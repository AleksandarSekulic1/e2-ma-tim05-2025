package ftn.ma.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ftn.ma.myapplication.data.model.Friend;

/**
 * DAO za upravljanje prijateljstvima u bazi podataka
 */
@Dao
public interface FriendDao {
    
    // CRUD operacije
    @Insert
    long insertFriend(Friend friend);
    
    @Update
    void updateFriend(Friend friend);
    
    @Delete
    void deleteFriend(Friend friend);
    
    // Pretraga prijatelja
    @Query("SELECT * FROM friends WHERE friendshipId = :friendshipId")
    Friend getFriendshipById(int friendshipId);
    
    @Query("SELECT * FROM friends WHERE (userId = :userId OR friendId = :userId) AND status = 'ACCEPTED'")
    List<Friend> getFriendsForUser(int userId);
    
    @Query("SELECT * FROM friends WHERE (userId = :userId OR friendId = :userId) AND status = 'PENDING'")
    List<Friend> getPendingFriendRequestsForUser(int userId);
    
    @Query("SELECT * FROM friends WHERE friendId = :userId AND status = 'PENDING'")
    List<Friend> getIncomingFriendRequests(int userId);
    
    @Query("SELECT * FROM friends WHERE userId = :userId AND status = 'PENDING'")
    List<Friend> getOutgoingFriendRequests(int userId);
    
    // Proverava da li prijateljstvo već postoji
    @Query("SELECT * FROM friends WHERE ((userId = :userId1 AND friendId = :userId2) OR (userId = :userId2 AND friendId = :userId1))")
    Friend checkExistingFriendship(int userId1, int userId2);
    
    // Pretraga korisnika koji nisu prijatelji
    @Query("SELECT * FROM friends WHERE " +
           "(userUsername LIKE '%' || :searchQuery || '%' OR friendUsername LIKE '%' || :searchQuery || '%') " +
           "AND ((userId = :currentUserId OR friendId = :currentUserId) OR " +
           "(userId != :currentUserId AND friendId != :currentUserId))")
    List<Friend> searchPotentialFriends(int currentUserId, String searchQuery);
    
    // Status operacije
    @Query("UPDATE friends SET status = :status WHERE friendshipId = :friendshipId")
    void updateFriendshipStatus(int friendshipId, Friend.FriendshipStatus status);
    
    @Query("UPDATE friends SET status = 'ACCEPTED', acceptedAt = datetime('now') WHERE friendshipId = :friendshipId")
    void acceptFriendship(int friendshipId);
    
    @Query("UPDATE friends SET status = 'REJECTED' WHERE friendshipId = :friendshipId")
    void rejectFriendship(int friendshipId);
    
    // Statistike
    @Query("SELECT COUNT(*) FROM friends WHERE (userId = :userId OR friendId = :userId) AND status = 'ACCEPTED'")
    int getFriendsCount(int userId);
    
    @Query("SELECT COUNT(*) FROM friends WHERE friendId = :userId AND status = 'PENDING'")
    int getPendingRequestsCount(int userId);
    
    // Cleanup operacije
    @Query("DELETE FROM friends WHERE status = 'REJECTED' AND createdAt < datetime('now', '-30 days')")
    void cleanupOldRejectedRequests();
    
    @Query("DELETE FROM friends")
    void deleteAllFriends();
}