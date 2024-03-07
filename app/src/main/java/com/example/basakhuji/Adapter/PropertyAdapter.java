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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {

    private final Context context;
    private final List<PropertyList> propertyList;
    public PropertyAdapter(Context context, ArrayList<PropertyList> propertyList) {
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
        PropertyList property = propertyList.get(position); // Get the property directly based on the position

        // Display property details
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
        TextView categoryFlatTypeTextView, locationTextView, likeTextView;
        ImageView propertyImageView, likeButton;

        FirebaseFirestore firestore;
        FirebaseUser currentUser;
       private String id;

        public PropertyViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryFlatTypeTextView = itemView.findViewById(R.id.categoryFlatTypeTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            propertyImageView = itemView.findViewById(R.id.propertyImageView);
            likeButton = itemView.findViewById(R.id.like_btn);
            likeTextView = itemView.findViewById(R.id.like_text);
        }

        public void bind(PropertyList property) {
            // Display property details
            String categoryFlatType = property.getCategory() + " " + property.getFlatType();
            categoryFlatTypeTextView.setText(categoryFlatType);
            locationTextView.setText(property.getLocation());


            // Load property image using Glide
            Glide.with(itemView.getContext())
                    .load(property.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(propertyImageView);


//            // Set like button icon based on like status
//            if (property.isLiked()) {
//                likeButton.setImageResource(R.drawable.liked);
//            } else {
//                likeButton.setImageResource(R.drawable.like); // Change to your default like icon
//            }
        }

    }
}
