package ftn.ma.myapplication.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

import java.util.Date;

/**
 * Model klasa za poruke u savezu
 */
@Entity(tableName = "chat_messages")
public class ChatMessage {
    
    @PrimaryKey(autoGenerate = true)
    private int messageId;
    
    private int allianceId;       // ID saveza
    private int senderId;         // ID pošiljaoca
    private String senderUsername; // Username pošiljaoca
    private String content;       // Sadržaj poruke
    private MessageType type;     // Tip poruke
    private Date timestamp;       // Vreme slanja
    private boolean isRead;       // Da li je poruka pročitana
    
    public enum MessageType {
        TEXT,           // Obična tekstualna poruka
        SYSTEM,         // Sistemska poruka (npr. "X se pridružio savezu")
        ANNOUNCEMENT    // Najava od vođe
    }
    
    public ChatMessage() {
        this.timestamp = new Date();
        this.type = MessageType.TEXT;
        this.isRead = false;
    }
    
    @Ignore
    public ChatMessage(int allianceId, int senderId, String senderUsername, String content) {
        this();
        this.allianceId = allianceId;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.content = content;
    }
    
    @Ignore
    public ChatMessage(int allianceId, String content, MessageType type) {
        this();
        this.allianceId = allianceId;
        this.content = content;
        this.type = type;
        this.senderId = -1; // Sistemska poruka
        this.senderUsername = "Sistem";
    }
    
    /**
     * Kreira sistemsku poruku
     */
    public static ChatMessage createSystemMessage(int allianceId, String content) {
        return new ChatMessage(allianceId, content, MessageType.SYSTEM);
    }
    
    /**
     * Označava poruku kao pročitanu
     */
    public void markAsRead() {
        this.isRead = true;
    }
    
    /**
     * Proverava da li je poruka od trenutnog korisnika
     */
    public boolean isFromUser(int userId) {
        return senderId == userId;
    }
    
    /**
     * Proverava da li je sistemska poruka
     */
    public boolean isSystemMessage() {
        return type == MessageType.SYSTEM;
    }
    
    /**
     * Formatira timestamp za prikaz
     */
    public String getFormattedTime() {
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("HH:mm");
        return formatter.format(timestamp);
    }
    
    /**
     * Formatira datum za prikaz
     */
    public String getFormattedDate() {
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("dd.MM.yyyy");
        return formatter.format(timestamp);
    }
    
    /**
     * Formatira pun datum i vreme za prikaz
     */
    public String getFormattedDateTime() {
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm");
        return formatter.format(timestamp);
    }
    
    // Getters and Setters
    public int getMessageId() { return messageId; }
    public void setMessageId(int messageId) { this.messageId = messageId; }
    
    public int getAllianceId() { return allianceId; }
    public void setAllianceId(int allianceId) { this.allianceId = allianceId; }
    
    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }
    
    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
    
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}