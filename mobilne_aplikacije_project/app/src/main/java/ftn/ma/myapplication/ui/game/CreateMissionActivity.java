// CreateMissionActivity.java
package ftn.ma.myapplication.ui.game;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.model.SpecialMission;
import ftn.ma.myapplication.util.SharedPreferencesManager;

public class CreateMissionActivity extends AppCompatActivity {

    private EditText editTextMissionTitle;
    private Button buttonSaveMission;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_mission);
        setTitle("Nova Specijalna Misija");

        editTextMissionTitle = findViewById(R.id.editTextMissionTitle);
        buttonSaveMission = findViewById(R.id.buttonSaveMission);

        buttonSaveMission.setOnClickListener(v -> saveMission());
    }

    private void saveMission() {
        String title = editTextMissionTitle.getText().toString().trim();

        // Validacija unosa
        if (TextUtils.isEmpty(title)) {
            editTextMissionTitle.setError("Naziv misije ne može biti prazan!");
            return;
        }

        // Učitaj postojeću listu misija
        List<SpecialMission> missions = SharedPreferencesManager.loadMissions(this);

        // Generiši novi, jedinstveni ID
        long newId = 1;
        if (!missions.isEmpty()) {
            // Pronađi maksimalni postojeći ID i dodaj 1
            newId = missions.stream().mapToLong(SpecialMission::getId).max().orElse(0) + 1;
        }

        // Kreiraj novu misiju (uvek počinje kao neaktivna)
        SpecialMission newMission = new SpecialMission((int)newId, title, 0, false);
        missions.add(newMission);

        // Sačuvaj ažuriranu listu
        SharedPreferencesManager.saveMissions(this, missions);

        // Zatvori ekran i vrati se na listu
        finish();
    }
}
