package ftn.ma.myapplication.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "chat_messages")
public class ChatMessage {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private long allianceId;
    private long senderId;
    private String senderUsername;
    private String message;
    private Date timestamp;
    
    public ChatMessage() {}
    
    public ChatMessage(long allianceId, long senderId, String senderUsername, String message) {
        this.allianceId = allianceId;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.message = message;
        this.timestamp = new Date();
    }
    
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public long getAllianceId() { return allianceId; }
    public void setAllianceId(long allianceId) { this.allianceId = allianceId; }
    
    public long getSenderId() { return senderId; }
    public void setSenderId(long senderId) { this.senderId = senderId; }
    
    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
