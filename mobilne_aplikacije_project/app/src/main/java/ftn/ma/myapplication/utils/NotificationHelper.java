package ftn.ma.myapplication.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import ftn.ma.myapplication.activities.ChatActivity;
import ftn.ma.myapplication.activities.FriendsActivity;
import ftn.ma.myapplication.activities.AllianceActivity;
import ftn.ma.myapplication.R;

public class NotificationHelper {
    private static final String CHANNEL_FRIENDS = "friends_channel";
    private static final String CHANNEL_ALLIANCE = "alliance_channel";
    private static final String CHANNEL_CHAT = "chat_channel";
    
    private static final int NOTIFICATION_FRIEND_REQUEST = 1001;
    private static final int NOTIFICATION_ALLIANCE_INVITE = 1002;
    private static final int NOTIFICATION_CHAT_MESSAGE = 1003;
    
    private Context context;
    private NotificationManagerCompat notificationManager;
    
    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannels();
    }
    
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Friends channel
            NotificationChannel friendsChannel = new NotificationChannel(
                CHANNEL_FRIENDS,
                "Prijatelji",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            friendsChannel.setDescription("Obaveštenja o zahtevima za prijateljstvo");
            
            // Alliance channel
            NotificationChannel allianceChannel = new NotificationChannel(
                CHANNEL_ALLIANCE,
                "Savezi",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            allianceChannel.setDescription("Obaveštenja o savezima i pozivima");
            
            // Chat channel
            NotificationChannel chatChannel = new NotificationChannel(
                CHANNEL_CHAT,
                "Chat poruke",
                NotificationManager.IMPORTANCE_HIGH
            );
            chatChannel.setDescription("Obaveštenja o novim porukama");
            
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(friendsChannel);
                manager.createNotificationChannel(allianceChannel);
                manager.createNotificationChannel(chatChannel);
            }
        }
    }
    
    public void showFriendRequestNotification(String senderName) {
        Intent intent = new Intent(context, FriendsActivity.class);
        intent.putExtra("open_tab", "requests");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            NOTIFICATION_FRIEND_REQUEST, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_FRIENDS)
            .setSmallIcon(R.drawable.ic_person_add)
            .setContentTitle("Novi zahtev za prijateljstvo")
            .setContentText(senderName + " želi da vas doda kao prijatelja")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);
        
        notificationManager.notify(NOTIFICATION_FRIEND_REQUEST, builder.build());
    }
    
    public void showAllianceInviteNotification(String allianceName, String senderName) {
        Intent intent = new Intent(context, AllianceActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            NOTIFICATION_ALLIANCE_INVITE, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ALLIANCE)
            .setSmallIcon(R.drawable.ic_alliance)
            .setContentTitle("Poziv u savez")
            .setContentText(senderName + " vas poziva u savez \"" + allianceName + "\"")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);
        
        notificationManager.notify(NOTIFICATION_ALLIANCE_INVITE, builder.build());
    }
    
    public void showChatMessageNotification(String senderName, String message, String chatType, int chatId, String chatTitle) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("chat_type", chatType);
        intent.putExtra("chat_id", chatId);
        intent.putExtra("chat_title", chatTitle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            NOTIFICATION_CHAT_MESSAGE + chatId, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        String contentTitle = chatType.equals("friend") ? 
            "Poruka od " + senderName : 
            "Poruka u savezu " + chatTitle;
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_CHAT)
            .setSmallIcon(R.drawable.ic_chat)
            .setContentTitle(contentTitle)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);
        
        notificationManager.notify(NOTIFICATION_CHAT_MESSAGE + chatId, builder.build());
    }
    
    public void showAllianceMissionNotification(String allianceName, String missionType) {
        Intent intent = new Intent(context, AllianceActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            NOTIFICATION_ALLIANCE_INVITE + 100, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ALLIANCE)
            .setSmallIcon(R.drawable.ic_mission)
            .setContentTitle("Nova misija saveza")
            .setContentText("Savez \"" + allianceName + "\" je započeo " + missionType + " misiju!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);
        
        notificationManager.notify(NOTIFICATION_ALLIANCE_INVITE + 100, builder.build());
    }
    
    public void showSystemNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_CHAT)
            .setSmallIcon(R.drawable.ic_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true);
        
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
    
    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }
    
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }
    
    // Static helper methods for easy access
    public static void showFriendRequest(Context context, String senderName) {
        new NotificationHelper(context).showFriendRequestNotification(senderName);
    }
    
    public static void showAllianceInvite(Context context, String allianceName, String senderName) {
        new NotificationHelper(context).showAllianceInviteNotification(allianceName, senderName);
    }
    
    public static void showChatMessage(Context context, String senderName, String message, 
                                     String chatType, int chatId, String chatTitle) {
        new NotificationHelper(context).showChatMessageNotification(senderName, message, chatType, chatId, chatTitle);
    }
    
    public static void showAllianceMission(Context context, String allianceName, String missionType) {
        new NotificationHelper(context).showAllianceMissionNotification(allianceName, missionType);
    }
    
    public static void showSystem(Context context, String title, String message) {
        new NotificationHelper(context).showSystemNotification(title, message);
    }
}