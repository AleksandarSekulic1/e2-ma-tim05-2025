package ftn.ma.myapplication.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility klasa za rad sa QR kodovima
 * Omogućava generiranje QR kodova za korisnike i parsiranje skeniranih QR kodova
 */
public class QRCodeUtils {
    
    private static final String QR_TYPE_USER = "user";
    private static final String QR_VERSION = "1.0";
    
    /**
     * Generiše QR kod za korisnika
     * @param userId ID korisnika
     * @param username Username korisnika
     * @param size Veličina QR koda u pikselima
     * @return Bitmap QR koda ili null ako je generiranje neuspešno
     */
    public static Bitmap generateUserQRCode(int userId, String username, int size) {
        try {
            // Kreiraj JSON objekat sa korisničkim podacima
            JSONObject qrData = new JSONObject();
            qrData.put("type", QR_TYPE_USER);
            qrData.put("version", QR_VERSION);
            qrData.put("userId", userId);
            qrData.put("username", username);
            qrData.put("timestamp", System.currentTimeMillis());
            
            String qrText = qrData.toString();
            
            // Konfiguracija za QR kod
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);
            
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrText, BarcodeFormat.QR_CODE, size, size, hints);
            
            // Konvertuj BitMatrix u Bitmap
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            
            return bitmap;
            
        } catch (WriterException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Parsira QR kod podatke za korisnika
     * @param qrData String podataka iz QR koda
     * @return UserQRData objekat ili null ako parsiranje nije uspešno
     */
    public static UserQRData parseUserQRCode(String qrData) {
        try {
            JSONObject jsonObject = new JSONObject(qrData);
            
            // Proveri tip QR koda
            String type = jsonObject.optString("type", "");
            if (!QR_TYPE_USER.equals(type)) {
                return null;
            }
            
            // Proveri verziju
            String version = jsonObject.optString("version", "");
            if (!QR_VERSION.equals(version)) {
                return null;
            }
            
            // Izvuci korisničke podatke
            int userId = jsonObject.getInt("userId");
            String username = jsonObject.getString("username");
            long timestamp = jsonObject.optLong("timestamp", 0);
            
            // Proveri da li je QR kod relativno svež (ne stariji od 24h)
            long currentTime = System.currentTimeMillis();
            long maxAge = 24 * 60 * 60 * 1000; // 24 sata u milisekundama
            
            if (timestamp > 0 && (currentTime - timestamp) > maxAge) {
                return null; // QR kod je zastario
            }
            
            return new UserQRData(userId, username, timestamp);
            
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Validira da li je QR kod podatak valjan za korisnika
     * @param qrData String podataka iz QR koda
     * @return true ako je QR kod valjan, false inače
     */
    public static boolean isValidUserQRCode(String qrData) {
        return parseUserQRCode(qrData) != null;
    }
    
    /**
     * Klasa koja enkapsulira podatke iz korisničkog QR koda
     */
    public static class UserQRData {
        private final int userId;
        private final String username;
        private final long timestamp;
        
        public UserQRData(int userId, String username, long timestamp) {
            this.userId = userId;
            this.username = username;
            this.timestamp = timestamp;
        }
        
        public int getUserId() {
            return userId;
        }
        
        public String getUsername() {
            return username;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        /**
         * Proverava da li je QR kod još uvek svež
         * @return true ako je QR kod svež, false ako je zastario
         */
        public boolean isFresh() {
            long currentTime = System.currentTimeMillis();
            long maxAge = 24 * 60 * 60 * 1000; // 24 sata
            return timestamp > 0 && (currentTime - timestamp) <= maxAge;
        }
        
        @Override
        public String toString() {
            return "UserQRData{" +
                    "userId=" + userId +
                    ", username='" + username + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
    
    /**
     * Generiše QR kod sa većom greškom korekcijom za bolje skeniranje
     * @param userId ID korisnika
     * @param username Username korisnika
     * @param size Veličina QR koda
     * @return Bitmap QR koda
     */
    public static Bitmap generateUserQRCodeHighQuality(int userId, String username, int size) {
        try {
            JSONObject qrData = new JSONObject();
            qrData.put("type", QR_TYPE_USER);
            qrData.put("version", QR_VERSION);
            qrData.put("userId", userId);
            qrData.put("username", username);
            qrData.put("timestamp", System.currentTimeMillis());
            
            String qrText = qrData.toString();
            
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // Visoka korekcija grešaka
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 2);
            
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrText, BarcodeFormat.QR_CODE, size, size, hints);
            
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            
            return bitmap;
            
        } catch (WriterException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Validira QR kod i pokazuje odgovarajuću poruku korisniku
     * @param context Context aplikacije
     * @param qrData String podataka iz QR koda
     * @return UserQRData ako je QR kod valjan, null inače
     */
    public static UserQRData validateAndParseQRCode(Context context, String qrData) {
        UserQRData userData = parseUserQRCode(qrData);
        
        if (userData == null) {
            Toast.makeText(context, "Nevaljan QR kod!", Toast.LENGTH_SHORT).show();
            return null;
        }
        
        if (!userData.isFresh()) {
            Toast.makeText(context, "QR kod je zastario! Molimo zatražite novi.", Toast.LENGTH_SHORT).show();
            return null;
        }
        
        return userData;
    }
}