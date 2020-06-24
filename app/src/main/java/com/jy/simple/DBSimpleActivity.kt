package com.jy.simple

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jy.litedb.api.utils.LiteLogUtils
import com.jy.simple.bean.TestInfo
import com.jy.simple.db.AppDatabase


/**

 * @Author Administrator
 * @Date 2019/10/24-18:30
 * @TODO
 */
class DBSimpleActivity : AppCompatActivity() {

    companion object {
        fun startAct(context: Context) {
            val intent = Intent()
            intent.setClass(context, DBSimpleActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.simple_db_activity)
    }

    fun onLoadDB(view: View) {
        when (view.id) {
            //查询db
            R.id.query -> query()
            //插入db
            R.id.install -> install()
        }
    }


    private fun query() {
        val startTime = System.currentTimeMillis()
        val list = AppDatabase.instance.getTestJavaDao().getListInfo()
//        list.forEach {
//            LiteLogUtils.iFormat(
//                "单条数据--id：%s--savePath：%s--connectionTime：%s--testFilter：%s--testUpdate：%s--testUpdateTwo：%s",
//                it.id,
//                it.savePath,
//                it.connectionTime,
//                it.testFilter,
//                it.testUpdate,
//                it.testUpdateTwo
//            )
//        }
        LiteLogUtils.iFormat("查询--用时%sms--数据:%s", System.currentTimeMillis() - startTime, list.size)
    }


    private var connectionTime = 0

    private fun install() {

        connectionTime++

        val startTime = System.currentTimeMillis()

        val testInfo = TestInfo()
        testInfo.savePath = "sdcard"
        testInfo.connectionTime = connectionTime
        testInfo.testFilter = "过滤字段"
        testInfo.testUpdate = "更新字段"
        testInfo.testUpdateTwo = "更新字段2"
//        testInfo.url="测试地址"
        //测试批量插入
//        val list=ArrayList<TestInfo>()
//        list.add(testInfo)
//        list.add(testInfo)
//        list.add(testInfo)
        AppDatabase.instance.getTestJavaDao().insertOrUpdate(testInfo)

        LiteLogUtils.iFormat("插入--用时%sms", System.currentTimeMillis() - startTime)

    }
}