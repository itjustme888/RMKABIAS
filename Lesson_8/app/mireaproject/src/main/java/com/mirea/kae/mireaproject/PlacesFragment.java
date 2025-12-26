package com.mirea.kae.mireaproject;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class PlacesFragment extends Fragment {

    // Внутренний класс для данных
    public static class Place {
        private String name;
        private String description;
        private String address;
        private double latitude;
        private double longitude;

        public Place(String name, String description, String address,
                     double latitude, double longitude) {
            this.name = name;
            this.description = description;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getAddress() { return address; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
    }

    // Поля класса
    private MapView mapView;
    private RecyclerView recyclerView;
    private Button showListButton, showMapButton;
    private List<Place> placesList = new ArrayList<>();
    private static final int REQUEST_LOCATION_PERMISSION = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_places, container, false);

        // Находим элементы
        recyclerView = view.findViewById(R.id.placesRecyclerView);
        showListButton = view.findViewById(R.id.showListButton);
        showMapButton = view.findViewById(R.id.showMapButton);
        mapView = view.findViewById(R.id.mapView);

        initPlacesList();
        setupRecyclerView();
        setupMap();
        checkLocationPermission();

        // Обработчики кнопок
        showMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                addMarkersToMap();
            }
        });

        showListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    private void initPlacesList() {
        placesList.clear();
        placesList.add(new Place("МИРЭА", "Университет", "Вернадского 78", 55.670005, 37.479894));
        placesList.add(new Place("Кафе", "Кофе и выпечка", "Строителей 15", 55.675432, 37.485123));
        placesList.add(new Place("Библиотека", "Книги", "Ленинский 125", 55.683210, 37.492345));
    }

    private void setupRecyclerView() {
        PlacesAdapter adapter = new PlacesAdapter(placesList, new PlacesAdapter.OnPlaceClickListener() {
            @Override
            public void onPlaceClick(Place place) {
                mapView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);

                IMapController controller = mapView.getController();
                controller.setZoom(18.0);
                controller.setCenter(new GeoPoint(place.getLatitude(), place.getLongitude()));

                showSingleMarker(place);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupMap() {
        Configuration.getInstance().load(getContext(),
                androidx.preference.PreferenceManager.getDefaultSharedPreferences(getContext()));

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        IMapController controller = mapView.getController();
        controller.setZoom(15.0);
        controller.setCenter(new GeoPoint(55.670005, 37.479894));
    }

    private void addMarkersToMap() {
        mapView.getOverlays().clear();

        for (final Place place : placesList) {
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(place.getLatitude(), place.getLongitude()));
            marker.setTitle(place.getName());

            marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {
                    Toast.makeText(getContext(),
                            place.getName() + "\n" + place.getAddress(),
                            Toast.LENGTH_LONG).show();
                    return true;
                }
            });

            mapView.getOverlays().add(marker);
        }

        mapView.invalidate();
    }

    private void showSingleMarker(Place place) {
        mapView.getOverlays().clear();

        Marker marker = new Marker(mapView);
        marker.setPosition(new GeoPoint(place.getLatitude(), place.getLongitude()));
        marker.setTitle(place.getName());

        marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                Toast.makeText(getContext(),
                        place.getName() + "\n" + place.getAddress() + "\n" + place.getDescription(),
                        Toast.LENGTH_LONG).show();
                return true;
            }
        });

        mapView.getOverlays().add(marker);
        mapView.invalidate();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }
}