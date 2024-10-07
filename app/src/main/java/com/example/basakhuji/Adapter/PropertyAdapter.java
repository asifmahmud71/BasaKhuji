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

import java.util.ArrayList;
import java.util.List;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {

    private final Context context;
    private List<PropertyList> propertyList;
    public PropertyAdapter(Context context, ArrayList<PropertyList> propertyList) {
        this.context = context;
        this.propertyList = propertyList;
    }

    public void setFilteredList(List<PropertyList> filteredList){
        this.propertyList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.property_list, parent, false);
        return new PropertyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PropertyViewHolder holder, int position) {
        PropertyList property = propertyList.get(position); // Get the property directly based on the position

        holder.bind(property);

        // Set click listener for property image
        holder.propertyImageView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PropertyDetailsActivity.class);
            intent.putExtra("property", property);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return propertyList.size();
    }

   static class PropertyViewHolder extends RecyclerView.ViewHolder {
        TextView categoryFlatTypeTextView, locationTextView;
        ImageView propertyImageView;

        public PropertyViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryFlatTypeTextView = itemView.findViewById(R.id.categoryFlatTypeTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            propertyImageView = itemView.findViewById(R.id.propertyImageView);
        }

        public void bind(PropertyList property) {

            String categoryFlatType = property.getCategory() + " " + property.getFlatType();
            categoryFlatTypeTextView.setText(categoryFlatType);
            String location = property.getArea() + ", " + property.getDistrict();
            locationTextView.setText(location);

            // Load property image using Glide
            List<String> imageUrlList = property.getImageUrl();
            if (imageUrlList != null && !imageUrlList.isEmpty()) {
                String firstImageUrl = imageUrlList.get(0);
                Glide.with(itemView.getContext())
                        .load(firstImageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(propertyImageView);

            }
        }
    }
}
