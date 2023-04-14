package com.example.campushub;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.campushub.Calendar.CalendarFragmentMonth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        getSupportFragmentManager().beginTransaction()
//                .add(R.id.constraintLayoutMonth, new CalendarFragmentMonth(), "month")
//                .commit();
    }
}