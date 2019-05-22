package com.example.barcodescanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class SettingsActivity extends AppCompatActivity {

    public static final String APP_PREFERENCES = "settings";
    public static final String APP_PREFERENCES_BASIC = "basic";
    public static final String APP_PREFERENCES_PASSWORD = "password";
    private SharedPreferences mSettings;

    private EditText login, pass;
    private Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);


        login = findViewById(R.id.login);
        pass = findViewById(R.id.pass);

        String[] data = null;
        if (mSettings.contains(APP_PREFERENCES_BASIC)) {
            String basic = mSettings.getString(APP_PREFERENCES_BASIC, "");
            try {
                String decoded = new String(Base64.decode(basic), "UTF-8");
                data = decoded.split(":");
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(data != null){
                login.setText(data[0]);
                pass.setText(data[1]);
            }
        }

        saveBtn = findViewById(R.id.saveLogPass);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!checkEditText()) {
                    byte[] bytes = null;
                    try {
                        String logPass = login.getText().toString() + ':' + pass.getText().toString();
                        bytes = logPass.getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    String encode = null;
                    try {
                        encode = Base64.encode(bytes);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putString(APP_PREFERENCES_BASIC, encode);
                    editor.apply();

                    finish();
                }
            }
        });
    }

    private boolean checkEditText() {
        boolean check = false;

        if (login.getText().toString().length() == 0) {
            Toast.makeText(SettingsActivity.this, "Введите логин!", Toast.LENGTH_SHORT).show();
            check = true;
        }
        if (pass.getText().toString().length() == 0) {
            Toast.makeText(SettingsActivity.this, "Введите пароль!", Toast.LENGTH_SHORT).show();
            check = true;
        }
        return check;
    }
}
