package com.kadullin.distributionofrequests.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kadullin.distributionofrequests.R;
import com.kadullin.distributionofrequests.adapters.TaskAdapter;
import com.kadullin.distributionofrequests.fragments.TaskFragment;
import com.kadullin.distributionofrequests.models.Task;
import com.kadullin.distributionofrequests.utilities.Constants;
import com.kadullin.distributionofrequests.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class MyTasksActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;
    private BottomNavigationView mainMenu;
    private TextView noTasksTv;

    public TaskAdapter getTaskAdapter() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view_tasks);
        return (TaskAdapter) recyclerView.getAdapter();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tasks);
        init();
        getMenu();
        loadTasks();
    }

    private void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        RecyclerView recyclerView = findViewById(R.id.recycler_view_tasks);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();
        mainMenu = findViewById(R.id.main_menu);
        mainMenu.setSelectedItemId(R.id.bottom_my_tasks);
        noTasksTv = findViewById(R.id.no_tasks_tv);
    }

    private void loadTasks() {
        String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
        db.collection(Constants.KEY_COLLECTION_TASK)
                .whereEqualTo(Constants.KEY_USER_ID, currentUserId)
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        List<Task> taskList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String taskId = document.getId();
                            String title = document.getString(Constants.KEY_TASK);
                            String createTime = document.getString(Constants.KEY_TASK_TIME);
                            String status = document.getString(Constants.KEY_TASK_STATUS);
                            String employee = document.getString(Constants.KEY_NAME);
                            Task task1;
                            if (employee != null) {
                                task1 = new Task(title, createTime, status, employee);
                            } else {
                                task1 = new Task(title, createTime, status);
                            }
                            task1.setId(taskId);
                            taskList.add(task1);
                        }
                        TaskAdapter taskAdapter = new TaskAdapter(taskList, position -> {
                            Task selectedTask = taskList.get(position);
                            showTaskDetailsFragment(selectedTask);
                        });
                        RecyclerView recyclerView = findViewById(R.id.recycler_view_tasks);
                        recyclerView.setAdapter(taskAdapter);
                        if (taskList.isEmpty()) {
                            showErrorMessage();
                        } else {
                            noTasksTv.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void showTaskDetailsFragment(Task task) {
        String taskId = task.getId();
        TaskFragment taskFragment = TaskFragment.newInstance(taskId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, taskFragment)
                .addToBackStack(null)
                .commit();
    }

    @SuppressLint("NonConstantResourceId")
    private void getMenu() {
        String role = preferenceManager.getString(Constants.KEY_ROLE);
        Menu menu = mainMenu.getMenu();
        MenuItem createTaskItem = menu.findItem(R.id.bottom_create_task);
        createTaskItem.setVisible("Директор".equals(role));
        mainMenu.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.bottom_main:
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                    return true;
                case R.id.bottom_task_list:
                    startActivity(new Intent(getApplicationContext(), TaskListActivity.class));
                    finish();
                    return true;
                case R.id.bottom_create_task:
                    if ("Директор".equals(role)) {
                        startActivity(new Intent(getApplicationContext(), CreateTaskActivity.class));
                        finish();
                        return true;
                    } else {
                        return false;
                    }
                case R.id.bottom_my_tasks:
                    return true;
            }
            return false;
        });
    }
    private void showErrorMessage(){
        noTasksTv.setText(String.format("%s", "Задания отсутствуют"));
        noTasksTv.setVisibility(View.VISIBLE);
    }


}