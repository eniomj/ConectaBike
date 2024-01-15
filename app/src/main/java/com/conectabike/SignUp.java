package com.conectabike;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SignUp extends AppCompatActivity {
    EditText editTextPassword, editTextEmail, editTextUsername, editTextFullname;
    Button btn_register;
    TextView logintext;
    FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_cadastro);

        mAuth = FirebaseAuth.getInstance();
        editTextPassword = findViewById(R.id.password);
        editTextEmail = findViewById(R.id.email);
        editTextUsername = findViewById(R.id.username);
        editTextFullname = findViewById(R.id.fullname);
        btn_register = findViewById(R.id.btn_register);
        logintext = findViewById(R.id.logintext);

        logintext.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignIn.class);
            startActivity(intent);
            finish();
        });

        btn_register.setOnClickListener(v -> {

            String email, password, username, fullname;
            email = String.valueOf(editTextEmail.getText());
            password = String.valueOf(editTextPassword.getText());
            username = String.valueOf(editTextUsername.getText());
            fullname = String.valueOf(editTextFullname.getText());

            if(!email.equals("") && !password.equals("") && !username.equals("") && !fullname.equals("")) {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();



                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username)
                                        .setPhotoUri(Uri.parse("https://firebasestorage.googleapis.com/v0/b/pedalaamigo-e5710.appspot.com/o/user_profile_picture%2Fuser_default_profile.jpg?alt=media&token=6606af83-a1ce-4096-9949-a0175a67f0ba"))
                                        .build();

                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
                                                String uid = user.getUid();

                                                LocalDateTime now = LocalDateTime.now();
                                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
                                                String date = now.format(formatter);

                                                User user1 = new User();
                                                user1.setFullName(fullname);
                                                user1.setDate(date);
                                                user1.setEmail(email);
                                                user1.setUsername(username);
                                                user1.setProfilePictureUri("https://firebasestorage.googleapis.com/v0/b/pedalaamigo-e5710.appspot.com/o/user_profile_picture%2Fuser_default_profile.jpg?alt=media&token=6606af83-a1ce-4096-9949-a0175a67f0ba");

                                                usersRef.child(uid).setValue(user1);

                                                Toast.makeText(SignUp.this, "Conta criada com sucesso!",
                                                        Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(getApplicationContext(), SignIn.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(SignUp.this, "Erro ao criar conta.",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(SignUp.this, "Erro ao criar conta.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(SignUp.this, "É necessário preencher todos os campos.", Toast.LENGTH_SHORT).show();
            }
        }
        );
    }
}

