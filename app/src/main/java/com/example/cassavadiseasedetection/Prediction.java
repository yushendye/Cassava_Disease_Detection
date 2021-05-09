package com.example.cassavadiseasedetection;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.opencsv.CSVWriter;
import com.squareup.picasso.Picasso;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Prediction extends AppCompatActivity {
    ImageView imv_captured;
    private int CAM_PERM_ID = 101;
    private int GALLERY_ID = 102;

    Bitmap bmp_img_to_predict;
    TextView txt_detected;
    Dialog hover_dialog;

    TensorImage inputImageBuffer;
    Interpreter tflite;
    TensorBuffer output_probability_buffer;
    TensorProcessor probability_processor;

    private List<String> labels;

    String current_image_path;
    float IMAGE_MEAN = 0.0f;
    float IMAGE_STD = 1.0f;

    float PROBABILITY_MEAN = 127.5f;
    float PROBABILITY_STD = 127.5f;

    int image_size_x = 224;
    int image_size_y = 224;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction);
        imv_captured = findViewById(R.id.img_captured_plant);
        txt_detected = findViewById(R.id.txt_detected);

        //Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //startActivityForResult(camera_intent, CAM_PERM_ID);
        current_image_path = "";
        Snackbar.make(findViewById(R.id.img_captured_plant), R.string.str_hint_tap, Snackbar.LENGTH_LONG).show();
        try {
            tflite = new Interpreter(loadModel(this));
            //Toast.makeText(getApplicationContext(), "Model loaded!!", Toast.LENGTH_LONG).show();
        }catch (IOException e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menu_inflater = getMenuInflater();
        menu_inflater.inflate(R.menu.menu, menu);

        return true;
    }

    Bitmap getBitmap(Bitmap bitmap){
        return bitmap;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        imv_captured.setBackgroundResource(R.drawable.background);
        switch (requestCode){
            case 101:
                Thread save_thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Bitmap proper_image = Picasso.get().load("file://" + current_image_path).get();
                            bmp_img_to_predict = getBitmap(proper_image);
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                });
                save_thread.start();

                Picasso.get().
                        load("file://" + current_image_path).
                        resize(0, 512).
                        into(imv_captured);
                //Toast.makeText(getApplicationContext(), current_image_path, Toast.LENGTH_LONG).show();
                break;
            case 102:
                Uri uri = data.getData();
                save_thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            Bitmap proper_image = Picasso.get().
                                    load(uri).get();
                            bmp_img_to_predict = getBitmap(proper_image);
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                });
                save_thread.start();
                Picasso.get().load(uri).resize(0, 512).into(imv_captured);
                /*
                try{
                    uploaded_drawable = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    imv_captured.setBackgroundResource(R.drawable.background);
                    BitmapDrawable drawable = new BitmapDrawable(uploaded_drawable);
                    imv_captured.setImageDrawable(drawable);
                    bmp_img_to_predict = getBitmap(uploaded_drawable);
                }catch (IOException e){
                    Toast.makeText(getApplicationContext(), e.getCause().toString(), Toast.LENGTH_LONG).show();
                }
                 */
        }
    }

    public File createFile() throws IOException{
        String name = System.currentTimeMillis() + "";

        File storage_dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(name, ".jpg", storage_dir);
        current_image_path = image.getAbsolutePath();
        return image;
    }

    public void captureImage(View view){
        hover_dialog.dismiss();
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File image = null;
        if(camera_intent.resolveActivity(getPackageManager()) != null){
            try{
                image = createFile();

            }catch (IOException e){
                e.printStackTrace();
            }

            if(image != null){
                Uri image_uri = FileProvider.getUriForFile(this,
                        "com.example.cassavadiseasedetection.fileprovider",
                        image
                );
                camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
            }
        }
        startActivityForResult(camera_intent, CAM_PERM_ID);
    }

    public void uploadImage(View view){
        hover_dialog.dismiss();
        Intent gallery_intent = new Intent();
        gallery_intent.setType("image/*");
        gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(gallery_intent, GALLERY_ID);
    }

    public void showHover(View view){
        hover_dialog = new Dialog(this);
        hover_dialog.setContentView(R.layout.hover_options);
        hover_dialog.setCancelable(true);
        hover_dialog.show();
    }
    public void predict(View view){
        String saved_as = "";

        if(bmp_img_to_predict == null) {
            Toast.makeText(getApplicationContext(), "Capture the image first", Toast.LENGTH_SHORT).show();
            return;
        }
        int image_tensor_index = 0;
        int[] image_shape = tflite.getInputTensor(image_tensor_index).shape();
        image_size_x = image_shape[1];
        image_size_y = image_shape[2];
        DataType image_data_type = tflite.getInputTensor(image_tensor_index).dataType();


        int probability_tensor_index = 0;
        int[] probability_shape = tflite.getOutputTensor(probability_tensor_index).shape();
        DataType probability_data_type = tflite.getInputTensor(probability_tensor_index).dataType();

        inputImageBuffer = new TensorImage(image_data_type);
        output_probability_buffer = TensorBuffer.createFixedSize(probability_shape, probability_data_type);
        probability_processor = new TensorProcessor.Builder().add(getPostProcessNormalizeOp()).build();

        inputImageBuffer = load_image(bmp_img_to_predict);

        tflite.run(inputImageBuffer.getBuffer(), output_probability_buffer.getBuffer());

        try{
            labels = FileUtil.loadLabels(this,"labels.txt");
        }catch (Exception e){
            e.printStackTrace();
        }

        Map<String, Float> labeledProbability =
                new TensorLabel(labels, probability_processor.process(output_probability_buffer))
                        .getMapWithFloatValue();
        float maxValueInMap =(Collections.max(labeledProbability.values()));

        for (Map.Entry<String, Float> entry : labeledProbability.entrySet()) {
            if (entry.getValue()==maxValueInMap) {
                /*try{
                    saved_as = savePredictedImage(bmp_img_to_predict, entry.getKey());
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }*/
                txt_detected.setText(entry.getKey());
                txt_detected.setVisibility(View.VISIBLE);
            }
        }
    }

    public void re_capture(View view){
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera_intent, CAM_PERM_ID);
    }

    public void addToReport(View view){
        if(bmp_img_to_predict == null) {
            Toast.makeText(getApplicationContext(), "Capture the image first", Toast.LENGTH_SHORT).show();
            return;
        }

        String prediction = txt_detected.getText().toString();
        try {
            String saved_as = savePredictedImage(bmp_img_to_predict, prediction);
            HashMap<String, Integer> map = new HashMap<String, Integer>();
            map.put("Cassava Bacterial Blight (CBB)", 0);
            map.put("Cassava Brown Streak Disease (CBSD)", 1);
            map.put("Cassava Green Mottle (CGM)", 2);
            map.put("Cassava Mosaic Disease (CMD)", 3);
            map.put("Healthy", 4);
            map.put("Unknown", 5);

            int lbl = 0;
            if(map.get(prediction) != null)
                lbl = map.get(prediction);
            else
                lbl = 0;
            writeToCSV(saved_as, lbl);
        }catch (IOException e){
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    TensorImage load_image(final Bitmap bitmap){
        inputImageBuffer.load(bitmap);
        int crop_size = Math.min(bitmap.getHeight(), bitmap.getWidth());

        ImageProcessor image_processor =
                new ImageProcessor.Builder().add(new ResizeWithCropOrPadOp(crop_size, crop_size))
                        .add(new ResizeOp(image_size_x, image_size_y, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                        .add(getPreProcessNormalize()).build();

        return image_processor.process(inputImageBuffer);
    }

    private TensorOperator getPreProcessNormalize(){
        return new NormalizeOp(IMAGE_MEAN, IMAGE_STD);
    }

    private TensorOperator getPostProcessNormalizeOp(){
        return new NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD);
    }

    private MappedByteBuffer loadModel(Activity activity) throws IOException {
        AssetFileDescriptor assetFileDescriptor = activity.getAssets().openFd("cassava_model.tflite");
        FileInputStream fileInputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel channel = fileInputStream.getChannel();

        long start_offset = assetFileDescriptor.getStartOffset();
        long declared_length = assetFileDescriptor.getDeclaredLength();

        return channel.map(FileChannel.MapMode.READ_ONLY, start_offset, declared_length);
    }

    private String savePredictedImage(Bitmap bitmap, String name) throws IOException{
        boolean saved;
        OutputStream fos;
        name = name.replaceAll("\\s", "");
        name += System.currentTimeMillis();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = getApplicationContext().getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + "Detected");
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(imageUri);
        } else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + File.separator + "Detected";

            File file = new File(imagesDir);

            if (!file.exists()) {
                file.mkdir();
            }
            File image = new File(imagesDir, name + ".jpg");
            fos = new FileOutputStream(image);
        }

        saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        if(saved)
            Toast.makeText(getApplicationContext(), "Image saved!", Toast.LENGTH_LONG);
        else
            Toast.makeText(getApplicationContext(), "Could not save image", Toast.LENGTH_LONG).show();
        fos.flush();
        fos.close();

        return name + ".jpg";
    }

    public void writeToCSV(String filename, int label){
        String base_dir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String file_name = "New_Detections.csv";
        String abs_path = base_dir + File.separator + file_name;
        File csv_file = new File(abs_path);

        CSVWriter writer;
        try{
            if (csv_file.exists() && ! csv_file.isDirectory()) {
                writer = new CSVWriter(new FileWriter(abs_path, true));
                String[] data = {filename, String.valueOf(label)};

                writer.writeNext(data);
                Toast.makeText(getApplicationContext(), "Successfully updated " + file_name, Toast.LENGTH_LONG).show();
                writer.close();
            }else{
                if(csv_file.createNewFile()){
                    Toast.makeText(getApplicationContext(), "File created for first time usage", Toast.LENGTH_LONG).show();
                }
                writer = new CSVWriter(new FileWriter(abs_path));
                String[] data = {filename, String.valueOf(label)};
                writer.writeNext(data);
                writer.close();
                Toast.makeText(getApplicationContext(), "Successfully updated " + file_name, Toast.LENGTH_LONG).show();
            }
        }catch (IOException e){
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }
}