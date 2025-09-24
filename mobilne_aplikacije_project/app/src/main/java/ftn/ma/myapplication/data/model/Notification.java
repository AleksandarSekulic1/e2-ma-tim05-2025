package ftn.ma.myapplication.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "notifications")
public class Notification {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private long userId;
    private String type;
    private String title;
    private String message;
    private String status;
    private Date createdAt;
    
    public Notification() {}
    
    public Notification(long userId, String type, String title, String message) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.status = "UNREAD";
        this.createdAt = new Date();
    }
    
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
