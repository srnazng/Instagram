package com.example.instagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {
    final FragmentManager fragmentManager = getSupportFragmentManager();

    // define fragments
    final Fragment homeFragment = HomeFragment.newInstance();
    final Fragment newPostFragment = NewPostFragment.newInstance();
    final Fragment settingsFragment = SettingsFragment.newInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            Fragment selectedFragment = null;

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        selectedFragment = homeFragment;
                        break;
                    case R.id.action_post:
                        selectedFragment = newPostFragment;
                        break;
                    case R.id.action_settings:
                        selectedFragment = settingsFragment;
                        break;
                    default: selectedFragment = homeFragment;
                }
                fragmentManager.beginTransaction().replace(R.id.rlContainer, selectedFragment).commit();
                return true;
            }
        });
    }


}