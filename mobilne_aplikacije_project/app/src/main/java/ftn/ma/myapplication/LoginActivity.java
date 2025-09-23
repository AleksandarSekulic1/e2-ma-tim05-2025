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
    private android.widget.TextView textViewRegisterLink;

    // Više se ne koriste hardkodovani podaci

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
        textViewRegisterLink = findViewById(R.id.textViewRegisterLink);

        textViewRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(this, ftn.ma.myapplication.ui.RegisterActivity.class);
            startActivity(intent);
        });

        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString();

            ftn.ma.myapplication.data.model.User user = ftn.ma.myapplication.data.local.UserStorage.getUser(this);
            if (user == null) {
                Toast.makeText(this, "Nalog ne postoji. Registrujte se.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!user.getEmail().equals(email)) {
                Toast.makeText(this, "Pogrešan email ili lozinka.", Toast.LENGTH_SHORT).show();
                return;
            }
            String passwordHash = Integer.toString(password.hashCode());
            if (!user.getPasswordHash().equals(passwordHash)) {
                Toast.makeText(this, "Pogrešan email ili lozinka.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!user.isActive()) {
                long now = System.currentTimeMillis();
                if (now > user.getActivationExpiry()) {
                    Toast.makeText(this, "Aktivacioni link je istekao. Registrujte se ponovo.", Toast.LENGTH_LONG).show();
                    ftn.ma.myapplication.data.local.UserStorage.clearUser(this);
                } else {
                    Toast.makeText(this, "Nalog nije aktiviran. Proverite email za aktivacioni link.", Toast.LENGTH_LONG).show();
                }
                return;
            }
            SharedPreferencesManager.setUserLoggedIn(this, true);
            navigateToMainApp();
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