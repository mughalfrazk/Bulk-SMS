package com.globaladvice.xlsBulk;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.globaladvice.xlsBulk.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Settings extends AppCompatActivity {
    private Spinner numberSpinner;
    TextView selectedPackage;
    EditText customSMS;
    Button customSMSBTN;
    LinearLayout customSection;

    private static final String[] paths = {"Package List", "25", "50", "100", "250", "Custom"};

    public static String userID;
    Map<String,Object> numberOfSms;

    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    DocumentReference documentReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        numberSpinner = findViewById(R.id.numberSpinner);
        selectedPackage = findViewById(R.id.selectedPackage);
        customSMS = findViewById(R.id.customSMS);
        customSMSBTN = findViewById(R.id.customSMSBTN);
        customSection = findViewById(R.id.customSection);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();
        numberOfSms = new HashMap<>();

        documentReference = fStore.collection("users").document(userID);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                int smsPackage = value.getLong("number_of_sms").intValue();
                selectedPackage.setText(String.valueOf(smsPackage));
            }
        });

        ArrayAdapter<String>adapter = new ArrayAdapter<String>(Settings.this,
                android.R.layout.simple_spinner_item,paths);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        numberSpinner.setAdapter(adapter);
        numberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        Toast.makeText(getApplicationContext(), "Package Updated: "+ 25, Toast.LENGTH_SHORT).show();
                        numberOfSms.put("number_of_sms", 25);
                        documentReference.update(numberOfSms);
                        break;
                    case 2:
                        Toast.makeText(getApplicationContext(), "Package Updated: "+ 50, Toast.LENGTH_SHORT).show();
                        numberOfSms.put("number_of_sms", 50);
                        documentReference.update(numberOfSms);
                        break;
                    case 3:
                        Toast.makeText(getApplicationContext(), "Package Updated: "+ 100, Toast.LENGTH_SHORT).show();
                        numberOfSms.put("number_of_sms", 100);
                        documentReference.update(numberOfSms);
                        break;
                    case 4:
                        Toast.makeText(getApplicationContext(), "Package Updated: "+ 250, Toast.LENGTH_SHORT).show();
                        numberOfSms.put("number_of_sms", 250);
                        documentReference.update(numberOfSms);
                        break;
                    case 5:
                        Toast.makeText(getApplicationContext(), "Package Updated: Custom", Toast.LENGTH_SHORT).show();
                        customSection.setVisibility(View.VISIBLE);
                        customSMSBTN.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (customSMS.getText().toString().matches("")) {
                                    Toast.makeText(getApplicationContext(), "Write a Number", Toast.LENGTH_SHORT).show();
                                } else {
                                    int customNo = Integer.parseInt(customSMS.getText().toString());
                                    numberOfSms.put("number_of_sms", customNo);
                                    documentReference.update(numberOfSms);
                                }
                            }
                        });
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}