package com.conectabike;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import android.Manifest;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;

import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CriarRota extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_CODE = 101;
    private GoogleMap mMap;
    private SearchView mapSearchView;
    private Polyline routePolyline;
    Marker marker;
    TextView title;
    List<LatLng> points = new ArrayList<>();
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    ArrayList<LatLng> markerCount = new ArrayList<LatLng>();
    Long currentValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_rota);

        mapSearchView = findViewById(R.id.mapsearch);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(CriarRota.this);
        getLastLocation();
        //barra de pesquisa
        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = mapSearchView.getQuery().toString();
                List<Address> adressList = null;

                if (mapSearchView != null) {
                    Geocoder geocoder = new Geocoder(CriarRota.this);

                    try {
                        adressList = geocoder.getFromLocationName(location, 1);
                        Address adress = adressList.get(0);
                        LatLng latLng = new LatLng(adress.getLatitude(), adress.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(CriarRota.this);

                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        LatLng mlatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        //mMap.addMarker(new MarkerOptions().position(mlatLng).title("Minha Localização"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mlatLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mlatLng, 10));

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng arg0) {
                if (markerCount.size() < 2) {
                    marker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(arg0.latitude, arg0.longitude))
                            .draggable(true)
                            .visible(true));
                    markerCount.add(arg0);
                    mostrarRota();
                }
            }
        });
    }

    private void getDirections(LatLng origin, LatLng destination) {
        runOnUiThread(() -> {

            GeoApiContext context = new GeoApiContext.Builder()
                    .apiKey(BuildConfig.MAPS_API_KEY)
                    .build();

            DirectionsApiRequest request = DirectionsApi.newRequest(context)
                    .mode(TravelMode.BICYCLING)
                    .origin(new com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                    .destination(new com.google.maps.model.LatLng(destination.latitude, destination.longitude));

            request.setCallback(new PendingResult.Callback<DirectionsResult>() {
                @Override
                public void onResult(DirectionsResult result) {
                    runOnUiThread(() -> {
                        if (routePolyline != null) {
                            routePolyline.remove();
                        }

                        DirectionsRoute route = result.routes[0];
                        for (DirectionsStep step : route.legs[0].steps) {
                            points.add(new LatLng(
                                    step.startLocation.lat,
                                    step.startLocation.lng
                            ));
                        }

                        PolylineOptions polylineOptions = new PolylineOptions()
                                .addAll(points)
                                .color(Color.BLUE)
                                .width(5);

                        routePolyline = mMap.addPolyline(polylineOptions);
                    });
                }
                @Override
                public void onFailure(Throwable e) {
                    runOnUiThread(() -> {
                        Log.d("logdebug", "getDirections() onFailure: " + e);
                    });
                }
            });
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();

            } else {
                Toast.makeText(getApplicationContext(), "Permissão de localização foi negada.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void mostrarRota() {
        if (markerCount.size() == 2) {
            runOnUiThread(() -> getDirections(markerCount.get(0), markerCount.get(1)));
        }
    }

    public void clearMarkers(View view) {
        mMap.clear();
        points.clear();
        markerCount.clear();
    }



    public void salvarRota(View view) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        title = findViewById(R.id.titulo);

        if (markerCount.size() == 2 && !title.getText().toString().equals("") && points.size() > 0) {

            List<List<Double>> latLngList = new ArrayList<>();
            ArrayList<ArrayList<String>> message = new ArrayList<>();

            for (int i = 0; i < points.size(); i += 1) {
                List<Double> pointValues = new ArrayList<>();
                pointValues.add(points.get(i).latitude);
                pointValues.add(points.get(i).longitude);
                latLngList.add(pointValues);
            }

            DatabaseReference counterRef = database.getReference("Counter");
            DatabaseReference rotaRef = database.getReference("Rota");
            String key = rotaRef.push().getKey();
            counterRef.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {

                    currentValue = mutableData.getValue(Long.class);
                    if (currentValue == null) {
                        currentValue = 0L;
                    }
                    currentValue++;
                    mutableData.setValue(currentValue);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                    if (committed) {
                        LocationData locationData = new LocationData();
                        locationData.setOriginLat(markerCount.get(0).latitude);
                        locationData.setOriginLng(markerCount.get(0).longitude);
                        locationData.setDestinationLat(markerCount.get(1).latitude);
                        locationData.setDestinationLng(markerCount.get(1).longitude);
                        locationData.setTitle(title.getText().toString());
                        locationData.setPoints(latLngList);
                        locationData.setUser(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                        locationData.setDistance(distance(
                                markerCount.get(0).latitude,
                                markerCount.get(0).longitude,
                                markerCount.get(1).latitude,
                                markerCount.get(1).longitude));
                        locationData.setMessage(message);
                        locationData.setPosition(currentValue);
                        rotaRef.child(key).setValue(locationData);
                        // encerra atividade
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            });
        } else {
            Toast.makeText(this, "É preciso de origem, destino e titulo para criar uma rota.", Toast.LENGTH_SHORT).show();
        }
    }
    public static double distance(Double lt1, Double ln1, Double lt2, Double ln2) {
        double r2d = 180.0D / 3.141592653589793D;
        double d2km = 111189.57696D * r2d;
        double d2r = 3.141592653589793D / 180.0D;
        double x = lt1 * d2r;
        double y = lt2 * d2r;
        double result = Math.acos( Math.sin(x) * Math.sin(y) + Math.cos(x) * Math.cos(y) * Math.cos(d2r * (ln1 - ln2))) * d2km;
        return (int) Math.round(result / 1000);
    }
}
