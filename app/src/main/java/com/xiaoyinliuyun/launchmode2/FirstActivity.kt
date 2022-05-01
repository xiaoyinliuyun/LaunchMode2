package com.xiaoyinliuyun.launchmode2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.MemoryFile
import android.util.Log

/**
 * 使用共享内存的方式进程间传递大数据量场景的数据
 *
 * 在跳转前写入内存，跳转后读取内存，实现打数据量的传输
 */
class FirstActivity : AppCompatActivity() {
    private val TAG = "FirstActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)
        Log.i(TAG, "onCreate: ")


        val byteArray = ByteArray(1024 * 1024);
        TestAshemeMemoryFile.getInstance().readBytes(byteArray, 0, 0, 1024 * 1024);
        Log.i(TAG, "testAshmem: ${byteArray[1024 * 1024 - 1]}")
        // todo 为什么读取的数据不符合预期？


    }


    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart: ")
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume: ")
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause: ")
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy: ")
    }
}