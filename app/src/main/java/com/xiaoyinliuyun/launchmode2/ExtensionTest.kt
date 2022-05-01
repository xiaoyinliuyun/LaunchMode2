package com.xiaoyinliuyun.launchmode2

/**
 * @Author yangkunjian.
 *
 * @Date   2022/4/18 10:38.
 *
 * @Desc
 */
//举例
fun <T> MutableList<T>.swap(from: Int, to: Int): Boolean {
    if (from < 0 || from >= this.size) {
        return false
    }
    if (to < 0 || to >= this.size) {
        return false
    }
    val temp = this[from]
    this[from] = this[to]
    this[to] = temp
    return true
}

fun main(args :Array<String>){
    var uname = "我的中国"
    println(uname?.isEmpty())
}

//对String类进行扩展一个isEmpty方法
//fun String.扩展函数名称
fun String?.isEEmpty() :Boolean {
    return this==null || this.isEmpty()
}


