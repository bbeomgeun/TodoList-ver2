package com.example.todolist_intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.FragmentManager;
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

public class MainActivity extends AppCompatActivity{

    private FragmentManager fragmentManager = getSupportFragmentManager();

    private fragment_main fragmentMain = new fragment_main();
    private fragment_menu2 fragmentMenu2 = new fragment_menu2();
    private fragment_menu3 fragmentMenu3 = new fragment_menu3();
    private fragment_menu4 fragmentMenu4 = new fragment_menu4();


    final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    Button register, release;
    TextView rightInput;

    String TAG = "로그 지점";

    Intent registerIntent, releaseIntent;
    LocationManager lm;

    double lat, lng;

    AlertReceiver receiver;
    PendingIntent proximityIntent;
    // 특정 지역에 가까워지면 알려줄 수 있는 기능은 기본적으로 위치정보를 이용하게 되며
    // 이 기능을 근접 경보(Proximity Alarm)라 부름
    // 안드로이드에서는 이 기능을 손쉽게 사용할 수 있도록
    // 위치 관리자(LocationManager) 클래스에 addProximityAlert() 메소드 제공

    //근접 경보 설정을 위해 특정 위치를 미리 등록해야 하는데,
    // 이 목표지점 등록은 '펜딩인텐드(pendingIntent)'를 이용해 이루어진다.
    // 이는 인텐트 정보를 가지고 있다가 근접 경보가 발생했을 때 브로드케스트 수신자에게 그 인텐트를 전달하는 역할을 함.


    ArrayList<String> array = new ArrayList<>(); // 데이터를 저장할 array 리스트
    ArrayList<String> location = new ArrayList<>(); // 데이터를 저장할 location 리스트
    ListView m_ListView; // 데이터를 보여줄 ListView
    ArrayAdapter<String> m_Adapter; // 옮겨줄 adapter
    // 하나의 object로 보여지는 view와 그 view에 올릴 data를 연결하는 bridge

    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        androidx.appcompat.app.ActionBar ab = getSupportActionBar(); //action bar 설정
        ab.setTitle("To-do List");
        setContentView(R.layout.fragment_main);

//        location = new ArrayList<>(); //정의한 location list 선언
        rightInput = findViewById(R.id.rightInput); // rightInput textView (invisible 상태)
        register = findViewById(R.id.register); //register 버튼(add로 넘어감)
        release = findViewById(R.id.rel); //release 버튼(remove로 넘어감)

        //텍스트뷰를 3개를 만들면 삭제할 때나 추가할 때 불편해서 리스트뷰로 구현하는 방법을 택함.
        m_ListView = (ListView) findViewById(R.id.list); // main창에 있는 list 설정

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //locationManager 설정(location service)

        //런타임 퍼미션 체크
        myPermissionCheck();

        //버튼에 대한 리스터를 구현한 함수
        button();

        //이전의 경보에 대한 정보를 불러오는 함수
        readAlertFile();

        //실질적으로 리시버 등록을 해주는 함수
        receiverMaker();

    }

    @Override
    protected void onStart() { // activity화면이 불려질때 시작(생명주기)
        super.onStart();
        rightInput.setVisibility(View.INVISIBLE); // 안보이게 설정
        m_Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, location);
        // adapter 선언 후 adapter가 화면에 데이터를 불러온다
        // simple list item은 먼지 모르겠고 location은 아까 데이터 list
        m_ListView.setAdapter(m_Adapter); // main list에 어뎁트한다.


        //실질적으로 리시버 등록을 해주는 함수
        receiverMaker();
    }
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
    public void button() {
        register.setOnClickListener(new View.OnClickListener() { // 할일 추가하기 버튼 리스터 등록
            @Override
            public void onClick(View view) {
                //등록 버튼을 누를 때
                registerIntent = new Intent(getApplicationContext(), AddActivity.class);
                //addActivity로 인텐트된다.
                startActivityForResult(registerIntent, 100);
                //코드 주면서 인텐트시작.
            }
        });
        release.setOnClickListener(new View.OnClickListener() { // 삭제하기 버튼 리스너 등록
            @Override
            public void onClick(View view) {
                //해제 버튼을 누를 때
                //해제 버튼을 눌렀는데 아무것도 등록된 경보가 없을 시 예외처리해줌
                if (location.size() == 0) { // location에 아무것도 없을때(할일이 아무것도 없을때)
                    rightInput.setVisibility(View.VISIBLE); // rightInput textview 나오게하기
                    //삭제할 일이 없습니다.
                } else {
                    releaseIntent = new Intent(getApplicationContext(), RemoveActivity.class);
                    //삭제할 게 있으면 RemoveActivity로 인텐트
                    releaseIntent.putExtra("locationInformationArray", array);
                    // array배열 데이터 보내기
                    releaseIntent.putExtra("onlyLocationArray", location);
                    // location 배열 데이터
                    startActivityForResult(releaseIntent, 50);
                    //코드주면서 인텐트 시작.
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            //registerIntent . 추가받고 난 후 호출
            if (resultCode == RESULT_OK) {
                //array배열에 데이터들 주기
//                double lat = Double.parseDouble(data.getStringExtra("latitude"));
//                double lon = Double.parseDouble(data.getStringExtra("longitude"));
                array.add(data.getStringExtra("todo"));
                array.add(data.getStringExtra("latitude"));
                array.add(data.getStringExtra("longitude"));
//                array.add(data.getStringExtra("radius"));
                array.add(data.getStringExtra("place"));
//                array.add(data.getStringExtra("date"));
//                onAddMarker(lat,lon);
                //location 배열에 데이터주기(이게 화면에 나오는 데이터)
                location.add(data.getStringExtra("todo"));
//                location.add(data.getStringExtra("latitude"));
//                location.add(data.getStringExtra("longitude"));
//                location.add(data.getStringExtra("radius"));
//                location.add(data.getStringExtra("place"));
//                location.add(data.getStringExtra("date"));
                m_Adapter.notifyDataSetChanged();
                // adapter에게 말해주기?
            }
        }
//         release Intent . 삭제받고 난 후 호출
        if (requestCode == 50) {
            if (resultCode == RESULT_OK) {
//
                //모든 경보 해제
                lm.removeProximityAlert(proximityIntent);
                //removeProximityAlert함수를 통해 위치관리자에 등록되었던 위치경보 정보를 해제

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
                    //mode proviate으로 일단 덮어쓰고, arraysize만큼 돌리면서 각 array당 써주고 줄을 바꾼다.
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
    public void receiverMaker() {

        //리시버 등록 코드
        receiver = new AlertReceiver();
        IntentFilter filter = new IntentFilter("beomGeun");
        registerReceiver(receiver, filter);

// 인텐트의 액션 정보 정의 - 목표지점을 등록할 때 사용하는 인텐트를 브로드캐스트 수신자에서
// 받아 처리할 수 있어야 하므로 전송될 인텐트와 수신을 위한 인텐트 필터에 동일한 액션 정보(키값)-beomgeun 를 정의함
        intent = new Intent("beomGeun");
        try {
            for (int i = 0; i < location.size(); i++) {
                intent.putExtra("location", location.get(i)); //location list 내용들 차례차례 인텐트해준다.
                proximityIntent = PendingIntent.getBroadcast(getApplicationContext(),
                        //getBroadcast룰 통해 팬딩인텐트객체참조
                        0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//이때 전달되는 파라미터인
// PendingIntent.FLAG_CANCEL_CURRENT 상수는 새로운 근접 경보가 발생할 때 이전의 펜딩인텐트를 취소하도록 만들어줍니다.

                lm.addProximityAlert(Double.parseDouble(array.get(i * 4 + 1)), Double.parseDouble(array.get(i * 4 + 2))
                        , 100, 10000, proximityIntent);
//                lm.addProximityAlert(Double.parseDouble(location.get(1+location.size()-6)),Double.parseDouble(array.get(2+location.size()-6))
//                        ,Float.parseFloat(array.get(3+location.size()-6)),5000,proximityIntent);
                }
            //2) 인텐트와 펜딩인텐트를 이용한 목표지점 추가 - 인텐트를 생성하고 목표지점의 위도, 경도와 같은 정보를
// 추가하면 이를 이용해 브로드캐스팅을 위한 펜딩인텐트로 만들 수 있다.
// addProximityAlert()메소드를 이용해 목표지점을 추가할 때 좌표값과 펜딩 인텐트를 파라미터로전달

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    //앱이 실행될 때 런타임 퍼미션 체크하는 함수
    public void myPermissionCheck() {
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
            try {
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    //앱이 실행될 때 파일을 읽어와서 ArrayList 에 정보를 담는다.
    public void readAlertFile() {
        try {
            FileInputStream fis = openFileInput("location.txt");
            BufferedReader buffer = new BufferedReader(new InputStreamReader(fis));
            String str = buffer.readLine();

            while (str != null) {
                array.add(str);
                Log.d("리드 : ", str);
                str = buffer.readLine();
            }
            buffer.close();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "할 일을 추가해보아요!!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        //장소에 대한 내용만 location 이라는 리스트에 따로 담아 리스트뷰에 담도록 함.
        //장소의 위도, 경도, 반경 등에 대한 정보는 array 라는 리스트에 담아
        // 경보를 등록할 때만 사용하면 됨.(메인화면에 나타내줄 필요없음)

        for (int i = 0; i < array.size(); i++) {
            if (i % 4 == 0) {
                location.add(array.get(i));
            }
        }
    }

    LocationListener locationListener = new LocationListener() {
        @Override


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
    };
}