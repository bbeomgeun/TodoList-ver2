package com.example.todolist_intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LocationListener {

    final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    Button register, release;
    TextView rightInput;

    String TAG = "로그 지점";

    Intent registerIntent, releaseIntent;
    LocationManager lm;

    double lat, lng;


    AlertReceiver receiver;
    PendingIntent proximityIntent;

    ArrayList<String> array = new ArrayList<>();
    ArrayList<String> location = new ArrayList<>();
    ListView m_ListView;
    ArrayAdapter<String> m_Adapter;

    Intent intent;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        androidx.appcompat.app.ActionBar ab = getSupportActionBar();
        ab.setTitle("Location Alarm Service");
        setContentView(R.layout.activity_main);

        location = new ArrayList<>();
        rightInput = findViewById(R.id.rightInput);
        register = findViewById(R.id.register);
        release = findViewById(R.id.rel);

        //텍스트뷰를 3개를 만들면 삭제할 때나 추가할 때 불편해서 리스트뷰로 구현하는 방법을 택함.
        m_ListView = (ListView) findViewById(R.id.list);



        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        //런타임 퍼미션 체크
        myPermissionCheck();

        //버튼에 대한 리스터를 구현한 함수
        button();

        //이전의 경보에 대한 정보를 불러오는 함수
        readAlertFile();





    }

    @Override
    protected void onStart() {
        super.onStart();

        rightInput.setVisibility(View.INVISIBLE);
        m_Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, location);
        m_ListView.setAdapter(m_Adapter);


        //실질적으로 리시버 등록을 해주는 함수
        receiverMaker();

    }



    //런타임 퍼미션 얻기
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                } else {
                    //요청이 거부됨, 앱을 종료시킴.
                    finish();
                }

            }

        }
    }




    //버튼의 클릭에 대한 이벤트 처리 함수
    public void button(){
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //등록 버튼을 누를 때
                registerIntent = new Intent(getApplicationContext(), AddActivity.class);
                startActivityForResult(registerIntent, 100);

            }
        });
        release.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //해제 버튼을 누를 때
                //해제 버튼을 눌렀는데 아무것도 등록된 경보가 없을 시 예외처리해줌
                if(location.size() == 0){
                    rightInput.setVisibility(View.VISIBLE);
                }
                else {
                    releaseIntent = new Intent(getApplicationContext(), RemoveActivity.class);
                    releaseIntent.putExtra("locationInformationArray", array);
                    releaseIntent.putExtra("onlyLocationArray", location);
                    startActivityForResult(releaseIntent, 50);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            //추가받고 난 후 호출
            if (resultCode == RESULT_OK) {

                array.add(data.getStringExtra("title"));
                array.add(data.getStringExtra("latitude"));
                array.add(data.getStringExtra("longitude"));
                array.add(data.getStringExtra("radius"));

                location.add(data.getStringExtra("title"));
                m_Adapter.notifyDataSetChanged();
            }
        }
        //삭제받고 난 후 호출
        if (requestCode == 50) {
            if (resultCode == RESULT_OK) {

                //모든 경보 해제
                lm.removeProximityAlert(proximityIntent);

                //모든 경보에 대한 내용은 일단 다 지움.
                array.clear();
                location.clear();

                //지우지 않은 부분만으로 새로 갱신
                array = data.getStringArrayListExtra("locationInformationArray");
                location = data.getStringArrayListExtra("onlyLocationArray");

                //파일의 내용도 수정을 해줘야 한다.
                //기존의 파일 내용을 지우고 array 리스트의 내용을 읽어 새롭게 다시 쓰도록한다.(효율은 별로임)
                try {
                    //MODE_PRIVATE 을 사용하여 기존의 파일이 있어도 덮어쓴다
                    FileOutputStream fos = openFileOutput("location.txt", Context.MODE_PRIVATE);
                    for (int i = 0; i < array.size(); i++) {
                        fos.write(array.get(i).getBytes());
                        fos.write("\n".getBytes());
                    }
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //갱신된 리스트들을 이용하여 경보 재등록
                receiverMaker();

                m_Adapter.notifyDataSetChanged();
            }
        }
    }

    //리시버 등록 해주는 함수
    //리스트만 제대로 갖춰져 있으면 알아서 제값을 찾아 경보를 등록해줌.
    public void receiverMaker(){
        //리시버 등록 코드
        receiver = new AlertReceiver();
        IntentFilter filter = new IntentFilter("beomGeun");
        registerReceiver(receiver,filter);

        intent = new Intent("beomGeun");

        try {
            for(int i=0;i<location.size();i++){
                intent.putExtra("location",location.get(i));
                proximityIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
                lm.addProximityAlert(Double.parseDouble(array.get(i*4+1)),Double.parseDouble(array.get(i*4+2))
                        ,Float.parseFloat(array.get(i*4+3)),5000,proximityIntent);
            }

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    //앱이 실행될 때 런타임 퍼미션 체크하는 함수
    public void myPermissionCheck(){
        //퍼미션 코드는 교수님의 자료를 퍼와서 수정함.
        //런타임 퍼미션 체크
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // 퍼미션에 대한 설명을 해줘야하니? - 네
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                //다이어로그를 사용하여 설명해주기
            } else {

                //퍼미션에 대한 설명 필요없으면, 바로 권한 부여
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        } else {
            //허용되었을 때
            try{
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0,this);
            }catch (SecurityException e){
                e.printStackTrace();
            }
        }

    }

    //앱이 실행될 때 파일을 읽어와서 ArrayList 에 정보를 담는다.
    public void readAlertFile(){
        try{
            FileInputStream fis = openFileInput("location.txt");
            BufferedReader buffer = new BufferedReader(new InputStreamReader(fis));
            String str = buffer.readLine();

            while (str != null) {
                array.add(str);
                Log.d("리드 : ",str);
                str = buffer.readLine();
            }
            buffer.close();
        }catch (IOException e){
            Toast.makeText(getApplicationContext(),"근접 경보를 추가해보아요!!",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        //장소에 대한 내용만 location 이라는 리스트에 따로 담아 리스트뷰에 담도록 함.
        //장소의 위도, 경도, 반경 등에 대한 정보는 array 라는 리스트에 담아 경보를 등록할 때만 사용하면 됨.(메인화면에 나타내줄 필요없음)
        for(int i=0;i<array.size();i++){
            if(i%4 == 0) {
                location.add(array.get(i));
            }
        }
    }

    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onProviderDisabled(String provider) {
    }


}
