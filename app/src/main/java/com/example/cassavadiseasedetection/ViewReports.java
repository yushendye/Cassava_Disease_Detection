package com.example.cassavadiseasedetection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ViewReports extends AppCompatActivity {
    RecyclerView rcv_disease_list;
    List<Disease> diseases_from_csv;
    Button btn_comm_results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reports);

        rcv_disease_list = findViewById(R.id.rcv_disease_list);
        btn_comm_results = findViewById(R.id.btn_comm_res);

        populateDiseasesFromCSV();

        DiseaseAdapter adapter = new DiseaseAdapter(diseases_from_csv);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getApplicationContext());

        rcv_disease_list.setLayoutManager(manager);
        rcv_disease_list.setItemAnimator(new DefaultItemAnimator());
        rcv_disease_list.setAdapter(adapter);
    }

    public void populateDiseasesFromCSV(){
        diseases_from_csv = new ArrayList<Disease>();
        CSVReader reader;
        String base_dir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String csv_file_name = "New_Detections.csv";
        String absolute_path = base_dir + File.separator + csv_file_name;

        File csv_file = new File(absolute_path);

        if(! csv_file.exists()){
            Toast.makeText(getApplicationContext(), "You have not detected any diseases yet, so no CSV File found!!", Toast.LENGTH_LONG).show();
            btn_comm_results.setVisibility(View.GONE);
        }else{
            btn_comm_results.setVisibility(View.VISIBLE);
            try{
                reader = new CSVReader(new FileReader(absolute_path));

                String disease_name = "";

                String[] read_data;
                while ((read_data = reader.readNext()) != null){
                    Disease single_disease = new Disease();
                    String img_name = read_data[0];
                    String label = read_data[1];

                    String image_path = base_dir + File.separator + "DCIM" + File.separator + "Detected" + File.separator + img_name;
                    switch (label) {
                        case "0":
                            disease_name = "Cassava Bacterial Blight (CBB)";
                            break;
                        case "1":
                            disease_name = "Cassava Brown Streak Disease (CBSD)";
                            break;
                        case "2":
                            disease_name = "Cassava Green Mottle (CGM)";
                            break;
                        case "3":
                            disease_name = "Cassava Mosaic Disease (CMD)";
                            break;
                        case "4":
                            disease_name = "Healthy";
                            break;
                    }

                    Bitmap disease_image = BitmapFactory.decodeFile(image_path);
                    single_disease.setImage(disease_image);
                    single_disease.setLabel(disease_name);
                    single_disease.setImage_id(img_name);

                    diseases_from_csv.add(single_disease);
                }
            }catch (IOException e){
                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void communicateReports(View view){
        Intent comm_intent = new Intent(ViewReports.this, CommunicateReports.class);
        startActivity(comm_intent);
    }


}