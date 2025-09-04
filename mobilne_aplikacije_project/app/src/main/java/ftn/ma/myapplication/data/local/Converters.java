package ftn.ma.myapplication.data.local;

import androidx.room.TypeConverter;
import java.util.Date;
import ftn.ma.myapplication.data.model.Task;

public class Converters {

    // Konverteri za Date <-> Long
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    // Konverteri za Difficulty Enum <-> String
    @TypeConverter
    public static Task.Difficulty toDifficulty(String value) {
        return value == null ? null : Task.Difficulty.valueOf(value);
    }

    @TypeConverter
    public static String fromDifficulty(Task.Difficulty value) {
        return value == null ? null : value.name();
    }

    // Konverteri za Importance Enum <-> String
    @TypeConverter
    public static Task.Importance toImportance(String value) {
        return value == null ? null : Task.Importance.valueOf(value);
    }

    @TypeConverter
    public static String fromImportance(Task.Importance value) {
        return value == null ? null : value.name();
    }

    // Konverteri za Status Enum <-> String
    @TypeConverter
    public static Task.Status toStatus(String value) {
        return value == null ? null : Task.Status.valueOf(value);
    }

    @TypeConverter
    public static String fromStatus(Task.Status value) {
        return value == null ? null : value.name();
    }

    // Uklonjen je blok za RepetitionUnit jer više ne postoji u Task modelu

}