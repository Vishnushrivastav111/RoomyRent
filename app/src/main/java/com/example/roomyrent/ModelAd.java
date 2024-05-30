package com.example.roomyrent;

public class ModelAd {
    String id;
    String uid;
    String size;
    String floor;
    String house;
    String Condition;
    String bathroom;
    String kitchen;
    String wifi;
    String category;
    String address;
    String furniture;
    String light;
    String rent;
    String title;
    String description;
    String status;
    String timestamp;
    String latitude;
    String longitude;
    boolean favorite;
    public ModelAd(){

    }

    public ModelAd(String id,String category,String Condition, String uid, String size, String floor, String house, String bathroom, String kitchen, String wifi, String address, String furniture, String light, String rent, String title, String description, String status, String timestamp, String latitude, String longitude, boolean favorite) {
        this.id = id;
        this.uid = uid;
        this.size = size;
        this.floor = floor;
        this.house = house;
        this.bathroom = bathroom;
        this.kitchen = kitchen;
        this.Condition = Condition;
        this.wifi = wifi;
        this.address = address;
        this.furniture = furniture;
        this.category = category;
        this.light = light;
        this.rent = rent;
        this.title = title;
        this.description = description;
        this.status = status;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.favorite = favorite;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }
    public String getCategory(){return category;}
    public void setCategory(String category){this.category = category;}

    public String getBathroom() {
        return bathroom;
    }

    public void setBathroom(String bathroom) {
        this.bathroom = bathroom;
    }

    public String getKitchen() {
        return kitchen;
    }

    public void setKitchen(String kitchen) {
        this.kitchen = kitchen;
    }

    public String getWifi() {
        return wifi;
    }

    public void setWifi(String wifi) {
        this.wifi = wifi;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getFurniture() {
        return furniture;
    }

    public void setFurniture(String furniture) {
        this.furniture = furniture;
    }

    public String getLight() {
        return light;
    }

    public void setLight(String light) {
        this.light = light;
    }

    public String getRent() {
        return rent;
    }

    public void setRent(String rent) {
        this.rent=rent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getCondition() {
        return Condition;
    }

    public void setCondition(String title) {
        this.Condition = Condition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
