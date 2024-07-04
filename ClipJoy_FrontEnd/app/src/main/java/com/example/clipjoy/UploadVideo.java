package com.example.clipjoy;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.clipjoy.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import Distributed_Project.utils.Message;
import Distributed_Project.utils.PublisherImp;
import Distributed_Project.utils.SingletonClass;

import static Distributed_Project.utils.Variables.VIDEO_PART;

public class UploadVideo extends AppCompatActivity implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int REQUEST_TAKE_GALLERY_VIDEO = 1001;

    Button btnChooseVideo;
    String videoName;
    PublisherImp publisher;
    SingletonClass single;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        btnChooseVideo = (Button) findViewById(R.id.sendBtnChooseVideo);

        Intent intent = getIntent();
        videoName = intent.getStringExtra("parameterName"); //παίρνω το όνομα του βίντεου που μας δώσαν στο προηγούμενο activity
        Log.e("Name of video on upload", videoName);

        //παίρνουμε το singleton instance του publisher μας
        single = SingletonClass.getInstance();
        publisher = single.getPublisher();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //ΜΟΛΙΣ ΠΑΤΗΘΕΙ ΤΟ CHOOSE
        btnChooseVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT); //απαραίτητο για να μπορεί να ανοίξει το gallery μετά
                startActivityForResult(Intent.createChooser(intent,"Select Video"),REQUEST_TAKE_GALLERY_VIDEO); //ΣΟΥ ΑΝΟΙΓΕΙ ΤΟ GALLERY καλώντας την onActivityResult
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                Uri selectedImageUri = data.getData(); //ΚΡΑΤΑΕΙ ΤΟ URI ΤΟΥ ΒΙΝΤΕΟ ΠΟΥ ΠΑΤΗΣΕΣ ΣΤΟ GALLERY ΣΟΥ

                ArrayList<Message> list = new ArrayList<>(); // αρχικοποιεί κενή λίστα η οποία θα επιστραφεί στο τέλος

                //ΔΙΑΔΙΚΑΣΙΑ ΓΙΑ ΧΩΡΙΣΜΟΣ ΤΟΥ ΒΙΝΤΕΟΥ ΣΕ CHUNKS ΜΕΣΩ ΤΩΝ OBJECT MESSAGE
                try {
                    FileInputStream is = (FileInputStream) getApplicationContext().getContentResolver().openInputStream(selectedImageUri);
                    int len = is.available(); //όσα bytes μπορούν να γίνουν read τα δείχνει στο available()
                    int i = 0;
                    while (len > 0) {
                        if (len > 512000) len = 512000; // MAx δεκτά bytes = 512 kb
                        byte[] buffer = new byte[len]; // byte array με χώρο όσο το len
                        is.read(buffer);  //εδώ τα κάνουμε read
                        Message m = new Message(VIDEO_PART + " " + i); //να μας δείχνει ποίο τσανκ γύρισε
                        i++;
                        m.setVideoChunk(buffer); //βάζουμε το τσανκ στο μήνυμα m ώστε μέσω αυτού να τα βάλουμε στην λίστα
                        list.add(m); // βάζουμε τα bytes στην λίστα μας
                        len = is.available(); //τα μειομένα πλέον bytes που μπορούν να γίνουν read τα βάζουμε στο len
                    }
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                this.publisher.videoNameChunks.put(this.videoName,list); //βάζουμε ως key το όνομα του βίντεο και ως value το ArrayList με τα Message(όπου κάθε ένα έχει και ένα chunk(byte[]))
                Log.e("video name", this.videoName);
                Log.d("Debug: ", "onActivityResult:" + data.getData());

                //πηγαίνουμε στο Activity tou AddHashtagsToVideo για να βάλουμε hashtags εκεί
                Intent intent = new Intent(getApplicationContext(), AddHashtagsToVideo.class);
                intent.putExtra("videoName",this.videoName); //βάζουμε και ως παράμετρο το όνομα του βίντεο
                startActivity(intent);
            }
        }
    }
}