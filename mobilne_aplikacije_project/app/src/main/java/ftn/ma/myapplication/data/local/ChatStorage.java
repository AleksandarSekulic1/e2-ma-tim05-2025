package ftn.ma.myapplication.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ftn.ma.myapplication.data.model.ChatMessage;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatStorage {
    private static final String PREF_NAME = "chat_storage";
    private static final String KEY_MESSAGES = "messages_";
    private static final Gson gson = new Gson();

    // Sačuvaj poruku
    public static void saveMessage(Context context, ChatMessage message) {
        List<ChatMessage> messages = getMessagesForAlliance(context, message.getAllianceId());
        messages.add(message);
        
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = gson.toJson(messages);
        prefs.edit().putString(KEY_MESSAGES + message.getAllianceId(), json).apply();
    }
    
    // Dobij sve poruke za alliance
    public static List<ChatMessage> getMessagesForAlliance(Context context, long allianceId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_MESSAGES + allianceId, null);
        
        if (json == null) {
            return new ArrayList<>();
        }
        
        Type listType = new TypeToken<ArrayList<ChatMessage>>(){}.getType();
        return gson.fromJson(json, listType);
    }
    
    // Kreiraj novu poruku
    public static ChatMessage createMessage(long allianceId, long senderId, String senderUsername, String messageText) {
        ChatMessage message = new ChatMessage(allianceId, senderId, senderUsername, messageText);
        message.setTimestamp(new Date());
        return message;
    }
}