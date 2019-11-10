package com.example.android.architecture.blueprints.todoapp.util

/**
 * View和它的子View的扩展方法
 */

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.android.architecture.blueprints.todoapp.Event
import com.example.android.architecture.blueprints.todoapp.ScrollChildSwipeRefreshLayout
import com.example.android.architecture.blueprints.todoapp.tasks.TasksViewModel
import com.google.android.material.snackbar.Snackbar

/**
 * 将静态Java函数Snackbar.make()转换为视图中的扩展函数
 */
//扩展函数，参考：https://juejin.im/post/5c74add5f265da2da15dc75b
fun View.showSnackbar(snackbarText: String, timeLength: Int) {
    //run范围函数，为调用函数提供一个内部范围，参考：https://juejin.im/post/5a676159f265da3e3c6c4d82
    Snackbar.make(this, snackbarText, timeLength).run {
        addCallback(object : Snackbar.Callback() {
            override fun onShown(sb: Snackbar?) {
                EspressoIdlingResource.increment()
            }

            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                EspressoIdlingResource.decrement()
            }
        })
        show()
    }
}

/**
 * 当修改snackbarTaskMessageLiveEvent包含的值时，触发snackbar消息
 */
fun View.setupSnackbar(
        lifecycleOwner: LifecycleOwner,
        snackbarEvent: LiveData<Event<Int>>,
        timeLength: Int
) {

    //观察Fragment生命周期，只要内容变化，且没有被使用过则提示信息
    //LiveData，参考：https://developer.android.com/topic/libraries/architecture/livedata
    snackbarEvent.observe(lifecycleOwner, Observer { event ->
        event.getContentIfNotHandled()?.let {
            showSnackbar(context.getString(it), timeLength)
        }
    })
}

/**
 * Reloads the data when the pull-to-refresh is triggered.
 *
 * Creates the `android:onRefresh` for a [SwipeRefreshLayout].
 */
@BindingAdapter("android:onRefresh")
fun ScrollChildSwipeRefreshLayout.setSwipeRefreshLayoutOnRefreshListener(
        viewModel: TasksViewModel) {
    setOnRefreshListener { viewModel.loadTasks(true) }
}
