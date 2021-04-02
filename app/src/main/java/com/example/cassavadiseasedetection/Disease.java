package com.example.cassavadiseasedetection;

import android.graphics.Bitmap;

public class Disease {
    Bitmap image;
    String label;
    String image_id;

    public String getImage_id() {
        return image_id;
    }

    public void setImage_id(String image_id) {
        this.image_id = image_id;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    Disease(){

    }
    
    public Disease(Bitmap image, String label, String image_id) {
        this.image = image;
        this.label = label;
        this.image_id = image_id;
    }
}
