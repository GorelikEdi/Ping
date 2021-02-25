package com.example.ping;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.UUID;

public class ChatFragment extends Fragment {

    private TextView usernameHeader = null;
    private ImageView photoHeader;
    private MessageAdapter messageAdapter;
    private ArrayList<Message> messages;
    private RecyclerView recyclerView;
    private User receiver;
    private User sender;
    private String receivers;
    private ArrayList<User> userReceivers = new ArrayList<>();
    private LinearProgressIndicator progress;
    private Context context;
    private final int SEND_IMAGE = 2;
    public static int REQUEST_LOCATION_PERMISSION = 3;
    private ImageDialog imageDialog;
    private FusedLocationProviderClient fusedLocationClient;
    private PingSnackbar snackbar;
    private InviteDialog inviteDialog;
    private ImageButton chatMenu;
    private ChatFragmentListener listener;
    private ImageButton sendMessage;
    private ImageButton shareBtn;

    interface ChatFragmentListener{
        void leaveChat(String chatName);
    }

    public ChatFragment() {
    }

    public void setNames(ArrayList<String> users, boolean isStart, ArrayList<Message> messages, ChatFragment groupFragment){
        if (!isStart) {
            inviteDialog.setNames(users);
            inviteDialog.setChatKey(receivers);
            inviteDialog.setGroupFragment(groupFragment);
            inviteDialog.setGroupMessages(messages);
            inviteDialog.show(getFragmentManager(), "invite dialog");
        }
    }

    public void setInviteDialog(InviteDialog inviteDialog){this.inviteDialog = inviteDialog;}


    public void setUsernameHeader(String newUsernameHeader) {
        usernameHeader.setText(newUsernameHeader);
    }

    public void setPhotoHeader(String url) {
        if (url != null)
            Glide.with(context).load(url).circleCrop().into(photoHeader);
        else
            Glide.with(context).load(R.drawable.logo).circleCrop().into(photoHeader);
    }

    public void setImageDialog(ImageDialog imageDialog) {
        this.imageDialog = imageDialog;
    }

    public void notifyAdapter() {
        messageAdapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);
    }

    public void loadAdapter() {
        if (messageAdapter != null)
            messageAdapter.notifyDataSetChanged();
    }

    public void setChatReady() {
        if (progress != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progress.hide();
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }, 300);
        }
    }

    public void setChatNotReady() {
        if (progress != null) {
            progress.show();
            recyclerView.setVisibility(View.INVISIBLE);
        }
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public void setReceiver(User user) {
        receiver = user;
        chatMenu.setVisibility(View.INVISIBLE);
        setUsernameHeader(user.getUsername());
        setPhotoHeader(user.getPhotoUrl());
    }

    public void setReceivers(String chatName, String chatKey){
        receivers = chatKey;
        chatMenu.setVisibility(View.VISIBLE);
        setUsernameHeader(chatName);
        setPhotoHeader(null);
    }

    public ArrayList<User> getUserReceivers(){
        return userReceivers;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setSender(User user) {
        sender = user;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        FrameLayout frameLayout = view.findViewById(R.id.chat_layout);
        usernameHeader = view.findViewById(R.id.username_header);
        photoHeader = view.findViewById(R.id.photo_header);
        progress = view.findViewById(R.id.progress);
        shareBtn = view.findViewById(R.id.share_btn);
        chatMenu  = view.findViewById(R.id.chat_menu);
        context = getContext();
        Activity activity = getActivity();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        snackbar = new PingSnackbar(getResources(), activity.getTheme(), frameLayout);
        final InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        listener = (ChatFragmentListener) getActivity();
        chatMenu.setVisibility(View.INVISIBLE);
        sendMessage = view.findViewById(R.id.send_message_btn);
        final EditText messageET = view.findViewById(R.id.message_edit_text);
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.send));
                if (Chat.CURRENT_CHAT != null) {
                    String tokenOrNotifKey;
                    if (receivers == null)
                        tokenOrNotifKey = receiver.getToken();
                    else
                        tokenOrNotifKey = receivers;

                    if (!messageET.getText().toString().replace(" ", "").isEmpty() && !usernameHeader.getText().toString().isEmpty()) {
                        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        Message message = new Message(messageET.getText().toString(),
                                sender.getUsername(),
                                tokenOrNotifKey,
                                sender.getPhotoUrl());
                        PingMessagingService.sendMessage(message, context);
                        if (receivers == null) {
                            messages.add(message);
                            notifyAdapter();
                        }
                        FirebaseRDBClient.uploadMessage(message, Chat.CURRENT_CHAT);
                        messageET.setText("");
                    }
                }
            }
        });

        chatMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, view);
                popupMenu.inflate(R.menu.chat_menu);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                    if (menuItem.getItemId() == R.id.invite){
                        FirebaseRDBClient.getUsernames(false);
                        inviteDialog.setChatName(usernameHeader.getText().toString());
                    }
                    else if (menuItem.getItemId() == R.id.leave){
                        PingMessagingService.removeUserFromGroupChat(sender.getToken(), getContext(), usernameHeader.getText().toString(), Chat.CURRENT_CHAT);
                        listener.leaveChat(usernameHeader.getText().toString());
                        FirebaseRDBClient.leaveChat(sender.getUsername(), receivers);
                    }
                        return false;
                    }
                });

                popupMenu.show();
            }
        });


        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareBtn.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.send));

                PopupMenu popupMenu = new PopupMenu(context, view);
                popupMenu.inflate(R.menu.share_menu);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.share_location){
                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                        REQUEST_LOCATION_PERMISSION);
                            } else
                                sendLocation();
                        }
                        else{
                            sendImage();
                        }
                        return false;
                    }
                });

                popupMenu.show();
            }
        });

        FirebaseStorageClient.setFirebaseStorageClientListener(context);

        recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        messageAdapter = new MessageAdapter(messages);
        messageAdapter.setFragmentManager(getFragmentManager());
        messageAdapter.setMessagesListener(getActivity());
        messageAdapter.setImageDialog(imageDialog);

        recyclerView.setAdapter(messageAdapter);

        return view;
    }

    public void sendImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select image"), SEND_IMAGE);
    }

    @SuppressLint("MissingPermission")
    public void sendLocation() {
        CancellationToken token = new CancellationToken() {
            @Override
            public boolean isCancellationRequested() {
                return false;
            }

            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                return null;
            }
        };
        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, token)
                .addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();

                    String uri = "geo:" + lat + "," + lon + "?q=" + lat + "," + lon;
                    String tokenOrNotifKey;
                    if (receivers == null)
                        tokenOrNotifKey = receiver.getToken();
                    else
                        tokenOrNotifKey = receivers;
                    Message message = new Message(AppConfig.getInstance().getLocation_prefix() + uri,
                            sender.getUsername(),
                            tokenOrNotifKey,
                            sender.getPhotoUrl());
                    PingMessagingService.sendMessage(message, context);
                    if (receivers == null) {
                        messages.add(message);
                        notifyAdapter();
                    }
                    FirebaseRDBClient.uploadMessage(message, Chat.CURRENT_CHAT);
                }
                else{
                    snackbar.set("Enable location", R.drawable.network_icon, Snackbar.LENGTH_INDEFINITE);
                    snackbar.show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SEND_IMAGE && data.getData() != null) {
                String filename = AppConfig.getInstance().getImage_prefix() + UUID.randomUUID().toString();
                FirebaseStorageClient.uploadPhoto(data.getData(), filename);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendLocation();
            }
            else{
                snackbar.set("Allow location access", -1, Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }
    }

    public void setImageMessageUri(Uri uri){
        String tokenOrNotifKey;
        if (receivers == null)
            tokenOrNotifKey = receiver.getToken();
        else
            tokenOrNotifKey = receivers;
        Message message = new Message(uri.toString(),
                sender.getUsername(),
                tokenOrNotifKey,
                sender.getPhotoUrl());
        PingMessagingService.sendMessage(message, context);
        if (receivers == null) {
            messages.add(message);
            notifyAdapter();
        }
        FirebaseRDBClient.uploadMessage(message, Chat.CURRENT_CHAT);
    }
}