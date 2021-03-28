package com.example.cassavadiseasedetection;

import android.graphics.Bitmap;

public class Disease {
    Bitmap image;
    String label;

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
    
    public Disease(Bitmap image, String label) {
        this.image = image;
        this.label = label;
    }
}
