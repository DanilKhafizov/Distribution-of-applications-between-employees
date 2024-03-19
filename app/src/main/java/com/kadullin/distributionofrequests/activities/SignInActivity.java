package com.kadullin.distributionofrequests.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kadullin.distributionofrequests.R;
import com.kadullin.distributionofrequests.utilities.Constants;
import com.kadullin.distributionofrequests.utilities.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class SignInActivity extends AppCompatActivity {
    private PreferenceManager preferenceManager;
    private MaterialButton signUpButton, signInButton;
    private TextInputEditText inputLogin, inputPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        listeners();
    }
    private void listeners(){

        signInButton.setOnClickListener(v -> signIn());
        signUpButton.setOnClickListener(v -> showSignUpActivity());
    }

    private void init(){
        signUpButton = findViewById(R.id.SignUpBtn);
        signInButton = findViewById(R.id.SignInBtn);
        inputLogin = findViewById(R.id.input_login);
        inputPassword = findViewById(R.id.input_password);
    }

    private void signIn(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_LOGIN, Objects.requireNonNull(inputLogin.getText()).toString())
                .whereEqualTo(Constants.KEY_PASSWORD, Objects.requireNonNull(inputPassword.getText()).toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null
                            && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_LOGIN, documentSnapshot.getString(Constants.KEY_LOGIN));
                        preferenceManager.putString(Constants.KEY_ROLE, documentSnapshot.getString(Constants.KEY_ROLE));
                        preferenceManager.putString(Constants.KEY_PASSWORD, documentSnapshot.getString(Constants.KEY_PASSWORD));
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                        String loginTime = sdf.format(new Date());
                        database.collection(Constants.KEY_COLLECTION_USERS)
                                .document(documentSnapshot.getId())
                                .update(Constants.KEY_LOGIN_TIME, loginTime)
                                .addOnSuccessListener(aVoid -> {
                                    preferenceManager.putString(Constants.KEY_LOGIN_TIME, loginTime);
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> showMessage("Ошибка при обновлении времени входа"));
                    } else {
                        showMessage("Пользователь не найден");
                    }
                });
    }

    private void showMessage(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void showSignUpActivity()
    {
        Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }


}