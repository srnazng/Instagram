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
import android.view.View;
import android.widget.ImageView;
import android.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {
    final FragmentManager fragmentManager = getSupportFragmentManager();

    // define fragments
    final Fragment homeFragment = HomeFragment.newInstance();
    final Fragment newPostFragment = NewPostFragment.newInstance();
    final Fragment profileFragment = ProfileFragment.newInstance(ParseUser.getCurrentUser());
    final Fragment settingsFragment = SettingsFragment.newInstance();

    public Fragment selectedFragment;

    private ImageView logo;
    private androidx.appcompat.widget.Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectedFragment = homeFragment;

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        toolbar = findViewById(R.id.toolbar);

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_post:
                        toolbar.setVisibility(View.VISIBLE);
                        selectedFragment = newPostFragment;
                        break;
                    case R.id.action_profile:
                        toolbar.setVisibility(View.GONE);
                        selectedFragment = profileFragment;
                        break;
                    case R.id.action_settings:
                        toolbar.setVisibility(View.VISIBLE);
                        selectedFragment = settingsFragment;
                        break;
                    default:
                        toolbar.setVisibility(View.VISIBLE);
                        selectedFragment = homeFragment;
                }
                fragmentManager.beginTransaction().replace(R.id.frame, selectedFragment).commit();
                return true;
            }
        });

        logo = findViewById(R.id.ivTitleLogo);

        ParseObject.registerSubclass(Post.class);
        ParseObject.registerSubclass(Like.class);
    }

    public void setSelectedFragment(Fragment selectedFragment) {
        fragmentManager.beginTransaction().replace(R.id.frame, selectedFragment).commit();
    }

}