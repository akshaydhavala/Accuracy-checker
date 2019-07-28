package edu.cs4730.wearabledatalayer2;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity implements
    MessageClient.OnMessageReceivedListener{
    FirebaseDatabase database;
    DatabaseReference myref;
    //DatabaseReference stepRef;
    TextView textserverheart;
    TextView textserverStep;
    TextView textheart;
    TextView textstep;
    String TAG = "Mobile MainActivity";
    TextView textView;
    Map<String,String> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        database=FirebaseDatabase.getInstance();
        myref=database.getReference();
        //get the widgets
        textView=(TextView)findViewById(R.id.text);
        textstep=(TextView)findViewById(R.id.mstep);
        textheart=(TextView)findViewById(R.id.mheart);
        textserverStep=(TextView)findViewById(R.id.mstep2);
        textserverheart=(TextView)findViewById(R.id.mheart2);
        //realtime updation of values
        myref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Store values in map object,it will be stored in the for of key value pair
                map=(Map<String,String>) dataSnapshot.getValue();
               textserverStep.setText(map.get("Steps"));
                textserverheart.setText(map.get("Heart"));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
    // activate the listener when resumed
    @Override
    public void onResume() {
        super.onResume();
        Wearable.getMessageClient(this).addListener(this);
    }
    // remove the listener when paused
    @Override
    public void onPause() {
        super.onPause();
        Wearable.getMessageClient(this).removeListener(this);
    }
    // recieve data from blutooth connected wearable
    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        Log.e("path",messageEvent.getPath().toString());

        if(messageEvent.getPath().equals("/heart_path")){
            String message = new String(messageEvent.getData());
            textheart.setText(message);
        }
        if(messageEvent.getPath().equals("/message_path")){
            String message = new String(messageEvent.getData());
            textstep.setText(message);
        }
    }

}
