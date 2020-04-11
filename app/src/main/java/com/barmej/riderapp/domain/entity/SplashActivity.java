package com.barmej.riderapp.domain.entity;

import androidx.appcompat.app.AppCompatActivity;
import callback.CallBack;

import android.os.Bundle;
import android.widget.Toast;

import com.barmej.riderapp.HomeActivity;
import com.barmej.riderapp.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_splash );
    }

    @Override
    protected void onResume() {
        super.onResume();
        TripManager.getInstance().login( new CallBack() {
            @Override
            public void onComplete(boolean isSuccessful) {
                if(isSuccessful){
                    startActivity( HomeActivity.getStartIntent(SplashActivity.this) );
                    fileList();
                }else{
                    Toast.makeText( SplashActivity.this, R.string.error_loading_data, Toast.LENGTH_LONG ).show();
                }

            }
        } );
    }
}
