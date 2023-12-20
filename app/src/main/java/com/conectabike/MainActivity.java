package com.conectabike;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    ImageView profilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        ToolbarUtil.setupToolbar(this, toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);

        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        TextView user_email = navigationView.getHeaderView(0).findViewById(R.id.user_email);
        user_email.setText(user.getEmail());

        profilePicture = navigationView.getHeaderView(0).findViewById(R.id.profilepicture);
        String photoUrl = String.valueOf(user.getPhotoUrl());
        Glide.with(this)
                .load(photoUrl)
                .into(profilePicture);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }
        if (item.getItemId() == R.id.nav_userprofile) {
            Intent intent = new Intent(getApplicationContext(), UserProfile.class);
            startActivity(intent);
            finish();
        }
        if (item.getItemId() == R.id.nav_chat) {
            Intent intent = new Intent(getApplicationContext(), Chat.class);
            startActivity(intent);
            finish();
        }
        if (item.getItemId() == R.id.nav_education) {
            Intent intent = new Intent(getApplicationContext(), Education.class);
            startActivity(intent);
            finish();
        }
        if (item.getItemId() == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}

