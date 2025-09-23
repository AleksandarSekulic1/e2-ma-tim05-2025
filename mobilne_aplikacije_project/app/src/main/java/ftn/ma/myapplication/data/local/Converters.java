package ftn.ma.myapplication.data.local;

import androidx.room.TypeConverter;
import java.util.Date;
import ftn.ma.myapplication.data.model.Task;
import ftn.ma.myapplication.data.model.Potion;
import ftn.ma.myapplication.data.model.Clothing;
import ftn.ma.myapplication.data.model.Weapon;
import ftn.ma.myapplication.data.model.Friend;
import ftn.ma.myapplication.data.model.Alliance;
import ftn.ma.myapplication.data.model.AllianceInvitation;
import ftn.ma.myapplication.data.model.ChatMessage;
import ftn.ma.myapplication.data.model.AllianceMember;

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

    // Konverteri za PotionEffect Enum <-> String
    @TypeConverter
    public static Potion.PotionEffect toPotionEffect(String value) {
        return value == null ? null : Potion.PotionEffect.valueOf(value);
    }

    @TypeConverter
    public static String fromPotionEffect(Potion.PotionEffect value) {
        return value == null ? null : value.name();
    }

    // Konverteri za ClothingType Enum <-> String
    @TypeConverter
    public static Clothing.ClothingType toClothingType(String value) {
        return value == null ? null : Clothing.ClothingType.valueOf(value);
    }

    @TypeConverter
    public static String fromClothingType(Clothing.ClothingType value) {
        return value == null ? null : value.name();
    }

    // Konverteri za WeaponType Enum <-> String
    @TypeConverter
    public static Weapon.WeaponType toWeaponType(String value) {
        return value == null ? null : Weapon.WeaponType.valueOf(value);
    }

    @TypeConverter
    public static String fromWeaponType(Weapon.WeaponType value) {
        return value == null ? null : value.name();
    }

    // Konverteri za FriendshipStatus Enum <-> String
    @TypeConverter
    public static Friend.FriendshipStatus toFriendshipStatus(String value) {
        return value == null ? null : Friend.FriendshipStatus.valueOf(value);
    }

    @TypeConverter
    public static String fromFriendshipStatus(Friend.FriendshipStatus value) {
        return value == null ? null : value.name();
    }

    // Konverteri za AllianceStatus Enum <-> String
    @TypeConverter
    public static Alliance.AllianceStatus toAllianceStatus(String value) {
        return value == null ? null : Alliance.AllianceStatus.valueOf(value);
    }

    @TypeConverter
    public static String fromAllianceStatus(Alliance.AllianceStatus value) {
        return value == null ? null : value.name();
    }

    // Konverteri za InvitationStatus Enum <-> String
    @TypeConverter
    public static AllianceInvitation.InvitationStatus toInvitationStatus(String value) {
        return value == null ? null : AllianceInvitation.InvitationStatus.valueOf(value);
    }

    @TypeConverter
    public static String fromInvitationStatus(AllianceInvitation.InvitationStatus value) {
        return value == null ? null : value.name();
    }

    // Konverteri za MessageType Enum <-> String
    @TypeConverter
    public static ChatMessage.MessageType toMessageType(String value) {
        return value == null ? null : ChatMessage.MessageType.valueOf(value);
    }

    @TypeConverter
    public static String fromMessageType(ChatMessage.MessageType value) {
        return value == null ? null : value.name();
    }

    // Konverteri za MemberRole Enum <-> String
    @TypeConverter
    public static AllianceMember.MemberRole toMemberRole(String value) {
        return value == null ? null : AllianceMember.MemberRole.valueOf(value);
    }

    @TypeConverter
    public static String fromMemberRole(AllianceMember.MemberRole value) {
        return value == null ? null : value.name();
    }

}