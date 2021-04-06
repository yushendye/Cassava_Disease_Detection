package com.example.cassavadiseasedetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class DiseaseDetail extends AppCompatActivity {
    ImageView img_disease_image;
    TextView txt_disease_name, txt_disease_info, txt_disease_cure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease_detail);

        img_disease_image = findViewById(R.id.img_det_crop);
        txt_disease_name = findViewById(R.id.txt_det_disease_name);
        txt_disease_info = findViewById(R.id.txt_det_info);
        txt_disease_cure = findViewById(R.id.txt_det_measures);

        Intent intent = getIntent();
        update(intent.getStringExtra("image_name"), intent.getStringExtra("label"));
    }

    void update(String image_id, String label){
        String base_dir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String abs_path = base_dir + File.separator + "DCIM" + File.separator + "Detected" + File.separator + image_id;
        img_disease_image.setBackgroundColor(Color.WHITE);
        img_disease_image.setImageBitmap(BitmapFactory.decodeFile(abs_path));
        switch (label){
            case "Cassava Bacterial Blight (CBB)":
                txt_disease_name.setText(R.string.str_cbb);
                txt_disease_info.setText(R.string.str_cbb_info);
                txt_disease_cure.setText(R.string.str_cbb_cure);
                break;

            case "Cassava Brown Streak Disease (CBSD)":
                txt_disease_name.setText(R.string.str_cbsd);
                txt_disease_info.setText(R.string.str_cbsd_info);
                txt_disease_cure.setText(R.string.str_cbsd_cure);
                break;

            case "Cassava Green Mottle (CGM)":
                txt_disease_name.setText(R.string.str_cgm);
                txt_disease_info.setText(R.string.str_cgm_info);
                txt_disease_cure.setText(R.string.str_cgm_cure);
                break;

            case "Cassava Mosaic Disease (CMD)":
                txt_disease_name.setText(R.string.str_cmd);
                txt_disease_info.setText(R.string.str_cmd_info);
                txt_disease_cure.setText(R.string.str_cmd_cure);
                break;

            case "Healthy":
                txt_disease_name.setText(R.string.str_healthy);
                txt_disease_info.setText(R.string.str_healthy_info);
                txt_disease_cure.setText(R.string.str_healthy_cure);
                break;
        }
    }
}