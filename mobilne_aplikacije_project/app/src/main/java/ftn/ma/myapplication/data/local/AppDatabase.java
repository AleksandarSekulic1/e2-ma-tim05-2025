package ftn.ma.myapplication.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import ftn.ma.myapplication.data.model.Category;
import ftn.ma.myapplication.data.model.Task;
import ftn.ma.myapplication.data.model.User;
import ftn.ma.myapplication.data.model.Potion;
import ftn.ma.myapplication.data.model.Clothing;
import ftn.ma.myapplication.data.model.Weapon;
import ftn.ma.myapplication.data.dao.EquipmentDao;
import ftn.ma.myapplication.data.dao.UserDao;

// 1. Definišemo da je ovo klasa baze, navodimo sve entitete i verziju
@TypeConverters({Converters.class})
@Database(entities = {Category.class, Task.class, User.class, Potion.class, Clothing.class, Weapon.class}, version = 5)
public abstract class AppDatabase extends RoomDatabase {

    // 2. Apstraktna metoda koja vraća naš DAO (daljinski upravljač)
    public abstract CategoryDao categoryDao();
    public abstract TaskDao taskDao();
    public abstract UserDao userDao();
    public abstract EquipmentDao equipmentDao();
    // 3. Deo koda koji implementira Singleton pattern
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    // Kreiramo instancu baze podataka
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
