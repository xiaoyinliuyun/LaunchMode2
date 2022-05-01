package com.xiaoyinliuyun.launchmode2

import kotlinx.coroutines.*

/**
 * @Author yangkunjian.
 *
 * @Date   2022/4/17 22:46.
 *
 * @Desc 协程是在哪个线程里运行的
 */
fun log(msg: String) {
    println("[${Thread.currentThread().name}] $msg")
}

fun mainCase1() = runBlocking {
    val job1 = GlobalScope.launch {
        log("launch before delay")
        delay(100)
        log("launch after delay")
    }
    val job2 = GlobalScope.launch {
        log("launch2 before delay")
        delay(200)
        log("launch2 after delay")
    }

    job1.join()
    job2.join()
}

fun mainCase2() = runBlocking<Unit> {
//sampleStart
    // 运行在父协程的上下文中，即 runBlocking 主协程
    launch {
        println("main runBlocking : I'm working in thread ${Thread.currentThread(
        ).name}")
    }
    // 不受限的——将工作在主线程中
    launch(Dispatchers.Unconfined) {
        println("Unconfined : I'm working in thread ${Thread.currentThread(
        ).name}")
    }
    // 将会获取默认调度器
    launch(Dispatchers.Default) {
        println("Default : I'm working in thread ${Thread.currentThread(
        ).name}")
    }
    // 将使它获得一个新的线程
    launch(newSingleThreadContext("MyOwnThread")) {
        println("newSingleThreadContext: I'm working in thread ${Thread.currentThread(
        ).name}")
    }
//sampleEnd
}

fun main() {

//使用
    val list = mutableListOf(1, 2, 3)
    list.swap(0,2);
    println(list)
}
