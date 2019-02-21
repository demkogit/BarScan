package com.example.barcodescanner;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private ImageButton scanBtn;
    private TextView codeView, charView, nameView, partyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        codeView = findViewById(R.id.code);
        nameView = findViewById(R.id.name);
        charView = findViewById(R.id.charateristic);
        partyView = findViewById(R.id.party);

        scanBtn = (ImageButton) findViewById(R.id.scanButton);
        final Activity mActivity = this;
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(mActivity);
                integrator.setOrientationLocked(true);
                integrator.initiateScan(IntentIntegrator.PRODUCT_CODE_TYPES);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() != null) {
                new JSONAsyncTask().execute("http://axiantest.dynvpn.ru:34080/UNF/hs/apiScanner/getInfo?id=" + result.getContents());
            } else {
                Toast.makeText(this, "canceled", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    public class JSONAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(s.equals("0")) {
                Toast.makeText(MainActivity.this, "Отсутствует соединение с сервисом(", Toast.LENGTH_LONG).show();
            }
            else if (s != null){
                Object obj = null; // Object obj = new JSONParser().parse(new FileReader("JSONExample.json"));
                try {
                    obj = new JSONParser().parse(s);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                // Кастим obj в JSONObject
                JSONObject jo = (JSONObject) obj;
                String id = null;
                String name = null;
                String characteristic = null;
                String party = null;
                String error = (String) jo.get("error");

                if (error.equals("false")) {
                    id = (String) jo.get("id");
                    name = (String) jo.get("name");
                    characteristic = (String) jo.get("characteristic");
                    party = (String) jo.get("party");
                } else {
                    id = "Error";
                    name = (String) jo.get("msg");
                }

                codeView.setText(id);
                nameView.setText(name);
                charView.setText(characteristic);
                partyView.setText(party);
            }

        }

        @Override
        protected String doInBackground(String... urls) {
            URL url;
            HttpURLConnection urlConnection = null;
            String server_response = null;
            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Authorization", "Basic V2ViQXBpOjEyMzQ1");

                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    server_response = readStream(urlConnection.getInputStream());
                    Log.v("CatalogClient", server_response);
                }else {
                    Toast.makeText(MainActivity.this, "Ошибка: " + responseCode, Toast.LENGTH_LONG).show();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();

            } catch (IOException e) {
                server_response = "0";
                e.printStackTrace();
            }finally {
                if(urlConnection!=null){
                    urlConnection.disconnect();
                }
            }

            return server_response;
        }
    }

    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }
}

