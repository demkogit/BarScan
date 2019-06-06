package com.example.barcodescanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class CreateProductActivity extends AppCompatActivity {

    private Button createProductButton;
    private EditText product_name, product_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_product);

        product_code = findViewById(R.id.product_code);
        product_name = findViewById(R.id.product_name);

        createProductButton = findViewById(R.id.createProductBtn);
        createProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!check()) {
                    Intent intent = new Intent();
                    intent.putExtra("name", product_name.getText().toString());
                    intent.putExtra("code", product_code.getText().toString());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });


    }

    private boolean check(){
        boolean result = false;
        if(product_code.getText().toString().length() == 0){
            Toast.makeText(CreateProductActivity.this, "Введите штрихкод!", Toast.LENGTH_SHORT).show();
            result = true;
        }
        if(product_name.getText().toString().length() == 0){
            Toast.makeText(CreateProductActivity.this, "Введите название!", Toast.LENGTH_SHORT).show();
            result = true;
        }
        return result;
    }
}
