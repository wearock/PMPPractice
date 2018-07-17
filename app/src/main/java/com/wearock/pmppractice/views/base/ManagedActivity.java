package com.wearock.pmppractice.views.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.wearock.pmppractice.controllers.ActivityController;

public class ManagedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityController.addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityController.removeActivity(this);
    }

}
