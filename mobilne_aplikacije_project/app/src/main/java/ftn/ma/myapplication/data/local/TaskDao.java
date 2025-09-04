package ftn.ma.myapplication.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.Date; // Potreban import
import java.util.List;
import ftn.ma.myapplication.data.model.Task;

@Dao
public interface TaskDao {

    @Insert
    void insert(Task task);

    // --- NOVA METODA za unos više zadataka odjednom ---
    @Insert
    void insertAll(List<Task> tasks);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    // --- NOVA METODA za brisanje svih budućih ponavljanja iz serije ---
    @Query("DELETE FROM tasks WHERE recurringGroupId = :groupId AND executionTime >= :date")
    void deleteFutureByGroupId(long groupId, Date date);

    @Query("SELECT * FROM tasks")
    List<Task> getAllTasks();

    // --- NOVA POMOĆNA METODA za pronalaženje svih zadataka iz iste serije ---
    @Query("SELECT * FROM tasks WHERE recurringGroupId = :groupId")
    List<Task> findByGroupId(long groupId);
}
