package com.matthewcannefax.notificationscheduler;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button scheduleBTN;
    private Button cancelBTN;

    private Switch mDeviceIdleSwitch;
    private Switch mDeviceChargingSwitch;

    private SeekBar mSeekBar;

    private JobScheduler mScheduler;

    private static final int JOB_ID = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scheduleBTN = findViewById(R.id.scheduleBTN);
        cancelBTN = findViewById(R.id.cancelJobsBTN);

        mDeviceIdleSwitch = findViewById(R.id.idleSwitch);
        mDeviceChargingSwitch = findViewById(R.id.chargingSwitch);

        mSeekBar = findViewById(R.id.seekBar);

        //define the seekbar
        final TextView seekBarProgress = findViewById(R.id.seekBarProgress);

        //setup the seekbar listener
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                //track the progress of the seekbar
                if(i > 0){
                    seekBarProgress.setText(i + " s");
                }else{
                    seekBarProgress.setText("Not Set");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        scheduleJob();
        cancelJobs();
    }

    private void scheduleJob(){

        scheduleBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioGroup networkOptions = findViewById(R.id.networkOptions);
                mScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
                int selectedNetworkID = networkOptions.getCheckedRadioButtonId();

                int selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE;

                switch(selectedNetworkID){
                    case R.id.noNetwork:
                        selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE;
                        break;
                    case R.id.anyNetwork:
                        selectedNetworkOption = JobInfo.NETWORK_TYPE_ANY;
                        break;
                    case R.id.wifiNetwork:
                        selectedNetworkOption = JobInfo.NETWORK_TYPE_UNMETERED;
                        break;
                }

                ComponentName serviceName = new ComponentName(getPackageName(), NotificationJobService.class.getName());

                //create the job builder
                JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceName);

                //set constraints based on the switches for idle and charging
                builder.setRequiresDeviceIdle(mDeviceIdleSwitch.isChecked());
                builder.setRequiresCharging(mDeviceChargingSwitch.isChecked());
                builder.setRequiredNetworkType(selectedNetworkOption);

                //check if the seekbar has a value greater than zero
                int seekBarInteger = mSeekBar.getProgress();
                boolean seekBarSet = seekBarInteger > 0;

                if(seekBarSet){
                    //set up the override deadline if the seekbar has a value greater than zero
                    builder.setOverrideDeadline(seekBarInteger *1000);
                }

                //set up a check to make sure that there is an active connection
                //or that the job requires idle or charging
                //or if the seekbar is set
                boolean constraintSet = (selectedNetworkOption != JobInfo.NETWORK_TYPE_NONE) || mDeviceChargingSwitch.isChecked() || mDeviceIdleSwitch.isChecked() || seekBarSet;

                 if(constraintSet){
                     //schedule the job and notify the user
                     JobInfo myJobInfo = builder.build();

                     //schedule the job
                     mScheduler.schedule(myJobInfo);

                     //notify the user that the job has been scheduled
                     Toast.makeText(getApplicationContext(), "Jobs scheduled, job will run when the constraints are met.", Toast.LENGTH_SHORT).show();
                 }else{
                     //notify the user that there needs to be at least on constraint (at least one connection type)
                     Toast.makeText(getApplicationContext(), "Please set at least one constraint", Toast.LENGTH_SHORT).show();
                 }






            }
        });

    }

    private void cancelJobs(){
        cancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mScheduler!=null){
                    mScheduler.cancelAll();
                    mScheduler = null;
                    Toast.makeText(getApplicationContext(), "Jobs cancelled", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
