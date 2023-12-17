package com.conectabike;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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

public class Rota extends AppCompatActivity {

    RecyclerView recyclerView;

    Polyline routePolyline = null;
    List<LatLng> points = new ArrayList<>();
    MessageAdapter myAdapter;
    TextView messageText;
    ArrayList<String> message;
    int position;
    ArrayList<LocationData> list = new ArrayList<>();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                myAdapter = new MessageAdapter(getApplicationContext(), list.get(position).getMessage());
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
        String user = getIntent().getStringExtra("user");
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

        titleView.setText(title);
        userView.setText("Criado por: " + user.substring(0, user.indexOf("@")));
        DecimalFormat format = new DecimalFormat("0.#");
        distanceView.setText("Distância (aprox): " + format.format(distance) + "km");

        MapView map = findViewById(R.id.mapContainerRota);
        map.onCreate(null);
        map.getMapAsync(googleMap -> {
            googleMap.getUiSettings().setZoomControlsEnabled(true);

            LatLng locationOrigin = new LatLng(originLat, originLng);
            LatLng locationDestination = new LatLng(destinationlat,destinationlng);

            Marker origem = googleMap.addMarker(new MarkerOptions().position(locationOrigin).title("Origem"));
            Marker destino = googleMap.addMarker(new MarkerOptions().position(locationDestination).title("Destino"));
            // Move a camera entre os dois marcadores
            List<Marker> markers = new ArrayList<Marker>();
            markers.add(origem);
            markers.add(destino);

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker m : markers) {
                builder.include(m.getPosition());
            }

            LatLngBounds bounds = builder.build();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

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

    public void openProfile(View view) {
        //logic to open another user profile
    }

    public void sendMessage(View view) {

        messageText = findViewById(R.id.messageBox);
        String user = FirebaseAuth.getInstance().getCurrentUser().getEmail();

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
                        message.add(user.substring(0, user.indexOf("@")));

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