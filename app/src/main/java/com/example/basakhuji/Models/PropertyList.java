package com.example.basakhuji.Models;

public class PropertyList {
    private String imageUrl;
    private String category;
    private String flatType;
    private String location;
    private String price;
    private String description;
    private String addedDate;
    private String availableMonth;
    private int beds;
    private int baths;
    private String id;
    private int likes;
    private int dislikes;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }


    public PropertyList() {
        // Default constructor required for Firestore
    }


    public PropertyList(String imageUrl, String category, String flatType, String location, String price,
                        String description, String addedDate, String availableMonth, String id) {
        this.imageUrl = imageUrl;
        this.category = category;
        this.flatType = flatType;
        this.location = location;
        this.price = price;
        this.description = description;
        this.addedDate = addedDate;
        this.availableMonth = availableMonth;
        this.id = id;
        this.beds = beds;
        this.baths = baths;
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
    public void setPrice(String price){
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


    public int getBeds() {
        return beds;
    }

    public void setBeds(int beds) {
        this.beds = beds;
    }

    public int getBaths() {
        return baths;
    }

    public void setBaths(int baths) {
        this.baths = baths;
    }
}
