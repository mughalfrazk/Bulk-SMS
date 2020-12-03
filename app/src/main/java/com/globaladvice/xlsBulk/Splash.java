package com.globaladvice.xlsBulk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.globaladvice.xlsBulk.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Splash extends AppCompatActivity {
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        ActivityCompat.requestPermissions(Splash.this,
                new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET},
                1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 1) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.d("TAG", "onRequestPermissionsResult: Permission Granted");
                int secondsDelayed = 1;
                if (currentUser == null) {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            startActivity(new Intent(getApplicationContext(), TabbedSignUp.class));
                            finish();
                        }
                    }, secondsDelayed * 2000);
                }
                else {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            startActivity(new Intent(getApplicationContext(), Dashboard.class));
                            finish();
                        }
                    }, secondsDelayed * 2000);
                }
            } else {

                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Toast.makeText(Splash.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                int secondsDelayed = 1;
                if (currentUser == null) {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            startActivity(new Intent(getApplicationContext(), TabbedSignUp.class));
                            finish();
                        }
                    }, secondsDelayed * 2000);
                }
                else {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            startActivity(new Intent(getApplicationContext(), Dashboard.class));
                            finish();
                        }
                    }, secondsDelayed * 2000);
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


}