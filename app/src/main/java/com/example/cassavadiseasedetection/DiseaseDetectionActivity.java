package com.example.cassavadiseasedetection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

public class DiseaseDetectionActivity extends AppCompatActivity {
    private boolean CAMERA_PERMISSION = false;
    private boolean READ_PERMISSION = false;
    private boolean WRITE_PERMISSION = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease_detection);

        askCameraPermission();
        askStorageReadPermission();
        askStorageWritePermission();
    }

    public void capture_image(View view){
        startActivity(new Intent(DiseaseDetectionActivity.this, Prediction.class));
    }

    public void view_reports(View view){
        startActivity(new Intent(DiseaseDetectionActivity.this, ViewReports.class));
    }
    public void askCameraPermission(){
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){

        }else{
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);

            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                CAMERA_PERMISSION = true;
            else
                CAMERA_PERMISSION = false;
        }
    }

    public void askStorageWritePermission(){
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

        }else{
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 102);
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                WRITE_PERMISSION = true;
            else
                WRITE_PERMISSION = false;
        }
    }

    public void askStorageReadPermission(){
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

        }else{
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 103);

            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                READ_PERMISSION = true;
            else
                READ_PERMISSION = false;
        }
    }
}