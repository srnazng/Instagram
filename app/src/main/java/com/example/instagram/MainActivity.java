package com.example.instagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    final FragmentManager fragmentManager = getSupportFragmentManager();

    // define fragments
    final Fragment homeFragment = HomeFragment.newInstance();
    final Fragment newPostFragment = NewPostFragment.newInstance();
    final Fragment settingsFragment = SettingsFragment.newInstance();

    public Fragment selectedFragment;

    private ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectedFragment = homeFragment;

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_post:
                        selectedFragment = newPostFragment;
                        break;
                    case R.id.action_settings:
                        selectedFragment = settingsFragment;
                        break;
                    default: selectedFragment = homeFragment;
                }
                fragmentManager.beginTransaction().replace(R.id.frame, selectedFragment).commit();
                return true;
            }
        });

        logo = findViewById(R.id.ivTitleLogo);
    }

    public void setSelectedFragment(Fragment selectedFragment) {
        fragmentManager.beginTransaction().replace(R.id.frame, selectedFragment).commit();
    }

}