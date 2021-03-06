package com.example.disastermanagement.Fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.disastermanagement.Files.Feed;
import com.example.disastermanagement.Files.GPSTracker;
import com.example.disastermanagement.Files.Resource;
import com.example.disastermanagement.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class ResourceFragment extends android.support.v4.app.Fragment {

    EditText name, loc, contact, amt, desc;
    Button subm;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    Spinner spinner1;
    SharedPreferences preferences;
    private ProgressBar progressBar;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_resource, container, false);
        name = view.findViewById(R.id.Name);
        contact = view.findViewById(R.id.PhoneNumber);
        amt = view.findViewById(R.id.Quantity);
        desc = view.findViewById(R.id.Description);
        subm = view.findViewById(R.id.submit);
        int reqcode=0;
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},reqcode);
        }
        spinner1=view.findViewById(R.id.in);
        List<String> list = new ArrayList<String>();
        list.add("Water");
        list.add("Food");
        list.add("Rescue team");
        list.add("Blankets");
        list.add("First Aid");
        list.add("Battery and torch lights");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(dataAdapter);

        firebaseAuth=FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        final FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        progressBar=view.findViewById(R.id.progressbar);
        progressBar.setVisibility(View.INVISIBLE);

        subm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int valid = 1;
                if(name.getText().toString().equalsIgnoreCase(""))
                {
                    Toast.makeText(getContext(), "Enter proper NAME", Toast.LENGTH_LONG).show();
                    valid=0;
                }
                else if(contact.getText().toString().equalsIgnoreCase(""))
                {
                    Toast.makeText(getContext(), "Enter proper CONTACT NUMBER", Toast.LENGTH_LONG).show();
                    valid=0;
                }
                else if(amt.getText().toString().equalsIgnoreCase(""))
                {
                    Toast.makeText(getContext(), "Enter proper AMOUNT", Toast.LENGTH_LONG).show();
                    valid=0;
                }
                else if(desc.getText().toString().equalsIgnoreCase(""))
                {
                    Toast.makeText(getContext(), "Enter proper DESCRIPTION", Toast.LENGTH_LONG).show();
                    valid=0;
                }
                if (valid==1) {


                    String nm = remSpace(name.getText().toString().trim());

                    String lat, lon;

                    String ph = remSpace(contact.getText().toString().trim());

                    String amount = remSpace(amt.getText().toString().trim());

                    String itm = (String) spinner1.getSelectedItem();

                    String descr = remSpace(desc.getText().toString().trim());
                    GPSTracker gpsTracker = new GPSTracker(getContext());
                    double latitude = 0.0, longitude = 0.0;

                    // check if GPS enabled
                    if (gpsTracker.canGetLocation()) {

                        latitude = gpsTracker.getLatitude();
                        longitude = gpsTracker.getLongitude();

                    } else {
                        // can't get location
                        // GPS or Network is not enabled
                        // Ask user to enable GPS/network in settings
                        gpsTracker.showSettingsAlert();
                    }
                    String area=get_city(latitude,longitude);
                    lat = latitude + "";
                    lon = longitude + "";


                    SimpleDateFormat s = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
                    String format = s.format(new Date());
                    if (isNetworkAvailable()) {
                        Resource data = new Resource(amount, descr, itm, nm, lat, lon, ph, area, format);
                        Toast.makeText(getContext(), data.toString(), Toast.LENGTH_LONG).show();
                        Log.i("data", data.toString());
                        databaseReference.child("Resources").child(firebaseUser.getUid()).push().setValue(data);
                    }else{
                        preferences=getContext().getSharedPreferences("GoogleInfo",MODE_PRIVATE);
                        String id=preferences.getString("personID","");
                        String msg="Resource:"+amount+":"+descr+":"+itm+":"+nm+":"+lat+":"+lon+":"+ph+":"+area+":"+format+":"+id;
                        progressBar.setVisibility(View.VISIBLE);


                        if( sendSMS("8082191919",msg)){
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(getContext(),"SMS sent",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(getContext(),"SMS NO",Toast.LENGTH_SHORT).show();
                        }
                    }
                    name.setText("");
                    contact.setText("");
                    amt.setText("");
                    desc.setText("");
                }
                /*if(valid==1) {
                    new ResourceSubmit(getApplicationContext()).execute(message);

                }*/


            }
        });

        return view;
    }
    private boolean sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        try {

            sms.sendTextMessage(phoneNumber, null, message, null, null);
            return true;

        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public String get_city(double lat, double lng) {
        String add="";
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
//            add = obj.getAddressLine(0);
//            add = add + " " + obj.getCountryName();
//            add = add + " " + obj.getCountryCode();
//            add = add + " " + obj.getAdminArea();
//            add = add + " " + obj.getPostalCode();
//            add = add + " " + obj.getSubAdminArea();
            add = add + " " + obj.getLocality();
//            add = add + " " + obj.getSubThoroughfare();

            Log.v("IGA", "Address" + add);
            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();

            // TennisAppActivity.showDialog(add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return add;
    }

    public String remSpace(String old)
    {
        String new1="";
        while(old.contains(" "))
        {
            int ind=old.indexOf(" ");
            old = old.substring(0,ind)+"."+old.substring(ind+1);
        }

        new1 = old;
        return new1;
    }



}
