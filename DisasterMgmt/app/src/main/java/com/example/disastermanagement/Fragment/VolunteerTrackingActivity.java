package com.example.disastermanagement.Fragment;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.example.disastermanagement.Files.Register_Volunteer;
import com.example.disastermanagement.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VolunteerTrackingActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.volunteer_tracking_activity);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        databaseReference= FirebaseDatabase.getInstance().getReference("Volunteer");

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

//hello
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(17, 78);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        //mMap.animateCamera(CameraUpdateFactory.newLatLng(sydney,8.0f));
//<<<<<<< HEAD:DisasterMgmt/app/src/main/java/com/example/disastermanagement/VolunteerTrackingActivity.java
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                mMap.clear();
//                System.out.println(dataSnapshot.getValue().toString());
//                for(DataSnapshot users:dataSnapshot.getChildren()){
//                    Register_Volunteer volunteer=users.getValue(Register_Volunteer.class);
//                    mMap.clear();
//                    LatLng a=new LatLng(Double.parseDouble(volunteer.getLat()),Double.parseDouble(volunteer.getLng()));
//                    mMap.addMarker(new MarkerOptions().position(a).title(volunteer.getEmail()));
//                    mMap.moveCamera(CameraUpdateFactory.newLatLng(a));
//====
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 8.0f));
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMap.clear();
                System.out.println(dataSnapshot.getValue().toString());
                for(DataSnapshot users:dataSnapshot.getChildren()){
                    Register_Volunteer volunteer=users.getValue(Register_Volunteer.class);
                    System.out.println(users.getValue().toString());
                    LatLng a=new LatLng(Double.parseDouble(volunteer.lat),Double.parseDouble(volunteer.lng));
                    mMap.addMarker(new MarkerOptions().position(a).title(volunteer.getEmail()));
                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(a));

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println(databaseError.getMessage());

            }
        });
    }
}
