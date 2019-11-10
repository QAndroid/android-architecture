/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.architecture.blueprints.todoapp.tasks

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.android.architecture.blueprints.todoapp.Event
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.ADD_EDIT_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.util.DELETE_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.util.EDIT_RESULT_OK
import java.util.ArrayList

/**
 * 暴露在任务列表中药显示的数据
 *
 * [BaseObservable]实现了一个监听注册机制，当属性改变的时候会被通知。这是通过给这个属性的getter方法分配 [Bindable]注解来实现的。
 */
class TasksViewModel(
        private val tasksRepository: TasksRepository
) : ViewModel() {
    //数据源相关LiveData
    //当前展示的Task
    private val _items = MutableLiveData<List<Task>>().apply { value = emptyList() }
    val items: LiveData<List<Task>>
        get() = _items

    //UI相关LiveData
    //是否正在请求数据，显示Loading
    //FIXME 为什么要将_dataLoading和dataLoading"倒一遍"??
    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean>
        get() = _dataLoading
    //当前的过滤器文案，显示在列表头部
    private val _currentFilteringLabel = MutableLiveData<Int>()
    val currentFilteringLabel: LiveData<Int>
        get() = _currentFilteringLabel
    //没有Task标签
    private val _noTasksLabel = MutableLiveData<Int>()
    val noTasksLabel: LiveData<Int>
        get() = _noTasksLabel
    //没有Task Icon
    private val _noTaskIconRes = MutableLiveData<Int>()
    val noTaskIconRes: LiveData<Int>
        get() = _noTaskIconRes
    private val _tasksAddViewVisible = MutableLiveData<Boolean>()
    val tasksAddViewVisible: LiveData<Boolean>
        get() = _tasksAddViewVisible

    //snackbar消息事件
    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarMessage: LiveData<Event<Int>>
        get() = _snackbarText
    //当前的过滤器
    private var _currentFiltering = TasksFilterType.ALL_TASKS
    //数据加载失败
    private val isDataLoadingError = MutableLiveData<Boolean>()

    //页面跳转相关LiveData，使用LiveData定义事件，监听事件变化，进行通信
    //打开任务事件
    private val _openTaskEvent = MutableLiveData<Event<String>>()
    val openTaskEvent: LiveData<Event<String>>
        get() = _openTaskEvent
    //新增Task事件
    private val _newTaskEvent = MutableLiveData<Event<Unit>>()
    val newTaskEvent: LiveData<Event<Unit>>
        get() = _newTaskEvent

    // This LiveData depends on another so we can use a transformation.
    val empty: LiveData<Boolean> = Transformations.map(_items) {
        it.isEmpty()
    }

    init {
        //初始化的时候，设置展示所有任务
        setFiltering(TasksFilterType.ALL_TASKS)
    }

    /**
     * 加载所有Tasks
     */
    fun start() {
        loadTasks(false)
    }

    /**
     * 加载所有Task
     * @param forceUpdate 是否强制更新
     */
    fun loadTasks(forceUpdate: Boolean) {
        loadTasks(forceUpdate, true)
    }

    /**
     * Sets the current task filtering type.
     *
     * @param requestType Can be [TasksFilterType.ALL_TASKS],
     * [TasksFilterType.COMPLETED_TASKS], or
     * [TasksFilterType.ACTIVE_TASKS]
     */
    fun setFiltering(requestType: TasksFilterType) {
        _currentFiltering = requestType

        // Depending on the filter type, set the filtering label, icon drawables, etc.
        when (requestType) {
            TasksFilterType.ALL_TASKS -> {
                setFilter(R.string.label_all, R.string.no_tasks_all,
                        R.drawable.ic_assignment_turned_in_24dp, true)
            }
            TasksFilterType.ACTIVE_TASKS -> {
                setFilter(R.string.label_active, R.string.no_tasks_active,
                        R.drawable.ic_check_circle_24dp, false)
            }
            TasksFilterType.COMPLETED_TASKS -> {
                setFilter(R.string.label_completed, R.string.no_tasks_completed,
                        R.drawable.ic_verified_user_24dp, false)
            }
        }
    }

    private fun setFilter(@StringRes filteringLabelString: Int, @StringRes noTasksLabelString: Int,
                          @DrawableRes noTaskIconDrawable: Int, tasksAddVisible: Boolean) {
        _currentFilteringLabel.value = filteringLabelString
        _noTasksLabel.value = noTasksLabelString
        _noTaskIconRes.value = noTaskIconDrawable
        _tasksAddViewVisible.value = tasksAddVisible
    }

    /**
     * 清除已经完成的Task
     */
    fun clearCompletedTasks() {
        //数据清除已经完成的Task
        tasksRepository.clearCompletedTasks()
        //UI展示更新
        _snackbarText.value = Event(R.string.completed_tasks_cleared)
        //重新大家新的任务
        loadTasks(forceUpdate = false, showLoadingUI = false)
    }

    fun completeTask(task: Task, completed: Boolean) {
        if (completed) {
            //通知Repository，更新UI展示Snackbar
            tasksRepository.completeTask(task)
            showSnackbarMessage(R.string.task_marked_complete)
        } else {
            tasksRepository.activateTask(task)
            showSnackbarMessage(R.string.task_marked_active)
        }
    }

    /**
     * 通过数据绑定库和FAB的点击监听调用
     */
    fun addNewTask() {
        _newTaskEvent.value = Event(Unit)
    }

    /**
     * 在 [TasksAdapter]中调用，打开任务详情页面，通过LiveData通知
     */
    internal fun openTask(taskId: String) {
        _openTaskEvent.value = Event(taskId)
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int) {
        if (AddEditTaskActivity.REQUEST_CODE == requestCode) {
            when (resultCode) {
                EDIT_RESULT_OK -> _snackbarText.setValue(
                        Event(R.string.successfully_saved_task_message)
                )
                ADD_EDIT_RESULT_OK -> _snackbarText.setValue(
                        Event(R.string.successfully_added_task_message)
                )
                DELETE_RESULT_OK -> _snackbarText.setValue(
                        Event(R.string.successfully_deleted_task_message)
                )
            }
        }
    }

    /**
     * 展示Snackbar信息
     */
    private fun showSnackbarMessage(message: Int) {
        _snackbarText.value = Event(message)
    }

    /**
     * 加载任务
     * @param forceUpdate 传递true刷新在[TasksDataSource]中的数据
     * @param showLoadingUI 传递true在UI上展示loading icon
     */
    private fun loadTasks(forceUpdate: Boolean, showLoadingUI: Boolean) {
        //是否展示Loading
        if (showLoadingUI) {
            _dataLoading.value = true
        }

        //是否强制更新
        if (forceUpdate) {
            tasksRepository.refreshTasks()
        }

        //还是通过Callback返回数据，包装在viewModel中
        tasksRepository.getTasks(object : TasksDataSource.LoadTasksCallback {
            override fun onTasksLoaded(tasks: List<Task>) {
                //返回所有的Tasks
                val tasksToShow = ArrayList<Task>()
                //基于请求类型过滤Tasks
                for (task in tasks) {
                    when (_currentFiltering) {
                        TasksFilterType.ALL_TASKS -> tasksToShow.add(task)
                        TasksFilterType.ACTIVE_TASKS -> if (task.isActive) {
                            tasksToShow.add(task)
                        }
                        TasksFilterType.COMPLETED_TASKS -> if (task.isCompleted) {
                            tasksToShow.add(task)
                        }
                    }
                }

                //更新展示变量
                if (showLoadingUI) {
                    _dataLoading.value = false
                }
                isDataLoadingError.value = false

                //将返回并过滤的Task转换成LiveData
                val itemsValue = ArrayList(tasksToShow)
                _items.value = itemsValue
            }

            override fun onDataNotAvailable() {
                isDataLoadingError.value = true
            }
        })
    }
}
