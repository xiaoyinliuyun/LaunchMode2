package com.xiaoyinliuyun.launchmode2

import kotlinx.coroutines.*


/**
 * @Author yangkunjian.
 *
 * @Date   2022/4/17 20:40.
 *
 * @Desc
 */
/* ------------------ 协程基础 -------------------- */

/**
 * 你的第一个协程程序
 */
fun main1() {
    GlobalScope.launch {
        delay(1000L)
        println("World!")
    }
    println("Hello,")
//    Thread.sleep(2000L)
    runBlocking {
        delay(2000L)
    }
}

/**
 * 桥接阻塞与非阻塞的世界
 */
fun main21() {
    GlobalScope.launch {
        delay(1000L)
        println("World!")
    }
    println("Hello,")
    runBlocking {
        delay(2000L)
    }
}


fun main22() = runBlocking {
    GlobalScope.launch {
        delay(1000L)
        println("World!")
    }
    println("Hello,")
    delay(2000)
}

/**
 * 等待一个任务
 */
fun main3() = runBlocking {
    val job = GlobalScope.launch {
        delay(1000L)
        println("World!")
    }

    println("Hello,")
    job.join()
}

/**
 * 结构化的并发
 */
fun main4() = runBlocking {
    launch { // 在 runBlocking 作用域中启动一个新协程
        delay(1000L)
        println("World!")
    }
    println("Hello,")
}

/**
 * 作用域构建器
 */
fun main5() = runBlocking { // this: CoroutineScope
    launch {
        delay(200L)
        println("Task from runBlocking")
    }
    // 创建一个新的协程作用域
    coroutineScope {
        launch {
            delay(500L)
            println("Task from nested launch")
        }
        delay(100L)
        // 该行将在嵌套启动之前执行打印
        println("Task from coroutine scope")
    }
    // 该行将在嵌套结束之后才会被打印
    println("Coroutine scope is over")
}

/**
 * 提取函数重构
 */
fun main6() = runBlocking {
    launch { doWorld() }
    println("Hello,")
}
// 你的第一个挂起函数
suspend fun doWorld() {
    delay(1000L)
    println("World!")
}

/**
 * 协程是轻量级的
 */
fun main7() = runBlocking {
    repeat(100_000) { // 启动大量的协程
        launch {
            delay(1000L)
            print(".")
        }
    }
}

/**
 * 像守护线程一样的全局协程
 */
fun main8() = runBlocking {
//sampleStart
    GlobalScope.launch {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
    }
    delay(13000L) // 在延时之后结束程序
//sampleEnd
}


/* ------------------ 取消与超时 -------------------- */

/**
 * 取消协程的执行
 */
fun main9() = runBlocking {
//sampleStart
    val job = launch {
        repeat(25) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
    }
    delay(10000L) // 延迟一段时间
    println("main: I'm tired of waiting!")
    job.cancel() // 取消该任务
    job.join() // 等待任务执行结束
    println("main: Now I can quit.")
//sampleEnd
}

/**
 * 取消是协作的
 */
fun main10() = runBlocking {
//sampleStart
    val startTime = System.currentTimeMillis()
    val job = launch(Dispatchers.Default) {
        var nextPrintTime = startTime
        var i = 0
        while (i < 5) { // 一个执行计算的循环，只是为了占用CPU
            // 每秒打印消息两次
            if (System.currentTimeMillis() >= nextPrintTime) {
                println("I'm sleeping ${i++} ...")
                nextPrintTime += 500L
            }
        }
    }
    delay(1300L) // 等待一段时间
    println("main: I'm tired of waiting!")
    job.cancelAndJoin() // 取消一个任务并且等待它结束
    println("main: Now I can quit.")
//sampleEnd
}

/**
 * 使计算代码可取消
 */
fun main11() = runBlocking {
//sampleStart
    val startTime = System.currentTimeMillis()
    val job = launch(Dispatchers.Default) {
        var nextPrintTime = startTime
        var i = 0
        while (isActive) { // 可以被取消的计算循环
            // 每秒打印消息两次
            if (System.currentTimeMillis() >= nextPrintTime) {
                println("I'm sleeping ${i++} ...")
                nextPrintTime += 500L
            }
        }
    }
    delay(1300L) // 等待一段时间
    println("main: I'm tired of waiting!")
    job.cancelAndJoin() // 取消该任务并等待它结束
    println("main: Now I can quit.")
//sampleEnd
}

/**
 * 在 finally 中释放资源
 */
fun main12() = runBlocking {
//sampleStart
    val job = launch {
        try {
            repeat(1000) { i ->
                println("I'm sleeping $i ...")
                delay(500L)
            }
        } finally {
            println("I'm running finally")
        }
    }
    delay(1300L) // 延时一段时间
    println("main: I'm tired of waiting!")
    job.cancelAndJoin() // 取消该任务并且等待它结束
    println("main: Now I can quit.")
//sampleEnd
}

/**
 * 运行不能取消的代码块
 */
fun main13() = runBlocking {
//sampleStart
    val job = launch {
        try {
            repeat(1000) { i ->
                println("I'm sleeping $i ...")
                delay(500L)
            }
        } finally {
            withContext(NonCancellable) {
                println("I'm running finally")
                delay(1000L)
                println("And I've just delayed for 1 sec because I'm non-cancellable")
            }
        }
    }
    delay(1300L) // 延时一段时间
    println("main: I'm tired of waiting!")
    job.cancelAndJoin() // 取消该任务并等待它结束
    println("main: Now I can quit.")
//sampleEnd
}

/**
 * 超时
 */
fun main14() = runBlocking {
//sampleStart
    withTimeout(1300L) {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
    }
//sampleEnd
}

fun main() = runBlocking {
//sampleStart
    val result = withTimeoutOrNull(1300L) {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
        "Done" // 在它运行得到结果之前取消它
    }
    println("Result is $result")
//sampleEnd
}


