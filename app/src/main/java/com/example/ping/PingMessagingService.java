package com.example.ping;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PingMessagingService extends FirebaseMessagingService implements FirebaseRDBClient.FirebaseRDBClientListener {

    private static final int NOTIF_ID = 1;
    public static boolean APP_IS_UP = false;
    private boolean send = true;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            remoteMessage.getData();
            Intent intent = new Intent("message_received");
            intent.putExtra("content", remoteMessage.getData().get("content"));
            intent.putExtra("sender", remoteMessage.getData().get("sender"));
            intent.putExtra("receiver", remoteMessage.getData().get("receiver"));
            intent.putExtra("timestamp", remoteMessage.getData().get("timestamp"));
            intent.putExtra("senderPhotoUrl", remoteMessage.getData().get("senderPhotoUrl"));
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            send = true;

            if (APP_IS_UP && Chat.CURRENT_CHAT != null && Chat.CURRENT_CHAT.equals(remoteMessage.getData().get("receiver")))
                send = false;
            if (APP_IS_UP && remoteMessage.getData().get("sender").equals(FirebaseAuthClient.getCurrentUser().getDisplayName()))
                send = false;

            if (send) {
                    PendingIntent activityPendingIntent = null;
                    if (!APP_IS_UP) {
                        Intent activityIntent = new Intent(this, Intro.class);
                        activityIntent.putExtra("sender", remoteMessage.getData().get("sender"));
                        activityIntent.putExtra("receiver", remoteMessage.getData().get("receiver"));
                        activityPendingIntent = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    }
                    NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    if (Build.VERSION.SDK_INT >= 26) {
                        NotificationChannel channel = new NotificationChannel("PingApp", "PingApp channel", NotificationManager.IMPORTANCE_HIGH);
                        manager.createNotificationChannel(channel);
                    }
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "PingApp");
                    String content = remoteMessage.getData().get("content");
                    if (remoteMessage.getData().get("receiver").length() == 119){
                        content = "Message in Group Chat";
                    }
                    builder.setSmallIcon(R.drawable.message_icon)
                            .setContentTitle(remoteMessage.getData().get("sender"))
                            .setContentText(content)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setOnlyAlertOnce(true);
                    if (!APP_IS_UP)
                        builder.setContentIntent(activityPendingIntent);
                    manager.notify(NOTIF_ID, builder.build());
            }
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        SharedPreferences sharedPreferences = getSharedPreferences("pingApp", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.apply();
    }

    public static String getToken(SharedPreferences sharedPreferences){
        return sharedPreferences.getString("token", null);
    }

    @Override
    public void onUserExists(boolean isExists) {

    }

    @Override
    public void applyUser(User user) {

    }

    @Override
    public void applyChat(Chat chat, boolean isFirstLoad) {

    }

    @Override
    public void applyUsers(ArrayList<String> users, boolean isStart) {

    }

    @Override
    public void chatCreated(String chatName, String chatKey) {

    }

    @Override
    public void applyGroupChat(Chat chat) {

    }

    @Override
    public void privateChatCreated(String key) {

    }

    @Override
    public void applyChatNames(ArrayList<String> chatNames, boolean isCreate) {

    }

    @Override
    public void applyChatToJoin(Chat chat) {

    }


    public static void sendMessage(Message message, Context context){
        final JSONObject root = new JSONObject();
        JSONObject data = new JSONObject();
        final String key = AppConfig.getInstance().getKey();
        try {
            data.put("content", message.getContent());
            data.put("sender", message.getSender());
            data.put("timestamp", String.valueOf(message.getTimestamp()));
            data.put("receiver", Chat.CURRENT_CHAT);
            data.put("senderPhotoUrl", message.getSenderPhotoUrl());
            root.put("data", data);
            root.put("to", message.getReceiver());

            String url = "https://fcm.googleapis.com/fcm/send";

            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "key=" + key);
                    return headers;
                }

                @Override
                public byte[] getBody() {
                    return root.toString().getBytes();
                }
            };
            queue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void createChat(final String chatName, final String password, Context context, String userToken){
        final JSONObject root = new JSONObject();
        final String key = AppConfig.getInstance().getKey();
        final long id = AppConfig.getInstance().getProject_id();
        final JSONArray users = new JSONArray();
        users.put(userToken);

        try {
            root.put("operation", "create");
            root.put("notification_key_name", chatName);
            root.put("registration_ids", users);

            String url = "https://fcm.googleapis.com/fcm/notification";

            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    String notifKey = response.replace("{\"notification_key\":\"", "").replace("\"}", "");
                    Chat chat = new Chat(chatName, null,
                            new ArrayList<>(Arrays.asList(FirebaseAuthClient.getCurrentUser()
                                    .getDisplayName())), 1, notifKey, password);
                    FirebaseRDBClient.createGroupChat(chat);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "key=" + key);
                    headers.put("project_id", String.valueOf(id));
                    return headers;
                }

                @Override
                public byte[] getBody() {
                    return root.toString().getBytes();
                }
            };
            queue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void addUserToGroupChat(String userToken, Context context, Chat chat){
        final JSONObject root = new JSONObject();
        final String key = AppConfig.getInstance().getKey();
        final long id = AppConfig.getInstance().getProject_id();
        final JSONArray users = new JSONArray();
        users.put(userToken);

        try {
            root.put("operation", "add");
            root.put("notification_key_name", chat.getName());
            root.put("notification_key", chat.getKey());
            root.put("registration_ids", users);

            String url = "https://fcm.googleapis.com/fcm/notification";
            Message message = new Message(AppConfig.getInstance().getLabel_prefix()+FirebaseAuthClient.getCurrentUser().getDisplayName() + " has joined chat",
                    FirebaseAuthClient.getCurrentUser().getDisplayName(), chat.getKey(),null);
            sendMessage(message, context);
            FirebaseRDBClient.uploadMessage(message, Chat.CURRENT_CHAT);

            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "key=" + key);
                    headers.put("project_id", String.valueOf(id));
                    return headers;
                }

                @Override
                public byte[] getBody() {
                    return root.toString().getBytes();
                }
            };
            queue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void inviteUserToGroupChat(String userToken, Context context, String chatKey, String chatName){
        final JSONObject root = new JSONObject();
        final String key = AppConfig.getInstance().getKey();
        final long id = AppConfig.getInstance().getProject_id();
        final JSONArray users = new JSONArray();
        users.put(userToken);
        try {
            root.put("operation", "add");
            root.put("notification_key_name", chatName);
            root.put("notification_key", chatKey);
            root.put("registration_ids", users);

            String url = "https://fcm.googleapis.com/fcm/notification";

            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "key=" + key);
                    headers.put("project_id", String.valueOf(id));
                    return headers;
                }

                @Override
                public byte[] getBody() {
                    return root.toString().getBytes();
                }
            };
            queue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void removeUserFromGroupChat(String userToken, Context context, String chatName, String chatKey){
        final JSONObject root = new JSONObject();
        final String key = AppConfig.getInstance().getKey();
        final long id = AppConfig.getInstance().getProject_id();
        final JSONArray users = new JSONArray();
        users.put(userToken);
        try {
            root.put("operation", "remove");
            root.put("notification_key_name", chatName);
            root.put("notification_key", chatKey);
            root.put("registration_ids", users);

            String url = "https://fcm.googleapis.com/fcm/notification";
            Message message = new Message(AppConfig.getInstance().getLabel_prefix()+FirebaseAuthClient.getCurrentUser().getDisplayName() + " has left chat",
                    FirebaseAuthClient.getCurrentUser().getDisplayName(), chatKey,null);
            sendMessage(message, context);
            FirebaseRDBClient.uploadMessage(message, chatKey);

            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "key=" + key);
                    headers.put("project_id", String.valueOf(id));
                    return headers;
                }

                @Override
                public byte[] getBody() {
                    return root.toString().getBytes();
                }
            };
            queue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
