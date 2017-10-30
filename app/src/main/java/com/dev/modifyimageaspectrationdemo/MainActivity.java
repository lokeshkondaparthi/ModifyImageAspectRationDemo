package com.dev.modifyimageaspectrationdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btGetImages;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btGetImages = (Button) findViewById(R.id.content_main_change_sz_bt);
        btGetImages.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        /* When click on change imaga button */
        switch (view.getId()) {
            case R.id.content_main_change_sz_bt:

                break;
        }
    }
}
