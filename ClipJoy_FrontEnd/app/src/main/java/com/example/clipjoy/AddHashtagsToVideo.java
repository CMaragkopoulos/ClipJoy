package com.example.clipjoy;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.clipjoy.R;

import com.google.android.material.snackbar.Snackbar;

import java.io.Serializable;
import java.util.ArrayList;

import Distributed_Project.utils.Message;
import Distributed_Project.utils.PublisherImp;
import Distributed_Project.utils.SingletonClass;

@RequiresApi(api = Build.VERSION_CODES.N)
public class AddHashtagsToVideo extends AppCompatActivity implements Serializable {

    public static final long serialVersionUID = 123456789L; //This used by Serializable

    Button btnHashtag;
    EditText inputParamAddHashtag;
    ArrayList<String> hashtagsNew;
    ArrayList<Message> chunks;
    PublisherImp publisher;
    SingletonClass single;
    View v;
    String videoName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hashtags_to_video);

        Intent intent = getIntent();
        videoName = intent.getStringExtra("videoName");;

        hashtagsNew = new ArrayList<>();

        single = SingletonClass.getInstance();
        publisher = single.getPublisher();

        chunks = publisher.getVideoNameChunks().get(videoName);

        btnHashtag = (Button) findViewById(R.id.BtnAddHashtags);
        inputParamAddHashtag = (EditText)findViewById(R.id.inputNameAddHashtags);
    }

    @Override
    protected void onStart() {
        super.onStart();

        btnHashtag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v = view;

                String[] hashtagsSplit = inputParamAddHashtag.getText().toString().split(" "); //φτιάχνει ένα String array και βάζει μέσα τα hashtags που μας έδωσε ο χρήστης(του έχω πει να βάζει κενό ανάμεσα)
                for (int i = 0; i < hashtagsSplit.length; i++) { //για κάθε χασταγκ που μας δώσαν
                    hashtagsNew.add(hashtagsSplit[i]); //βάζουμε το χασταγκ στο ArrayList μας
                    //αν υπάρχει ήδη το hashtag φτίαξε νέο ArrayList με String και
                    if (publisher.hashtagChunks.containsKey(hashtagsSplit[i])) {
                        ArrayList<String> existingVideoPathList = publisher.hashtagChunks.get(hashtagsSplit[i]); //βάλε μέσα όσα βίντεο είχε ήδη το hashtag
                        existingVideoPathList.add(videoName);  //πρόσθεσε και το νέο όνομα του νέου βίντεου
                        publisher.hashtagChunks.put(hashtagsSplit[i],existingVideoPathList); //βάλτε μέσα στο hashmap hashtagChunks του publisher
                    }
                    else { //αν δεν υπήρχε ήδη το hashtag φτίαξε νέο key στο hashmap hashtagChunks και βάλε ως κλειδί το hashtag και ως ArrayList<String> το όνομα του βίντεο
                        ArrayList<String> videoPathList = new ArrayList<>();
                        videoPathList.add(videoName);
                        publisher.hashtagChunks.put(hashtagsSplit[i],videoPathList);
                    }
                    Log.e("Path2",publisher.hashtagChunks.get(hashtagsSplit[i]).get(0));
                }
                publisher.channelName.setHashtagsPublished(hashtagsNew); //βάζουμε τα νέα μας hashtag στον publisher μας

                //AsyncTask για το push
                SaveVideo newVideo = new SaveVideo();
                newVideo.execute(publisher);
            }
        });

    }

    public class SaveVideo extends AsyncTask<PublisherImp,Void,Void> {

        @Override
        protected Void doInBackground(PublisherImp... publisherImps) {
            publisherImps[0].push();

            new android.os.Handler(Looper.getMainLooper()).postDelayed(
                    new Runnable() {
                        public void run() {
                            Log.i("tag", "This'll run 300 milliseconds later");
                        }
                    },
                    300);

            //μηνυματάκι για να ενημερώσεις τον χρήστη ότι μπήκαν τα hashtags επιτυχώς
            Snackbar mySnackbar = Snackbar.make(v, "You Have added hashtags to your video!", Snackbar.LENGTH_LONG);
            mySnackbar.show();
            return null;
        }


    }
}