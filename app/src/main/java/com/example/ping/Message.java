package com.example.ping;

public class Message {

    private String content;
    private String sender;
    private String receiver;
    private String timestamp;
    private String senderPhotoUrl;

    public Message(String content, String sender, String receiver, String senderPhotoUrl) {
        setContent(content);
        setSender(sender);
        setReceiver(receiver);
        setTimestamp(String.valueOf(PingTime.getEpoch()));
        setSenderPhotoUrl(senderPhotoUrl);
    }

    public Message(String content, String sender, String receiver, String timestamp, String senderPhotoUrl) {
        setContent(content);
        setSender(sender);
        setReceiver(receiver);
        setTimestamp(timestamp);
        setSenderPhotoUrl(senderPhotoUrl);
    }

    public Message() {}

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSenderPhotoUrl() {
        return senderPhotoUrl;
    }

    public void setSenderPhotoUrl(String senderPhotoUrl) {
        this.senderPhotoUrl = senderPhotoUrl;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
