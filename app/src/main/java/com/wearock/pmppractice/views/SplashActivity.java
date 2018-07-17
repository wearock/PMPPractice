package com.wearock.pmppractice.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.wearock.pmppractice.Application;
import com.wearock.pmppractice.R;
import com.wearock.pmppractice.controllers.ViewHelper;

public class SplashActivity extends AppCompatActivity {

    private ImageView ivWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ivWelcome = findViewById(R.id.ivWelcome);
        ivWelcome.setImageBitmap(ViewHelper.readBitmap(SplashActivity.this, R.drawable.splush_bg));
    }

    @Override
    protected void onResume() {
        super.onResume();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initializeLocalDB();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                }
            }
        }, 1000);
    }

    private void initializeLocalDB() {
        try {
            Application.getInstance().initialize(SplashActivity.this);
            Application.getInstance().getDBHelper(SplashActivity.this).getReadableDatabase().close();
            finish();
        } catch (IllegalStateException ise) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
            TextView tvMessage = new TextView(SplashActivity.this);
            tvMessage.setText(ise.getMessage());
            builder.setView(tvMessage);
            builder.setPositiveButton(R.string.dialog_positive_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Send broadcast to close MainActivity as well
                    Intent broadcast = new Intent();
                    broadcast.setAction(MainActivity.ACTION_EXIT_APP);
                    sendBroadcast(broadcast);

                    // Dismiss dialog itself
                    dialogInterface.dismiss();
                }
            });

            builder.create().show();
        }
    }
}
