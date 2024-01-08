package com.conectabike;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Chat extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DrawerLayout drawerLayout;
    RecyclerView recyclerView;
    TextView messageText;
    ChatAdapter myAdapter;
    ArrayList<ChatModel> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_chat);

        // toolbar/navigation drawer
        Toolbar toolbar = findViewById(R.id.toolbar);
        ToolbarUtil.setupToolbar(this, toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        // Header email
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        TextView user_email = navigationView.getHeaderView(0).findViewById(R.id.user_email);
        assert firebaseUser != null;
        user_email.setText(firebaseUser.getEmail());

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String profilePictureUri = dataSnapshot.child("profilePictureUri").getValue(String.class);

                    // Header fullname
                    TextView fullnameHeader = navigationView.getHeaderView(0).findViewById(R.id.user_fullname);
                    fullnameHeader.setText(dataSnapshot.child("fullName").getValue(String.class));
                    // Header photo
                    ImageView profilePictureHeader = navigationView.getHeaderView(0).findViewById(R.id.profilepicture);

                    Glide.with(getApplicationContext())
                            .load(profilePictureUri)
                            .into(profilePictureHeader);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
                Log.d("logdebug", "db error: " + databaseError);
            }
        });
        // RecyclerView
        list.clear();

        recyclerView = findViewById(R.id.chat);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chat");

        myAdapter = new ChatAdapter( list);
        recyclerView.setAdapter(myAdapter);

        databaseReference.addChildEventListener(new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ChatModel chatModel = dataSnapshot.getValue(ChatModel.class);
                list.add(chatModel);
                myAdapter.notifyDataSetChanged();

                if (myAdapter.getItemCount() > 0) {
                    recyclerView.scrollToPosition(myAdapter.getItemCount() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("logdebug", "db error: " + databaseError.getMessage());
            }
        });
    }

    public void sendChatMessage(View view) {
        messageText = findViewById(R.id.messageBox);
        String email = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
        String date = now.format(formatter);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chat").push();

        HashMap<String, Object> messageMap = new HashMap<>();
        messageMap.put("message", messageText.getText().toString());
        messageMap.put("date", date);
        messageMap.put("email", email);

        databaseReference.setValue(messageMap);

        messageText.setText("");
        messageText.clearFocus();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.nav_home) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
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