package com.example.cassavadiseasedetection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.Zip4jConfig;
import net.lingala.zip4j.model.Zip64ExtendedInfo;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.Zip4jUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CommunicateReports extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communicate_reports);
    }

    public void sendReports(View view){
        /*
        Using the default zip utility in android did not work efficiently.
        So I had to use Zip4J library
         */

        /*
        imagesDir holds the image directory path
        csv_dir holds path of the directory where CSV file is stored
         */
        String imagesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM).toString() + File.separator + "Detected";
        String csv_dir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();

        //name of the zip file
        String zip_file_name = csv_dir + File.separator + "detected.zip";

        //this will compress all image in the directory
        zip(imagesDir, zip_file_name);

        Intent email = new Intent(Intent.ACTION_SEND_MULTIPLE);
        email.setType("plain/text");
        email.putExtra(Intent.EXTRA_SUBJECT, "Re: About detected files");
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{"theycallmechinmay@gmail.com"});
        ArrayList<Uri> files = new ArrayList<>();


        /*
        https://stackoverflow.com/questions/42516126/fileprovider-illegalargumentexception-failed-to-find-configured-root

        Followed this link for sending the multiple files through the email
         */
        //Toast.makeText(getApplicationContext(), "Zip " + zip_file_name + " and csv " + csv_dir + File.separator + "New_Detections.csv", Toast.LENGTH_LONG).show();
        files.add(FileProvider.getUriForFile(this, "com.example.cassavadiseasedetection.fileprovider", new File(zip_file_name)));
        files.add(FileProvider.getUriForFile(this, "com.example.cassavadiseasedetection.fileprovider", new File(csv_dir + File.separator + "New_Detections.csv")));

        email.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        email.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        email.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        startActivity(Intent.createChooser(email, "Send reports to developers"));
    }

    private void zip(String folder_path, String zip_name){
        /*
        Zip4J library provides facility to compress the whole directory

        folder_path contains the path of folder that we have to compress
        zip_name is the name of the resulting zip file
         */
        try {
            File input_file = new File(folder_path);

            ZipParameters zipParameters = new ZipParameters();
            zipParameters.setCompressionMethod(CompressionMethod.AES_INTERNAL_ONLY);
            zipParameters.setCompressionLevel(CompressionLevel.NORMAL);

            File output = new File(zip_name);
            ZipFile zipFile = new ZipFile(output);

            if(input_file.isDirectory())
                zipFile.addFolder(input_file);
            else
                zipFile.addFile(input_file);

            File zip = zipFile.getFile();
        }catch (Exception e){
            Log.d("Compress Error : ", e.getMessage());
        }
    }
}