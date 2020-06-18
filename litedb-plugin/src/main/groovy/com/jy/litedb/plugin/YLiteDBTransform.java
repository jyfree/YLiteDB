package com.jy.litedb.plugin;

import com.android.SdkConstants;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.google.common.collect.ImmutableSet;
import com.jy.litedb.annotation.common.Const;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class YLiteDBTransform extends Transform {

    private static final String TRANSFORM = "Transform: ";
    private static final String GENERATE_INIT = "GenerateInit: ";

    /**
     * Linux/Unix： com/jy/litedb/generated
     * Windows：    com\jy\litedb\generated
     */
    public static final String INIT_SERVICE_DIR = Const.GEN_PKG.replace('.', File.separatorChar);
    /**
     * com/jy/litedb/generated
     */
    public static final String INIT_SERVICE_PATH = Const.GEN_PKG.replace('.', '/');

    @Override
    public String getName() {
        return Const.NAME;
    }

    // 输入类型，可以使class文件，也可以是源码文件 ，这是表示输入的class文件
    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    // 作用范围
    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    //是否支持增量编译
    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(TransformInvocation invocation) throws TransformException, InterruptedException, IOException {
        super.transform(invocation);
        YLiteDBLogger.info(TRANSFORM + "start...");
        long ms = System.currentTimeMillis();
        Set<String> initClasses = Collections.newSetFromMap(new ConcurrentHashMap<>());
        //遍历Transform 要消费的输入流
        for (TransformInput input : invocation.getInputs()) {
            //JarInput 代表以 jar 包方式参与项目编译的所有本地 jar 包或远程 jar 包，可以借助它来动态添加 jar 包。
            //outputProvider 是用来获取输出目录的
            //getContentLocation方法相当于创建一个对应名称表示的目录
            //是从0 、1、2开始递增。如果是目录，名称就是对应的数字，如果是jar包就类似0.jar
            input.getJarInputs().parallelStream().forEach(jarInput -> {
                File src = jarInput.getFile();
                File dst = invocation.getOutputProvider().getContentLocation(
                        jarInput.getName(), jarInput.getContentTypes(), jarInput.getScopes(),
                        Format.JAR);
                try {
                    scanJarFile(src, initClasses);
                    FileUtils.copyFile(src, dst);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            //DirectoryInput 代表以源码方式参与项目编译的所有目录结构及其目录下的源码文件
            // 可以借助于它来修改输出文件的目录结构以及目标字节码文件。
            input.getDirectoryInputs().parallelStream().forEach(directoryInput -> {
                File src = directoryInput.getFile();
                File dst = invocation.getOutputProvider().getContentLocation(
                        directoryInput.getName(), directoryInput.getContentTypes(),
                        directoryInput.getScopes(), Format.DIRECTORY);
                try {
                    scanDir(src, initClasses);
                    FileUtils.copyDirectory(src, dst);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        File dest = invocation.getOutputProvider().getContentLocation(
                Const.NAME, TransformManager.CONTENT_CLASS,
                ImmutableSet.of(QualifiedContent.Scope.PROJECT), Format.DIRECTORY);
        //生成ServiceLoaderInit
        generateServiceInitClass(dest.getAbsolutePath(), initClasses);

        YLiteDBLogger.info(TRANSFORM + "cost %s ms", System.currentTimeMillis() - ms);

    }


    /**
     * 扫描由注解生成器生成到包 {@link Const#GEN_PKG} 里的初始化类
     */
    private void scanJarFile(File file, Set<String> initClasses) throws IOException {
        JarFile jarFile = new JarFile(file);
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.endsWith(SdkConstants.DOT_CLASS) && name.startsWith(INIT_SERVICE_PATH)) {
                String className = trimName(name, 0).replace('/', '.');
                initClasses.add(className);
                YLiteDBLogger.info("    find ServiceInitClass: %s", className);
            }
        }
    }

    /**
     * 扫描由注解生成器生成到包 {@link Const#GEN_PKG} 里的初始化类
     */
    private void scanDir(File dir, Set<String> initClasses) throws IOException {
        File packageDir = new File(dir, INIT_SERVICE_DIR);
        if (packageDir.exists() && packageDir.isDirectory()) {
            Collection<File> files = FileUtils.listFiles(packageDir,
                    new SuffixFileFilter(SdkConstants.DOT_CLASS, IOCase.INSENSITIVE), TrueFileFilter.INSTANCE);
            for (File f : files) {
                String className = trimName(f.getAbsolutePath(), dir.getAbsolutePath().length() + 1)
                        .replace(File.separatorChar, '.');
                initClasses.add(className);
                YLiteDBLogger.info("    find ServiceInitClass: %s", className);
            }
        }
    }

    /**
     * [prefix]com/xxx/aaa.class --> com/xxx/aaa
     * [prefix]com\xxx\aaa.class --> com\xxx\aaa
     */
    private String trimName(String s, int start) {
        return s.substring(start, s.length() - SdkConstants.DOT_CLASS.length());
    }


    /**
     * 生成格式如下的代码，其中FieldInfo_xxx由注解生成器生成。
     * <pre>
     * package com.jy.litedb.generated;
     *
     * public class FieldInfoLoaderInit {
     *
     *     public static void init() {
     *         FieldInfo_xxx1.init();
     *         FieldInfo_xxx2.init();
     *     }
     * }
     * </pre>
     */
    private void generateServiceInitClass(String directory, Set<String> classes) {

        if (classes.isEmpty()) {
            YLiteDBLogger.info(GENERATE_INIT + "skipped, no service found");
            return;
        }

        try {
            YLiteDBLogger.info(GENERATE_INIT + "start...");
            long ms = System.currentTimeMillis();

            //ASM，字节码操作，生成ServiceLoaderInit代码
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, writer) {
            };
            String className = Const.FIELD_INFO_LOADER_INIT.replace('.', '/');
            cv.visit(50, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", null);

            MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    Const.INIT_METHOD, "()V", null, null);

            mv.visitCode();

            for (String clazz : classes) {
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, clazz.replace('.', '/'),
                        "init",
                        "()V",
                        false);
            }
            mv.visitMaxs(0, 0);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitEnd();
            cv.visitEnd();

            File dest = new File(directory, className + SdkConstants.DOT_CLASS);
            dest.getParentFile().mkdirs();
            new FileOutputStream(dest).write(writer.toByteArray());

            YLiteDBLogger.info(GENERATE_INIT + "cost %s ms", System.currentTimeMillis() - ms);

        } catch (IOException e) {
            YLiteDBLogger.fatal(e);
        }
    }

}
