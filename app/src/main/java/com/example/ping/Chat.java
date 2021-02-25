package com.example.ping;

import java.util.ArrayList;

public class Chat {

    public static final int PRIVATE_CHAT = 0;
    public static final int GROUP_CHAT = 1;
    public static String CURRENT_CHAT = null;
    public static String LATEST_GROUP_CHAT = null;
    public static String LATEST_GROUP_CHAT_UID = null;
    public static String LATEST_PRIVATE_CHAT = null;
    public static String LATEST_PRIVATE_CHAT_UID = null;

    private int type;
    private String name;
    private ArrayList<Message> messages;
    private ArrayList<String> users;
    private String key;
    private String password;

    public Chat(String name, ArrayList<Message> messages, ArrayList<String> users, int type) {
        setName(name);
        setMessages(messages);
        setUsers(users);
        setType(type);
    }

    public Chat(String name, ArrayList<Message> messages, ArrayList<String> users, int type, String key, String password) {
        setName(name);
        setMessages(messages);
        setUsers(users);
        setType(type);
        setKey(key);
        setPassword(password);
    }

    public Chat() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }

    public String getSecondUsername(String username){
        String name = null;
        for (String temp: this.users) {
            if (!temp.equals(username))
                name = temp;
        }
        return name;
    }
}

