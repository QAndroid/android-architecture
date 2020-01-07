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

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource.LoadTasksCallback;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 实现TasksPresenter的单元测试
 */
//参考Mockito入门：https://waylau.com/mockito-quick-start/
public class TasksPresenterTest {

    private static List<Task> TASKS;

    @Mock
    private TasksRepository mTasksRepository;

    @Mock
    private TasksContract.View mTasksView;

    /**
     * ArgumentCaptor是一个强大的Mokito API，用来捕获参数值，然后在它们上执行功能action或者assertion。
     */
    @Captor
    private ArgumentCaptor<LoadTasksCallback> mLoadTasksCallbackCaptor;

    private TasksPresenter mTasksPresenter;

    @Before
    public void setupTasksPresenter() {
        // Mockito通过使用@mock注解，非常方便的注入mock。为了在测试中注入mock，initMocks方法必须被调用。
        MockitoAnnotations.initMocks(this);

        //获取被测试的类的引用
        mTasksPresenter = new TasksPresenter(mTasksRepository, mTasksView);

        //使用Studing Mock mTasksView永远是active的
        //因为presneter只有在view是active的时候，才会会更新view
        when(mTasksView.isActive()).thenReturn(true);

        //我们从3个任务开始，一个活动的两个完成的
        TASKS = Lists.newArrayList(new Task("Title1", "Description1"),
                new Task("Title2", "Description2", true), new Task("Title3", "Description3", true));
    }

    @Test
    public void createPresenter_setsThePresenterToView() {
        //获取测试类的索引
        mTasksPresenter = new TasksPresenter(mTasksRepository, mTasksView);

        //使用Mockito验证行为，验证new TasksPresenter()调用后，mTasksView调用了setPresenter()方法
        verify(mTasksView).setPresenter(mTasksPresenter);
    }

    @Test
    public void loadAllTasksFromRepositoryAndLoadIntoView() {
        //设置TaskPresenter过滤所有任务
        mTasksPresenter.setFiltering(TasksFilterType.ALL_TASKS);
        mTasksPresenter.loadTasks(true);
        //当任务加载的时候请求，捕获Callback，然后调用插桩方法onTasksLoaded，返回所有task
        verify(mTasksRepository).getTasks(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onTasksLoaded(TASKS);

        //然后过程指示器展示
        InOrder inOrder = inOrder(mTasksView);
        inOrder.verify(mTasksView).setLoadingIndicator(true);
        //然后过程指示器隐藏，并且所有任务都在UI上展示
        inOrder.verify(mTasksView).setLoadingIndicator(false);
        //验证展示Task方法showTasks的调用，并且参数传入3个任务
        ArgumentCaptor<List> showTasksArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(mTasksView).showTasks(showTasksArgumentCaptor.capture());
        assertTrue(showTasksArgumentCaptor.getValue().size() == 3);
    }

    @Test
    public void loadActiveTasksFromRepositoryAndLoadIntoView() {
        //设置TasksPresenter过滤活动的任务
        mTasksPresenter.setFiltering(TasksFilterType.ACTIVE_TASKS);
        //当请求加载任务的时候捕获Callback，然后调用插桩方法onTasksLoaded返回所有task
        mTasksPresenter.loadTasks(true);
        verify(mTasksRepository).getTasks(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onTasksLoaded(TASKS);

        //过程指示器被隐藏，活动的Task被展示在UI上
        verify(mTasksView).setLoadingIndicator(false);
        //验证展示Task方法showTasks调用，并且参数传入1个活动的任务
        ArgumentCaptor<List> showTasksArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(mTasksView).showTasks(showTasksArgumentCaptor.capture());
        assertTrue(showTasksArgumentCaptor.getValue().size() == 1);
    }

    @Test
    public void loadCompletedTasksFromRepositoryAndLoadIntoView() {
        //设置onTasksLoaded过滤已完成的任务
        mTasksPresenter.setFiltering(TasksFilterType.COMPLETED_TASKS);
        mTasksPresenter.loadTasks(true);
        //当请求加载任务的时候，捕获Callback，然后调用插桩方法onTasksLoaded方法所有task
        verify(mTasksRepository).getTasks(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onTasksLoaded(TASKS);

        //过程指示器被隐藏，完成的Task被展示在UI上
        verify(mTasksView).setLoadingIndicator(false);
        ArgumentCaptor<List> showTasksArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(mTasksView).showTasks(showTasksArgumentCaptor.capture());
        assertTrue(showTasksArgumentCaptor.getValue().size() == 2);
    }

    @Test
    public void clickOnFab_ShowsAddTaskUi() {
        //当添加一个新的任务的时候
        mTasksPresenter.addNewTask();

        //然后添加任务的UI被展示
        verify(mTasksView).showAddTask();
    }

    @Test
    public void clickOnTask_ShowsDetailUi() {
        //给一个插桩的活动的task
        Task requestedTask = new Task("Details Requested", "For this task");

        //当打开任务详情的时候
        mTasksPresenter.openTaskDetails(requestedTask);

        //验证任务详情UI被展示的
        verify(mTasksView).showTaskDetailsUi(any(String.class));
    }

    @Test
    public void completeTask_ShowsTaskMarkedComplete() {
        //一个插桩的task
        Task task = new Task("Details Requested", "For this task");

        //当task被标记完成
        mTasksPresenter.completeTask(task);

        //repository被调用，并且标记的task在UI中展示
        verify(mTasksRepository).completeTask(task);
        verify(mTasksView).showTaskMarkedComplete();
    }

    @Test
    public void activateTask_ShowsTaskMarkedActive() {
        //给一个插桩完成的任务
        Task task = new Task("Details Requested", "For this task", true);
        mTasksPresenter.loadTasks(true);

        // 当任务被标记为活动的
        mTasksPresenter.activateTask(task);

        // repository激活task被调用，并且标记的task在UI中展示
        verify(mTasksRepository).activateTask(task);
        verify(mTasksView).showTaskMarkedActive();
    }

    @Test
    public void unavailableTasks_ShowsError() {
        //当所有task被加载
        mTasksPresenter.setFiltering(TasksFilterType.ALL_TASKS);
        mTasksPresenter.loadTasks(true);
        //并且在repository没有任务返回
        verify(mTasksRepository).getTasks(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onDataNotAvailable();

        //验证错误信息被展示
        verify(mTasksView).showLoadingTasksError();
    }
}
