package com.gautam.socialfly.Model;

public class ChatModel {
    private String sender,receiver,message,type,messageID;
    boolean seen;

    public ChatModel()
    {
    }

    public ChatModel(String sender, String receiver, String message, String type, String messageID,boolean seen) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.type = type;
        this.messageID = messageID;
        this.seen=seen;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }
}
