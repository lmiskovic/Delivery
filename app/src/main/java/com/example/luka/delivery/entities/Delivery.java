package com.example.luka.delivery.entities;


public class Delivery {

    private int id;
    private String created_at;
    private String updated_at;
    private int user_id;
    private String deliveryAddress;
    private String customerName;
    private String contactPhoneNumber;
    private String note;
    private MapLocation mapLocation;

    public Delivery(int id, String created_at, String updated_at, int user_id,
                    String deliveryAddress, String customerName, String contactPhoneNumber,
                    String note, MapLocation mapLocation) {

                            this.id = id;
                            this.created_at = created_at;
                            this.updated_at = updated_at;
                            this.user_id = user_id;
                            this.deliveryAddress = deliveryAddress;
                            this.customerName = customerName;
                            this.contactPhoneNumber = contactPhoneNumber;
                            this.note = note;
                            this.mapLocation = mapLocation;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getContactPhoneNumber() {
        return contactPhoneNumber;
    }

    public void setContactPhoneNumber(String contactPhoneNumber) {
        this.contactPhoneNumber = contactPhoneNumber;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getId() {

        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MapLocation getMapLocation() {
        return mapLocation;
    }

    public void setMapLocation(MapLocation mapLocation) {
        this.mapLocation = mapLocation;
    }
}