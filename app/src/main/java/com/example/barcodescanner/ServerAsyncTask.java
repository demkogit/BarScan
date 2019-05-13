package com.example.barcodescanner;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Adapter;
import android.widget.Toast;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ServerAsyncTask extends AsyncTask<String, Void, String> {
    String type_of_request;
    Context mContext;
    ArrayList<ProductItem> productItemList;
    ProductAdapter adapter;

    public ServerAsyncTask(Context mContext, ArrayList<ProductItem> productItemList, ProductAdapter adapter) {
        this.mContext = mContext;
        this.productItemList = productItemList;
        this.adapter = adapter;
    }

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
                createDoc(data);
        }
    }

    @Override
    protected String doInBackground(String... params) {

        type_of_request = params[1];
        URL url;
        HttpURLConnection urlConnection = null;
        String server_response = null;

        try {
            url = new URL(params[2]);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(params[0]);
            if (params[0].toLowerCase().equals("post")) {
                urlConnection.setRequestProperty("Type", type_of_request);
                urlConnection.setRequestProperty("JSON", params[3]);
            }
            urlConnection.setRequestProperty("Authorization", "Basic V2ViQXBpOjEyMzQ1");

            int responseCode = urlConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                server_response = readStream(urlConnection.getInputStream());
            } else {
                Toast.makeText(mContext, "Ошибка: " + responseCode, Toast.LENGTH_LONG).show();
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

        if (data.equals("0")) {
            Toast.makeText(mContext, "Отсутствует соединение с сервисом(", Toast.LENGTH_LONG).show();
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
                //saveData();

            } else {
                type_of_request = "addProduct";
                Toast.makeText(mContext, "" + jo.get("msg"), Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void addProduct(ProductItem product) {

        if (productExist(product)) {
            Toast.makeText(mContext, "Данный товар уже находится в списке товаров!", Toast.LENGTH_LONG).show();
        } else {
            productItemList.add(product);
            adapter.notifyDataSetChanged();
            Toast.makeText(mContext, "Товар успешно добавлен!", Toast.LENGTH_LONG).show();
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


    private void createDoc(String data) {
        if (data.equals("0")) {
            Toast.makeText(mContext, "Отсутствует соединение с сервисом(", Toast.LENGTH_LONG).show();
        } else if (data != null) {
            Toast.makeText(mContext, data.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void createProductReply(String data) {

    }

    private void test(String data) {
        if (data.equals("0")) {
            Toast.makeText(mContext, "Отсутствует соединение с сервисом(", Toast.LENGTH_LONG).show();
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
