package com.example.clipjoy;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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

public class RecordVideo extends AppCompatActivity implements Serializable {

    private static final long serialVersionUID = 1L;

    static final int REQUEST_VIDEO_CAPTURE = 1;
    Button btnRecordVideo;
    PublisherImp publisher;
    SingletonClass single;
    String videoName;
    private Uri videoUri;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_video);
        btnRecordVideo = (Button) findViewById(R.id.sendBtnRecordVideo);

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

        //ΜΟΛΙΣ ΠΑΤΗΘΕΙ ΤΟ RECORD
        btnRecordVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE); //απαραίτητο για να μπορεί να γίνει record μετά
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,15); //time limit of video record 15 seconds
                startActivityForResult(intent,REQUEST_VIDEO_CAPTURE); //ΣΟΥ ΞΕΚΙΝΑΕΙ ΤΟ RECORD καλώντας την onActivityResult
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK) {
                videoUri = data.getData(); //ΚΡΑΤΑΕΙ ΤΟ URI ΑΠ ΤΟ ΒΙΝΤΕΟ ΠΟΥ ΤΡΑΒΗΞΕΣ
                Log.d("VIDEO_RECORD_TAG", "VIDEO IS RECORDED AND AVAILABLE AT PATH: " + data.getData());

                ArrayList<Message> list = new ArrayList<>(); // αρχικοποιεί κενή λίστα η οποία θα επιστραφεί στο τέλος

                //ΔΙΑΔΙΚΑΣΙΑ ΓΙΑ ΧΩΡΙΣΜΟΣ ΤΟΥ ΒΙΝΤΕΟΥ ΣΕ CHUNKS ΜΕΣΩ ΤΩΝ OBJECT MESSAGE
                try {
                    FileInputStream is = (FileInputStream) getApplicationContext().getContentResolver().openInputStream(videoUri);
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

                this.publisher.videoNameChunks.put(this.videoName, list); //βάζουμε ως key το όνομα του βίντεο και ως value το ArrayList με τα Message(όπου κάθε ένα έχει και ένα chunk(byte[]))
                Log.e("video name", this.videoName);
                Log.d("Debug3: ", "onActivityResult:" + data.getData());

                //πηγαίνουμε στο Activity tou AddHashtagsToVideo για να βάλουμε hashtags εκεί
                Intent intent = new Intent(getApplicationContext(), AddHashtagsToVideo.class);
                intent.putExtra("videoName", this.videoName); //βάζουμε και ως παράμετρο το όνομα του βίντεο
                startActivity(intent);
            }
            else if(resultCode == RESULT_CANCELED) {
                Log.d("VIDEO_RECORD_TAG", "VIDEO IS CANCELED");
            }
            else {
                Log.d("VIDEO_RECORD_TAG", "VIDEO HAS GOT SOME ERROR");

            }
        }
    }
}