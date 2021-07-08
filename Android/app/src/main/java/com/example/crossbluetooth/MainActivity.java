package com.example.crossbluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;


import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;



public class MainActivity extends AppCompatActivity {
    public static FirebaseDatabase database = FirebaseDatabase.getInstance();
    public static DatabaseReference myRef = database.getInstance().getReference();
    public static DatabaseReference userRef=myRef.child("User");
    public static boolean hasuserIf=false;
    public static UserInfo user;
    private TextView status;
    private EditText inputID;
    private EditText inputPassWord;
    private Button loginButton;
    public static final String SHARED_PREFS="SharedPrefs";
    public static final String TEXTS="text";

   // private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        status=(TextView)findViewById(R.id.status);
        inputID=(EditText)findViewById(R.id.inputID);
        inputPassWord=(EditText)findViewById(R.id.inputPassWord);
        loginButton=(Button)findViewById(R.id.logInButton);
        //myRef.setValue("Hello, World!");
        user=new UserInfo("a","b");
        LoadUserIf();
        if (hasuserIf){
            Intent intent = new Intent(MainActivity.this, home.class);
            intent.putExtra("User ID", user.id);
            startActivity(intent);
        }
        /*
        myRef.child("User").child("Andy").addValueEventListener(new ValueEventListener() {
            //@Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.child("Email").getValue(String.class);
                //Log.d(TAG, "Value is: " + value);
                //status.setText(value);
                Toast.makeText(MainActivity.this,value, Toast.LENGTH_LONG).show();
            }
            //@Override
            public void onCancelled(DatabaseError error) {
            }
        });
         */

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id=inputID.getText().toString();
                String password=inputPassWord.getText().toString();
                logIn(id,password);

            }
        });
        status.append("MODEL "+Build.MODEL);






    }

    protected void logIn(String id,String inputPassWord){
          DatabaseReference currentUserRef=userRef.child(id);
          currentUserRef.addValueEventListener(new ValueEventListener() {
            //@Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String firepassword = dataSnapshot.child("PassWord").getValue(String.class);
                if (firepassword.equals(inputPassWord)){
                    String email = dataSnapshot.child("Email").getValue(String.class);
                    status.append(email);
                    SaveUserIf(id,inputPassWord);
                }else{
                    status.append("Something Wrong");
                }
                //Log.d(TAG, "Value is: " + value);
            }
            //@Override
            public void onCancelled(DatabaseError error) {
            }
        });

    }

    private void SaveUserIf(String id, String password){
        String userIF=id+"|"+password;
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(TEXTS, userIF);
        editor.apply();
        Toast.makeText(this, "userSaved", Toast.LENGTH_SHORT).show();
    }

    private void LoadUserIf() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String userIF = sharedPreferences.getString(TEXTS, "");
        if (userIF.contains("|")) {
            user = new UserInfo(userIF.substring(0, userIF.indexOf("|")), userIF.substring(userIF.indexOf("|") + 1, userIF.length()));
            Toast.makeText(this, "user "+userIF.substring(0, userIF.indexOf("|"))+"Load", Toast.LENGTH_SHORT).show();
            hasuserIf = true;

        }
    }

    //myRef.setValue("Hello, World!");


}