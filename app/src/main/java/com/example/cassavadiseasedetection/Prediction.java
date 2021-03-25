package com.example.cassavadiseasedetection;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Prediction extends AppCompatActivity {
    ImageView imv_captured;
    private int CAM_PERM_ID = 101;
    Bitmap bmp_img_to_predict;

    TensorImage inputImageBuffer;
    Interpreter tflite;
    TensorBuffer output_probability_buffer;
    TensorProcessor probability_processor;
    private List<String> labels;

    float IMAGE_MEAN = 0.0f;
    float IMAGE_STD = 1.0f;

    float PROBABILITY_MEAN = 0.0f;
    float PROBABILITY_STD = 255.0f;

    int image_size_x = 224;
    int image_size_y = 224;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction);

        imv_captured = findViewById(R.id.img_captured_plant);


        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera_intent, CAM_PERM_ID);

        try {
            tflite = new Interpreter(loadModel(this));
        }catch (IOException e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    Bitmap getBitmap(Bitmap bitmap){
        return bitmap;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 101:
                Bitmap captured_bmp = (Bitmap)data.getExtras().get("data");
                Drawable bitmap_drawable = new BitmapDrawable(getResources(), captured_bmp);
                imv_captured.setImageDrawable(bitmap_drawable);

                bmp_img_to_predict = getBitmap(captured_bmp);
        }
    }

    public void predict(View view){
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
                savePredictedImage(bmp_img_to_predict, entry.getKey());
                Toast.makeText(getApplicationContext(), entry.getKey(), Toast.LENGTH_LONG).show();
            }
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
        AssetFileDescriptor assetFileDescriptor = activity.getAssets().openFd("model.tflite");
        FileInputStream fileInputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel channel = fileInputStream.getChannel();

        long start_offset = assetFileDescriptor.getStartOffset();
        long declared_length = assetFileDescriptor.getDeclaredLength();

        return channel.map(FileChannel.MapMode.READ_ONLY, start_offset, declared_length);
    }

    private void savePredictedImage(Bitmap image, String label){
        File media_storage_dir = new File(Environment.getStorageDirectory() , "Detected");
        if(! media_storage_dir.exists())
            media_storage_dir.mkdir();

        String file_name = label + "_" + System.currentTimeMillis() + ".jpeg";
        File file = new File(media_storage_dir, file_name);

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
        }catch (IOException exception){
            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
        }

        image.compress(Bitmap.CompressFormat.JPEG, 224, fileOutputStream);
        Toast.makeText(getApplicationContext(), "Record saved successfully!!", Toast.LENGTH_LONG).show();

        try {
            fileOutputStream.flush();
        }catch (IOException e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        try {
            fileOutputStream.close();
        }catch (IOException e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}