package com.conectabike;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class RotaAdapter extends RecyclerView.Adapter<RotaAdapter.MyViewHolder> {

    private final RecyclerViewInterface recyclerViewInterface;
    Context context;
    ArrayList<LocationData> list;
    Polyline routePolyline = null;

    public RotaAdapter(Context context, ArrayList<LocationData> list, RecyclerViewInterface recyclerViewInterface) {
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
        // distance
        DecimalFormat format = new DecimalFormat("0.#");
        String distanceStr = format.format(locationData.getDistance())+"km";
        holder.distance.setText(distanceStr);
        // user
        String email = locationData.getUser();
        holder.user.setText(email.substring(0, email.indexOf("@")));
        // title
        holder.title.setText(locationData.getTitle());
        // map
        holder.mapView.onCreate(null);
        holder.mapView.getMapAsync(googleMap -> {
            if (googleMap != null) {

                googleMap.getUiSettings().setAllGesturesEnabled(false);

                LatLng locationOrigin = new LatLng(locationData.getOriginLat(), locationData.getOriginLng());
                LatLng locationDestination = new LatLng(locationData.getDestinationLat(), locationData.getDestinationLng());

                // convert List<List<Double>> into List<LatLng>
                List<LatLng> points = new ArrayList<>();
                for (List<Double> point : locationData.getPoints()) {
                    if (point.size() >= 2) {
                        double latitude = point.get(0);
                        double longitude = point.get(1);
                        LatLng latLng = new LatLng(latitude, longitude);
                        points.add(latLng);
                    }
                }

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
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 60));

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
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        MapView mapView;
        TextView title, user, distance;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            mapView = itemView.findViewById(R.id.mapContainer);
            user = itemView.findViewById(R.id.user);
            distance = itemView.findViewById(R.id.distance);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recyclerViewInterface != null) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onItemClick(pos);

                        }
                    }
                }
            });
        }
    }
}