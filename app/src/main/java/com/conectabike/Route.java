package com.conectabike;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Route extends AppCompatActivity {

    RecyclerView recyclerView;
    String user;
    Polyline routePolyline = null;
    List<LatLng> points = new ArrayList<>();
    RouteAdapter myAdapter;
    TextView messageText;
    ArrayList<String> message;
    int position;
    ArrayList<LocationData> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_rota);

        list.clear();

        recyclerView = findViewById(R.id.messages);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DatabaseReference databaseAdapter = FirebaseDatabase.getInstance().getReference("Rota");
        databaseAdapter.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    LocationData locationData = dataSnapshot.getValue(LocationData.class);
                    list.add(locationData);
                }
                position = getIntent().getIntExtra("position", 0);
                myAdapter = new RouteAdapter(getApplicationContext(), list.get(position).getMessage());
                recyclerView.setAdapter(myAdapter);
                myAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("logdebug", "db error: " + error);
            }
        });

        String title = getIntent().getStringExtra("title");
        Double distance = getIntent().getDoubleExtra("distance", 0);
        Double originLat = getIntent().getDoubleExtra("originLat", 0);
        Double originLng = getIntent().getDoubleExtra("originLng", 0);
        Double destinationlat = getIntent().getDoubleExtra("destinationLat", 0);
        Double destinationlng = getIntent().getDoubleExtra("destinationLng", 0);
        user = getIntent().getStringExtra("user");
        Serializable pointsSerialized = getIntent().getSerializableExtra("points");

        if (pointsSerialized != null && pointsSerialized instanceof List) {
            List<List<Double>> pointsList = (List<List<Double>>) pointsSerialized;

            for (List<Double> point : pointsList) {
                if (point.size() >= 2) {
                    double latitude = point.get(0);
                    double longitude = point.get(1);
                    LatLng latLng = new LatLng(latitude, longitude);
                    points.add(latLng);
                }
            }
        }

        TextView titleView = findViewById(R.id.titleRota);
        TextView userView = findViewById(R.id.userRota);
        TextView distanceView = findViewById(R.id.distanceRota);
        ImageView profilePicture = findViewById(R.id.profilepicture);

        titleView.setText(title);
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users");
        database.orderByChild("email").equalTo(user).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String profilePictureUri = dataSnapshot.child("profilePictureUri").getValue(String.class);
                    Glide.with(getApplicationContext())
                            .load(profilePictureUri)
                            .into(profilePicture);

                    String username = dataSnapshot.child("username").getValue(String.class);
                    userView.setText("Criado por: " + username);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("logdebug", "db error: " + error);
            }
        });
        DecimalFormat format = new DecimalFormat("0.#");
        distanceView.setText("Distância (aprox): " + format.format(distance) + "km");

        MapView map = findViewById(R.id.mapContainerRota);
        map.onCreate(null);
        map.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                map.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                map.getMapAsync(googleMap -> {
                    googleMap.getUiSettings().setZoomControlsEnabled(true);

                    LatLng locationOrigin = new LatLng(originLat, originLng);
                    LatLng locationDestination = new LatLng(destinationlat,destinationlng);

                    Marker origem = googleMap.addMarker(new MarkerOptions().position(locationOrigin).title("Origem"));
                    Marker destino = googleMap.addMarker(new MarkerOptions().position(locationDestination).title("Destino"));
                    origem.showInfoWindow();
                    // Move a camera entre os dois marcadores
                    List<Marker> markers = new ArrayList<Marker>();
                    markers.add(origem);
                    markers.add(destino);

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (Marker m : markers) {
                        builder.include(m.getPosition());
                    }

                    LatLngBounds bounds = builder.build();
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));

                    new Handler(Looper.getMainLooper()).post(new Runnable(){
                        @Override
                        public void run() {
                            PolylineOptions polylineOptions = new PolylineOptions()
                                    .addAll(points)
                                    .color(Color.BLUE)
                                    .width(5);
                            routePolyline = googleMap.addPolyline(polylineOptions);
                        }
                    });
                });
            }
        });
    }

    public void openProfile(View view) {
        // logic to open another user profile
        // get uid by comparing email from this activity (String user) with email from database
        // key from the database is a uid
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users");
        database.orderByChild("email").equalTo(user).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String userID = dataSnapshot.getKey();
                    Intent intent = new Intent(getApplicationContext(), UserProfile.class);
                    intent.putExtra("userID", userID);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("logdebug", "db error: " + error);
            }
        });
    }

    public void sendMessage(View view) {
        messageText = findViewById(R.id.messageBox);
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
        String date = now.format(formatter);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Rota");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Long databasePosition = childSnapshot.child("position").getValue(Long.class);
                    if (databasePosition != null && databasePosition.equals((long) position + 1)) {

                        message = new ArrayList<>();
                        message.add(messageText.getText().toString());
                        message.add(date);
                        message.add(email);

                        DatabaseReference messagesRef = childSnapshot.getRef().child("message");
                        messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot messageSnapshot) {
                                if (messageSnapshot.exists()) {
                                    ArrayList<ArrayList<String>> existingMessages = (ArrayList<ArrayList<String>>) messageSnapshot.getValue();
                                    existingMessages.add(message);
                                    messagesRef.setValue(existingMessages);
                                    list.get(position).setMessage(existingMessages);
                                } else {
                                    ArrayList<ArrayList<String>> initialMessages = new ArrayList<>();
                                    initialMessages.add(message);
                                    messagesRef.setValue(initialMessages);
                                    list.get(position).setMessage(initialMessages);
                                }

                                runOnUiThread(() -> {
                                    myAdapter.notifyDataSetChanged();
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d("logdebug", "db error: " + error);
                            }
                        });
                        messageText.setText("");
                        messageText.clearFocus();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("logdebug", "db error: " + databaseError);
            }
        });
    }
}