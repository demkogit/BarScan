package com.example.barcodescanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.itextpdf.text.DocumentException;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import Decoder.BASE64Decoder;


public class MainActivity extends AppCompatActivity implements ProductAdapter.OnItemClickListener, DeleteDialog.IDeleteItem {

    public static final String APP_PREFERENCES = "settings";
    public static final String APP_PREFERENCES_BASIC = "basic";
    private static final String TAG = "myLogs";
    final int REQUEST_CODE_CREATE_PRODUCT = 1;
    final int REQUEST_CODE_SCAN = 2;
    final int REQUEST_CODE_SETTINGS = 3;
    RecyclerView recyclerView;
    ProductAdapter adapter;
    List<ProductItem> productItemList;
    private SharedPreferences mSettings;
    private ImageButton scanBtn, saveBtn, createDocBtn, createProductBtn, settingsBtn;
    private String mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        String data = readData();
        importData(data);

        new ServerAsyncTask().execute("GET", "test", "http://axiantest.dynvpn.ru:34080/UNF/hs/apiScanner/getInfo?id=0");
    }

    private void init() {
        final Activity mActivity = this;

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        productItemList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProductAdapter(MainActivity.this, productItemList, this, MainActivity.this);
        recyclerView.setAdapter(adapter);

        scanBtn = (ImageButton) findViewById(R.id.scanButton);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(mActivity);
                integrator.setOrientationLocked(true);
                integrator.initiateScan(IntentIntegrator.PRODUCT_CODE_TYPES);
            }
        });


//        saveBtn = findViewById(R.id.saveDataBtn);
//        saveBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                saveData();
//            }
//        });

        createDocBtn = findViewById(R.id.createDocBtn);
        createDocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(mActivity, mAuth, Toast.LENGTH_SHORT).show();
                String jsonArray = createDoc();
                //new JSONAsyncTask().execute("http://axiantest.dynvpn.ru:34080/UNF/hs/apiScanner/createDoc", "CreateDoc", jsonArray);
                new ServerAsyncTask().execute("POST", "createDoc", "http://axiantest.dynvpn.ru:34080/UNF/hs/apiScanner/createDoc", jsonArray);
            }
        });
//        final Intent createProductIntent = new Intent(this, CreateProductActivity.class);
//        createProductBtn = findViewById(R.id.createProductBtn);
//        createProductBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivityForResult(createProductIntent, REQUEST_CODE_CREATE_PRODUCT);
//                //new ServerAsyncTask().execute("post", "createProduct", "http://axiantest.dynvpn.ru:34080/UNF/hs/apiScanner/createProduct");
//            }
//        });

        settingsBtn = findViewById(R.id.settingsBtn);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });

    }

    private void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.menu_main);

        final Intent createProductIntent = new Intent(this, CreateProductActivity.class);
        final Intent settingsIntent = new Intent(this, SettingsActivity.class);
        popupMenu
                .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.settings:
                                startActivityForResult(settingsIntent, REQUEST_CODE_SETTINGS);
                                return true;
                            case R.id.saveData:
                                saveData();
                                return true;
                            case R.id.createProduct:
                                startActivityForResult(createProductIntent, REQUEST_CODE_CREATE_PRODUCT);
                                return true;
                        }
                        return true;
                    }
                });

//        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
//            @Override
//            public void onDismiss(PopupMenu menu) {
//                Toast.makeText(getApplicationContext(), "onDismiss",
//                        Toast.LENGTH_SHORT).show();
//            }
//        });
        popupMenu.show();
    }

    @Override
    public void onItemClick(final int position) {
        DeleteDialog dialog = new DeleteDialog();
        dialog.show(getSupportFragmentManager(), "deleteDialog");
        Bundle args = new Bundle();
        args.putInt("position", position);
        dialog.setArguments(args);
    }

    @Override
    public void deleteItem(int position) {
        productItemList.remove(position);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_CREATE_PRODUCT) {
                String product_name = data.getStringExtra("name");
                String product_code = data.getStringExtra("code");
                product_name = product_name.trim().replace(' ', '_');
                product_code = product_code.trim().replace(' ', '_');
                new ServerAsyncTask().execute("POST", "addProduct", "http://axiantest.dynvpn.ru:34080/UNF/hs/apiScanner/addProduct?name=" + product_name + "&code=" + product_code);
                Toast.makeText(this, product_code + '\n' + product_name, Toast.LENGTH_SHORT).show();
            } else if (resultCode == REQUEST_CODE_SETTINGS) {
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
            } else {
                //Log.d(TAG, "Ок");
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    //Log.d(TAG, "Ок");
                    if (result.getContents() != null) {
                        //Log.d(TAG, "Ок");
                        new ServerAsyncTask().execute("GET", "getInfo", "http://axiantest.dynvpn.ru:34080/UNF/hs/apiScanner/getInfo?id=" + result.getContents());
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
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

    private void addProduct(ProductItem product) {

        if (productExist(product)) {
            Toast.makeText(MainActivity.this, "Данный товар уже находится в списке товаров!", Toast.LENGTH_LONG).show();
        } else {
            productItemList.add(product);
            adapter.notifyDataSetChanged();
            Toast.makeText(MainActivity.this, "Товар успешно добавлен!", Toast.LENGTH_LONG).show();
        }
    }

    private boolean productExist(ProductItem product) {
        String code = product.getCode();
        for (ProductItem item : productItemList) {
            if (code.equals(item.getCode())) {
                return true;
            }
        }
        return false;
    }

    private void saveData() {
        JSONArray array = new JSONArray();

        for (ProductItem item : productItemList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", item.getCode());
            jsonObject.put("name", item.getName());
            jsonObject.put("count", item.getCount());
            jsonObject.put("party", item.getParty());
            array.add(jsonObject);
        }
        try {
            FileOutputStream fileOutput = openFileOutput("data.txt", MODE_PRIVATE);
            fileOutput.write(array.toString().getBytes());
            Toast.makeText(MainActivity.this, "Сохранено", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readData() {
        String result = null;
        try {
            FileInputStream fileInput = openFileInput("data.txt");
            InputStreamReader reader = new InputStreamReader(fileInput);
            BufferedReader buffer = new BufferedReader(reader);
            StringBuffer strBuffer = new StringBuffer();
            String line = "";
            while ((line = buffer.readLine()) != null) {
                strBuffer.append(line);
            }

            //Toast.makeText(MainActivity.this, strBuffer.toString(), Toast.LENGTH_LONG).show();
            result = strBuffer.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private String createDoc() {
        JSONArray array = new JSONArray();
        for (ProductItem item : productItemList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", item.getCode());
            jsonObject.put("name", item.getName());
            jsonObject.put("count", item.getCount());
            jsonObject.put("party", item.getParty());
            array.add(jsonObject);
        }
        Toast.makeText(MainActivity.this, array.toString(), Toast.LENGTH_SHORT).show();
        return array.toString();
    }

    private void importData(String data) {
        productItemList.clear();
        adapter.notifyDataSetChanged();

        Object obj = null;
        try {
            obj = new JSONParser().parse(data);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        JSONArray array = (JSONArray) obj;

        for (int i = 0; i < array.size(); i++) {
            JSONObject jsonObject = (JSONObject) array.get(i);
            String id = null;
            String name = null;
            String party = null;
            String count = null;
            id = (String) jsonObject.get("id");
            name = (String) jsonObject.get("name");
            party = (String) jsonObject.get("party");
            count = (String) jsonObject.get("count");
            productItemList.add(new ProductItem(id, name, party, count));
        }

    }

    public class JSONAsyncTask extends AsyncTask<String, Void, String> {

        String type_of_request;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            switch (this.type_of_request) {
                case "getInfo":
                    getInfo(s);
                    break;
                case "test":
                    test(s);
                    break;
                case "createDoc":
                    createDocReply(s);
            }
        }

        private void test(String data) {
            if (data.equals("0")) {
                Toast.makeText(MainActivity.this, "Отсутствует соединение с сервисом(", Toast.LENGTH_LONG).show();
            }
        }

        private void getInfo(String data) {

            if (data.equals("0")) {
                Toast.makeText(MainActivity.this, "Отсутствует соединение с сервисом(", Toast.LENGTH_LONG).show();
            } else if (data != null) {
                Object obj = null;
                try {
                    obj = new JSONParser().parse(data);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

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

                    addProduct(new ProductItem(id, name, party, "1"));
                    saveData();

                } else {
                    type_of_request = "addProduct";
                    Toast.makeText(MainActivity.this, "" + jo.get("msg"), Toast.LENGTH_SHORT).show();
                }
            }
        }

        private void createDocReply(String data) {
            if (data.equals("0")) {
                Toast.makeText(MainActivity.this, "Отсутствует соединение с сервисом(", Toast.LENGTH_LONG).show();
            } else if (data != null) {
                Toast.makeText(MainActivity.this, data.toString(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... urls) {

            type_of_request = urls[1];
            URL url;
            HttpURLConnection urlConnection = null;
            String server_response = null;
            if (type_of_request.equals("createDoc")) {
                try {
                    url = new URL(urls[0]);
                    urlConnection = (HttpURLConnection) url.openConnection();

                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Authorization", "Basic " + mAuth);
                    urlConnection.setConnectTimeout(5000);
                    urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);

                    OutputStream os = urlConnection.getOutputStream();
                    os.write(urls[3].getBytes("UTF-8"));
                    os.close();

                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    String result = org.apache.commons.io.IOUtils.toString(in, "UTF-8");

                    in.close();

                    int responseCode = urlConnection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        //server_response = readStream(urlConnection.getInputStream());
                        server_response = result;
                        //Log.v("CreateDoc", server_response);
                    } else {
                        Toast.makeText(MainActivity.this, "Ошибка: " + responseCode, Toast.LENGTH_LONG).show();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();

                } catch (IOException e) {
                    server_response = "0";
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            } else {
                try {
                    url = new URL(urls[0]);
                    urlConnection = (HttpURLConnection) url.openConnection();

                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Authorization", "Basic V2ViQXBpOjEyMzQ1");

                    int responseCode = urlConnection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        server_response = readStream(urlConnection.getInputStream());
                        //Log.v("CatalogClient", server_response);
                    } else {
                        Toast.makeText(MainActivity.this, "Ошибка: " + responseCode, Toast.LENGTH_LONG).show();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();

                } catch (IOException e) {
                    server_response = "0";
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
            return server_response;
        }
    }


    public class ServerAsyncTask extends AsyncTask<String, Void, String> {

        String type_of_request;
        byte[] bytes = null;

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            switch (type_of_request) {
                case "getInfo":
                    getInfo(data);
                    break;
                case "test":
                    test(data);
                    break;
                case "createDoc":
                    try {
                        createDoc(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }
                    break;
                case "addProduct":
                    addProductReply(data);
                    break;
            }
        }

        private void addProductReply(String data) {
            Log.d(TAG, "addProductReply " + data);
            if (data != null) {
                if (data.equals("0")) {
                    Toast.makeText(MainActivity.this, "Отсутствует соединение с сервисом(", Toast.LENGTH_LONG).show();
                } else {
                    Object obj = null;
                    try {
                        obj = new JSONParser().parse(data);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    JSONObject jo = (JSONObject) obj;
                    String msg = (String) jo.get("msg");
                    Toast.makeText(MainActivity.this, "" + jo.get("msg"), Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        protected String doInBackground(String... params) {

            type_of_request = params[1];
            URL url;
            HttpURLConnection urlConnection = null;
            String server_response = null;


            if (mSettings.contains(APP_PREFERENCES_BASIC)) {
                mAuth = mSettings.getString(APP_PREFERENCES_BASIC, "");
            }

            Log.d(TAG, "doInBackground");
            try {
                url = new URL(params[2]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod(params[0]);
                if (params[0].toLowerCase().equals("POST")) {
                    urlConnection.setRequestProperty("Type", type_of_request);
                    urlConnection.setConnectTimeout(5000);
                    urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                }
                urlConnection.setRequestProperty("Authorization", "Basic " + mAuth);
                String result = null;
                if (type_of_request.equals("createDoc")) {
                    Log.d(TAG, "outputStream: ");
                    OutputStream os = urlConnection.getOutputStream();
                    os.write(params[3].getBytes("UTF-8"));
                    os.close();

                    Log.d(TAG, "inputStream: ");
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    result = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
                    bytes = IOUtils.toByteArray(in);
                    Log.d(TAG, "inputStream2: " + bytes);
                    in.close();
                }
                Log.d(TAG, "Authorization");
                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Response code: " + responseCode);
                    if (type_of_request.equals("createDoc"))
                        server_response = result;
                    else
                        server_response = readStream(urlConnection.getInputStream());
                } else {
                    //Toast.makeText(MainActivity.this, "Ошибка: " + responseCode, Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Ошибка: " + responseCode);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();

            } catch (IOException e) {
                server_response = "0";
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return server_response;
        }

        private void getInfo(String data) {
            if (data != null) {
                if (data.equals("0")) {
                    Toast.makeText(MainActivity.this, "Отсутствует соединение с сервисом(", Toast.LENGTH_LONG).show();
                } else {
                    Object obj = null;
                    try {
                        obj = new JSONParser().parse(data);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

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

                        addProduct(new ProductItem(id, name, party, "1"));
                        saveData();

                    } else {
                        type_of_request = "addProduct";
                        Toast.makeText(MainActivity.this, "" + jo.get("msg"), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        public boolean isExternalStorageWritable() {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                return true;
            }
            return false;
        }

        private void createDoc(String data) throws IOException, DocumentException {
            if (data.equals("0")) {
                Toast.makeText(MainActivity.this, "Отсутствует соединение с сервисом(", Toast.LENGTH_LONG).show();
            } else if (data != null) {
                BASE64Decoder decoder = new BASE64Decoder();
                byte[] decodedBytes = decoder.decodeBuffer(data);
                File file = new File(Environment.getExternalStorageDirectory().toString()+"/Download/" + "newfile.pdf");
                FileOutputStream fop = new FileOutputStream(file);
                fop.write(decodedBytes);
                fop.flush();
                fop.close();
            }
        }

        private void createProductReply(String data) {

        }

        private void test(String data) {
            if (data != null) {
                if (data.equals("0")) {
                    Toast.makeText(MainActivity.this, "Отсутствует соединение с сервисом(", Toast.LENGTH_LONG).show();
                }
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


}

