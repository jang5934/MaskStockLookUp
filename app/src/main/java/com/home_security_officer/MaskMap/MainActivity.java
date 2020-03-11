package com.home_security_officer.MaskMap;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker mMarker;
    private int zoom_level;

    private static final int REQUEST_ACCESS_LOCATION = 100;

    private double current_latitude;
    private double current_longitude;
    private int searching_range = 1500;

    private int REQUEST_FORCE_LOCATION_SETTING = 1;
    private int REQUEST_RANGE_SETTING = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("GPS 권한 설정");
                builder.setMessage("현재 위치를 기준으로 마스크 재고현황을 파악하므로 \n" +
                        "빠르게 현황을 파악하기 위해 GPS 권한을 설정해 주세요.");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_ACCESS_LOCATION);
                    }
                });
                builder.show();
        }
        else
            startupProcess();

        ActionBar actionBar = getSupportActionBar() ;
        actionBar.setTitle("마스크 재고 현황 지도");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.address_search_activity :
                intent = new Intent(MainActivity.this, ForceLocationSettingActivity.class);
                intent.putExtra("lat", Double.toString(current_latitude));
                intent.putExtra("lng", Double.toString(current_longitude));
                startActivityForResult(intent, REQUEST_FORCE_LOCATION_SETTING);
                break;
            case R.id.setting_activity :
                intent = new Intent(MainActivity.this, SettingActivity.class);
                intent.putExtra("range", Integer.toString(searching_range));
                startActivityForResult(intent, REQUEST_RANGE_SETTING);
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request it is that we're responding to

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_FORCE_LOCATION_SETTING) {
            if (resultCode == RESULT_OK) {
                current_latitude = Double.parseDouble(data.getStringExtra("lat"));
                current_longitude = Double.parseDouble(data.getStringExtra("lng"));
                addMarker(current_latitude, current_longitude);
            } else {   // RESULT_CANCEL
                Toast.makeText(MainActivity.this, "선택 취소되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == REQUEST_RANGE_SETTING) {
            if (resultCode == RESULT_OK) {
                searching_range = Integer.parseInt(data.getStringExtra("range"));
            } else {   // RESULT_CANCEL
                Toast.makeText(MainActivity.this, "기존 설정된 범위로 검색합니다.", Toast.LENGTH_SHORT).show();
            }
        }

        mMap.clear();
        RequestData requestData = new RequestData((float)current_latitude, (float)current_longitude, searching_range);
        try {
            ArrayList<Seller> sellers = requestData.execute().get();
            Iterator<Seller> it = sellers.iterator();

            while(it.hasNext()) {
                Seller temp_obj = it.next();
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(temp_obj.getLat(), temp_obj.getLng()));
                markerOptions.alpha(0.7f);

                markerOptions.title(temp_obj.getName() + "(재고:" + transferStatus(temp_obj.getRemain_stat()) + ")");
                markerOptions.snippet(temp_obj.getAddr());

                if(temp_obj.getRemain_stat().equals("plenty"))
                    markerOptions.icon(bitmapDescriptorFromVector(this, R.drawable.ic_location_on_plenty_24dp));
                else if(temp_obj.getRemain_stat().equals("some"))
                    markerOptions.icon(bitmapDescriptorFromVector(this, R.drawable.ic_location_on_some_24dp));
                else if(temp_obj.getRemain_stat().equals("few"))
                    markerOptions.icon(bitmapDescriptorFromVector(this, R.drawable.ic_location_on_few_24dp));
                else
                    markerOptions.icon(bitmapDescriptorFromVector(this, R.drawable.ic_location_on_empty_24dp));

                mMap.addMarker(markerOptions);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        addMarker(current_latitude, current_longitude);
    }

    public String transferStatus(String eng) {
        if(eng.equals("plenty"))
            return "넉넉";
        else if(eng.equals("some"))
            return "적당";
        else if(eng.equals("few"))
            return "얼마없음";
        else
            return "없음";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_LOCATION: {
                if (grantResults.length == 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("GPS 권한 설정");
                    builder.setMessage("위치 조회 권한이 없어 현재 위치를 찾을 수 없습니다.\n" +
                            " 현재 위치를 직접 설정해주세요.");
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // 설정 액티비티로 넘어감
                        }
                    });
                    builder.show();
                } else {
                    startupProcess();
                }
                return;
            }
        }
    }

    public void startupProcess() {
        Calendar cal = Calendar.getInstance();
        String strWeek[] = {"일요일", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일"};
        String yearOfBirth[] = {"1, 6", "2, 7", "3, 8", "4, 9", "5, 0"};
        String confirmMessage = "마스크 구입 전에 꼭 참고해 주시기 바랍니다. \n ex) 1901년 생의 경우 평일 중 월요일에 구매 가능, 토요일, 일요일은 제한없음";
        int nWeek = cal.get(Calendar.DAY_OF_WEEK);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("오늘은 '" + strWeek[nWeek - 1] + "' 입니다.");
        if (nWeek > 1 && nWeek < 7) {
            builder.setMessage("생년월일의 끝자리가 " + yearOfBirth[nWeek - 2] + "인 분들이 마스크를 구입하실 수 있는 날입니다. \n" +
                    confirmMessage);
        } else {
            builder.setMessage("생년월일의 끝자리에 상관없이 마스크를 구입하실 수 있는 날입니다. \n" +
                    confirmMessage);
        }
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(MainActivity.this);
            }
        });
        builder.show();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(new Criteria(), true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("GPS 권한 설정");
                builder.setMessage("위치 조회 권한이 없어 현재 위치를 찾을 수 없습니다.\n" +
                        " 현재 위치를 직접 설정해주세요.");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.this, ForceLocationSettingActivity.class);
                        startActivityForResult(intent, REQUEST_FORCE_LOCATION_SETTING);
                    }
                });
                builder.show();
            }
        }
        Location currentLocation = locationManager.getLastKnownLocation(provider);

        if(currentLocation == null) {
            if(provider.equals(LocationManager.NETWORK_PROVIDER))
                currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            else if(provider.equals(LocationManager.GPS_PROVIDER))
                currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("위치 조회 실패");
                builder.setMessage("GPS 위치 조회에 문제가 있어 주소를 직접 입력해 주셔야 합니다.");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 설정 액티비티로 넘어감
                    }
                });
                builder.show();
            }
        }
        mMap = googleMap;
        current_latitude = currentLocation.getLatitude();
        current_longitude = currentLocation.getLongitude();

        RequestData requestData = new RequestData((float)current_latitude, (float)current_longitude, searching_range);
        try {
            ArrayList<Seller> sellers = requestData.execute().get();
            Iterator<Seller> it = sellers.iterator();

            while(it.hasNext()) {
                Seller temp_obj = it.next();
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(temp_obj.getLat(), temp_obj.getLng()));
                markerOptions.alpha(0.7f);
                markerOptions.title(temp_obj.getName() + "(재고:" + transferStatus(temp_obj.getRemain_stat()) + ")");
                markerOptions.snippet(temp_obj.getAddr());

                if(temp_obj.getRemain_stat().equals("plenty"))
                    markerOptions.icon(bitmapDescriptorFromVector(this, R.drawable.ic_location_on_plenty_24dp));
                else if(temp_obj.getRemain_stat().equals("some"))
                    markerOptions.icon(bitmapDescriptorFromVector(this, R.drawable.ic_location_on_some_24dp));
                else if(temp_obj.getRemain_stat().equals("few"))
                    markerOptions.icon(bitmapDescriptorFromVector(this, R.drawable.ic_location_on_few_24dp));
                else
                    markerOptions.icon(bitmapDescriptorFromVector(this, R.drawable.ic_location_on_empty_24dp));

                mMap.addMarker(markerOptions);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        addMarker(current_latitude, current_longitude);
    }

    private void addMarker(double lat, double lng) {
        if(mMarker != null)
            mMarker.remove();
        List<Address> addressList = null;
        try {
            addressList = new Geocoder(this).getFromLocation(
                    lat, lng, 10);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        String []splitStr = addressList.get(0).toString().split(",");
        String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1,splitStr[0].length() - 2);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(lat, lng));
        markerOptions.alpha(0.5f);
        markerOptions.title("현재위치");
        markerOptions.snippet(address);
        mMarker = mMap.addMarker(markerOptions);

        mMarker.showInfoWindow();

        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(current_latitude, current_longitude)));
        setZoomLevel();
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom_level));
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void setZoomLevel() {
        int lv_cnt = 1;
        int half_distance = 12288000;
        while(half_distance > searching_range) {
            lv_cnt += 1;
            half_distance /= 2;
        }
        zoom_level = lv_cnt;
    }
}
