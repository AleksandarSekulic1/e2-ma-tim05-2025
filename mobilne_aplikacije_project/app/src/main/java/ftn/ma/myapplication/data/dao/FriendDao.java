package ftn.ma.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ftn.ma.myapplication.data.model.Friend;

@Dao
public interface FriendDao {
    
    @Insert
    long insert(Friend friend);
    
    @Update
    void update(Friend friend);
    
    @Delete
    void delete(Friend friend);
    
    @Query("SELECT * FROM friends WHERE userId = :userId")
    List<Friend> getFriendsForUser(long userId);
    
    @Query("SELECT * FROM friends WHERE userId = :userId AND friendId = :friendId")
    Friend getFriendship(long userId, long friendId);
    
    @Query("DELETE FROM friends WHERE userId = :userId AND friendId = :friendId")
    void removeFriendship(long userId, long friendId);
}
