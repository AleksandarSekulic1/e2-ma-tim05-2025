
package ftn.ma.myapplication.ui;
import ftn.ma.myapplication.LoginActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import ftn.ma.myapplication.R;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextUsername, editTextPassword, editTextPasswordRepeat;
    private LinearLayout avatarContainer;
    private Button buttonRegister;
    private int selectedAvatarIndex = -1;
    private static final int AVATAR_COUNT = 5;
    private ExecutorService executorService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPasswordRepeat = findViewById(R.id.editTextPasswordRepeat);
        avatarContainer = findViewById(R.id.avatarContainer);
        buttonRegister = findViewById(R.id.buttonRegister);
        executorService = Executors.newSingleThreadExecutor();

        setupAvatars();

        buttonRegister.setOnClickListener(v -> attemptRegister());
    }

    private void setupAvatars() {
        for (int i = 0; i < AVATAR_COUNT; i++) {
            final int index = i;
            ImageView avatar = new ImageView(this);
            int avatarResId = getResources().getIdentifier("avatar_" + (i+1), "drawable", getPackageName());
            avatar.setImageResource(avatarResId);
            avatar.setPadding(16, 16, 16, 16);
            avatar.setOnClickListener(view -> {
                selectedAvatarIndex = index;
                highlightSelectedAvatar();
            });
            avatarContainer.addView(avatar, new LinearLayout.LayoutParams(160, 160));
        }
    }

    private void highlightSelectedAvatar() {
        for (int i = 0; i < avatarContainer.getChildCount(); i++) {
            avatarContainer.getChildAt(i).setAlpha(i == selectedAvatarIndex ? 1.0f : 0.5f);
        }
    }

    private void attemptRegister() {
        String email = editTextEmail.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString();
        String passwordRepeat = editTextPasswordRepeat.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(passwordRepeat)) {
            Toast.makeText(this, "Sva polja su obavezna!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Neispravan email!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(passwordRepeat)) {
            Toast.makeText(this, "Lozinke se ne poklapaju!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedAvatarIndex == -1) {
            Toast.makeText(this, "Izaberite avatar!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hash lozinke (za demo, koristi se plain tekst, preporučuje se prava hash funkcija)
        String passwordHash = Integer.toString(password.hashCode());
        long expiry = System.currentTimeMillis() + 24 * 60 * 60 * 1000; // 24h

        ftn.ma.myapplication.data.model.User user = new ftn.ma.myapplication.data.model.User(
                email, username, passwordHash, selectedAvatarIndex, false, expiry
        );
        // Sačuvaj korisnika lokalno
        ftn.ma.myapplication.data.local.UserStorage.saveUser(this, user);

        // Simulacija slanja emaila za aktivaciju
        Toast.makeText(this, "Registracija uspešna! Proverite email za aktivaciju (simulacija).", Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
