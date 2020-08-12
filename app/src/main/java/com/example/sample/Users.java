package com.example.sample;

public class Users {
    private String Name, Image, Status;
    public Users(){

    }
    public Users(String name, String image, String status) {
        this.Name = name;
        this.Image = image;
        this.Status = status;
    }
    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        this.Image = image;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        this.Status = status;
    }
}

