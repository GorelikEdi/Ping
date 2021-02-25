package com.example.ping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements MessageAdapter.MessageAdapterListener,
        NavigationView.OnNavigationItemSelectedListener, FirebaseRDBClient.FirebaseRDBClientListener,
        FirebaseStorageClient.FirebaseStorageClientListener, ImageDialog.ImageDialogListener,
        StartPrivateDialog.StartPrivateDialogListener, JoinChatDialog.JoinChatDialogListener,
        ChatFragment.ChatFragmentListener {

    private TabLayout tabLayout;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ChatFragment groupFragment;
    private ChatFragment privateFragment;
    private HomeFragment homeFragment;
    private final ArrayList<Chat> groupChats = new ArrayList<>();
    private final ArrayList<Chat> privateChats = new ArrayList<>();
    private final ArrayList<Message> privateMessages = new ArrayList<>();
    private final ArrayList<Message> groupMessages = new ArrayList<>();
    private boolean isPrivateChat;
    private TextView logoTV;
    private TextView titleTV;
    private User user;
    private int numberOfChats = 0;
    private int loading = 0;
    private BroadcastReceiver broadcastReceiver;
    private SubMenu subGroupMenu;
    private SubMenu subPrivateMenu;
    private BadgeDrawable privateBadge;
    private BadgeDrawable groupBadge;
    private ImageView photo;
    private final int ADD_IMAGE = 1;
    private String sender = null;
    private String receiver = null;
    private LoadingDialog loadingDialog;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingDialog = new LoadingDialog();
        loadingDialog.show(getSupportFragmentManager(), "loading dialog");

        AppConfig.generate();

        logoTV = findViewById(R.id.logoTV);
        titleTV = findViewById(R.id.titleTV);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        tabLayout = findViewById(R.id.tab_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        ViewPager viewPager = findViewById(R.id.view_pager);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle actionBDT = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBDT);
        actionBDT.syncState();

        homeFragment = new HomeFragment();
        privateFragment = new ChatFragment();
        groupFragment = new ChatFragment();

        ImageDialog imageDialog = new ImageDialog();
        privateFragment.setImageDialog(imageDialog);
        groupFragment.setImageDialog(imageDialog);

        privateFragment.setMessages(privateMessages);
        groupFragment.setMessages(groupMessages);

        tabLayout.setupWithViewPager(viewPager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPagerAdapter.addFragment(homeFragment, "Home");
        viewPagerAdapter.addFragment(privateFragment, "Private");
        viewPagerAdapter.addFragment(groupFragment, "Group");
        viewPager.setAdapter(viewPagerAdapter);

        Objects.requireNonNull(tabLayout.getTabAt(0)).setIcon(R.drawable.icon_home);
        Objects.requireNonNull(tabLayout.getTabAt(1)).setIcon(R.drawable.icon_private);
        Objects.requireNonNull(tabLayout.getTabAt(2)).setIcon(R.drawable.icon_group);

        privateBadge = Objects.requireNonNull(tabLayout.getTabAt(1)).getOrCreateBadge();
        privateBadge.setVisible(false);
        groupBadge = Objects.requireNonNull(tabLayout.getTabAt(2)).getOrCreateBadge();
        groupBadge.setVisible(false);

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        drawerLayout.bringToFront();

        FirebaseRDBClient.setFirebaseRDBClientListener(this);

        FirebaseRDBClient.getUser(Objects.requireNonNull(FirebaseAuthClient.getCurrentUser()).getUid());

        FirebaseStorageClient.setFirebaseStorageClientListener(this);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String content = intent.getStringExtra("content");
                String sender = intent.getStringExtra("sender");
                String receiver = intent.getStringExtra("receiver");
                String timestamp = intent.getStringExtra("timestamp");
                String senderPhotoUrl = intent.getStringExtra("senderPhotoUrl");
                onMessageReceived(new Message(content, sender, receiver, timestamp, senderPhotoUrl));
            }
        };
        IntentFilter filter = new IntentFilter("message_received");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (Chat.LATEST_PRIVATE_CHAT_UID == null && Chat.LATEST_GROUP_CHAT_UID == null){
                    privateFragment.setChatReady();
                    groupFragment.setChatReady();
                }
                switch (Objects.requireNonNull(tab.getText()).toString()){
                    case "Private":
                        groupFragment.setChatNotReady();
                        isPrivateChat = true;
                        Chat.CURRENT_CHAT = Chat.LATEST_PRIVATE_CHAT_UID;
                        FirebaseRDBClient.getChat(Chat.LATEST_PRIVATE_CHAT_UID, false);
                        break;
                    case "Group":
                        privateFragment.setChatNotReady();
                        isPrivateChat = false;
                        Chat.CURRENT_CHAT = Chat.LATEST_GROUP_CHAT_UID;
                        FirebaseRDBClient.getChat(Chat.LATEST_GROUP_CHAT_UID, false);
                        break;
                    default:
                        privateFragment.setChatNotReady();
                        groupFragment.setChatNotReady();
                        Chat.CURRENT_CHAT = null;
                        isPrivateChat = false;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        if (getIntent().hasExtra("sender")) {
            sender = getIntent().getStringExtra("sender");
            receiver = getIntent().getStringExtra("receiver");
        }
    }

    private void initMenu(){
        if (loading == 2 + numberOfChats) {
            LinearLayout header = navigationView.findViewById(R.id.header);
            TextView username = header.findViewById(R.id.username);
            TextView email = header.findViewById(R.id.email);

            photo = header.findViewById(R.id.photo);
            username.setText(user.getUsername());
            email.setText(user.getEmail());

            homeFragment.setToken(user.getToken());
            homeFragment.setStartPrivateDialog(new StartPrivateDialog());
            groupFragment.setInviteDialog(new InviteDialog());
            homeFragment.setJoinChatDialog(new JoinChatDialog(user.getToken()));
            homeFragment.setCreateChatDialog(new CreateChatDialog(user.getToken()));

            menu = navigationView.getMenu();
            subGroupMenu = menu.addSubMenu("Group chats");
            subPrivateMenu = menu.addSubMenu("Private chats");

            if (user.getPhotoUrl() != null)
                Glide.with(this).load(user.getPhotoUrl()).circleCrop().into(photo);

            for (Chat chat : groupChats) {
                subGroupMenu.add(Chat.GROUP_CHAT, Menu.NONE, Menu.NONE,
                        chat.getName())
                        .setContentDescription(chat.getKey())
                        .setIcon(R.drawable.message_icon_white)
                        .setNumericShortcut('f');
            }

            for (Chat chat : privateChats) {
                subPrivateMenu.add(Chat.PRIVATE_CHAT, Menu.NONE, Menu.NONE,
                        chat.getSecondUsername(user.getUsername()))
                        .setContentDescription(chat.getName())
                        .setIcon(R.drawable.message_icon_white)
                        .setNumericShortcut('f');
            }

            if (sender != null){
                if (receiver.length() != 119) {
                    if (subPrivateMenu != null) {
                        MenuItem usernameItem;
                        for (int i = 0; i < subPrivateMenu.size(); i++) {
                            usernameItem = subPrivateMenu.getItem(i);
                            if (sender.equals(usernameItem.getTitle().toString())) {
                                if (usernameItem.getNumericShortcut() != 't') {
                                    usernameItem.setNumericShortcut('t');
                                    usernameItem.setIcon(R.drawable.message_icon);
                                    privateBadge.setVisible(true);
                                    privateBadge.setNumber(privateBadge.getNumber() + 1);
                                }
                            }
                        }
                    }
                }
                else{
                    if (subGroupMenu != null) {
                        MenuItem chatNameItem;
                        for (int i = 0; i < subGroupMenu.size(); i++) {
                            chatNameItem = subGroupMenu.getItem(i);
                            if (receiver.equals(chatNameItem.getContentDescription().toString())) {
                                if (chatNameItem.getNumericShortcut() != 't') {
                                    chatNameItem.setNumericShortcut('t');
                                    chatNameItem.setIcon(R.drawable.message_icon);
                                    groupBadge.setVisible(true);
                                    groupBadge.setNumber(groupBadge.getNumber() + 1);
                                }
                            }
                        }
                    }
                }
            }
            loadingDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else {
            super.onBackPressed();
            finish();
        }
    }

    @Override
    public void usernameClicked(int position) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getContentDescription() != null && item.getContentDescription().toString()
                .equals(Chat.CURRENT_CHAT)){
            drawerLayout.closeDrawer(GravityCompat.START);
            return false;
        }
        int index = 0;
        switch (item.getGroupId()){
            case Chat.GROUP_CHAT:
                index = 2;
                break;
            case Chat.PRIVATE_CHAT:
                index = 1;
                break;
            default:
                switch (item.getTitle().toString()) {
                    case "Home":
                        index = 0;
                        break;
                    case "Private":
                        if (Chat.LATEST_PRIVATE_CHAT == null)
                            privateFragment.setChatReady();
                        index = 1;
                        break;
                    case "Group":
                        if (Chat.LATEST_GROUP_CHAT == null)
                            groupFragment.setChatReady();
                        index = 2;
                        break;
                }
        }
        Objects.requireNonNull(tabLayout.getTabAt(index)).select();
        privateFragment.setChatNotReady();
        groupFragment.setChatNotReady();
        switch (item.getGroupId()){
            case Chat.GROUP_CHAT:
                item.setIcon(R.drawable.message_icon_white).setNumericShortcut('f');
                if (groupBadge.getNumber() > 0)
                    groupBadge.setNumber(groupBadge.getNumber() - 1);
                if (groupBadge.getNumber() == 0)
                    groupBadge.setVisible(false);
                Chat.LATEST_GROUP_CHAT_UID = item.getContentDescription().toString();
                Chat.CURRENT_CHAT = Chat.LATEST_GROUP_CHAT_UID;
                FirebaseRDBClient.getChatByUID(Chat.LATEST_GROUP_CHAT_UID, false);
                isPrivateChat = false;
                Chat.LATEST_GROUP_CHAT = item.getTitle().toString();
                groupFragment.setUsernameHeader(Chat.LATEST_GROUP_CHAT);
                break;
            case Chat.PRIVATE_CHAT:
                item.setIcon(R.drawable.message_icon_white).setNumericShortcut('f');
                if (privateBadge.getNumber() > 0)
                    privateBadge.setNumber(privateBadge.getNumber() - 1);
                if (privateBadge.getNumber() == 0)
                    privateBadge.setVisible(false);
                Chat.LATEST_PRIVATE_CHAT_UID = item.getContentDescription().toString();
                Chat.CURRENT_CHAT = Chat.LATEST_PRIVATE_CHAT_UID;
                FirebaseRDBClient.getChat(Chat.LATEST_PRIVATE_CHAT_UID, false);
                isPrivateChat = true;
                Chat.LATEST_PRIVATE_CHAT = item.getTitle().toString();
                break;
            default:
                Chat.CURRENT_CHAT = null;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    public void add_photo(View view){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select image"), ADD_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ADD_IMAGE && data.getData() != null) {
                FirebaseStorageClient.uploadPhoto(data.getData(), user.getUsername());
            }
        }
    }

    public void log_out(View view){
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        FirebaseAuthClient.signOut();
        Intent intent = new Intent(MainActivity.this, SignIn.class);
        Pair[] pairs = new Pair[2];
        pairs[0] = new Pair<View, String>(logoTV, "logo");
        pairs[1] = new Pair<View, String>(titleTV, "title");
        ActivityOptions options = ActivityOptions
                .makeSceneTransitionAnimation(MainActivity.this, pairs);
        startActivity(intent, options.toBundle());
        finishActivity();
    }

    private void onMessageReceived(Message message) {
        boolean legal = true;
        if (message.getContent().contains(AppConfig.getInstance().getLabel_prefix()) && message.getSender().equals(FirebaseAuthClient.getCurrentUser().getDisplayName()))
            legal = false;
        if (legal) {
            if (message.getReceiver().equals(Chat.CURRENT_CHAT)) {
                if (isPrivateChat) {
                    if (privateFragment.getReceiver().getPhotoUrl() != null) {
                        if (!privateFragment.getReceiver().getPhotoUrl().equals(message.getSenderPhotoUrl())) {
                            privateFragment.setPhotoHeader(message.getSenderPhotoUrl());
                            updateImagesInCurrChat(message.getSenderPhotoUrl(), null, true, true);
                        }
                    } else {
                        if (message.getSenderPhotoUrl() != null) {
                            privateFragment.setPhotoHeader(message.getSenderPhotoUrl());
                            updateImagesInCurrChat(message.getSenderPhotoUrl(), null, true, true);
                        }
                    }
                    privateMessages.add(message);
                    privateFragment.notifyAdapter();
                } else {
                    if (message.getSenderPhotoUrl() != null) {
                        updateImagesInCurrChat(message.getSenderPhotoUrl(), message.getSender(), false, true);
                    }

                    groupMessages.add(message);
                    groupFragment.notifyAdapter();
                }
            } else {
                boolean isNew = true;
                if (message.getReceiver().length() != 119) {
                    if (subPrivateMenu != null) {
                        MenuItem usernameItem;
                        for (int i = 0; i < subPrivateMenu.size(); i++) {
                            usernameItem = subPrivateMenu.getItem(i);
                            if (message.getSender().equals(usernameItem.getTitle().toString())) {
                                isNew = false;
                                if (usernameItem.getNumericShortcut() != 't') {
                                    usernameItem.setNumericShortcut('t');
                                    usernameItem.setIcon(R.drawable.message_icon);
                                    privateBadge.setVisible(true);
                                    privateBadge.setNumber(privateBadge.getNumber() + 1);
                                }
                            }
                        }
                    }
                } else {
                    if (subGroupMenu != null) {
                        MenuItem chatNameItem;
                        for (int i = 0; i < subGroupMenu.size(); i++) {
                            chatNameItem = subGroupMenu.getItem(i);
                            if (message.getReceiver().equals(chatNameItem.getContentDescription().toString())) {
                                isNew = false;
                                if (chatNameItem.getNumericShortcut() != 't') {
                                    chatNameItem.setNumericShortcut('t');
                                    chatNameItem.setIcon(R.drawable.message_icon);
                                    groupBadge.setVisible(true);
                                    groupBadge.setNumber(groupBadge.getNumber() + 1);
                                }
                            }
                        }
                    }
                }
                if (isNew) {
                    if (message.getReceiver().length() != 119) {
                        subPrivateMenu.add(Chat.PRIVATE_CHAT, Menu.NONE, Menu.NONE,
                                message.getSender())
                                .setContentDescription(message.getReceiver())
                                .setIcon(R.drawable.message_icon)
                                .setNumericShortcut('t');
                        privateBadge.setVisible(true);
                        privateBadge.setNumber(privateBadge.getNumber() + 1);
                        user.getChats().add(message.getReceiver());
                        FirebaseRDBClient.updateChatsProfile(user.getChats());
                    } else {
                        FirebaseRDBClient.getGroupChat(message.getReceiver());
                    }
                }
            }
        }
    }

    @Override
    public void onUserExists(boolean isExists) {

    }

    @Override
    public void applyChat(Chat chat, boolean isFirstLoad) {
        if (isFirstLoad) {
            if (chat.getType() == Chat.GROUP_CHAT)
                groupChats.add(chat);
            else
                privateChats.add(chat);
            loading++;
            initMenu();
        }
        else{
            if (isPrivateChat) {
                for (String user : chat.getUsers()){
                    if (!user.equals(Objects.requireNonNull(FirebaseAuthClient
                            .getCurrentUser()).getDisplayName())){
                        FirebaseRDBClient.getUserByUsername(user);
                    }
                }

                privateMessages.clear();
                privateFragment.loadAdapter();
                for (Message message : chat.getMessages()) {
                    privateMessages.add(message);
                    privateFragment.notifyAdapter();
                }
            }
            else {
                homeFragment.setChat(chat);
                for (String user : chat.getUsers()){
                    if (!user.equals(Objects.requireNonNull(FirebaseAuthClient
                            .getCurrentUser()).getDisplayName())){
                        FirebaseRDBClient.getUserByUsername(user);
                    }
                }

                groupMessages.clear();
                groupFragment.loadAdapter();
                for (Message message : chat.getMessages()) {
                    groupMessages.add(message);
                    groupFragment.notifyAdapter();
                }
                groupFragment.setReceivers(chat.getName(), chat.getKey());
                groupFragment.setSender(this.user);
            }
            privateFragment.setChatReady();
            groupFragment.setChatReady();
        }
    }

    @Override
    public void applyUser(User user) {
        if (user.getUsername().equals(Objects.requireNonNull(FirebaseAuthClient
                .getCurrentUser()).getDisplayName())) {
            this.user = user;
            if (user.getChats() != null) {
                numberOfChats = user.getChats().size();
                for (String chat : user.getChats()) {
                    FirebaseRDBClient.getChat(chat, true);
                }
            }
            else
                numberOfChats = 0;
            if (user.getPhotoUrl() == null) {
                FirebaseStorageClient.getPhoto(user.getUsername(), true);
            }
            else
                loading++;
            SharedPreferences sharedPreferences = getSharedPreferences("pingApp", MODE_PRIVATE);
            String token = sharedPreferences.getString("token", null);
            if (!user.getToken().equals(token)) {
                FirebaseRDBClient.updateToken(token);
            }
            loading++;
            initMenu();
        }
        else{
            if (isPrivateChat){
                privateFragment.setReceiver(user);
                privateFragment.setSender(this.user);
            }
            else{
                groupFragment.getUserReceivers().add(user);
            }
        }
    }

    @Override
    public void applyPhotoUri(Uri uri, boolean firstLoad) {
        if (uri != null && uri.toString().contains(AppConfig.getInstance().getImage_prefix())) {
            if (isPrivateChat)
                privateFragment.setImageMessageUri(uri);
            else
                groupFragment.setImageMessageUri(uri);
        } else {
            if (uri != null) {
                user.setPhotoUrl(uri.toString());
                FirebaseRDBClient.uploadPhotoUrl(uri.toString());
            }
            if (firstLoad) {
                loading++;
                initMenu();
            } else {
                FirebaseRDBClient.updateAllImages(uri, groupChats, privateChats);
                updateImagesInCurrChat(uri.toString(), null,true, false);
                updateImagesInCurrChat(uri.toString(), FirebaseAuthClient.getCurrentUser().getDisplayName(),false, false);
                Glide.with(this).load(uri).circleCrop().into(photo);
            }
        }
    }

    private void updateImagesInCurrChat(String uri, String sender, boolean inPrivateChat, boolean onReceiverPhones){
        if (inPrivateChat) {
            for (Message message : privateMessages) {
                if(onReceiverPhones){
                    if (!message.getSender().equals(Objects.requireNonNull(FirebaseAuthClient.getCurrentUser()).getDisplayName())){
                        message.setSenderPhotoUrl(uri);
                    }
                } else {
                    if (message.getSender().equals(Objects.requireNonNull(FirebaseAuthClient.getCurrentUser()).getDisplayName()))
                        message.setSenderPhotoUrl(uri);
                }
            }
            privateFragment.loadAdapter();
        }
        else {
            for (Message message : groupMessages) {
                if (sender.equals(message.getSender())){
                    message.setSenderPhotoUrl(uri);
                }
            }
            groupFragment.loadAdapter();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        PingMessagingService.APP_IS_UP = false;
    }

    @Override
    public void applyResponse(String text) {
        PingSnackbar snackbar = new PingSnackbar(getResources(), getTheme(), drawerLayout);
        snackbar.set(text, -1, -1);
        snackbar.show();
    }

    @Override
    public void startPrivateChat(String username) {
        boolean isNew = true;
        MenuItem usernameItem;
        if (subPrivateMenu != null) {
            for (int i = 0; i < subPrivateMenu.size(); i++) {
                usernameItem = subPrivateMenu.getItem(i);
                if (username.equals(usernameItem.getTitle().toString())) {
                    isNew = false;
                    usernameItem.setIcon(R.drawable.message_icon_white).setNumericShortcut('f');
                    if (privateBadge.getNumber() > 0)
                        privateBadge.setNumber(privateBadge.getNumber() - 1);
                    if (privateBadge.getNumber() == 0)
                        privateBadge.setVisible(false);
                    Chat.LATEST_PRIVATE_CHAT_UID = usernameItem.getContentDescription().toString();
                    Chat.LATEST_PRIVATE_CHAT = usernameItem.getTitle().toString();
                }
            }
        }
        else
            isNew = true;
        isPrivateChat = true;
        if(isNew) {
            String chatUUID = UUID.randomUUID().toString() + UUID.randomUUID().toString();
            FirebaseRDBClient.createPrivateChat(new Chat(chatUUID, null,
                    new ArrayList<>(Arrays.asList(username, user.getUsername())), 0));

            FirebaseRDBClient.getUserByUsername(username);
            privateMessages.clear();
            privateFragment.loadAdapter();

            subPrivateMenu.add(Chat.PRIVATE_CHAT, Menu.NONE, Menu.NONE,
                    username)
                    .setContentDescription(chatUUID)
                    .setIcon(R.drawable.message_icon_white)
                    .setNumericShortcut('f');

            Chat.LATEST_PRIVATE_CHAT_UID = chatUUID;
            Chat.LATEST_PRIVATE_CHAT = username;
        }
        else {
            FirebaseRDBClient.getChat(Chat.LATEST_PRIVATE_CHAT_UID, false);
        }
        Objects.requireNonNull(tabLayout.getTabAt(1)).select();
        privateFragment.setChatNotReady();
        groupFragment.setChatNotReady();
        Chat.CURRENT_CHAT = Chat.LATEST_PRIVATE_CHAT_UID;
    }

    @Override
    public void applyUsers(ArrayList<String> users, boolean isStart) {
        homeFragment.setNames(users, isStart);
        groupFragment.setNames(users, isStart, groupMessages, groupFragment);
    }

    @Override
    public void chatCreated(String chatName, String chatKey) {
        groupChatStart(chatName, chatKey, true);
    }

    @Override
    public void applyGroupChat(Chat chat) {
        subGroupMenu.add(Chat.GROUP_CHAT, Menu.NONE, Menu.NONE,
                chat.getName())
                .setContentDescription(chat.getKey())
                .setIcon(R.drawable.message_icon)
                .setNumericShortcut('t');
        groupBadge.setVisible(true);
        groupBadge.setNumber(groupBadge.getNumber() + 1);
        user.getChats().add(chat.getName());
        FirebaseRDBClient.updateChatsProfile(user.getChats());
    }

    @Override
    public void privateChatCreated(String key) {
        user.getChats().add(key);
        FirebaseRDBClient.updateChatsProfile(user.getChats());
    }

    @Override
    public void applyChatNames(ArrayList<String> chatNames, boolean isCreate) {
        homeFragment.setChatNames(chatNames, isCreate);
    }

    @Override
    public void applyChatToJoin(Chat chat) {
        for (Chat tempChat : groupChats) {
            if (tempChat.getName().equals(chat.getName())){
                chat.setType(-1);
                break;
            }
        }
        homeFragment.setChat(chat);
    }

    private void finishActivity(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finishAfterTransition();
            }
        }, 1500);
    }

    @Override
    public void joinChat(String chatName, String chatKey, boolean isNew) {
        groupChatStart(chatName, chatKey, isNew);
        if (isNew)
            FirebaseRDBClient.addUserToGroupChatUsers(chatKey, FirebaseAuthClient.getCurrentUser().getDisplayName());
    }

    private void groupChatStart(String chatName, String chatKey, boolean isNew){
        isPrivateChat = false;

        groupMessages.clear();
        groupFragment.loadAdapter();

        if (isNew) {
            subGroupMenu.add(Chat.GROUP_CHAT, Menu.NONE, Menu.NONE,
                    chatName)
                    .setContentDescription(chatKey)
                    .setIcon(R.drawable.message_icon_white)
                    .setNumericShortcut('f');
            if (user.getChats() == null)
                user.setChats(new ArrayList<String>());
            user.getChats().add(chatName);
            FirebaseRDBClient.updateChatsProfile(user.getChats());
        }

        Chat.LATEST_GROUP_CHAT_UID = chatKey;
        Chat.LATEST_GROUP_CHAT = chatName;

        Objects.requireNonNull(tabLayout.getTabAt(2)).select();
        groupFragment.setReceivers(chatName, chatKey);
        groupFragment.setSender(this.user);

        privateFragment.setChatNotReady();
        groupFragment.setChatNotReady();

        FirebaseRDBClient.getChatByUID(Chat.LATEST_GROUP_CHAT_UID, false);
        Chat.CURRENT_CHAT = Chat.LATEST_GROUP_CHAT_UID;

    }

    @Override
    public void leaveChat(String chatName) {
        int size = subGroupMenu.size();
        MenuItem menuItem;
        for (int i = 0; i < size; i++){
            menuItem = subGroupMenu.getItem(i);
            if (!menuItem.getTitle().equals(chatName)){
                subGroupMenu.add(Chat.GROUP_CHAT, Menu.NONE, Menu.NONE,
                        menuItem.getTitle().toString())
                        .setContentDescription(menuItem.getContentDescription().toString())
                        .setIcon(menuItem.getIcon())
                        .setNumericShortcut(menuItem.getNumericShortcut());
            }
        }

        for (int i = 0; i < size; i++){
            subGroupMenu.removeItem(0);
        }

        user.getChats().remove(chatName);
        FirebaseRDBClient.updateChatsProfile( user.getChats());
        Objects.requireNonNull(tabLayout.getTabAt(0)).select();
        privateFragment.setChatNotReady();
        groupFragment.setChatNotReady();
    }
}