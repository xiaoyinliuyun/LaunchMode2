package com.xiaoyinliuyun.launchmode2

import android.app.Application
import android.os.StrictMode
import android.util.Log

private const val TAG = "MyApplication"

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if(BuildConfig.DEBUG){
            // 设置线程检测策略
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                .detectDiskReads() // 检查磁盘读操作
                .detectDiskWrites()// 检测磁盘写操作
                .detectNetwork()
                .penaltyLog()// 违规则打印日志
                .penaltyDeath()// 违规则崩溃
                .build())

            // 设置虚拟机检测策略
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects() // sqlLite对象泄露
                .detectLeakedClosableObjects() // 未关闭的 Closable 对象泄露
                .penaltyLog() // 违规则打印日志
                .penaltyDeath() // 违规则崩溃
                .build())
            Log.i(TAG, "onCreate: 设置严苛模式")
        }else{
            Log.i(TAG, "onCreate: 未设置严苛模式")
            Log.i(TAG, "onCreate:${BuildConfig.Info0}  ${BuildConfig.Info1} ${BuildConfig.Info2}")
        }
    }


}