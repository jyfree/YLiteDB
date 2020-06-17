package com.jy.simple2;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.jy.litedb.api.FieldManager;
import com.jy.litedb.api.LoaderFieldInfo;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LoaderFieldInfo.openDexFileLoaderService(this);
        FieldManager.INSTANCE.createTable(TestJava.class);
    }
}
