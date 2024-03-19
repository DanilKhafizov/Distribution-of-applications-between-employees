package com.kadullin.distributionofrequests.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kadullin.distributionofrequests.R;
import com.kadullin.distributionofrequests.utilities.Constants;
import com.kadullin.distributionofrequests.utilities.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class CreateTaskActivity extends AppCompatActivity {
    private PreferenceManager preferenceManager;
    private TextInputEditText inputTask, inputDesc;
    private Button createBtn;
    private Boolean ischeckCorrectInput = false;
    private BottomNavigationView mainMenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);
        init();
        listeners();
    }
    private void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        inputTask = findViewById(R.id.input_task);
        inputDesc = findViewById(R.id.input_desc);
        createBtn = findViewById(R.id.create_btn);
        mainMenu = findViewById(R.id.main_menu);
        mainMenu.setSelectedItemId(R.id.bottom_create_task);
    }
    private void listeners(){
        createBtn.setOnClickListener(v -> saveQuestionToDatabase());
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
                    startActivity(new Intent(getApplicationContext(), MyTasksActivity.class));
                    finish();
                    return true;
            }
            return false;});}
    private void saveQuestionToDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String task = Objects.requireNonNull(inputTask.getText()).toString();
        String desc = Objects.requireNonNull(inputDesc.getText()).toString();
        String status = "В ОЖИДАНИИ...";
        checkCorrectInput(task, desc);
        if (ischeckCorrectInput) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String currentTime = sdf.format(new Date());
            HashMap<String, Object> tasks = new HashMap<>();
            tasks.put(Constants.KEY_TASK, task);
            tasks.put(Constants.KEY_DESCRIPTION, desc);
            tasks.put(Constants.KEY_TASK_TIME, currentTime);
            tasks.put(Constants.KEY_TASK_STATUS, status);
            db.collection(Constants.KEY_COLLECTION_TASK)
                    .add(tasks)
                    .addOnSuccessListener(documentReference -> {
                        preferenceManager.putString(Constants.KEY_TASK_ID, documentReference.getId());
                        preferenceManager.putString(Constants.KEY_TASK, task);
                        preferenceManager.putString(Constants.KEY_DESCRIPTION, desc);
                        preferenceManager.putString(Constants.KEY_TASK_STATUS, status);
                        preferenceManager.putString(Constants.KEY_TASK_TIME, currentTime);
                        showMessage("Задание успешно создано");
                        showSignUpActivity();
                        finish();
                    })
                    .addOnFailureListener(exception -> showMessage(exception.getMessage()));}
        else {
            showMessage("Пожалуйста, заполните все поля");}
    }
    private void checkCorrectInput(String questionTitle, String question){
        ischeckCorrectInput = !TextUtils.isEmpty(questionTitle) && !TextUtils.isEmpty(question);
    }

    private void showMessage(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    public void showSignUpActivity()
    {
        Intent intent = new Intent(CreateTaskActivity.this, TaskListActivity.class);
        startActivity(intent);
        finish();
    }
}