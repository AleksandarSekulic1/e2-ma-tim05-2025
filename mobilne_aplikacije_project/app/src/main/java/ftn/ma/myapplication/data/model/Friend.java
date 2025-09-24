package ftn.ma.myapplication.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "friends")
public class Friend {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private long userId;
    private long friendId;
    private String status;
    
    public Friend() {}
    
    public Friend(long userId, long friendId, String status) {
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
    }
    
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    
    public long getFriendId() { return friendId; }
    public void setFriendId(long friendId) { this.friendId = friendId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
