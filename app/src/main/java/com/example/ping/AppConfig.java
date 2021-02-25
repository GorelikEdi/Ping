package com.example.ping;

public class AppConfig {

    private String chat_join_prefix;
    private String image_prefix;
    private String location_prefix;
    private String key;
    private String label_prefix;
    private long project_id;
    private static AppConfig appConfig = null;

    public static void generate(){
        FirebaseRDBClient.generateAppConfig();
    }

    public static AppConfig getInstance(){
        return appConfig;
    }

    public static void setAppConfig(AppConfig appConfig){
        if (AppConfig.appConfig == null) {
            AppConfig.appConfig = appConfig;
        }
    }

    public AppConfig() {
    }

    public String getChat_join_prefix() {
        return chat_join_prefix;
    }

    public void setChat_join_prefix(String chat_join_prefix) {
        this.chat_join_prefix = chat_join_prefix;
    }

    public String getLabel_prefix() {
        return label_prefix;
    }

    public void setLabel_prefix(String label_prefix) {
        this.label_prefix = label_prefix;
    }

    public String getImage_prefix() {
        return image_prefix;
    }

    public void setImage_prefix(String image_prefix) {
        this.image_prefix = image_prefix;
    }

    public String getLocation_prefix() {
        return location_prefix;
    }

    public void setLocation_prefix(String location_prefix) {
        this.location_prefix = location_prefix;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getProject_id() {
        return project_id;
    }

    public void setProject_id(long project_id) {
        this.project_id = project_id;
    }

}
