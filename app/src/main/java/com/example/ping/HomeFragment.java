package com.example.ping;

import android.os.Bundle;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private StartPrivateDialog startPrivateDialog;
    private CreateChatDialog createChatDialog;
    private JoinChatDialog joinChatDialog;
    private String token;

    public void setToken(String token){
        this.token = token;
    }

    public void setNames(ArrayList<String> users, boolean isStart){
        if (isStart) {
            startPrivateDialog.setNames(users);
            startPrivateDialog.show(getFragmentManager(), "start private chat dialog");
        }
    }

    public void setChat(Chat chat){
        joinChatDialog.setChat(chat);
    }

    public void setChatNames(ArrayList<String> chatNames, boolean isCreate){
        if (isCreate) {
            createChatDialog.setChatNames(chatNames);
            createChatDialog.show(getFragmentManager(), "create chat dialog");
        }
        else {
            joinChatDialog.setChatNames(chatNames);
            joinChatDialog.show(getFragmentManager(), "join chat dialog");
        }

    }

    public void setStartPrivateDialog(StartPrivateDialog startPrivateDialog){this.startPrivateDialog = startPrivateDialog;}
    public void setCreateChatDialog(CreateChatDialog createChatDialog){this.createChatDialog = createChatDialog;}
    public void setJoinChatDialog(JoinChatDialog joinChatDialog){this.joinChatDialog = joinChatDialog;}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        final AppCompatButton privateChatBtn = rootView.findViewById(R.id.private_chat_btn);
        final AppCompatButton createChatBtn = rootView.findViewById(R.id.create_group_chat_btn);
        final AppCompatButton joinChatBtn = rootView.findViewById(R.id.join_chat_btn);

        privateChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPrivateDialog = new StartPrivateDialog();
                privateChatBtn.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.button_click));
                FirebaseRDBClient.getUsernames(true);
            }
        });
        joinChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                joinChatDialog = new JoinChatDialog(token);
                joinChatBtn.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.button_click));
                FirebaseRDBClient.getChatNames(false);

            }
        });
        createChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createChatDialog = new CreateChatDialog(token);
                createChatBtn.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.button_click));
                FirebaseRDBClient.getChatNames(true);
            }
        });

        return rootView;
    }
}
