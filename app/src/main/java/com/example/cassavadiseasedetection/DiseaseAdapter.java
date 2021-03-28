package com.example.cassavadiseasedetection;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DiseaseAdapter extends RecyclerView.Adapter<DiseaseAdapter.DiseaseHolder> {
    List<Disease> detected_diseases;

    DiseaseAdapter(List<Disease> diseases){
        detected_diseases = diseases;
    }

    @NonNull
    @Override
    public DiseaseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.diseae_label_interface,parent, false);
        DiseaseHolder holder = new DiseaseHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull DiseaseHolder holder, int position) {
        Disease disease_item = detected_diseases.get(position);
        holder.image.setBackgroundColor(Color.rgb(255,255 ,255));
        holder.image.setImageBitmap(disease_item.getImage());
        holder.label.setText(disease_item.getLabel());
    }

    @Override
    public int getItemCount() {
        return detected_diseases.size();
    }

    class DiseaseHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView label;
        public DiseaseHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.img_list_crop);
            label = itemView.findViewById(R.id.txt_list_disease_name);
        }
    }
}
