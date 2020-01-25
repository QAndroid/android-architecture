package com.example.android.architecture.blueprints.todoapp

/**
 * 用于作为一个数据包装器，期望通过LiveData表示事件
 * //FIXME Event封装是不是有点多余，有多次处理的情况吗？
 */
open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set //允许外部读不允许写

    /**
     * 返回内容并防止再次使用
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * 返回内容，即使它已经被使用
     */
    fun peekContent(): T = content
}
