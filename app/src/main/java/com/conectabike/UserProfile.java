package com.conectabike;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UserProfile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int PICK_IMAGE_REQUEST = 1;
    Boolean isCurrentUserProfile = false;
    TextView username, profileUsername, profileEmail, accountAge, titleName, profileName;
    FirebaseAuth firebaseAuth;
    ImageView profilePicture, profilePictureHeader;
    DrawerLayout drawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_user_profile);

        // toolbar/navigation drawer
        Toolbar toolbar = findViewById(R.id.toolbarEducation);
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

        firebaseAuth = FirebaseAuth.getInstance();

        // Header email
        TextView user_email = navigationView.getHeaderView(0).findViewById(R.id.user_email);
        user_email.setText(firebaseAuth.getCurrentUser().getEmail());

        // Header photo
        profilePictureHeader = navigationView.getHeaderView(0).findViewById(R.id.profilepicture);
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        String uid = firebaseAuth.getCurrentUser().getUid();

        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String profilePictureUri = dataSnapshot.child("profilePictureUri").getValue(String.class);

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

        // Carrega os dados do perfil
        String userId = getIntent().getStringExtra("userID");
        if (userId != null) {
            fetchAndDisplayUserData(userId);
        } else {
            isCurrentUserProfile = true;
            fetchAndDisplayUserData(firebaseAuth.getCurrentUser().getUid());
        }

        // Botão para mudar foto de usuário
        float scale = getResources().getDisplayMetrics().density;
        int buttonSize = (int) (55 * scale + 0.5f);

        MaterialButton changeProfilePictureButton = new MaterialButton(this);
        changeProfilePictureButton.setStateListAnimator(null);
        changeProfilePictureButton.setElevation(20);
        changeProfilePictureButton.setLayoutParams(new ConstraintLayout.LayoutParams(buttonSize, buttonSize));
        changeProfilePictureButton.setPadding(0, 0, 0, 0);
        changeProfilePictureButton.setIconSize((int) (30 * scale + 0.5f));
        changeProfilePictureButton.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
        changeProfilePictureButton.setIconResource(R.drawable.camera_plus);
        changeProfilePictureButton.setIconPadding(0);
        changeProfilePictureButton.setId(View.generateViewId());

        changeProfilePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeProfilePicture(view);
            }
        });

        // Adiciona botão para o layout
        CardView profilePictureCard = findViewById(R.id.profilepicturecard);
        ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout);

        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                buttonSize,
                buttonSize
        );
        layoutParams.endToEnd = profilePictureCard.getId();
        layoutParams.bottomToBottom = profilePictureCard.getId();

        if (isCurrentUserProfile) {
            changeProfilePictureButton.setVisibility(View.VISIBLE);
        } else {
            changeProfilePictureButton.setVisibility(View.GONE);
        }

        constraintLayout.addView(changeProfilePictureButton, layoutParams);
    }

    private void fetchAndDisplayUserData(String userId) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        username = findViewById(R.id.username);
        profileUsername = findViewById(R.id.profileUsername);
        profilePicture = findViewById(R.id.profilepicture);
        profileEmail = findViewById(R.id.profileEmail);
        accountAge = findViewById(R.id.accountAge);
        titleName = findViewById(R.id.titleName);
        profileName = findViewById(R.id.profileName);

        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        username.setText(user.getUsername());
                        profileUsername.setText(user.getUsername());

                        String photoUrl = user.getProfilePictureUri();
                        Glide.with(UserProfile.this)
                                .load(photoUrl)
                                .into(profilePicture);

                        profileEmail.setText(user.getEmail());
                        profileName.setText(user.getFullName());
                        titleName.setText(user.getFullName());
                        accountAge.setText(user.getDate());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("logdebug", "db error: " + databaseError);
            }
        });
    }

    public void changeProfilePicture(View view) {
        // Abre galeria
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            updateProfilePicture(selectedImageUri);
        }
    }

    private void updateProfilePicture(Uri photoUri) {
        FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
        // Salva a foto da galeria para o Firebase Storage
        if (userAuth != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child("user_profile_picture")
                    .child(userAuth.getUid() + ".jpg");

            storageRef.putFile(photoUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(Uri.parse(downloadUrl))
                                    .build();

                            userAuth.updateProfile(profileUpdates)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Glide.with(this)
                                                    .load(photoUri)
                                                    .into(profilePicture);

                                            Glide.with(this)
                                                    .load(photoUri)
                                                    .into(profilePictureHeader);
                                            // Salva o link da foto pro banco de dados
                                            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
                                            usersRef.child(userAuth.getUid())
                                                    .child("profilePictureUri")
                                                    .setValue(downloadUrl);
                                        } else {
                                            Toast.makeText(UserProfile.this, "Falha ao atualizar foto de usuário", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.d("logdebug", "storage error: " + e);
                    });
        }
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