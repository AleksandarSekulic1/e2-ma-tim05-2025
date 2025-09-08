package ftn.ma.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import ftn.ma.myapplication.ui.ProfileActivity; // Menjaj po potrebi
import ftn.ma.myapplication.ui.tasks.TasksActivity;
import ftn.ma.myapplication.util.SharedPreferencesManager;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;

    // Hardkodovani podaci za prijavu
    private final String HARDCODED_EMAIL = "student";
    private final String HARDCODED_PASSWORD = "student";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Provera da li je korisnik već ulogovan
        if (SharedPreferencesManager.isUserLoggedIn(this)) {
            navigateToMainApp();
            return;
        }

        setContentView(R.layout.activity_login);
        setTitle("Prijava");

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();

            if (email.equals(HARDCODED_EMAIL) && password.equals(HARDCODED_PASSWORD)) {
                SharedPreferencesManager.setUserLoggedIn(this, true);
                navigateToMainApp();
            } else {
                Toast.makeText(this, "Pogrešan email ili lozinka.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Pomoćna metoda za preusmeravanje na glavni ekran aplikacije.
     * Osigurava da je preusmeravanje uvek isto.
     */
    private void navigateToMainApp() {
        // --- ISPRAVKA: Uvek preusmeravaj na TasksActivity ---
        // Ako želiš da početni ekran bude Profil, samo promeni TasksActivity.class u ProfileActivity.class
        Intent intent = new Intent(this, TasksActivity.class);

        // Ovi flag-ovi osiguravaju da korisnik ne može da se vrati na Login ekran
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Obavezno uništi LoginActivity
        finish();
    }
}