package com.conectabike;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder> {

    private final RecyclerViewInterface recyclerViewInterface;
    Context context;
    ArrayList<LocationData> list;
    Polyline routePolyline = null;

    public HomeAdapter(Context context, ArrayList<LocationData> list, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.list = list;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new MyViewHolder(v, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        LocationData locationData = list.get(position);
        // distância
        DecimalFormat format = new DecimalFormat("0.#");
        String distanceStr = "Distância: " + format.format(locationData.getDistance())+"km";
        holder.distance.setText(distanceStr);
        // foto de usuário e email
        String email = locationData.getUser();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users");
        database.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String profilePictureUri = dataSnapshot.child("profilePictureUri").getValue(String.class);
                    Glide.with(holder.itemView.getContext())
                            .load(profilePictureUri)
                            .into(holder.profilePicture);

                    String username = dataSnapshot.child("username").getValue(String.class);
                    holder.user.setText(username);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("logdebug", "db error: " + error);
            }
        });
        // titulo
        holder.title.setText(locationData.getTitle());
        // mapa
        holder.mapView.onCreate(null);
        holder.mapView.getMapAsync(googleMap -> {
            if (googleMap != null) {

                googleMap.getUiSettings().setAllGesturesEnabled(false);

                LatLng locationOrigin = new LatLng(locationData.getOriginLat(), locationData.getOriginLng());
                LatLng locationDestination = new LatLng(locationData.getDestinationLat(), locationData.getDestinationLng());

                // Converte List<List<Double>> para List<LatLng>
                List<LatLng> points = new ArrayList<>();
                for (List<Double> point : locationData.getPoints()) {
                    if (point.size() >= 2) {
                        double latitude = point.get(0);
                        double longitude = point.get(1);
                        LatLng latLng = new LatLng(latitude, longitude);
                        points.add(latLng);
                    }
                }

                // Move a camera entre os dois marcadores
                Marker origem = googleMap.addMarker(new MarkerOptions().position(locationOrigin).title("Origem"));
                Marker destino = googleMap.addMarker(new MarkerOptions().position(locationDestination).title("Destino"));

                List<Marker> markers = new ArrayList<Marker>();
                markers.add(origem);
                markers.add(destino);

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker m : markers) {
                    builder.include(m.getPosition());
                }
                LatLngBounds bounds = builder.build();

                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,100));

                // Adiciona polylines
                new Handler(Looper.getMainLooper()).post(() -> {
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .addAll(points)
                            .color(Color.BLUE)
                            .width(5);
                    routePolyline = googleMap.addPolyline(polylineOptions);
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        MapView mapView;
        ImageView profilePicture;
        TextView title, user, distance;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            mapView = itemView.findViewById(R.id.mapContainer);
            user = itemView.findViewById(R.id.user);
            distance = itemView.findViewById(R.id.distance);
            profilePicture = itemView.findViewById(R.id.profilepicture);

            itemView.setOnClickListener(v -> {
                if (recyclerViewInterface != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        recyclerViewInterface.onItemClick(pos);

                    }
                }
            });
        }
    }
}