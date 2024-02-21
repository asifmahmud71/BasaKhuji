package com.example.basakhuji.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.basakhuji.Models.PropertyList;
import com.example.basakhuji.PropertyDetailsActivity;
import com.example.basakhuji.R;

import java.util.List;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {

    private Context context;
    private List<PropertyList> propertyList;

    public PropertyAdapter(Context context, List<PropertyList> propertyList) {
        this.context = context;
        this.propertyList = propertyList;
    }

    @NonNull
    @Override
    public PropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.property_list, parent, false);
        return new PropertyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PropertyViewHolder holder, int position) {
        PropertyList property = propertyList.get(position);

        // Combine category and flat type
        String categoryFlatType = property.getCategory() + " " + property.getFlatType();
        holder.categoryFlatTypeTextView.setText(categoryFlatType);

        // Set location
        holder.locationTextView.setText(property.getLocation());

        // Load image using Glide
        Glide.with(context)
                .load(property.getImageUrl())
                .placeholder(R.drawable.placeholder_image) // Placeholder image while loading
                .error(R.drawable.error_image) // Error image if loading fails
                .into(holder.propertyImageView);

        // Set click listener to open PropertyDetailsActivity with property details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PropertyDetailsActivity.class);
            intent.putExtra("location", property.getLocation());
            intent.putExtra("id", property.getId());
            //intent.putExtra("beds", property.getBeds());
            //intent.putExtra("baths", property.getBaths());
            intent.putExtra("category", property.getCategory());
            intent.putExtra("flatType", property.getFlatType());
            intent.putExtra("price", property.getPrice());
            intent.putExtra("imageUrl", property.getImageUrl());
            intent.putExtra("addedDate", property.getAddedDate());
            intent.putExtra("availableMonth", property.getAvailableMonth());
            intent.putExtra("description", property.getDescription());
            context.startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        return propertyList.size();
    }

    public static class PropertyViewHolder extends RecyclerView.ViewHolder {
        TextView categoryFlatTypeTextView, locationTextView;
        ImageView propertyImageView;

        public PropertyViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryFlatTypeTextView = itemView.findViewById(R.id.categoryFlatTypeTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            propertyImageView = itemView.findViewById(R.id.propertyImageView);
        }
    }
}
