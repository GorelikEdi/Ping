package com.example.ping;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<Message> messages;
    private MessageAdapterListener listener;
    private Context context;
    private FragmentManager fragmentManager;
    private final String IMAGE_DIALOG_TAG = "image dialog";
    private ImageDialog imageDialog;
    private final int MESSAGE = 0;
    private final int IMAGE = 1;
    private final int LOCATION = 2;
    private final int LABEL = 3;

    interface MessageAdapterListener {
        void usernameClicked(int position);
    }

    public void setImageDialog(ImageDialog imageDialog){
        this.imageDialog = imageDialog;
    }

    public void setFragmentManager(FragmentManager fragmentManager){
        this.fragmentManager = fragmentManager;
    }

    public MessageAdapter(ArrayList<Message> messages){
        this.messages = messages;
    }

    public void setMessagesListener(Context listener) {
        this.listener = (MessageAdapterListener) listener;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        private final TextView body;
        private final TextView time;
        private final TextView username;
        private final ImageView photo;

        public MessageViewHolder(@NonNull final View itemView) {
            super(itemView);
            body = itemView.findViewById(R.id.body);
            time = itemView.findViewById(R.id.time);
            username = itemView.findViewById(R.id.username);
            photo = itemView.findViewById(R.id.photo);

            username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.usernameClicked(getAdapterPosition());
                }
            });
        }
    }

    public class ImageMessageViewHolder extends RecyclerView.ViewHolder{

        private final TextView time;
        private final TextView username;
        private final ImageView photo;
        private final ImageView imageMessage;

        public ImageMessageViewHolder(@NonNull final View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.time);
            username = itemView.findViewById(R.id.username);
            photo = itemView.findViewById(R.id.photo);
            imageMessage = itemView.findViewById(R.id.image_message);

            username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.usernameClicked(getAdapterPosition());
                }
            });
        }
    }

    public class LocationMessageViewHolder extends RecyclerView.ViewHolder{

        private final TextView time;
        private final TextView username;
        private final ImageView photo;
        private final LottieAnimationView location;

        public LocationMessageViewHolder(@NonNull final View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.time);
            username = itemView.findViewById(R.id.username);
            photo = itemView.findViewById(R.id.photo);
            location = itemView.findViewById(R.id.location_anim);
        }
    }

    public class LabelViewHolder extends RecyclerView.ViewHolder{

        private final TextView labelTV;

        public LabelViewHolder(@NonNull final View itemView) {
            super(itemView);
            labelTV = itemView.findViewById(R.id.label);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        String content = message.getContent();
        if (content.contains(AppConfig.getInstance().getImage_prefix()))
            return IMAGE;
        else if (content.contains(AppConfig.getInstance().getLocation_prefix()))
            return LOCATION;
        else if (content.contains(AppConfig.getInstance().getLabel_prefix()))
            return LABEL;
        else
            return MESSAGE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();

        View view;
        switch (viewType) {
            case MESSAGE:
                view = LayoutInflater.from(context).inflate(R.layout.message_layout, parent, false);
                return new MessageViewHolder(view);
            case LOCATION:
                view = LayoutInflater.from(context).inflate(R.layout.location_message_layout, parent, false);
                return new LocationMessageViewHolder(view);
            case LABEL:
                view = LayoutInflater.from(context).inflate(R.layout.label_layout, parent, false);
                return new LabelViewHolder(view);
            default:
                view = LayoutInflater.from(context).inflate(R.layout.image_message_layout, parent, false);
                return new ImageMessageViewHolder(view);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        final String content = message.getContent();
        switch (holder.getItemViewType()){
            case MESSAGE:
                MessageViewHolder messageViewHolder = (MessageViewHolder) holder;
                messageViewHolder.body.setText(message.getContent());
                messageViewHolder.time.setText(PingTime.getTimeFromEpoch(Long.parseLong(message.getTimestamp())));
                messageViewHolder.username.setText(message.getSender());
                if (message.getSenderPhotoUrl() != null)
                    Glide.with(context).load(message.getSenderPhotoUrl()).circleCrop().into(messageViewHolder.photo);
                else
                    Glide.with(context).load(R.drawable.logo).circleCrop().into(messageViewHolder.photo);
                break;
            case IMAGE:
                ImageMessageViewHolder imageMessageViewHolder = (ImageMessageViewHolder) holder;
                imageMessageViewHolder.time.setText(PingTime.getTimeFromEpoch(Long.parseLong(message.getTimestamp())));
                imageMessageViewHolder.username.setText(message.getSender());
                if (message.getSenderPhotoUrl() != null)
                    Glide.with(context).load(message.getSenderPhotoUrl()).circleCrop().into(imageMessageViewHolder.photo);
                else
                    Glide.with(context).load(R.drawable.logo).circleCrop().into(imageMessageViewHolder.photo);
                Glide.with(context).load(Uri.parse(content)).into(imageMessageViewHolder.imageMessage);
                imageMessageViewHolder.imageMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        imageDialog.setUri(Uri.parse(content));
                        imageDialog.show(fragmentManager, IMAGE_DIALOG_TAG);
                    }
                });
                break;
            case LOCATION:
                final LocationMessageViewHolder locationMessageViewHolder = (LocationMessageViewHolder) holder;
                locationMessageViewHolder.time.setText(PingTime.getTimeFromEpoch(Long.parseLong(message.getTimestamp())));
                locationMessageViewHolder.username.setText(message.getSender());
                if (message.getSenderPhotoUrl() != null)
                    Glide.with(context).load(message.getSenderPhotoUrl()).circleCrop().into(locationMessageViewHolder.photo);
                else
                    Glide.with(context).load(R.drawable.logo).circleCrop().into(locationMessageViewHolder.photo);
                locationMessageViewHolder.location.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openMap(Uri.parse(content.replace(AppConfig.getInstance().getLocation_prefix(),"")));
                    }
                });
                break;
            case LABEL:
                final LabelViewHolder labelViewHolder = (LabelViewHolder) holder;
                labelViewHolder.labelTV.setText(message.getContent().replace(AppConfig.getInstance().getLabel_prefix(),""));
        }

    }

    private void openMap(Uri location){
        context.startActivity(new Intent(android.content.Intent.ACTION_VIEW, location));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
