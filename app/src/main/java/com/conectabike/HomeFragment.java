package com.conectabike;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements RecyclerViewInterface {

    RecyclerView recyclerView;
    Button btn_criarrota;
    DatabaseReference database;
    HomeAdapter myAdapter;
    ArrayList<LocationData> list;
    ArrayList<LocationData> listOnClick = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        btn_criarrota = view.findViewById(R.id.btn_criarrota);
        btn_criarrota.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CreateRoute.class);
            startActivity(intent);
        });
        list = new ArrayList<>();
        recyclerView = view.findViewById(R.id.rotas);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.requireContext()));

        myAdapter = new HomeAdapter(this.requireContext(), list, this);
        recyclerView.setAdapter(myAdapter);

        database = FirebaseDatabase.getInstance().getReference("Rota");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    LocationData locationData = dataSnapshot.getValue(LocationData.class);
                    list.add(locationData);
                    listOnClick.add(locationData);
                }
                myAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("logdebug", "db error: " + error);
            }
        });

    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(getContext(), Route.class);

        LocationData locationData = listOnClick.get(position);

        intent.putExtra("title", locationData.getTitle());
        intent.putExtra("distance", locationData.getDistance());
        intent.putExtra("originLat", locationData.getOriginLat());
        intent.putExtra("originLng", locationData.getOriginLng());
        intent.putExtra("destinationLat", locationData.getDestinationLat());
        intent.putExtra("destinationLng", locationData.getDestinationLng());
        intent.putExtra("user", locationData.getUser());
        intent.putExtra("position", position);

        List<List<Double>> points = locationData.getPoints();
        intent.putExtra("points", new ArrayList<>(points));
        startActivity(intent);
    }
}