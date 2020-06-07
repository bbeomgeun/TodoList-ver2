package com.example.todolist_intent;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class AddActivity extends AppCompatActivity implements LocationListener {

    String TAG = "로그 지점";

    EditText todo, latitude, longitude, radius, date, place;
    Button currentLoca, addButton;
    TextView currentLatitude, currentLongitude;
    TextView rightInput;
    TextView result;

    Intent toMainIntent;

    LocationManager lm;

    Double Geolat, Geolng, lat, lng;
    String addressLine;

    File file = new File("location.txt");   //파일생성
    FileOutputStream fos;

    Geocoder coder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // action bar 생성
        androidx.appcompat.app.ActionBar ab = getSupportActionBar();
        ab.setTitle("Add To-do List");
        setContentView(R.layout.activity_add);

        todo = findViewById(R.id.todo);
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        radius = findViewById(R.id.radius);
        date = findViewById(R.id.date);
        place = findViewById(R.id.poi);

        currentLoca = findViewById(R.id.currentLoca);
        addButton = findViewById(R.id.addButton);

        currentLatitude = findViewById(R.id.currentLatitude);
        currentLongitude = findViewById(R.id.currentLongitude);
        rightInput = findViewById(R.id.rightInput);

        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        try{
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0,this);
            // 위치정보 요청

        }catch (SecurityException e){
            e.printStackTrace();
        }

        button();

        coder = new Geocoder(this);
        result = findViewById(R.id.result);
    }

    public void mOnClick(View v) {
        List<Address> list = null;
        String address = ((EditText) findViewById(R.id.poi)).getText() // 입력된 주소를 받아서 address에 저장
                .toString();
        try {
            list = coder.getFromLocationName(address,10); // 입력된 주소(address)를 리스트에 저장
            Address Geolocation = list.get(0); //리스트에서 정보 받아와서
            addressLine = Geolocation.getAddressLine(0); //받아온 정보중에 addressline 추출
            Geolat = Geolocation.getLatitude(); //받아온 정보에서 latitude 추출
           Geolng = Geolocation.getLongitude(); // longitude 추출
            result.setText(addressLine); // 결과textview에 addressline set
            latitude.setText(Geolat+""); //위도에 검색한 위치 위도 설정 //latitude에 받은 정보 set
            longitude.setText(Geolng+""); //경도에 검색한 위치 경도 설정

        } catch (IOException e) {
            result.setText("Error: "+ e.getMessage());
            e.printStackTrace();
        }
//        if (list != null)
//            result.setText(list.get(0).toString());
//            latitude.setText(currentLatitude.getText());
//            longitude.setText(currentLongitude.getText());
    }

    public void button(){

//        이 버튼을 누르면 직접 입력하지 않아도
//         하단에 나와있는 현재의 위도와 경도로 자동으로 입력해준다.
        currentLoca.setOnClickListener(new View.OnClickListener() { //currentLoca버튼에 온클릭리스터 설정
            @Override
            public void onClick(View view) { //클릭하면
                latitude.setText(currentLatitude.getText()); // 현재 내 위치가 lat/long textview에 자동입력
                longitude.setText(currentLongitude.getText());
            }
        });

        //장소 추가 완료 버튼
        addButton.setOnClickListener(new View.OnClickListener() { // addButton 버튼에 온클릭리스터 설정
            @Override
            public void onClick(View view) {

                // 6가지(할일, 날짜, 위경도, 범위, 위치) 사항 중 하나라도 빈칸일 때,
                // 예외처리 title이랑 변수 (할일)바꾸기 또 date 추가하기
                // radius는 걍 default 값으로 줄까 생각중
                if(todo.getText().toString().equals("")
//                        || latitude.getText().toString().equals("") 위치정보는 안넣어도 가능
//                        || longitude.getText().toString().equals("")
                        || radius.getText().toString().equals("")
                        || date.getText().toString().equals(""))
                    rightInput.setVisibility(View.VISIBLE); // 입력 안될시 rightInput이라는 textview 보이게 설정
                //현재 invisible상태

                else { //main으로 내용들 intent
                    toMainIntent = getIntent();
                    toMainIntent.putExtra("todo",todo.getText().toString());
                    toMainIntent.putExtra("date",date.getText().toString());
                    toMainIntent.putExtra("latitude",latitude.getText().toString());
                    toMainIntent.putExtra("longitude",longitude.getText().toString());
                    toMainIntent.putExtra("radius",radius.getText().toString());
                    toMainIntent.putExtra("place",place.getText().toString());
                    //6가지 항목 보낸다.

                    setResult(RESULT_OK,toMainIntent);
                    finish(); // main으로 돌아가기(인텐트 종료)
                    Log.d(TAG, "addButton 완료");

                    //파일을 열어 입력된 값을 씀.
                    try  {
                        fos = openFileOutput(file.toString(), Context.MODE_APPEND);
                        fos.write(todo.getText().toString().getBytes());
                        fos.write("\n".getBytes());
                        fos.write(date.getText().toString().getBytes());
                        fos.write("\n".getBytes()); // date 추가
                        fos.write(latitude.getText().toString().getBytes());
                        fos.write("\n".getBytes());
                        fos.write(longitude.getText().toString().getBytes());
                        fos.write("\n".getBytes());
                        fos.write(radius.getText().toString().getBytes());
                        fos.write("\n".getBytes());
                        fos.write(place.getText().toString().getBytes());
                        fos.write("\n".getBytes());

                        fos.close();

                    }catch (IOException e){ // 예외처리
                        Toast.makeText(getApplicationContext(), "FileNotFound 오류", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void onLocationChanged(Location location) { // onLocationChanged로 받아서 현재 내위치가 current에 뜬다.
        lat = location.getLatitude();
        lng = location.getLongitude();
        currentLatitude.setText(lat+"");
        currentLongitude.setText(lng+"");
    }
//location listener 기본 함수들
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onProviderDisabled(String provider) {
    }

}
