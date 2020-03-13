package com.home_security_officer.MaskMap;

import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class ForceLocationSettingActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private Geocoder geocoder;

    private Button search_button;
    private EditText address_edit;

    private double lat = 0.0;
    private double lng = 0.0;

    private MarkerOptions mOptions;
    private Marker mMarker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_force_location_setting);
        address_edit = (EditText) findViewById(R.id.address_edit_text);
        search_button = (Button)findViewById(R.id.address_search_button);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.force_search_map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        if(intent.getStringExtra("lat") == null || intent.getStringExtra("lng") == null) {
            lat = 37.5049033;
            lng = 127.0047928;
            // 기본주소는 서울
        } else {
            String temp = intent.getStringExtra("lat");
            lat = Double.parseDouble(intent.getStringExtra("lat"));
            lng = Double.parseDouble(intent.getStringExtra("lng"));
        }
        mOptions = new MarkerOptions();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        geocoder = new Geocoder(this);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
            @Override
            public void onMapClick(LatLng point) {
                lat = point.latitude;
                lng = point.longitude;

                List<Address> addressList = null;
                try {
                    addressList = geocoder.getFromLocation(
                            lat, lng, 10);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                String []splitStr = addressList.get(0).toString().split(",");
                String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1,splitStr[0].length() - 2);
                addMarker(mOptions, address, new LatLng(lat, lng));
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                setConfirmButton(marker);
            }
        });

        search_button.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                String str = address_edit.getText().toString();

                if(str.length() == 0) {
                    Toast.makeText(ForceLocationSettingActivity.this, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<Address> addressList = null;
                try {
                    addressList = geocoder.getFromLocationName(
                            str, 10);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                if(addressList.size() == 0) {
                    Toast.makeText(ForceLocationSettingActivity.this, "입력된 주소를 찾을 수 없습니다.\n" +
                            " 도로명 주소로 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
                else {
                    String[] splitStr = addressList.get(0).toString().split(",");
                    String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1, splitStr[0].length() - 2);
                    String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1);
                    String longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1);
                    LatLng tempLatlng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                    lat = tempLatlng.latitude;
                    lng = tempLatlng.longitude;
                    addMarker(mOptions, address, tempLatlng);
                }
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });

        LatLng currLoc = new LatLng(lat, lng);

        List<Address> addressList = null;
        try {
            addressList = geocoder.getFromLocation(
                    currLoc.latitude, currLoc.longitude, 10);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        String []splitStr = addressList.get(0).toString().split(",");
        String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1,splitStr[0].length() - 2);
        addMarker(mOptions, address, currLoc);
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        setConfirmButton(marker);
        return true;
    }

    public final void addMarker(MarkerOptions options, String address, LatLng point) {
        if(mMarker != null)
            mMarker.remove();

        options.title("검색된 위치").snippet(address).position(point);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point,17));
        mMarker = mMap.addMarker(options);
        mMarker.showInfoWindow();
    }

    public void setConfirmButton(Marker marker) {
        mMarker.showInfoWindow();
        RelativeLayout relativeLayout = findViewById(R.id.force_location_setting_layout);

        if(relativeLayout.findViewWithTag("confirm_current_location_button") == null) {
            Button setCurrentLocationButton = new Button(ForceLocationSettingActivity.this);
            setCurrentLocationButton.setTag("confirm_current_location_button");
            setCurrentLocationButton.setText("이 위치로 검색하기");

            GradientDrawable gd = new GradientDrawable();
            gd.setColor(0xFFFFFFFF); // Changes this drawbale to use a single color instead of a gradient
            gd.setCornerRadius(3);
            gd.setStroke(5, 0xFFd8d8d8);

            setCurrentLocationButton.setBackground(gd);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

            setCurrentLocationButton.setLayoutParams(layoutParams);


            setCurrentLocationButton.setOnClickListener(new Button.OnClickListener(){
                @Override
                public void onClick(View v){
                    Intent intent = new Intent();
                    intent.putExtra("lat", Double.toString(lat));
                    intent.putExtra("lng", Double.toString(lng));
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });

            relativeLayout.addView(setCurrentLocationButton);
        }
    }
}