package com.jy.simple2;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.jy.litedb.api.utils.LiteLogUtils;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.query);
        findViewById(R.id.install);
    }

    public void onLoadDB(View view) {
        long startTime = System.currentTimeMillis();
        switch (view.getId()) {
            case R.id.query:
                TestJava testJava = AppDatabase.getInstance().getTestJavaDao().getTestJava("123");
                LiteLogUtils.INSTANCE.iFormat("查询：%sms  数据：%s", System.currentTimeMillis() - startTime, testJava);
                break;
            case R.id.install:
                TestJava tj = new TestJava();
                tj.url = "1";
                tj.msg = "hello";

                TestJava tj2 = new TestJava();
                tj2.url = "123";
                tj2.msg = "hihi";

                AppDatabase.getInstance().getTestJavaDao().insertOrUpdate(tj);
                AppDatabase.getInstance().getTestJavaDao().insertOrUpdate(tj2);

                LiteLogUtils.INSTANCE.iFormat("插入：%sms", System.currentTimeMillis() - startTime);

                break;
        }
    }

}
