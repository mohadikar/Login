package com.example.hp.sales;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;


public class Animated extends AppCompatActivity {
   private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animated);
        imageView = (ImageView)findViewById(R.id.imageView4);
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.mytransition);
        imageView.startAnimation(animation);
        final Intent intent = new Intent(this,MainActivity.class);
        Thread time = new Thread(){
            public void run()
            {
                try{
                    sleep(3000);

                }  catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    startActivity(intent);
                    finish();
                }
            }
        };
        time.start();
    }
}
