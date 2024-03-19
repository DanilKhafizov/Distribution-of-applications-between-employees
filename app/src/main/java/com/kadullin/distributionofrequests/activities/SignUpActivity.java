package com.kadullin.distributionofrequests.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kadullin.distributionofrequests.R;
import com.kadullin.distributionofrequests.utilities.Constants;
import com.kadullin.distributionofrequests.utilities.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {
    private PreferenceManager preferenceManager;
    private TextInputEditText inputName, inputLogin, inputPassword, inputConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        preferenceManager = new PreferenceManager(getApplicationContext());
        inputName = findViewById(R.id.input_name);
        inputLogin = findViewById(R.id.input_login);
        inputPassword = findViewById(R.id.input_password);
        inputConfirmPassword = findViewById(R.id.input_confirm_password);
        MaterialButton signUpBtn = findViewById(R.id.SignUpBtn);
        MaterialButton signInBtn = findViewById(R.id.SignInBtn);

        signInBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            startActivity(intent);
        });


        signUpBtn.setOnClickListener(v -> {
            if(checkFields()){
                signUp();
            }
        });
    }

    public Boolean checkFields() {
        String name = Objects.requireNonNull(inputName.getText()).toString().trim();
        String login = Objects.requireNonNull(inputLogin.getText()).toString().trim();
        String password = Objects.requireNonNull(inputPassword.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(inputConfirmPassword.getText()).toString().trim();
        if (name.isEmpty()  || login.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showMessage("Пожалуйста заполните все поля");
            return false;
        }
        else if (!password.equals(confirmPassword)) {
            showMessage("Пароли не совпадают");
            return false;
        }
        else if(name.length() > 50 || login.length() > 50 || password.length() > 50){
            showMessage("Превышен лимит количества символов");
            return false;
        }
        else{
                return true;
            }
        }

    private void signUp() {
        clearUserData();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        final String userLogin = Objects.requireNonNull(inputLogin.getText()).toString();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_LOGIN, userLogin)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            HashMap<String, Object> user = new HashMap<>();
                            user.put(Constants.KEY_NAME, Objects.requireNonNull(inputName.getText()).toString());
                            user.put(Constants.KEY_LOGIN, userLogin);
                            user.put(Constants.KEY_PASSWORD, Objects.requireNonNull(inputPassword.getText()).toString());
                            if (userLogin.equals("Kadullin")) {
                                user.put(Constants.KEY_ROLE, "Директор");
                                preferenceManager.putString(Constants.KEY_ROLE, "Директор");
                            } else {
                                user.put(Constants.KEY_ROLE, "Сотрудник");
                                preferenceManager.putString(Constants.KEY_ROLE, "Сотрудник");
                            }
                            database.collection(Constants.KEY_COLLECTION_USERS)
                                    .add(user)
                                    .addOnSuccessListener(documentReference -> {
                                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                        preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                                        preferenceManager.putString(Constants.KEY_NAME, inputName.getText().toString());
                                        preferenceManager.putString(Constants.KEY_LOGIN, inputLogin.getText().toString());
                                        preferenceManager.putString(Constants.KEY_PASSWORD, inputPassword.getText().toString());
                                        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                                        String loginTime = sdf.format(new Date());
                                        database.collection(Constants.KEY_COLLECTION_USERS)
                                                .document(documentReference.getId())
                                                .update(Constants.KEY_LOGIN_TIME, loginTime)
                                                .addOnSuccessListener(aVoid -> {
                                                    preferenceManager.putString(Constants.KEY_LOGIN_TIME, loginTime);
                                                    openCorrectActivity();
                                                })
                                                .addOnFailureListener(e -> showMessage("Ошибка при обновлении времени входа"));
                                    })
                                    .addOnFailureListener(exception -> showMessage(exception.getMessage()));
                        } else {
                            showMessage("Пользователь с такой почтой уже зарегистрирован.");
                        }
                    } else {
                        showMessage("Ошибка: " + Objects.requireNonNull(task.getException()).getMessage());
                    }
                });
    }

    private void openCorrectActivity(){
        String role = preferenceManager.getString(Constants.KEY_ROLE);
        Intent intent = null;
        if(role.equals("Сотрудник")){
            intent = new Intent(getApplicationContext(), TaskListActivity.class);
        }
        if(role.equals("Директор")){
            intent = new Intent(getApplicationContext(), CreateTaskActivity.class);
        }
        assert intent != null;
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showMessage(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void clearUserData() {
        preferenceManager.clearPreferences();
    }
}