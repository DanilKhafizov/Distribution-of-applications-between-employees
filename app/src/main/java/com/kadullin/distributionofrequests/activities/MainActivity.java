package com.kadullin.distributionofrequests.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kadullin.distributionofrequests.R;
import com.kadullin.distributionofrequests.utilities.Constants;
import com.kadullin.distributionofrequests.utilities.PreferenceManager;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private PreferenceManager preferenceManager;
    private TextView fioInput, loginInput, passwordInput;
    private BottomNavigationView mainMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferenceManager = new PreferenceManager(getApplicationContext());
        Button openTasks = findViewById(R.id.open_my_tasks_btn);
        fioInput = findViewById(R.id.fio_input);
        loginInput = findViewById(R.id.login_input);
        passwordInput = findViewById(R.id.password_input);
        mainMenu = findViewById(R.id.main_menu);
        mainMenu.setSelectedItemId(R.id.bottom_main);
        AppCompatImageButton logout = findViewById(R.id.logout);
        logout.setOnClickListener(v -> logout());
        getUser();
        getMenu();
        openTasks.setOnClickListener(v -> showMyTasksActivity());
    }
    private void getUser(){
        String fio = preferenceManager.getString(Constants.KEY_NAME);
        String login = preferenceManager.getString(Constants.KEY_LOGIN);
        String password = preferenceManager.getString(Constants.KEY_PASSWORD);
        fioInput.setText(fio);
        loginInput.setText(login);
        passwordInput.setText(password);
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
            return false;
        });
    }



    private void logout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        dialogMessage.setText("Вы действительно хотите выйти из аккаунта");

        builder.setView(dialogView)
                .setPositiveButton("Да", (dialog, which) -> {
                    showMessage("Выход из аккаунта...");
                    FirebaseFirestore database = FirebaseFirestore.getInstance();
                    DocumentReference documentReference =
                            database.collection(Constants.KEY_COLLECTION_USERS).document(
                                    preferenceManager.getString(Constants.KEY_USER_ID)
                            );
                    HashMap<String, Object> updates = new HashMap<>();
                    documentReference.update(updates)
                            .addOnSuccessListener(unused -> {
                                preferenceManager.clearPreferences();
                                startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> showMessage("Не удалось выйти"));
                })
                .setNegativeButton("Нет", (dialog, which) -> {

                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        positiveButton.setTextColor(getResources().getColor(R.color.white));
        negativeButton.setTextColor(getResources().getColor(R.color.white));
    }

    private void showMessage(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showMyTasksActivity(){
        Intent intent = new Intent(MainActivity.this, MyTasksActivity.class);
        startActivity(intent);
    }
}