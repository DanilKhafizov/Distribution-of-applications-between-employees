package com.kadullin.distributionofrequests.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.kadullin.distributionofrequests.R;
import com.kadullin.distributionofrequests.utilities.Constants;
import com.kadullin.distributionofrequests.utilities.PreferenceManager;

public class LoadingScreenActivity extends AppCompatActivity {
    private PreferenceManager preferenceManager;
    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);
        preferenceManager = new PreferenceManager(getApplicationContext());
        handler = new Handler();
        if(!preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            handler.postDelayed(() -> {
                Intent intent = new Intent(LoadingScreenActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }, 3000);
        }
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(), TaskListActivity.class);
            startActivity(intent);
            finish();
        }
    }
}