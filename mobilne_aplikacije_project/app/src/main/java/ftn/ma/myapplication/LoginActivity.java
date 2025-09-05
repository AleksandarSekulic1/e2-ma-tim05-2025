package ftn.ma.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

        // --- Provera da li je korisnik već ulogovan ---
        // Ako jeste, odmah ga preusmeri na MainActivity i zatvori LoginActivity
        if (SharedPreferencesManager.isUserLoggedIn(this)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return; // Prekida dalje izvršavanje onCreate
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
                // Ako su podaci tačni, sačuvaj status prijave
                SharedPreferencesManager.setUserLoggedIn(this, true);

                // Pokreni MainActivity
                startActivity(new Intent(this, TasksActivity.class));
                finish(); // Zatvori LoginActivity da korisnik ne može da se vrati na nju
            } else {
                Toast.makeText(this, "Pogrešan email ili lozinka.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
