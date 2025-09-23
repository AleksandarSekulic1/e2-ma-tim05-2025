package ftn.ma.myapplication.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.Executors;

import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.local.AppDatabase;
import ftn.ma.myapplication.data.model.User;
import ftn.ma.myapplication.utils.QRCodeUtils;
import ftn.ma.myapplication.utils.UserSessionManager;

/**
 * Activity za prikazivanje QR koda trenutnog korisnika
 * Omogućava deljenje QR koda sa drugim korisnicima
 */
public class QRDisplayActivity extends AppCompatActivity {
    
    private ImageView qrCodeImageView;
    private TextView usernameTextView;
    private TextView instructionsTextView;
    private Button shareButton;
    private Button saveButton;
    private Button copyIdButton;
    
    private AppDatabase db;
    private UserSessionManager sessionManager;
    private User currentUser;
    private Bitmap qrCodeBitmap;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_display);
        
        initializeViews();
        setupActionBar();
        initializeData();
        loadUserAndGenerateQR();
    }
    
    private void initializeViews() {
        qrCodeImageView = findViewById(R.id.qr_code_image);
        usernameTextView = findViewById(R.id.username_text);
        instructionsTextView = findViewById(R.id.instructions_text);
        shareButton = findViewById(R.id.btn_share);
        saveButton = findViewById(R.id.btn_save);
        copyIdButton = findViewById(R.id.btn_copy_id);
        
        setupClickListeners();
    }
    
    private void setupClickListeners() {
        shareButton.setOnClickListener(v -> shareQRCode());
        saveButton.setOnClickListener(v -> saveQRCodeToGallery());
        copyIdButton.setOnClickListener(v -> copyUserIdToClipboard());
    }
    
    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Moj QR kod");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void initializeData() {
        db = AppDatabase.getDatabase(this);
        sessionManager = new UserSessionManager(this);
    }
    
    private void loadUserAndGenerateQR() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                int currentUserId = sessionManager.getCurrentUserId();
                currentUser = db.userDao().getUserById(currentUserId);
                
                if (currentUser != null) {
                    // Generiši QR kod
                    qrCodeBitmap = QRCodeUtils.generateUserQRCodeHighQuality(
                            currentUser.getUserId(), 
                            currentUser.getUsername(), 
                            512
                    );
                    
                    runOnUiThread(() -> {
                        displayUserInfo();
                        displayQRCode();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Greška pri učitavanju korisničkih podataka", 
                                Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Greška pri generiranju QR koda", 
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }
    
    private void displayUserInfo() {
        usernameTextView.setText(currentUser.getUsername());
        
        String instructions = "Pokažite ovaj QR kod prijateljima da vas dodaju u kontakte. " +
                "QR kod je važeći 24 sata.";
        instructionsTextView.setText(instructions);
    }
    
    private void displayQRCode() {
        if (qrCodeBitmap != null) {
            qrCodeImageView.setImageBitmap(qrCodeBitmap);
        } else {
            Toast.makeText(this, "Greška pri generiranju QR koda", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void shareQRCode() {
        if (qrCodeBitmap == null) {
            Toast.makeText(this, "QR kod nije spreman za deljenje", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Sačuvaj QR kod u cache direktorijum
            String savedImageURL = MediaStore.Images.Media.insertImage(
                    getContentResolver(), 
                    qrCodeBitmap, 
                    "QR_Code_" + currentUser.getUsername(), 
                    "QR kod za dodavanje prijatelja"
            );
            
            if (savedImageURL != null) {
                Uri savedImageURI = Uri.parse(savedImageURL);
                
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, savedImageURI);
                shareIntent.putExtra(Intent.EXTRA_TEXT, 
                        "Skeniraj moj QR kod da me dodaš kao prijatelja! Username: " + currentUser.getUsername());
                
                startActivity(Intent.createChooser(shareIntent, "Podeli QR kod"));
            } else {
                Toast.makeText(this, "Greška pri čuvanju QR koda", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Greška pri deljenju QR koda", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void saveQRCodeToGallery() {
        if (qrCodeBitmap == null) {
            Toast.makeText(this, "QR kod nije spreman za čuvanje", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            String savedImageURL = MediaStore.Images.Media.insertImage(
                    getContentResolver(),
                    qrCodeBitmap,
                    "QR_Code_" + currentUser.getUsername() + "_" + System.currentTimeMillis(),
                    "QR kod za dodavanje prijatelja - " + currentUser.getUsername()
            );
            
            if (savedImageURL != null) {
                Toast.makeText(this, "QR kod je sačuvan u galeriju", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Greška pri čuvanju QR koda", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Greška pri čuvanju QR koda u galeriju", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void copyUserIdToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("User ID", String.valueOf(currentUser.getUserId()));
        clipboard.setPrimaryClip(clip);
        
        Toast.makeText(this, "User ID kopiran u clipboard", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}