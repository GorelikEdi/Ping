package com.example.ping;

import java.util.ArrayList;

public class User {

    private String birthDate;
    private String username;
    private ArrayList<String> chats = new ArrayList<>();
    private String email;
    private String token;
    private String photoUrl;

    public User(String birthDate, String username, ArrayList<String> chats, String email, String token, String photoUrl){
        setBirthDate(birthDate);
        setUsername(username);
        setChats(chats);
        setEmail(email);
        setToken(token);
        setPhotoUrl(photoUrl);
    }

    public User(){}

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public ArrayList<String> getChats() {
        return chats;
    }

    public void setChats(ArrayList<String> chats) {
        this.chats = chats;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
