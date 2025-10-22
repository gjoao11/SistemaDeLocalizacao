package com.example.sistemadelocalizacao;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class DialogConfigActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dialog_config);

        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);

        CheckBox checkBoxGPS = findViewById(R.id.checkBox);
        CheckBox checkBoxGLONASS = findViewById(R.id.checkBox2);
        CheckBox checkBoxGALILEO = findViewById(R.id.checkBox3);
        CheckBox checkBoxBEIDOU = findViewById(R.id.checkBox4);
        Switch switchNotUsedInFix = findViewById(R.id.switch_mostrar_satelites);

        boolean boolGPS = sharedPreferences.getBoolean("GPS", true);
        boolean boolGLOONAS = sharedPreferences.getBoolean("GLONASS", true);
        boolean boolGALILEO = sharedPreferences.getBoolean("GALILEO", true);
        boolean boolBEIDOU = sharedPreferences.getBoolean("BEIDOU", true);
        boolean boolSwitch = sharedPreferences.getBoolean("notUsedInFix", true);

        checkBoxGPS.setChecked(boolGPS);
        checkBoxGLONASS.setChecked(boolGLOONAS);
        checkBoxGALILEO.setChecked(boolGALILEO);
        checkBoxBEIDOU.setChecked(boolBEIDOU);
        switchNotUsedInFix.setChecked(boolSwitch);

        SharedPreferences.Editor e = sharedPreferences.edit();

        checkBoxGPS.setOnCheckedChangeListener((buttonView, isChecked) -> {
            e.putBoolean("GPS", isChecked);
            e.apply();
        });
        checkBoxGLONASS.setOnCheckedChangeListener((buttonView, isChecked) -> {
            e.putBoolean("GLONASS", isChecked);
            e.apply();
        });
        checkBoxGALILEO.setOnCheckedChangeListener((buttonView, isChecked) -> {
            e.putBoolean("GALILEO", isChecked);
            e.apply();
        });
        checkBoxBEIDOU.setOnCheckedChangeListener((buttonView, isChecked) -> {
            e.putBoolean("BEIDOU", isChecked);
            e.apply();
        });
        switchNotUsedInFix.setOnCheckedChangeListener((buttonView, isChecked) -> {
            e.putBoolean("notUsedInFix", isChecked);
            e.apply();
        });
    }
}