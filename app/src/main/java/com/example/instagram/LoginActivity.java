package com.example.instagram;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText etLoginUsername;
    private EditText etLoginPassword;
    private Button btnLogin;

    public static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(ParseUser.getCurrentUser() != null){
            goMainActivity();
        }

        etLoginUsername = findViewById(R.id.etLoginUsername);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick login button");
                String username = etLoginUsername.getText().toString();
                String password = etLoginPassword.getText().toString();
                loginUser(username, password);
            }
        });
    }

    private void loginUser(String username, String password){
        Log.i(TAG, "attempt login");

        // login in background thread
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if(e != null){
                    // issue
                    Log.e(TAG, "Issue with login", e);
                    return;
                }
                Log.e(TAG, "login success");
                goMainActivity();
                Toast.makeText(LoginActivity.this, "Success Login", Toast.LENGTH_SHORT);
            }
        });
    }

    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish(); // app exists if you press back
    }
}