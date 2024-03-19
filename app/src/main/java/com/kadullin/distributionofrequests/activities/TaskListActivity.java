package com.kadullin.distributionofrequests.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class TaskListActivity extends AppCompatActivity {
    private PreferenceManager preferenceManager;
    private RecyclerView recyclerView;
    private List<Task> taskList;
    private FirebaseFirestore db;
    private BottomNavigationView mainMenu;
    private TextView noTasksTv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasklist);


        preferenceManager = new PreferenceManager(getApplicationContext());
        recyclerView = findViewById(R.id.recycler_view_tasks);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        preferenceManager = new PreferenceManager(getApplicationContext());
        noTasksTv = findViewById(R.id.no_tasks_tv);

        taskList = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        loadTasks();


        mainMenu = findViewById(R.id.main_menu);
        mainMenu.setSelectedItemId(R.id.bottom_task_list);
        getMenu();


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
                    startActivity(new Intent(getApplicationContext(), MyTasksActivity.class));
                    finish();
                    return true;
            }
            return false;
        });
    }

    public TaskAdapter getTaskAdapter() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view_tasks);
        return (TaskAdapter) recyclerView.getAdapter();
    }

    private void loadTasks() {
        db.collection(Constants.KEY_COLLECTION_TASK)
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String taskId = document.getId();
                            String title = document.getString(Constants.KEY_TASK);
                            String createTime = document.getString(Constants.KEY_TASK_TIME);
                            String status = document.getString(Constants.KEY_TASK_STATUS);
                            String employee = document.getString(Constants.KEY_NAME);
                            Task task1;
                            if (employee != null){
                                 task1 = new Task(title, createTime, status, employee);
                            }
                            else{
                                 task1 = new Task(title, createTime, status);
                            }
                            task1.setId(taskId);
                            taskList.add(task1);
                        }
                        TaskAdapter taskAdapter = new TaskAdapter(taskList, position -> {
                            Task selectedTask = taskList.get(position);
                            showTaskDetailsFragment(selectedTask);

                        });
                        recyclerView.setAdapter(taskAdapter);
                        if (taskList.isEmpty()) {
                            showErrorMessage();
                        } else {
                            noTasksTv.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void showErrorMessage(){
        noTasksTv.setText(String.format("%s", "Задания отсутствуют"));
        noTasksTv.setVisibility(View.VISIBLE);
    }


    private void showTaskDetailsFragment(Task task) {
        String taskId = task.getId();
        TaskFragment taskFragment = TaskFragment.newInstance(taskId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, taskFragment)
                .addToBackStack(null)
                .commit();
    }
}