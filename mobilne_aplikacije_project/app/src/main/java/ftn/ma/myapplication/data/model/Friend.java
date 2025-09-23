package ftn.ma.myapplication.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

import java.util.Date;

/**
 * Model klasa za prijateljstva između korisnika
 */
@Entity(tableName = "friends")
public class Friend {
    
    @PrimaryKey(autoGenerate = true)
    private int friendshipId;
    
    private int userId;           // ID korisnika koji je poslao zahtev
    private int friendId;         // ID korisnika koji je primio zahtev  
    private String userUsername;  // Username korisnika koji je poslao
    private String friendUsername; // Username korisnika koji je primio
    private FriendshipStatus status;
    private Date createdAt;
    private Date acceptedAt;
    
    public enum FriendshipStatus {
        PENDING,    // Zahtev poslat, čeka se odgovor
        ACCEPTED,   // Prijateljstvo prihvaćeno
        REJECTED,   // Zahtev odbačen
        BLOCKED     // Korisnik blokiran
    }
    
    public Friend() {
        this.createdAt = new Date();
        this.status = FriendshipStatus.PENDING;
    }
    
    @Ignore
    public Friend(int userId, int friendId, String userUsername, String friendUsername) {
        this();
        this.userId = userId;
        this.friendId = friendId;
        this.userUsername = userUsername;
        this.friendUsername = friendUsername;
    }
    
    /**
     * Proverava da li su dva korisnika prijatelji
     */
    public boolean areFriends() {
        return status == FriendshipStatus.ACCEPTED;
    }
    
    /**
     * Prihvata zahtev za prijateljstvo
     */
    public void acceptFriendship() {
        this.status = FriendshipStatus.ACCEPTED;
        this.acceptedAt = new Date();
    }
    
    /**
     * Odbacuje zahtev za prijateljstvo
     */
    public void rejectFriendship() {
        this.status = FriendshipStatus.REJECTED;
    }
    
    /**
     * Vraća ID drugog korisnika u prijateljstvu
     */
    public int getOtherUserId(int currentUserId) {
        return (currentUserId == userId) ? friendId : userId;
    }
    
    /**
     * Vraća username drugog korisnika u prijateljstvu
     */
    public String getOtherUsername(int currentUserId) {
        return (currentUserId == userId) ? friendUsername : userUsername;
    }
    
    // Getters and Setters
    public int getFriendshipId() { return friendshipId; }
    public void setFriendshipId(int friendshipId) { this.friendshipId = friendshipId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public int getFriendId() { return friendId; }
    public void setFriendId(int friendId) { this.friendId = friendId; }
    
    public String getUserUsername() { return userUsername; }
    public void setUserUsername(String userUsername) { this.userUsername = userUsername; }
    
    public String getFriendUsername() { return friendUsername; }
    public void setFriendUsername(String friendUsername) { this.friendUsername = friendUsername; }
    
    public FriendshipStatus getStatus() { return status; }
    public void setStatus(FriendshipStatus status) { this.status = status; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(Date acceptedAt) { this.acceptedAt = acceptedAt; }
}