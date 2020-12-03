package com.globaladvice.xlsBulk;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.globaladvice.xlsBulk.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class XLSSend extends AppCompatActivity {
    public static final String ACCOUNT_SID = "AC81327f710ac147b31f6d94c01317abc0";
    public static final String AUTH_TOKEN = "2f1d3f2e0005dd3d5103af16bb4187e6";

    public static int PICK_FILE = 1;

    List<String> list = new ArrayList<>();
    Button send;
    ListView listView;
    String userID;
    int selectedPackage;
    ArrayList<String> alNumbers = new ArrayList<String>();
    ProgressBar progressBar;
    EditText smsText;
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    DocumentReference documentReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xls_send);
        send = (Button)findViewById(R.id.send);
        listView = findViewById(R.id.listView);
        progressBar = findViewById(R.id.progressBar);
        smsText = findViewById(R.id.smsText);

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();
        documentReference = fStore.collection("users").document(userID);
    }


    private void sendMessage(String x) {
        String body = smsText.getText().toString();
        String from = "+14252925116";
        String to = x;

        String base64EncodedCredentials = "Basic " + Base64.encodeToString(
                (ACCOUNT_SID + ":" + AUTH_TOKEN).getBytes(), Base64.NO_WRAP
        );

        Map<String, String> smsData = new HashMap<>();
        smsData.put("From", from);
        smsData.put("To", to);
        smsData.put("Body", body);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.twilio.com/2010-04-01/")
                .build();
        TwilioApi api = retrofit.create(TwilioApi.class);

        api.sendMessage(ACCOUNT_SID, base64EncodedCredentials, smsData).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "SMS Sent", Toast.LENGTH_SHORT).show();
                    Log.d("TAG", "onResponse->success");
                }
                else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "SMS Not Sent", Toast.LENGTH_SHORT).show();
                    Log.d("TAG", "onResponse->failure");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Error Sending SMS", Toast.LENGTH_SHORT).show();
                Log.d("TAG", "onFailure");
            }
        });
    }


    public void select(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        startActivityForResult(intent, PICK_FILE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                String realPath = FilePath.getPath(getApplicationContext(), uri);
                List<Map<Integer, Object>> fileContent = ExcelUtil.readExcelNew(getApplicationContext(), uri, realPath);

                for (int i=0; i < fileContent.size(); i++) {
                    Map<Integer, Object> listItem = fileContent.get(i);

                    String plainNumber = listItem.values().toString();
                    plainNumber = plainNumber.replaceAll("\\p{P}","");

                    alNumbers.add(plainNumber);
                }

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, alNumbers);
                listView.setAdapter(arrayAdapter);

                send.setVisibility(View.VISIBLE);
                send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        progressBar.setVisibility(View.VISIBLE);

/*                        for (String pNum: alNumbers) {
                            Log.d("Done", "onActivityResult: "+ pNum);
                            try {
                                sendMessage(pNum);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Error Sending: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }*/

                        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                                selectedPackage = value.getLong("number_of_sms").intValue();
                                int noOfRecipients;

                                Log.d("TAG", "onEvent: "+ selectedPackage);
                                if (alNumbers.size() > selectedPackage) {
                                    noOfRecipients = selectedPackage;
                                } else {
                                    noOfRecipients = alNumbers.size();
                                }

                                for (int i = 0; i < noOfRecipients; i++ ) {
                                    try {
                                        //sendSimpleMessage(alNumbers.get(i));
                                        sendMessage(alNumbers.get(i));
                                        if (i == noOfRecipients-1) {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(getApplicationContext(), "All Sent ", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Toast.makeText(getApplicationContext(), "Error Sending: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });

                        Thread thread = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    while(true) {
                                        sleep(3000);
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        };

                        thread.start();

                    }
                });
            }
        }
    }

    private void sendSimpleMessage(String s) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(s, null, smsText.getText().toString(), null, null);
    }

    private String readTextFile(Uri uri) {
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));

            String line = "";
            while ((line = reader.readLine()) != null) {

              list.add(line);
               line=line+"\n";
                builder.append(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    private String readFile(String path) {
        String myData = "";
        File myExternalFile = new File("asd");
        try {
            FileInputStream fis = new FileInputStream(myExternalFile);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String strLine;
            while ((strLine = br.readLine()) != null) {
                myData = myData + strLine + "\n";
            }
            br.close();
            in.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return myData;
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}