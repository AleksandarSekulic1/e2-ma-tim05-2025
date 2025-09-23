package ftn.ma.myapplication.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import ftn.ma.myapplication.R;
import ftn.ma.myapplication.utils.QRCodeUtils;

/**
 * Activity za skeniranje QR kodova prijatelja
 * Koristi ZXing biblioteku za skeniranje i parsiranje QR kodova
 */
public class QRScannerActivity extends AppCompatActivity {
    
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    public static final String EXTRA_QR_RESULT = "qr_result";
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_USERNAME = "username";
    
    private DecoratedBarcodeView barcodeView;
    private CaptureManager capture;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);
        
        setupActionBar();
        initializeCamera();
    }
    
    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Skeniraj QR kod");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void initializeCamera() {
        // Proveri dozvolu za kameru
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }
        
        setupBarcodeScanner();
    }
    
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.CAMERA}, 
                CAMERA_PERMISSION_REQUEST);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupBarcodeScanner();
            } else {
                Toast.makeText(this, "Dozvola za kameru je potrebna za skeniranje QR kodova", 
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    
    private void setupBarcodeScanner() {
        barcodeView = findViewById(R.id.barcode_scanner);
        
        // Postavi callback za skeniranje
        barcodeView.setStatusText("Pozicioniraj QR kod u okvir");
        
        capture = new CaptureManager(this, barcodeView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        
        // Postavi custom rezultat callback
        barcodeView.decodeSingle(result -> {
            String scannedData = result.getText();
            handleScannedQRCode(scannedData);
        });
        
        capture.decode();
    }
    
    private void handleScannedQRCode(String qrData) {
        // Parsiranje QR koda
        QRCodeUtils.UserQRData userData = QRCodeUtils.validateAndParseQRCode(this, qrData);
        
        if (userData != null) {
            // QR kod je valjan, vrati rezultat
            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_QR_RESULT, true);
            resultIntent.putExtra(EXTRA_USER_ID, userData.getUserId());
            resultIntent.putExtra(EXTRA_USERNAME, userData.getUsername());
            
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            // QR kod nije valjan, nastavi skeniranje
            Toast.makeText(this, "QR kod nije prepoznat. Pokušajte ponovo.", Toast.LENGTH_SHORT).show();
            
            // Restartuj skeniranje
            barcodeView.resume();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (capture != null) {
            capture.onResume();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (capture != null) {
            capture.onPause();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (capture != null) {
            capture.onDestroy();
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (capture != null) {
            capture.onSaveInstanceState(outState);
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}