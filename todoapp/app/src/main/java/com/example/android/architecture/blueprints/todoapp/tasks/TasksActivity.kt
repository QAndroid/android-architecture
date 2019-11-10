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

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import com.example.android.architecture.blueprints.todoapp.Event
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsActivity
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity
import com.example.android.architecture.blueprints.todoapp.util.obtainViewModel
import com.example.android.architecture.blueprints.todoapp.util.replaceFragmentInActivity
import com.example.android.architecture.blueprints.todoapp.util.setupActionBar
import com.google.android.material.navigation.NavigationView

/**
 * TasksActivity页面
 */
class TasksActivity : AppCompatActivity(), TaskItemNavigator, TasksNavigator {

    private lateinit var drawerLayout: DrawerLayout

    private lateinit var viewModel: TasksViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tasks_act)

        //设置ActionBar
        setupActionBar(R.id.toolbar) {
            setHomeAsUpIndicator(R.drawable.ic_menu)
            setDisplayHomeAsUpEnabled(true)
        }

        //设置抽屉导航
        setupNavigationDrawer()

        //设置TasksFragment
        setupViewFragment()

        //标准函数apply
        viewModel = obtainViewModel().apply {
            //订阅打开任务详情事件
            openTaskEvent.observe(this@TasksActivity, Observer<Event<String>> { event ->
                //如果用户没有使用，则打开任务详情
                event.getContentIfNotHandled()?.let {
                    openTaskDetails(it)

                }
            })
            //订阅"new task"事件
            newTaskEvent.observe(this@TasksActivity, Observer<Event<Unit>> { event ->
                event.getContentIfNotHandled()?.let {
                    this@TasksActivity.addNewTask()
                }
            })
        }
    }

    /**
     * 设置TasksFragment
     */
    private fun setupViewFragment() {
        supportFragmentManager.findFragmentById(R.id.contentFrame)
            ?: replaceFragmentInActivity(TasksFragment.newInstance(), R.id.contentFrame)
    }

    /**
     * 设置抽屉导航栏
     */
    private fun setupNavigationDrawer() {
        drawerLayout = (findViewById<DrawerLayout>(R.id.drawer_layout))
            .apply {
                setStatusBarBackground(R.color.colorPrimaryDark)
            }
        setupDrawerContent(findViewById(R.id.nav_view))
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                android.R.id.home -> {
                    // Open the navigation drawer when the home icon is selected from the toolbar.
                    drawerLayout.openDrawer(GravityCompat.START)
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }

    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.list_navigation_menu_item -> {
                    // Do nothing, we're already on that screen
                }
                R.id.statistics_navigation_menu_item -> {
                    //进入到StatisticsActivity页面
                    val intent = Intent(this@TasksActivity, StatisticsActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                    startActivity(intent)
                }
            }
            //当item被选选中的时候，关闭导航抽屉
            menuItem.isChecked = true
            drawerLayout.closeDrawers()
            true
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //处理Activity返回结果
        viewModel.handleActivityResult(requestCode, resultCode)
    }

    /**
     * 打开任务详情页面
     */
    override fun openTaskDetails(taskId: String) {
        val intent = Intent(this, TaskDetailActivity::class.java).apply {
            putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId)
        }
        startActivityForResult(intent, AddEditTaskActivity.REQUEST_CODE)

    }

    /**
     * 打开添加新任务页面
     */
    override fun addNewTask() {
        val intent = Intent(this, AddEditTaskActivity::class.java)
        startActivityForResult(intent, AddEditTaskActivity.REQUEST_CODE)
    }

    /**
     * 获取TasksViewModel对象
     */
    fun obtainViewModel(): TasksViewModel = obtainViewModel(TasksViewModel::class.java)
}