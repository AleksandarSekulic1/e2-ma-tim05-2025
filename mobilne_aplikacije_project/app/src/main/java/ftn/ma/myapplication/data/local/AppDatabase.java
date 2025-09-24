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
import ftn.ma.myapplication.data.model.Alliance;
import ftn.ma.myapplication.data.model.AllianceMember;
import ftn.ma.myapplication.data.model.ChatMessage;
import ftn.ma.myapplication.data.dao.EquipmentDao;
import ftn.ma.myapplication.data.dao.UserDao;
import ftn.ma.myapplication.data.dao.AllianceDao;
import ftn.ma.myapplication.data.dao.AllianceMemberDao;
import ftn.ma.myapplication.data.dao.ChatMessageDao;

// 1. Definišemo da je ovo klasa baze, navodimo sve entitete i verziju
@TypeConverters({Converters.class})
@Database(entities = {Category.class, Task.class, User.class, Potion.class, Clothing.class, Weapon.class, Alliance.class, AllianceMember.class, ChatMessage.class}, version = 6)
public abstract class AppDatabase extends RoomDatabase {

    // 2. Apstraktna metoda koja vraća naš DAO (daljinski upravljač)
    public abstract CategoryDao categoryDao();
    public abstract TaskDao taskDao();
    public abstract UserDao userDao();
    public abstract EquipmentDao equipmentDao();
    public abstract AllianceDao allianceDao();
    public abstract AllianceMemberDao allianceMemberDao();
    public abstract ChatMessageDao chatMessageDao();
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
