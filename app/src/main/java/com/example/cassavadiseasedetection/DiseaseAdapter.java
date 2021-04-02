package com.example.cassavadiseasedetection;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DiseaseAdapter extends RecyclerView.Adapter<DiseaseAdapter.DiseaseHolder> {
    List<Disease> detected_diseases;
    Bitmap disease_image;
    String disease_label;

    DiseaseAdapter(List<Disease> detected_diseases){
        this.detected_diseases = detected_diseases;
    }

    @NonNull
    @Override
    public DiseaseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.diseae_label_interface,parent, false);
        return new DiseaseHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiseaseHolder holder, int position) {
        Disease disease_item = detected_diseases.get(position);

        disease_image = disease_item.getImage();
        disease_label = disease_item.getLabel();

        holder.image.setImageBitmap(disease_item.getImage());
        holder.label.setText(disease_item.getLabel());

        holder.rl_row.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Item no " + position, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(v.getContext(), DiseaseDetail.class);
            intent.putExtra("image_name", disease_item.getImage_id());
            intent.putExtra("label", disease_item.getLabel());

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return detected_diseases.size();
    }

    class DiseaseHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView label;
        RelativeLayout rl_row;

        public DiseaseHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.img_list_crop);
            label = itemView.findViewById(R.id.txt_list_disease_name);
            rl_row = itemView.findViewById(R.id.rcv_list);
        }
    }
}
