package com.kotlin_example.ludo_demo.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.kotlin_example.ludo_demo.R;
//import com.kotlin_example.ludo_demo.fragments.PermissionsDialogFragment;
import com.kotlin_example.ludo_demo.openvidu.LocalParticipant;
import com.kotlin_example.ludo_demo.openvidu.RemoteParticipant;
import com.kotlin_example.ludo_demo.openvidu.Session;
import com.kotlin_example.ludo_demo.utils.CustomHttpClient;
import com.kotlin_example.ludo_demo.websocket.CustomWebSocket;

import org.jetbrains.annotations.NotNull;
import org.webrtc.EglBase;
import org.webrtc.MediaStream;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SessionActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 101;
    private static final int MY_PERMISSIONS_REQUEST = 102;
    private final String TAG = "SessionActivity";
    @BindView(R.id.views_container)
    LinearLayout views_container;
    //    @BindView(R.id.start_finish_call)
//    Button start_finish_call;
//    @BindView(R.id.session_name)
//    EditText session_name;
//    @BindView(R.id.participant_name)
//    EditText participant_name;
//    @BindView(R.id.application_server_url)
//    EditText application_server_url;
    @BindView(R.id.imageView)
    ImageView ludo_iv;
    @BindView(R.id.local_gl_surface_view)
    SurfaceViewRenderer localVideoView;
//    @BindView(R.id.main_participant)
//    TextView main_participant;
//    @BindView(R.id.peer_container)
//    FrameLayout peer_container;

    private String APPLICATION_SERVER_URL = "https://demos.openvidu.io/";
    ;
    private Session session;
    private CustomHttpClient httpClient;
    private static final String SESSION_ID = "demopostmansession";

    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources()
                .getColor(R.color.purple_700)));

        askForPermissions();
        ButterKnife.bind(this);
        setImage();
        imageloop();
//        Random random = new Random();
//        int randomIndex = random.nextInt(100);
//        participant_name.setText(participant_name.getText().append(String.valueOf(randomIndex)));

        buttonPressed();
    }

    private void imageloop()
    {
        final int img[] = { R.drawable.ludo_1, R.drawable.ludo_2, R.drawable.ludo_4,
                R.drawable.ludo_5, R.drawable.ludo_6, R.drawable.ludo_7, R.drawable.ludo_8,R.drawable.ludo_9
        ,R.drawable.ludo_10,R.drawable.ludo_11,R.drawable.ludo_12,R.drawable.ludo_13,R.drawable.ludo_14};



        //final Handler
                handler=new Handler();

        // Runnable runnable
                runnable = new Runnable() {
            int i = 0;

            @Override
            public void run() {

                //layout.setBackgroundResource(img[i]);
                ludo_iv.setImageResource(img[i]);
                i++;
                if (i > img.length - 1) {
                    i = 0;
                }
                handler.postDelayed(this, 2500);  //for interval 4s..

            }
        };handler.postDelayed(runnable, 2500); //for initial delay..




    }

    private void setImage()
    {
//        ludo_iv.setImageResource();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ludo_iv.setImageDrawable(getResources().getDrawable(R.drawable.ludo_0, getApplicationContext().getTheme()));
        } else {
            ludo_iv.setImageDrawable(getResources().getDrawable(R.drawable.ludo_0));
       }

    }//set image close


    public void askForPermissions() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST);
        } else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }

    public void buttonPressed() {
//        if (start_finish_call.getText().equals(getResources().getString(R.string.hang_up))) {
//            // Already connected to a session
//            leaveSession();
//            return;
//        }
        if (arePermissionGranted()) {
            initViews();
           // viewToConnectingState();

            //APPLICATION_SERVER_URL = application_server_url.getText().toString();
            httpClient = new CustomHttpClient(APPLICATION_SERVER_URL);

            //String sessionId = session_name.getText().toString();
            getToken(SESSION_ID);
        } else {

AlertDialog.Builder builder = new AlertDialog.Builder(SessionActivity.this);

            builder.setTitle(R.string.permissions_dialog_title);
            builder.setMessage(R.string.no_permissions_granted)
                    .setPositiveButton(R.string.accept_permissions_dialog, (dialog, id) -> ((SessionActivity) this).askForPermissions())
                    .setNegativeButton(R.string.cancel_dialog, (dialog, id) -> Log.i(TAG, "User cancelled Permissions Dialog"));
            builder.show();


//
//            DialogFragment permissionsFragment = new PermissionsDialogFragment();
//            permissionsFragment.show(getSupportFragmentManager(), "Permissions Fragment");
        }
    }

    private void getToken(String sessionId) {
        try {
            // Session Request
            RequestBody sessionBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "{\"customSessionId\": \"" + sessionId + "\"}");
            this.httpClient.httpCall("/api/sessions", "POST", "application/json", sessionBody, new Callback() {

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    Log.d(TAG, "responseString: " + response.body().string());

                    // Token Request
                    RequestBody tokenBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "{}");
                    httpClient.httpCall("/api/sessions/" + sessionId + "/connections", "POST", "application/json", tokenBody, new Callback() {
//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxzzzzzzzzzzzzzzzzzzzzzzzzzvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) {
                            String responseString = null;
                            try {
                                responseString = response.body().string();
                            } catch (IOException e) {
                                Log.e(TAG, "Error getting body", e);
                            }
                            getTokenSuccess(responseString, sessionId);
                        }

                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            Log.e(TAG, "Error POST /api/sessions/SESSION_ID/connections", e);
                            connectionError(APPLICATION_SERVER_URL);
                        }
                    });
                }

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.e(TAG, "Error POST /api/sessions", e);
                    connectionError(APPLICATION_SERVER_URL);
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Error getting token", e);
            e.printStackTrace();
            connectionError(APPLICATION_SERVER_URL);
        }
    }

    private void getTokenSuccess(String token, String sessionId) {
        // Initialize our session
        session = new Session(sessionId, token, views_container, this);

        // Initialize our local participant and start local camera

        // String participantName = participant_name.getText().toString();
        String participantName="anil";
        LocalParticipant localParticipant = new LocalParticipant(participantName, session, this.getApplicationContext(), localVideoView);
        localParticipant.startCamera();
        runOnUiThread(() -> {
            // Update local participant view
            //---main_participant.setText(participant_name.getText().toString());
            //---main_participant.setPadding(20, 3, 20, 3);
        });

        // Initialize and connect the websocket to OpenVidu Server
        startWebSocket();
    }

    private void startWebSocket() {
        CustomWebSocket webSocket = new CustomWebSocket(session, this);
        webSocket.execute();
        session.setWebSocket(webSocket);
    }

    private void connectionError(String url) {
        Runnable myRunnable = () -> {
            Toast toast = Toast.makeText(this, "Error connecting to " + url, Toast.LENGTH_LONG);
            toast.show();
           // viewToDisconnectedState();
        };
        new Handler(this.getMainLooper()).post(myRunnable);
    }

    private void initViews() {
        EglBase rootEglBase = EglBase.create();
        localVideoView.init(rootEglBase.getEglBaseContext(), null);
        localVideoView.setMirror(true);
        localVideoView.setEnableHardwareScaler(true);
        localVideoView.setZOrderMediaOverlay(true);
    }

    public void viewToDisconnectedState() {
        runOnUiThread(() -> {
            localVideoView.clearImage();
            localVideoView.release();
//            start_finish_call.setText(getResources().getString(R.string.start_button));
//            start_finish_call.setEnabled(true);
//            application_server_url.setEnabled(true);
//            application_server_url.setFocusableInTouchMode(true);
//            session_name.setEnabled(true);
//            session_name.setFocusableInTouchMode(true);
//            participant_name.setEnabled(true);
//            participant_name.setFocusableInTouchMode(true);
//            main_participant.setText(null);
//            main_participant.setPadding(0, 0, 0, 0);
        });
    }

    public void viewToConnectingState() {
        runOnUiThread(() -> {
//            start_finish_call.setEnabled(false);
//            application_server_url.setEnabled(false);
//            application_server_url.setFocusable(false);
//            session_name.setEnabled(false);
//            session_name.setFocusable(false);
//            participant_name.setEnabled(false);
//            participant_name.setFocusable(false);
        });
    }

    public void viewToConnectedState() {
        runOnUiThread(() -> {
//            start_finish_call.setText(getResources().getString(R.string.hang_up));
//            start_finish_call.setEnabled(true);
        });
    }



    public void createRemoteParticipantVideo(final RemoteParticipant remoteParticipant) {
        Handler mainHandler = new Handler(this.getMainLooper());
        Runnable myRunnable = () -> {
            View rowView = this.getLayoutInflater().inflate(R.layout.peer_video, null);
            //v, height
            // LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout.LayoutParams lp= new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,1.0f);
            lp.setMargins(4, 0, 0, 0);
            rowView.setLayoutParams(lp);
            int rowId = View.generateViewId();
            rowView.setId(rowId);
            views_container.addView(rowView);
            SurfaceViewRenderer videoView = (SurfaceViewRenderer) ((ViewGroup) rowView).getChildAt(0);
            remoteParticipant.setVideoView(videoView);
            videoView.setMirror(false);
            EglBase rootEglBase = EglBase.create();
            videoView.init(rootEglBase.getEglBaseContext(), null);
            videoView.setZOrderMediaOverlay(true);
            //View textView = ((ViewGroup) rowView).getChildAt(1);
            //remoteParticipant.setParticipantNameText((TextView) textView);
            remoteParticipant.setView(rowView);

            //remoteParticipant.getParticipantNameText().setText(remoteParticipant.getParticipantName());
            //remoteParticipant.getParticipantNameText().setPadding(20, 3, 20, 3);
        };
        mainHandler.post(myRunnable);
    }

    public void setRemoteMediaStream(MediaStream stream, final RemoteParticipant remoteParticipant) {
        final VideoTrack videoTrack = stream.videoTracks.get(0);
        videoTrack.addSink(remoteParticipant.getVideoView());
        runOnUiThread(() -> {
            remoteParticipant.getVideoView().setVisibility(View.VISIBLE);
        });
    }

    public void leaveSession() {
        if(this.session != null) {
            this.session.leaveSession();
        }
        if(this.httpClient != null) {
            this.httpClient.dispose();
        }
       // viewToDisconnectedState();
    }

    private boolean arePermissionGranted() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_DENIED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_DENIED);
    }

    @Override
    protected void onDestroy() {
        leaveSession();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        try {
            leaveSession();
            //handler.removeCallbacks(runnable);
           // Intent intent =new Intent(SessionActivity.this,home_page.class);
            //startActivity(intent);
            super.onBackPressed();
        }catch (Exception e) {
            Toast.makeText(this, "crash by", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        leaveSession();
        super.onStop();
    }

}
