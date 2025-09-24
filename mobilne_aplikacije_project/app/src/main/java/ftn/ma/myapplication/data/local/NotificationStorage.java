package ftn.ma.myapplication.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NotificationStorage {
    private static final String PREF_NAME = "notification_storage";
    private static final String KEY_FRIEND_REQUESTS = "friend_requests";
    
    private SharedPreferences prefs;
    private Gson gson;
    
    public NotificationStorage(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    // Friend Request klasa
    public static class FriendRequest {
        public String fromUserId;
        public String fromUsername;
        public String toUserId;
        public String toUsername;
        public long timestamp;
        public String status; // "pending", "accepted", "rejected"
        
        public FriendRequest() {}
        
        public FriendRequest(String fromUserId, String fromUsername, String toUserId, String toUsername) {
            this.fromUserId = fromUserId;
            this.fromUsername = fromUsername;
            this.toUserId = toUserId;
            this.toUsername = toUsername;
            this.timestamp = System.currentTimeMillis();
            this.status = "pending";
        }
    }
    
    // Friend Requests metode
    public void saveFriendRequest(FriendRequest request) {
        List<FriendRequest> requests = getFriendRequests();
        requests.add(request);
        
        String json = gson.toJson(requests);
        prefs.edit().putString(KEY_FRIEND_REQUESTS, json).apply();
    }
    
    public List<FriendRequest> getFriendRequests() {
        String json = prefs.getString(KEY_FRIEND_REQUESTS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        
        Type type = new TypeToken<List<FriendRequest>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    public List<FriendRequest> getPendingFriendRequestsForUser(String userId) {
        List<FriendRequest> allRequests = getFriendRequests();
        List<FriendRequest> pendingRequests = new ArrayList<>();
        
        for (FriendRequest request : allRequests) {
            if (request.toUserId.equals(userId) && "pending".equals(request.status)) {
                pendingRequests.add(request);
            }
        }
        
        return pendingRequests;
    }
    
    public void updateFriendRequestStatus(String fromUserId, String toUserId, String newStatus) {
        List<FriendRequest> requests = getFriendRequests();
        
        for (FriendRequest request : requests) {
            if (request.fromUserId.equals(fromUserId) && request.toUserId.equals(toUserId)) {
                request.status = newStatus;
                break;
            }
        }
        
        String json = gson.toJson(requests);
        prefs.edit().putString(KEY_FRIEND_REQUESTS, json).apply();
    }
}