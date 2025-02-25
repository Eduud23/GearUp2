package com.example.gearup;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ShopPinLocation extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double shopLat, shopLon;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng userLocation;
    private TextView distanceText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_pin_location);

        distanceText = findViewById(R.id.distance_text);
        Button goNowButton = findViewById(R.id.go_now_button);

        Intent intent = getIntent();
        if (intent != null) {
            shopLat = intent.getDoubleExtra("latitude", 0.0);
            shopLon = intent.getDoubleExtra("longitude", 0.0);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        goNowButton.setOnClickListener(v -> getCurrentLocation());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        LatLng sellerLocation = new LatLng(shopLat, shopLon);
        mMap.addMarker(new MarkerOptions().position(sellerLocation).title("Seller's Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sellerLocation, 15));
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        Task<Location> locationTask = fusedLocationClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
                    drawRoute(userLocation, new LatLng(shopLat, shopLon));
                }
            }
        });
    }

    private void drawRoute(LatLng start, LatLng end) {
        String url = "https://router.project-osrm.org/route/v1/driving/"
                + start.longitude + "," + start.latitude + ";"
                + end.longitude + "," + end.latitude + "?overview=full&geometries=geojson";

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray routes = response.getJSONArray("routes");
                        if (routes.length() > 0) {
                            JSONObject route = routes.getJSONObject(0);
                            JSONObject geometry = route.getJSONObject("geometry");
                            JSONArray coordinates = geometry.getJSONArray("coordinates");

                            // Extract distance (in meters)
                            double distanceMeters = route.getDouble("distance");
                            double distanceKm = distanceMeters / 1000.0; // Convert to km

                            // Update UI with distance
                            runOnUiThread(() -> distanceText.setText("Distance: " + String.format("%.2f", distanceKm) + " km"));

                            List<LatLng> points = new ArrayList<>();
                            for (int i = 0; i < coordinates.length(); i++) {
                                JSONArray coord = coordinates.getJSONArray(i);
                                double lon = coord.getDouble(0);
                                double lat = coord.getDouble(1);
                                points.add(new LatLng(lat, lon));
                            }

                            drawPolylineOnMap(points);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> error.printStackTrace());

        queue.add(request);
    }

    private void drawPolylineOnMap(List<LatLng> points) {
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(points)
                .color(Color.BLUE)
                .width(10);

        mMap.addPolyline(polylineOptions);

        // Adjust camera to fit the route
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : points) {
            builder.include(point);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
    }



}