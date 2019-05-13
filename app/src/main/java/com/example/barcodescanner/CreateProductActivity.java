package com.example.barcodescanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

public class CreateProductActivity extends AppCompatActivity {

    private ImageButton createImageButton;
    private EditText product_name, product_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_product);

        product_code = findViewById(R.id.product_code);
        product_name = findViewById(R.id.product_name);

        createImageButton = findViewById(R.id.createProductBtn);
        createImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("name", product_name.getText().toString());
                intent.putExtra("code", product_code.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });


    }
}
