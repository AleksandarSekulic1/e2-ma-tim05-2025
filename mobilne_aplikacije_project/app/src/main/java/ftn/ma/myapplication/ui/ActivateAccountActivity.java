package ftn.ma.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import ftn.ma.myapplication.LoginActivity;
import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.local.UserStorage;
import ftn.ma.myapplication.data.model.User;

public class ActivateAccountActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activate_account);

        Button buttonActivate = findViewById(R.id.buttonActivate);
        buttonActivate.setOnClickListener(v -> {
            User user = UserStorage.getUser(this);
            if (user == null) {
                Toast.makeText(this, "Nalog ne postoji.", Toast.LENGTH_SHORT).show();
                return;
            }
            long now = System.currentTimeMillis();
            if (now > user.getActivationExpiry()) {
                Toast.makeText(this, "Aktivacioni link je istekao. Registrujte se ponovo.", Toast.LENGTH_LONG).show();
                UserStorage.clearUser(this);
                startActivity(new Intent(this, RegisterActivity.class));
                finish();
                return;
            }
            user.setActive(true);
            UserStorage.saveUser(this, user);
            Toast.makeText(this, "Nalog je uspešno aktiviran!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
