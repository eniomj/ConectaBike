package com.conectabike;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
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

public class Login extends AppCompatActivity {

    EditText EditTextPassword, EditTextEmail;
    Button btn_login;
    TextView textregister;
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
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        EditTextPassword = findViewById(R.id.password);
        EditTextEmail = findViewById(R.id.email);
        textregister = findViewById(R.id.textregister);
        btn_login = findViewById(R.id.btn_login);

        textregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Cadastro.class);
                startActivity(intent);
                finish();
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

              String email, password;
              email = String.valueOf(EditTextEmail.getText());
              password = String.valueOf(EditTextPassword.getText());

              if(!email.equals("") && !password.equals("")) {
                  mAuth.signInWithEmailAndPassword(email, password)
                          .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                              @Override
                              public void onComplete(@NonNull Task<AuthResult> task) {
                                  if (task.isSuccessful()) {
                                      Toast.makeText(Login.this, "Login bem sucedido.",
                                              Toast.LENGTH_SHORT).show();
                                      Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                      startActivity(intent);
                                      finish();
                                  } else {
                                      Toast.makeText(Login.this, "Login falhou.",
                                              Toast.LENGTH_SHORT).show();
                                  }
                              }
                          });
              } else {
                  Toast.makeText(Login.this, "É necessário preencher todos os campos.", Toast.LENGTH_SHORT).show();
              }
          }
      }
        );}

}
