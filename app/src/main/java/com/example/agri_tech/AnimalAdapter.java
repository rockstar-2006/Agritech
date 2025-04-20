package com.example.agri_tech;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AnimalAdapter extends RecyclerView.Adapter<AnimalAdapter.ViewHolder> {

    private List<Animal> animalList;

    public AnimalAdapter(List<Animal> animalList) {
        this.animalList = animalList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView labelTextView;
        public TextView timestampTextView;
        public ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            labelTextView = view.findViewById(R.id.labelTextView);
            timestampTextView = view.findViewById(R.id.timestampTextView);
            imageView = view.findViewById(R.id.animalImageView);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_animal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Animal animal = animalList.get(position);
        holder.labelTextView.setText(animal.getLabel());
        holder.timestampTextView.setText(animal.getTimestamp());

        // Decode base64 image string
        try {
            String base64Image = animal.getImage_base64();

            if (base64Image != null && !base64Image.isEmpty()) {
                // Remove the "data:image/jpeg;base64," prefix if it exists
                if (base64Image.contains(",")) {
                    base64Image = base64Image.substring(base64Image.indexOf(",") + 1);
                }

                byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.imageView.setImageBitmap(bitmap);
            } else {
                holder.imageView.setImageResource(R.drawable.ic_background); // fallback
            }
        } catch (Exception e) {
            holder.imageView.setImageResource(R.drawable.ic_background); // fallback on error
        }
    }

    @Override
    public int getItemCount() {
        return animalList.size();
    }
}
