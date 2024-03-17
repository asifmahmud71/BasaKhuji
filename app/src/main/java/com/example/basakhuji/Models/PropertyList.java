package com.example.basakhuji.Models;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

public class PropertyList implements Serializable {
    private String imageUrl, category, flatType, location, price, description, addedDate, availableMonth, id, beds, baths, userPhone;
    public PropertyList() {
        // Default constructor required for Firestore
    }


    private List<String> likedBy;
    private List<String> dislikedBy;


    public PropertyList(String imageUrl, String category, String flatType, String location, String price, String addedDate, String availableMonth, String description, String id, String beds, String baths, String userPhone) {
        this.id = id;
        this.price = price;
        this.category = category;
        this.flatType = flatType;
        this.location = location;
        this.imageUrl = imageUrl;
        this.addedDate = addedDate;
        this.availableMonth = availableMonth;
        this.description = description;
        this.beds = beds;
        this.baths = baths;
        this.userPhone = userPhone;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFlatType() {
        return flatType;
    }

    public void setFlatType(String flatType) {
        this.flatType = flatType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(String addedDate) {
        this.addedDate = addedDate;
    }

    public String getAvailableMonth() {
        return availableMonth;
    }

    public void setAvailableMonth(String availableMonth) {
        this.availableMonth = availableMonth;
    }

    public String getBeds() {
        return beds;
    }

    public void setBeds(String beds) {
        this.beds = beds;
    }

    public String getBaths() {
        return baths;
    }

    public void setBaths(String baths) {
        this.baths = baths;
    }


    public List<String> getLikedBy() {
        return likedBy;
    }
    public void setLikedBy(List<String> likedBy) {
        this.likedBy = likedBy;
    }

    public List<String> getDislikedBy() {
        return dislikedBy;
    }
    public void setDislikedBy(List<String> dislikedBy) {
        this.dislikedBy = dislikedBy;
    }


    @NonNull
    @Override
    public String toString() {
        return "PropertyList{" +
                "imageUrl='" + imageUrl + '\'' +
                ", category='" + category + '\'' +
                ", flatType='" + flatType + '\'' +
                ", location='" + location + '\'' +
                ", price='" + price + '\'' +
                ", description='" + description + '\'' +
                ", addedDate='" + addedDate + '\'' +
                ", availableMonth='" + availableMonth + '\'' +
                ", id='" + id + '\'' +
                ", beds=" + beds +
                ", baths=" + baths +
                '}';
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserPhone() {
        return userPhone;
    }
}
