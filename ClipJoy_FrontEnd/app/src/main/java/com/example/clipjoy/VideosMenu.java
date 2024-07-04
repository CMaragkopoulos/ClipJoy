package com.example.clipjoy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.clipjoy.R;

import java.util.ArrayList;

public class VideosMenu extends AppCompatActivity {

    public TextView txtParam;
    String channelName;
    ArrayList<String> videoNames;
    ArrayList<String> paths;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos_menu);

        txtParam = (TextView) findViewById(R.id.txtParam2);

        Intent intent = getIntent();
        channelName = intent.getStringExtra("channelName"); //λαμβάνουμε το channelName που μας δώσαν στο προηγούμενο Activity
        videoNames = intent.getStringArrayListExtra("videoNames"); //λαμβάνουμε το ArrayList με τα ονόματα των βίντεο που μας δώσαν στο προηγούμενο Activity
        paths = intent.getStringArrayListExtra("paths"); //λαμβάνουμε το ArrayList με τα paths των βίντεο που μας δώσαν στο προηγούμενο Activity

        String message = txtParam.getText().toString();
        txtParam.setText(message + " " + channelName);

        for(int i = 0; i < paths.size(); i++) { //create buttons with thumbnails dynamically
            Log.e("Path on menu",paths.get(i));
            ImageButton tempButton = new ImageButton(this); //create ImageButton
            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(paths.get(i),
                    MediaStore.Images.Thumbnails.MINI_KIND); //create bitmap
            tempButton.setImageBitmap(thumb); //set the bitmap of first frame on the ImageButton
            LinearLayout ll = (LinearLayout)findViewById(R.id.linearLay); //create the layout
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            //για κάθε κουμπί που κάναμε βάζουμε μια OnClickListener
            tempButton.setOnClickListener(getOnClick(paths.get(i),videoNames.get(i))); //paths.get(i) έχει το path του βίντεο και το videoNames.get(i) το όνομα του

            ll.addView(tempButton, lp);
        }
    }

    private View.OnClickListener getOnClick(String x, String y) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //όταν πατήσει κάποιο από τα ImageButton μας πηγαίνει στο Activity του VideoPlayer και στέλνει ως παραμέτρους το path και το όνομα του βίντεο
                Intent intent = new Intent(getApplicationContext() , VideoPlayer.class);
                intent.putExtra("path",x);
                intent.putExtra("videoName", y);
                startActivity(intent);
            }
        };
    }
}