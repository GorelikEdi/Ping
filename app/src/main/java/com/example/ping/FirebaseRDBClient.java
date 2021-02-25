package com.example.ping;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FirebaseRDBClient {

    private static FirebaseDatabase firebaseDatabase = null;
    private static FirebaseRDBClientListener firebaseRDBClientListener;
    private static DatabaseReference databaseReference = null;

    interface FirebaseRDBClientListener {
        void onUserExists(boolean isExists);
        void applyUser(User user);
        void applyChat(Chat chat, boolean isFirstLoad);
        void applyUsers(ArrayList<String> users, boolean isStart);
        void chatCreated(String chatName, String chatKey);
        void applyGroupChat(Chat chat);
        void privateChatCreated(String key);
        void applyChatNames(ArrayList<String> chatNames, boolean isCreate);
        void applyChatToJoin(Chat chat);
    }

    public static void setFirebaseRDBClientListener(Context context){
        firebaseRDBClientListener = (FirebaseRDBClientListener) context;
    }

    private static void setFirebaseDatabase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
    }

    private static FirebaseDatabase getFirebaseDatabase(){
        if (firebaseDatabase == null) {
            setFirebaseDatabase();
        }
        return firebaseDatabase;
    }

    private static void setDBReference(String name){
        databaseReference = getFirebaseDatabase().getReference(name);
    }

    private static DatabaseReference getDBReference(String name){
        setDBReference(UUID.randomUUID().toString());
        setDBReference(name);
        return databaseReference;
    }

    public static void getUsernames(final boolean isStart){
        getDBReference("usernames").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> userList = new ArrayList<>();
                Map<String, Map<String, String>> users = (Map<String, Map<String, String>>)snapshot.getValue();
                for (String key: users.keySet()){
                    userList.add(users.get(key).get("username"));
                }
                firebaseRDBClientListener.applyUsers(userList, isStart);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void addUserToGroupChatUsers(final String key, final String username){
        getDBReference("chats").orderByChild("users").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    getDBReference("chats").child(key).child("users").child(UUID.randomUUID().toString()).setValue(username);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void getChatNames(final boolean isCreate){
        getDBReference("chat_names").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<Map<String,Object>> mapType = new GenericTypeIndicator<Map<String, Object>>() { };
                Map<String, Object> map = snapshot.getValue(mapType);
                ArrayList<String> chats = new ArrayList<>();
                if (snapshot.exists()) {
                    for (String key : map.keySet()) {
                        chats.add((String) map.get(key));
                    }
                }

                firebaseRDBClientListener.applyChatNames(chats, isCreate);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void getGroupChat(String UUID){
        getDBReference("chats").child(UUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<Map<String,Object>> mapType = new GenericTypeIndicator<Map<String, Object>>() { };
                Map<String, Object> map = snapshot.getValue(mapType);
                ArrayList<HashMap<String,String>> messages = new ArrayList<>();
                ArrayList<String> users = new ArrayList<>();
                ArrayList<Message> content = new ArrayList<>();

                for (String key : ((HashMap<String, String>)map.get("users")).keySet()){
                    users.add(((HashMap<String, String>) map.get("users")).get(key));
                }

                if (map.get("messages") != null) {
                    for (String key : ((HashMap<String, HashMap<String, String>>) map.get("messages")).keySet()) {
                        messages.add(((HashMap<String, HashMap<String, String>>) map.get("messages")).get(key));
                    }

                    Collections.sort(messages, new Comparator<HashMap<String, String>>() {
                        public int compare(HashMap<String, String> one, HashMap<String, String> two) {
                            return one.get("timestamp").compareTo(two.get("timestamp"));
                        }
                    });

                    for (HashMap<String, String> temp : messages)
                        content.add(new Message(temp.get("content"), temp.get("sender"),
                                temp.get("receiver"), temp.get("timestamp"),
                                temp.get("senderPhotoUrl")));
                }
                String password;
                if (map.get("password") != null)
                    password = map.get("password").toString();
                else
                    password = null;
                Chat chat;
                if ((Long) map.get("type") == 0)
                    chat = new Chat(
                            map.get("name").toString(),
                            content,
                            users,
                            Integer.parseInt(map.get("type").toString())
                    );
                else
                    chat = new Chat(
                            map.get("name").toString(),
                            content,
                            users,
                            Integer.parseInt(map.get("type").toString()),
                            snapshot.getKey(),
                            password
                    );
                firebaseRDBClientListener.applyGroupChat(chat);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void createPrivateChat(Chat chat){
        getDBReference("chats").child(chat.getName()).setValue(chat);
        firebaseRDBClientListener.privateChatCreated(chat.getName());
        updateChatUsers(chat.getUsers(), chat.getName());
    }

    public static void updateChatsProfile(ArrayList<String> chats){
        getDBReference("users").child(FirebaseAuthClient.getCurrentUser().getUid())
                .child("chats").setValue(chats);
    }

    public static void createGroupChat(Chat chat){
        getDBReference("chats").child(chat.getKey()).setValue(chat);
        getDBReference("chat_names").child(chat.getKey()).setValue(chat.getName());
        firebaseRDBClientListener.chatCreated(chat.getName(), chat.getKey());
        updateChatUsers(chat.getUsers(), chat.getKey());
    }

    public static void updateChatUsers(ArrayList<String> users, String key){
        for (int i = 0; i < users.size(); i++){
            getDBReference("chats").child(key).child("users").child(UUID.randomUUID()
                    .toString()).setValue(users.get(i));

            getDBReference("chats").child(key).child("users").child(String.valueOf(i)).setValue(null);
        }
    }

    public static void checkUsername(String username){
        Query query = getDBReference("usernames").orderByChild("username").equalTo(username);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                firebaseRDBClientListener.onUserExists(snapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void uploadMessage(final Message message, String chatUID){
        getDBReference("chats").child(chatUID).child("messages")
                .child(UUID.randomUUID().toString()+UUID.randomUUID().toString()).setValue(message);
    }

    public static void uploadUser(final User user){
        getDBReference("users").child(FirebaseAuthClient.getCurrentUser()
                .getUid())
                .setValue(user);

        Map<String, String> username = new HashMap<>();
        username.put("username", user.getUsername());
        getDBReference("usernames").child(FirebaseAuthClient.getCurrentUser()
                    .getUid())
                    .setValue(username);
    }

    public static void updateToken(String token){
        getDBReference("users").child(FirebaseAuthClient.getCurrentUser()
                .getUid())
                .child("token")
                .setValue(token);
    }

    public static void getUserByUsername(String username){
        getDBReference("users").orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data: snapshot.getChildren())
                    getUser(data.getKey());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void inviteUserToGroupChat(String username, final Context context, final String chatKey, final String chatName){
        getDBReference("users").orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user;
                for (DataSnapshot data: snapshot.getChildren()) {
                    user = data.getValue(User.class);
                    PingMessagingService.inviteUserToGroupChat(user.getToken(), context, chatKey, chatName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public static void getUser(String uid){
        getDBReference("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                firebaseRDBClientListener.applyUser(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void getChat(String name, final boolean isFirstLoad){
        getDBReference("chats").orderByChild("name").equalTo(name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data: snapshot.getChildren()) {
                    getChatByUID(data.getKey(), isFirstLoad);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void uploadPhotoUrl(String url){
        getDBReference("users").child(FirebaseAuthClient.getCurrentUser()
                .getUid())
                .child("photoUrl")
                .setValue(url);
    }

    public static void getChatByUID(String uid, final boolean isFirstLoad){
        getDBReference("chats").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<Map<String,Object>> mapType = new GenericTypeIndicator<Map<String, Object>>() { };
                Map<String, Object> map = snapshot.getValue(mapType);
                ArrayList<HashMap<String,String>> messages = new ArrayList<>();
                ArrayList<String> users = new ArrayList<>();
                ArrayList<Message> content = new ArrayList<>();
                try {
                    for (String key : ((HashMap<String, String>) map.get("users")).keySet()) {
                        users.add(((HashMap<String, String>) map.get("users")).get(key));
                    }
                }catch (ClassCastException e){
                    users = (ArrayList<String>) map.get("users");
                }
                if (map.get("messages") != null) {
                    for (String key : ((HashMap<String, HashMap<String, String>>) map.get("messages")).keySet()) {
                        messages.add(((HashMap<String, HashMap<String, String>>) map.get("messages")).get(key));
                    }

                    Collections.sort(messages, new Comparator<HashMap<String, String>>() {
                        public int compare(HashMap<String, String> one, HashMap<String, String> two) {
                            return one.get("timestamp").compareTo(two.get("timestamp"));
                        }
                    });

                    for (HashMap<String, String> temp : messages)
                        content.add(new Message(temp.get("content"), temp.get("sender"),
                                temp.get("receiver"), temp.get("timestamp"),
                                temp.get("senderPhotoUrl")));
                }
                Chat chat;
                String password;
                if (map.get("password") != null)
                    password = map.get("password").toString();
                else
                    password = null;
                if ((Long) map.get("type") == 0)
                    chat = new Chat(
                            map.get("name").toString(),
                            content,
                            users,
                            Integer.parseInt(map.get("type").toString())
                    );
                else
                    chat = new Chat(
                            map.get("name").toString(),
                            content,
                            users,
                            Integer.parseInt(map.get("type").toString()),
                            snapshot.getKey(),
                            password
                    );

                firebaseRDBClientListener.applyChat(chat, isFirstLoad);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void updateAllImages(Uri uri, ArrayList<Chat> groupChats, ArrayList<Chat> privateChats){
        if (privateChats != null){
            for (Chat chat : privateChats)
                updateChatImages(uri, chat.getName());
        }

        if (groupChats != null) {
            for (Chat chat : groupChats)
                updateChatImages(uri, chat.getName());
        }
    }

    private static void updateChatImages(final Uri uri, final String chatName){
        getDBReference("chats").orderByChild("name").equalTo(chatName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data: snapshot.getChildren()) {
                    final String key = data.getKey();
                    getDBReference("chats").child(key).child("messages")
                            .orderByChild("sender").equalTo(FirebaseAuthClient.getCurrentUser()
                            .getDisplayName()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot data: snapshot.getChildren()){
                                getDBReference("chats").child(key).child("messages").child(data.getKey()).child("senderPhotoUrl")
                                        .setValue(uri.toString());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void generateAppConfig(){
        getDBReference("app_config").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                AppConfig.setAppConfig(snapshot.getValue(AppConfig.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void getSingleChatToJoin(String chatName){
        getDBReference("chats").orderByChild("name").equalTo(chatName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<Map<String,Object>> mapType = new GenericTypeIndicator<Map<String, Object>>() { };
                Map<String, Object> tempMap = snapshot.getValue(mapType);
                String mapKey = tempMap.keySet().iterator().next();
                Map<String, Object> map = (Map<String, Object>)tempMap.get(mapKey);
                ArrayList<HashMap<String,String>> messages = new ArrayList<>();
                ArrayList<String> users = new ArrayList<>();
                ArrayList<Message> content = new ArrayList<>();

                for (String key : ((HashMap<String, String>)map.get("users")).keySet()){
                    users.add(((HashMap<String, String>) map.get("users")).get(key));
                }
                if (map.get("messages") != null) {
                    for (String key : ((HashMap<String, HashMap<String, String>>) map.get("messages")).keySet()) {
                        messages.add(((HashMap<String, HashMap<String, String>>) map.get("messages")).get(key));
                    }

                    Collections.sort(messages, new Comparator<HashMap<String, String>>() {
                        public int compare(HashMap<String, String> one, HashMap<String, String> two) {
                            return one.get("timestamp").compareTo(two.get("timestamp"));
                        }
                    });

                    for (HashMap<String, String> temp : messages)
                        content.add(new Message(temp.get("content"), temp.get("sender"),
                                temp.get("receiver"), temp.get("timestamp"),
                                temp.get("senderPhotoUrl")));
                }
                String password;
                if (map.get("password") != null)
                    password = map.get("password").toString();
                else
                    password = null;
                Chat chat = new Chat(
                            map.get("name").toString(),
                            content,
                            users,
                            Integer.parseInt(map.get("type").toString()),
                            mapKey,
                            password
                    );

                firebaseRDBClientListener.applyChatToJoin(chat);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void leaveChat(final String username, final String chatKey){
        getDBReference("chats").child(chatKey).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (snapshot1.getValue(String.class).equals(username))
                        getDBReference("chats").child(chatKey).child("users").child(snapshot1.getKey()).setValue(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
