package com.globaladvice.xlsBulk;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.globaladvice.xlsBulk.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Objects;

public class Dashboard extends AppCompatActivity {
    CardView selectXLS, connectPC, settings;
    TextView userName;
    String userID;
    int selectedPackage;

    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    DocumentReference documentReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        selectXLS = findViewById(R.id.selectXLS);
        connectPC = findViewById(R.id.connectPC);
        settings = findViewById(R.id.settings);
        userName = findViewById(R.id.userName);

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();
        documentReference = fStore.collection("users").document(userID);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                selectedPackage = value.getLong("number_of_sms").intValue();
            }
        });

        if (userID != null) {
            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    userName.setText(value.getString("fName"));
                }
            });
        } else {
            startActivity(new Intent(getApplicationContext(), TabbedSignUp.class));
        }
    }

    public void SelectXLS(View view) {
        if (selectedPackage != 0) {
            startActivity(new Intent(getApplicationContext(), XLSSend.class));
        } else {
            Toast.makeText(getApplicationContext(), "Please Select a Package in Settings.", Toast.LENGTH_SHORT).show();
        }
    }

    public void ConnectPC(View view) {
        if (selectedPackage != 0) {
            startActivity(new Intent(getApplicationContext(), PCConnect.class));
        } else {
            Toast.makeText(getApplicationContext(), "Please Select a Package in Settings.", Toast.LENGTH_SHORT).show();
        }
    }

    public void AppSettigns(View view) {
            startActivity(new Intent(getApplicationContext(), Settings.class));
    }

    public void Logout(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Dashboard.this);

        builder.setMessage("Are you sure you want to Logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        finish();
                        startActivity(new Intent(getApplicationContext(), TabbedSignUp.class));
                    }
                }).setNegativeButton("No", null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}