package com.open.hule.mobilepayment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tvPayment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("price","50");
                intent.putExtra("balance","100");
                intent.setClass(MainActivity.this, PayDialogActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.push_bottom_in, R.anim.push_bottom_silent);
            }
        });
    }
}
