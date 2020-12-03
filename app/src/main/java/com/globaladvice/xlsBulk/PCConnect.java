package com.globaladvice.xlsBulk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.globaladvice.xlsBulk.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class PCConnect extends AppCompatActivity {
    EditText smsText;
    String autoToken, userID;

    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    CollectionReference collectionReference, messaegReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pc_connect);

        smsText = findViewById(R.id.smsText);
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();
        collectionReference = fStore.collection("users").document(userID).collection("Messages");
        messaegReference = fStore.collection("Messages");
    }

    public void generateToken(View view) {
        autoToken = getAlphaNumericString(8);

        final String txtMsg = smsText.getText().toString();

        final Map<String, String> msgs = new HashMap<>();
        msgs.put("txtmsg", txtMsg);

        messaegReference.document(autoToken).set(msgs);
        collectionReference.document(autoToken).set(msgs).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                getToken(txtMsg);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getToken(String txtMsg) {
        LayoutInflater inflater= PCConnect.this.getLayoutInflater();
        //this is what I did to added the layout to the alert dialog
        View layout = inflater.inflate(R.layout.token_box,null);

        TextView tokenText = layout.findViewById(R.id.tokenText);
        TextView webURL = layout.findViewById(R.id.webURL);
        Button cancelBTN = layout.findViewById(R.id.cancelBTN);


        final AlertDialog alert = new AlertDialog.Builder(PCConnect.this).create();
        Objects.requireNonNull(alert.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alert.setView(layout);

        tokenText.setText(autoToken);
        tokenText.setTextIsSelectable(true);

        webURL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://globaladvisor.pt/bulk-sms/")));
                alert.dismiss();
            }
        });

        cancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
            }
        });

        alert.setCancelable(false);
        alert.show();


        Toast.makeText(getApplicationContext(), "Token Generated: "+ autoToken, Toast.LENGTH_SHORT).show();
    }

    static String getAlphaNumericString(int n)
    {
        // length is bounded by 256 Character
        byte[] array = new byte[256];
        new Random().nextBytes(array);

        String randomString
                = new String(array, StandardCharsets.UTF_8);

        // Create a StringBuffer to store the result
        StringBuffer r = new StringBuffer();

        // Append first 20 alphanumeric characters
        // from the generated random String into the result
        for (int k = 0; k < randomString.length(); k++) {

            char ch = randomString.charAt(k);

            if (((ch >= 'a' && ch <= 'z')
                    || (ch >= 'A' && ch <= 'Z')
                    || (ch >= '0' && ch <= '9'))
                    && (n > 0)) {

                r.append(ch);
                n--;
            }
        }

        // return the resultant string
        return r.toString();
    }
}