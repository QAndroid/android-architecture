/*
 * Copyright 2016, The Android Open Source Project
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

package com.example.android.architecture.blueprints.todoapp.tasks;

import androidx.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.BaseView;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.BasePresenter;

import java.util.List;

/**
 * 这里指定连接view和presenter的契约
 */
public interface TasksContract {

    /**
     * View相关的接口
     */
    interface View extends BaseView<Presenter> {
        /**
         * 显示正在加载指示器
         * @param active
         */
        void setLoadingIndicator(boolean active);

        /**
         * 显示任务列表
         * @param tasks
         */
        void showTasks(List<Task> tasks);

        /**
         * 显示添加任务页面
         */
        void showAddTask();

        /**
         * 显示任务详情页面
         * @param taskId
         */
        void showTaskDetailsUi(String taskId);

        /**
         * 显示已完成的Task
         */
        void showTaskMarkedComplete();

        /**
         * 显示未完成的Task
         */
        void showTaskMarkedActive();

        /**
         * 显示全部的任务
         */
        void showCompletedTasksCleared();

        /**
         * 显示加载任务失败
         */
        void showLoadingTasksError();

        /**
         * 显示任务空列表
         */
        void showNoTasks();

        /**
         * 显示未完成过滤标签
         */
        void showActiveFilterLabel();

        /**
         * 显示完成过滤标签
         */
        void showCompletedFilterLabel();

        /**
         * 显示所有过滤标签
         */
        void showAllFilterLabel();

        /**
         * 显示未xx任务列表
         */
        void showNoActiveTasks();

        /**
         * 显示未完成任务列表
         */
        void showNoCompletedTasks();

        /**
         * 显示成功保存的消息
         */
        void showSuccessfullySavedMessage();

        boolean isActive();

        /**
         * 显示过滤PopUp菜单
         */
        void showFilteringPopUpMenu();
    }

    /**
     * Presenter相关接口
     */
    interface Presenter extends BasePresenter {
        /**
         * Activity返回result处理
         * @param requestCode
         * @param resultCode
         */
        void result(int requestCode, int resultCode);

        /**
         * 加载任务列表
         * @param forceUpdate
         */
        void loadTasks(boolean forceUpdate);

        /**
         * 添加新的任务
         */
        void addNewTask();

        /**
         * 打开任务详情
         * @param requestedTask
         */
        void openTaskDetails(@NonNull Task requestedTask);

        /**
         * 完成任务
         * @param completedTask
         */
        void completeTask(@NonNull Task completedTask);

        /**
         * 激活任务
         * @param activeTask
         */
        void activateTask(@NonNull Task activeTask);

        /**
         * 清除完成的任务
         */
        void clearCompletedTasks();

        /**
         * 设置过滤器
         * @param requestType
         */
        void setFiltering(TasksFilterType requestType);

        /**
         * 获取当前过滤类型
         * @return
         */
        TasksFilterType getFiltering();
    }
}
