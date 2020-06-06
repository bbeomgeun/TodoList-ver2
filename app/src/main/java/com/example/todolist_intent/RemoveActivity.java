package com.example.todolist_intent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class RemoveActivity extends AppCompatActivity {

    TextView rightInput;
    EditText removeLocation;
    Button remove;

    Intent fromMain;
    ArrayList<String> arrayList = new ArrayList<>();
    ArrayList<String> arrayListLocation = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.appcompat.app.ActionBar ab = getSupportActionBar();
        ab.setTitle("Remove Location");
        setContentView(R.layout.activity_remove);


        rightInput = findViewById(R.id.rightInput);
        removeLocation = findViewById(R.id.removeLocation);
        remove = findViewById(R.id.remove);

        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = 0;
                fromMain = getIntent();
                arrayList = fromMain.getStringArrayListExtra("locationInformationArray");
                arrayListLocation = fromMain.getStringArrayListExtra("onlyLocationArray");

                //제대로 된 입력이 아닌 경우 예외처리
                if(!arrayList.contains(removeLocation.getText().toString())){
                    rightInput.setVisibility(View.VISIBLE);
                }
                else {
                    for (int i = 0; i < arrayList.size(); i += 4) {
                        if (removeLocation.getText().toString().equals(arrayList.get(i))) {
                            index = i;
                        }
                    }

                    //받았던 인텐트 안의 두 배열에서 원하는 값을 지워주고 다시 메인액티비티에 보낸다.
                    arrayListLocation.remove(index / 4);
                    arrayList.remove(index);
                    arrayList.remove(index);
                    arrayList.remove(index);
                    arrayList.remove(index);

                    setResult(RESULT_OK, fromMain);
                    finish();
                }
            }
        });
    }
}
