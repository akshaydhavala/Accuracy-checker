package edu.cs4730.wearabledatalayer2;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.wearable.activity.WearableActivity;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.concurrent.ExecutionException;



public class MainActivity extends WearableActivity implements
    MessageClient.OnMessageReceivedListener,SensorEventListener {
    private TextView mTextView;
    String datapath = "/message_path";      //path for sending stepcount data
    String dataheartpath="/heart_path";     //path for sending  heart rate data
    SensorManager sensorManager;
    //Declare sensors
    Sensor mstepcounter;
    Sensor mheartrate;
    TextView textheart;
    TextView textstep;
    // Declare variables in reference to database
    FirebaseDatabase database;
    DatabaseReference stepRef;
    DatabaseReference heartRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Define database variables
        database=FirebaseDatabase.getInstance();
        stepRef=database.getReference("Steps");
        heartRef=database.getReference("Heart");
        //Define Sensor manager and sensors
        sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        mstepcounter=sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mheartrate=sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        // Set listener for stepcounter sensor
        sensorManager.registerListener(this,mstepcounter,sensorManager.SENSOR_DELAY_FASTEST);
        mTextView = findViewById(R.id.text);
        textheart=(TextView)findViewById(R.id.mheart);
        textstep=(TextView)findViewById(R.id.mstep);
        setAmbientEnabled();
    }
    // add listener when resumed
    @Override
    public void onResume() {
        super.onResume();
        Wearable.getMessageClient(this).addListener(this);
    }
    //Set heartrate listener for start button
    public void startHeart(View view){

        sensorManager.registerListener(this,mheartrate,sensorManager.SENSOR_DELAY_FASTEST);
    }
    //Function to be called to stop listening heart rate
    public void stopHeart(View view)

    {

        sensorManager.unregisterListener(this,mheartrate);
    }
    // remove listener when paused
    @Override
    public void onPause() {
        super.onPause();
        Wearable.getMessageClient(this).removeListener(this);
    }
    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
     // Display on wearable and send data to mobile through thread when the values inside the sensor are changed
        if( event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {

            String val = String.valueOf(event.values[0]);
            textstep.setText(val);  //display on wearble
            stepRef.setValue(val);  //store in realtime
            new SendThread(datapath, val).start(); //send it to mobile
        }
        if( event.sensor.getType() == Sensor.TYPE_HEART_RATE) {

            String val = String.valueOf(event.values[0]);
            textheart.setText(val);
            heartRef.setValue(val);
            new SendheartThread(dataheartpath, val).start();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("Tag", "onAccuracyChanged"+ accuracy);
    }
    // creating thread to use it as a medium to transfer data from wear to mobile
    class SendheartThread extends Thread {
        String path;
        String message;

        SendheartThread(String p, String msg) {
            path = p;
            message = msg;

        }
        public void run() {
            //Connect with mobile and share data
            Wearable.getMessageClient(MainActivity.this).sendMessage("mobile", path, message.getBytes());
        }
    }

    class SendThread extends Thread {
        String path;
        String message;

    SendThread(String p, String msg) {
            path = p;
            message = msg;

        }

        public void run() {
  //          Wearable.getMessageClient(MainActivity.this).sendMessage("mobile", path, message.getBytes());
   //     }
            //first get all the nodes, ie connected wearable devices.
            Task<List<Node>> nodeListTask =
                Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
            try {
                // Block on a task and get the result synchronously (because this is on a background
                // thread).
                List<Node> nodes = Tasks.await(nodeListTask);

                //Now send the message to each device.
                for (Node node : nodes) {
                    Task<Integer> sendMessageTask =
                        Wearable.getMessageClient(MainActivity.this).sendMessage(node.getId(), path, message.getBytes());

                    try {
                        // Block on a task and get the result synchronously (because this is on a background
                        // thread).
                        Integer result = Tasks.await(sendMessageTask);

                    } catch (ExecutionException exception) {

                    } catch (InterruptedException exception) {
                       }

                }

            } catch (ExecutionException exception) {

            } catch (InterruptedException exception) {
                }
        }
    }
}
