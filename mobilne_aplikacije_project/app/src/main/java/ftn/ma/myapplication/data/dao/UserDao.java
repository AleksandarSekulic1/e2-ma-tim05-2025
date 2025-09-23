package ftn.ma.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ftn.ma.myapplication.data.model.User;

/**
 * DAO za upravljanje korisnicima u bazi podataka
 */
@Dao
public interface UserDao {
    
    @Insert
    long insertUser(User user);
    
    @Update
    void updateUser(User user);
    
    @Delete
    void deleteUser(User user);
    
    @Query("SELECT * FROM users WHERE id = :id")
    User getUserById(int id);
    
    @Query("SELECT * FROM users WHERE email = :email")
    User getUserByEmail(String email);
    
    @Query("SELECT * FROM users")
    List<User> getAllUsers();
    
    @Query("SELECT * FROM users WHERE isActive = 1")
    List<User> getActiveUsers();
    
    @Query("DELETE FROM users")
    void deleteAllUsers();
}