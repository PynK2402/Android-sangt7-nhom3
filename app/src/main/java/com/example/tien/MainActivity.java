package com.example.tien;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    EditText valueFrom, valueTo;
    ImageView reverse;
    Button convert;
    Spinner exchangeFrom, exchangeTo;
    List<String> symbolList, codeList;
    String API_KEY = "6S96ITMNuC5sFMuBJ7JOm9rJWT6jVd82";
    String HTTP_REQUEST = "https://api.apilayer.com/fixer/symbols?apikey=" + API_KEY;
    Handler handler = new Handler();
    ProgressDialog progressDialog;
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        valueFrom = findViewById(R.id.valueFrom);
        valueTo = findViewById(R.id.valueTo);
        reverse = findViewById(R.id.reverse);
        convert = findViewById(R.id.convertButton);
        exchangeFrom = findViewById(R.id.exchangeFrom);
        exchangeTo = findViewById(R.id.exchangeTo);
        symbolList = new ArrayList<>();
        codeList = new ArrayList<>();
        new FetchData().start();
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, symbolList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int fromIndex = exchangeFrom.getSelectedItemPosition();
                int toIndex = exchangeTo.getSelectedItemPosition();
                exchangeFrom.setSelection(toIndex);
                exchangeTo.setSelection(fromIndex);
            }
        });
        convert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fromCode = codeList.get(exchangeFrom.getSelectedItemPosition());
                String toCode = codeList.get(exchangeTo.getSelectedItemPosition());
                String CONVERT_URL = "https://api.apilayer.com/fixer/convert?to=" + toCode + "&from=" + fromCode + "&amount=" + Float.parseFloat(valueFrom.getText().toString()) + "&apikey=" + API_KEY;
                new Convert(CONVERT_URL).start();
            }
        });
    }

    class FetchData extends Thread {
        String data = "";

        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("Loading");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                }
            });
            try {
                URL url = new URL(HTTP_REQUEST);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    data += line;
                }
                if (!data.isEmpty()) {
                    JSONObject jsonObject = new JSONObject(data);
                    JSONObject symbols = jsonObject.getJSONObject("symbols");
                    Iterator x = symbols.keys();
                    while (x.hasNext()) {
                        String key = (String) x.next();
                        symbolList.add(symbols.get(key).toString());
                        codeList.add(key);
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                        exchangeFrom.setAdapter(arrayAdapter);
                        exchangeTo.setAdapter(arrayAdapter);
                    }
                }
            });
        }
    }

    class Convert extends Thread {
        String data = "", convertURL, result = "";

        public Convert(String convertURL) {
            this.convertURL = convertURL;
        }

        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("Loading");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                }
            });
            String data = "";
            URL url = null;
            try {
                url = new URL(convertURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    data += line;
                }
                if (!data.isEmpty()) {
                    JSONObject jsonObject = new JSONObject(data);
                    result = jsonObject.getString("result");
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    valueTo.setText(result);
                }
            });
        }
    }
}