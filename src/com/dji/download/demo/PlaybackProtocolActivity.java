package com.dji.download.demo;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import com.dji.mediadownloaddemo.R;

import dji.log.DJILogHelper;
import dji.midware.data.manager.P3.ServiceManager;
import dji.sdk.api.DJIDrone;
import dji.sdk.api.DJIError;
import dji.sdk.api.DJIDroneTypeDef.DJIDroneType;
import dji.sdk.api.Camera.DJICameraPlaybackState;
import dji.sdk.api.Camera.DJICameraSettingsTypeDef.CameraMediaFileType;
import dji.sdk.api.Camera.DJICameraSettingsTypeDef.CameraMode;
import dji.sdk.api.Camera.DJICameraSettingsTypeDef.CameraPlaybackMode;
import dji.sdk.api.RemoteController.DJIRemoteControllerAttitude;
import dji.sdk.interfaces.DJICameraModeCallBack;
import dji.sdk.interfaces.DJICameraPlayBackStateCallBack;
import dji.sdk.interfaces.DJIExecuteResultCallback;
import dji.sdk.interfaces.DJIFileDownloadCallBack;
import dji.sdk.interfaces.DJIGerneralListener;
import dji.sdk.interfaces.DJIReceivedVideoDataCallBack;
import dji.sdk.interfaces.DJIRemoteControllerUpdateAttitudeCallBack;
import dji.sdk.widget.DjiGLSurfaceView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/** 
 * @author      : DJI Software
 * @date        : 18:30:40 22th, July 
 * @version     : V1.0
 */

public class PlaybackProtocolActivity extends Activity implements OnClickListener
{
    private final static String TAG = "PlaybackProtocolActivity";
    
    private final static int SHOWDIALOG = 0;
    private final static int SHOWTOAST = 1;
    private final static int SHOWDOWNLOADDIALOG = 2;
    private final static int CLOSEDOWNLOADDIALOG = 3;
    
    private final static int VISIBLE = 0;
    private final static int GONE = 8;
    
    private final static int CAPUTRE = 2;
    private final static int RECORD = 3;
    private final static int PLAYBACK = 4;
    private final static int MULTIPLEPLAYBACK = 5;
    
    private final static int GESTURETHRESHOLD = 50;
    
    private boolean isEdited = false;
    private boolean isMultiple = false;
    
    private Timer mTimer;
    
    class Task extends TimerTask {
        @Override
        public void run() {
            checkConnectState();
        }
    }
    
    private Button mStartTakePhotoBtn;
    private Button mStartRecordBtn;
    private Button mStopRecordBtn;
    private Button mCaptureModeBtn;
    private Button mRecordModeBtn;
    private Button mPlaybacnModeBtn;
    private Button mMultiplePreviewModeBtn;
    private Button mMultiSelectPreviewModeBtn;
    private Button mDownloadBtn;
    private Button mDeleteBtn;
    
    private ImageButton mPlayVideoBtn;
    private ImageButton mPauseVideoBtn;
    
    private TextView mConnectStatusTextView;
    
    private DjiGLSurfaceView mDjiGLSurfaceView;
    
    private PlaybackGridView mGridView;
    
    private ProgressDialog mProgressDialog;
    
    private DJICameraPlaybackState mCameraPlaybackState;
    
    private DJIExecuteResultCallback mExecuteCallback;
    private DJIFileDownloadCallBack mFileDownloadCallBack;
    private DJIReceivedVideoDataCallBack mReceivedVideoDataCallBack;
    private DJICameraPlayBackStateCallBack mCameraPlaybackStateCallBack;
    private DJIRemoteControllerUpdateAttitudeCallBack mRemoteControllerUpdateAttitudeCallBack;
        
    private GestureDetector mGestureDetector;
    
    private Handler handler = new Handler(new Handler.Callback() {
        
        @Override
        public boolean handleMessage(Message msg)
        {
            switch (msg.what) {
                case SHOWDIALOG : {
                    showMessage(getString(R.string.demo_activation_message_title),(String)msg.obj);
                    break;
                }
                
                case SHOWTOAST : {
                    setResultToToast((String)msg.obj); 
                    break;
                }
                
                case SHOWDOWNLOADDIALOG : {
                    ShowDownloadProgressDialog();
                    break;
                }
                
                case CLOSEDOWNLOADDIALOG : {
                    HideDownloadProgressDialog();
                    break;
                }
            }
            return false;
        }
    });
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback_protocol);
                
        Intent intent = getIntent();
        int type = intent.getIntExtra("droneType", 0);
        
        onInitSDK(type);
        
        new Thread(){
            public void run() {
                try {
                    DJIDrone.checkPermission(getApplicationContext(), new DJIGerneralListener() {
                        
                        @Override
                        public void onGetPermissionResult(int result) {
                            // TODO Auto-generated method stub
                            Log.e(TAG, "onGetPermissionResult = "+result);
                            Log.e(TAG, "onGetPermissionResultDescription = "+DJIError.getCheckPermissionErrorDescription(result));
                            if (result == 0) {
                                handler.sendMessage(handler.obtainMessage(SHOWDIALOG, DJIError.getCheckPermissionErrorDescription(result)));
                                //This method require sdk level higher than level-1. Also permission should be got befor invoking this method.
                                DJIDrone.getDjiCamera().getCameraMode(new DJICameraModeCallBack() {
                                    public void onResult(CameraMode mode) {
                                        onStatusChange(mode.value());
                                        if (mode._equals(CameraMode.Camera_PlayBack_Mode.value())) {
                                            DJIDrone.getDjiCamera().setCameraMode(CameraMode.Camera_PlayBack_Mode, mExecuteCallback);
                                        }
                                    }
                                });
                            } else {
                                handler.sendMessage(handler.obtainMessage(SHOWDIALOG, getString(R.string.demo_activation_error)
                                                                         +DJIError.getCheckPermissionErrorDescription(result)+
                                                                         "\n"+getString(R.string.demo_activation_error_code)+result));                        
                            }
                        }
                    });
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();
        
        onInitActivity();
        
        mDjiGLSurfaceView = (DjiGLSurfaceView)findViewById(R.id.DjiSurfaceView_PBP);
        
        mDjiGLSurfaceView.start();
        
        mReceivedVideoDataCallBack = new DJIReceivedVideoDataCallBack() {

            @Override
            public void onResult(byte[] videoBuffer, int size)
            {
                // TODO Auto-generated method stub
                mDjiGLSurfaceView.setDataToDecoder(videoBuffer, size);
            }
            
        };
        
        DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(mReceivedVideoDataCallBack);
        
        mExecuteCallback = new DJIExecuteResultCallback() {

            @Override
            public void onResult(DJIError mErr)
            {
                onResultToast(mErr.errorDescription);
            }
            
        };
        
        mRemoteControllerUpdateAttitudeCallBack = new DJIRemoteControllerUpdateAttitudeCallBack() {

            @Override
            public void onResult(DJIRemoteControllerAttitude attitude)
            {
                if (attitude.playbackStatus || attitude.recordStatus || attitude.shutterStatus) {
                    DJIDrone.getDjiCamera().getCameraMode(new DJICameraModeCallBack() {

                        @Override
                        public void onResult(CameraMode mode)
                        {
                            onStatusChange(mode.value());
                        }
                        
                    });
                }
            }
            
        };
        
        DJIDrone.getDjiRemoteController().setRemoteControllerUpdateAttitudeCallBack(mRemoteControllerUpdateAttitudeCallBack);
        
        mCameraPlaybackStateCallBack = new DJICameraPlayBackStateCallBack(){

            @Override
            public void onResult(DJICameraPlaybackState state)
            {
                mCameraPlaybackState = state;
                    PlaybackProtocolActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run()
                        {
                            // TODO Auto-generated method stub
                            isVideoPreview();
                        }
                        
                    });
            }
            
        };
        
        DJIDrone.getDjiCamera().setDJICameraPlayBackStateCallBack(mCameraPlaybackStateCallBack);
        
        CreateProgressDialog();
        
        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){ 
           @Override
           public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
               if (e2.getRawX() - e1.getRawX() > GESTURETHRESHOLD) {
                   DJIDrone.getDjiCamera().singlePreviewNextPage(mExecuteCallback);
                   return true;
               }
               
               if (e1.getRawX() - e2.getRawX() > GESTURETHRESHOLD) {
                   DJIDrone.getDjiCamera().singlePreviewPreviousPage(mExecuteCallback);
                   return true;
               }
               
               if (e1.getRawY() - e2.getRawY() > GESTURETHRESHOLD) {
                   DJIDrone.getDjiCamera().multiplePreviewPreviousPage(mExecuteCallback);
                   return true;
               }
               
               if (e2.getRawY() - e1.getRawY() > GESTURETHRESHOLD) {
                   DJIDrone.getDjiCamera().multiplePreviewNextPage(mExecuteCallback);
                   return true;
               }
               return false;
           }
        });
        
        mFileDownloadCallBack = new DJIFileDownloadCallBack() {
            
            @Override
            public void OnStart()
            {
                handler.sendMessage(handler.obtainMessage(SHOWDOWNLOADDIALOG, null));
                
                if(mProgressDialog != null){
                    mProgressDialog.setProgress(0);
                }
                
                handler.sendMessage(handler.obtainMessage(SHOWTOAST, "download OnStart"));
                DJILogHelper.getInstance().LOGD("", "download OnStart",true,false);
            }

            @Override
            public void OnError(Exception exception)
            {
                // TODO Auto-generated method stub
                handler.sendMessage(handler.obtainMessage(CLOSEDOWNLOADDIALOG, null));
                handler.sendMessage(handler.obtainMessage(SHOWTOAST, "download OnError :"+exception.toString()));
                DJILogHelper.getInstance().LOGD("", "download OnError :"+exception.toString(),true,false);
            }
            
            @Override
            public void OnEnd()
            {
                // TODO Auto-generated method stub
                
                handler.sendMessage(handler.obtainMessage(CLOSEDOWNLOADDIALOG, null));
                handler.sendMessage(handler.obtainMessage(SHOWTOAST, "download OnEnd" + mCameraPlaybackState.numbersOfSelected));
                
                DJILogHelper.getInstance().LOGD("", "download OnEnd",true,false);
                
                DJIDrone.getDjiCamera().finishDownloadAllSelectedFiles(new DJIExecuteResultCallback() {
                    
                    @Override
                    public void onResult(DJIError mErr)
                    {
                        // TODO Auto-generated method stub
                        DJILogHelper.getInstance().LOGD("", "download finishDownloadAllSelectedFiles:"+mErr.errorDescription,true,false);
                    }
                });
                
                isEdited = false;
            }

            @Override
            public void OnProgressUpdate(int progress)
            {
                // TODO Auto-generated method stub
                if(mProgressDialog != null){
                    mProgressDialog.setProgress(progress);
                }
                
                DJILogHelper.getInstance().LOGD("", "download OnProgressUpdate progress="+progress,true,false);
            }

        };
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event))
            return false;
        super.dispatchTouchEvent(event);
        return true;
    }
    
    private void onInitActivity() {
        mCameraPlaybackState = new DJICameraPlaybackState();
        
        mConnectStatusTextView = (TextView)findViewById(R.id.ConnectStateItTextView);
        
        mStartTakePhotoBtn = (Button)findViewById(R.id.CaptureBtnPlayback);
        mStartRecordBtn = (Button)findViewById(R.id.StartRecordBtnPlayback);
        mStopRecordBtn = (Button)findViewById(R.id.StopRecordBtnPlayback);
        mCaptureModeBtn = (Button)findViewById(R.id.CaptureModeBtnPlayback);
        mRecordModeBtn = (Button)findViewById(R.id.RecordModeBtnPlayback);
        mPlaybacnModeBtn = (Button)findViewById(R.id.PlaybackModeBtnPlayback);
        mMultiplePreviewModeBtn = (Button)findViewById(R.id.MultiplePreviewBtnPlayback);
        mMultiSelectPreviewModeBtn = (Button)findViewById(R.id.MultiSelectPreviewBtnPlayback);
        mDownloadBtn = (Button)findViewById(R.id.DownloadBtnPlayback);
        mDeleteBtn = (Button)findViewById(R.id.DeleteBtnPlayback);
        
        mPlayVideoBtn = (ImageButton)findViewById(R.id.PlayVideoBtnPlayback);
        mPauseVideoBtn = (ImageButton)findViewById(R.id.PauseVideoBtnPlayback);
        
        mGridView = (PlaybackGridView)findViewById(R.id.GridView);
        
        mStartTakePhotoBtn.setOnClickListener(this);
        mStartRecordBtn.setOnClickListener(this);
        mStopRecordBtn.setOnClickListener(this);
        mCaptureModeBtn.setOnClickListener(this);
        mRecordModeBtn.setOnClickListener(this);
        mPlaybacnModeBtn.setOnClickListener(this);
        mMultiplePreviewModeBtn.setOnClickListener(this);
        mMultiSelectPreviewModeBtn.setOnClickListener(this);
        mDownloadBtn.setOnClickListener(this);
        mDeleteBtn.setOnClickListener(this);
        mPlayVideoBtn.setOnClickListener(this);
        mPauseVideoBtn.setOnClickListener(this);
        
        ButtonAdapter adapter = new ButtonAdapter(this);
        mGridView.setAdapter(adapter);
    }
    
    @Override
    protected void onDestroy() {
        onUnInitSDK();
        super.onDestroy();
    }
    
    @Override
    protected void onResume() {
        mTimer = new Timer();
        Task task = new Task();
        mTimer.schedule(task, 0, 500);
        
        ServiceManager.getInstance().pauseService(false);
        DJIDrone.getDjiRemoteController().startUpdateTimer(1000);
        
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
        
        ServiceManager.getInstance().pauseService(true);
        DJIDrone.getDjiRemoteController().stopUpdateTimer();
        
        super.onPause();
    }
    
    private void checkConnectState() {
        PlaybackProtocolActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run()
            {
                if (null != DJIDrone.getDjiCamera()) {
                    boolean bConnectState = DJIDrone.getDjiCamera().getCameraConnectIsOk();
                    if (bConnectState) {
                        mConnectStatusTextView.setText(R.string.drone_connection_status_connect);
                    } else {
                        mConnectStatusTextView.setText(R.string.drone_connection_status_disconnect);
                    }
                }
            }
            
        });
    }
    
    private void onInitSDK(int type){
        switch(type){
            case 0 : {
                DJIDrone.initWithType(this.getApplicationContext(),DJIDroneType.DJIDrone_Inspire1);
                break;
            }
            case 1 : {
                DJIDrone.initWithType(this.getApplicationContext(),DJIDroneType.DJIDrone_Phantom3_Professional);
                break;
            }
            case 2 : {
                DJIDrone.initWithType(this.getApplicationContext(),DJIDroneType.DJIDrone_Phantom3_Advanced);
                break;
            }
            case 3 : {
                DJIDrone.initWithType(this.getApplicationContext(),DJIDroneType.DJIDrone_M100);
                break;
            }
            default : {
                break;
            }
        }
        
        DJIDrone.connectToDrone();
    }
    
    private void onUnInitSDK(){
        DJIDrone.disconnectToDrone();
    }
    
    public void showMessage(String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    
    public void onReturn(View view){
        Log.d(TAG ,"onReturn");  
        this.finish();
    }
    
    @Override
    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.CaptureBtnPlayback : {
                DJIDrone.getDjiCamera().startTakePhoto(mExecuteCallback);
                break;
            }
            
            case R.id.StartRecordBtnPlayback : {
                DJIDrone.getDjiCamera().startRecord(mExecuteCallback);
                break;
            }
            
            case R.id.StopRecordBtnPlayback : {
                DJIDrone.getDjiCamera().stopRecord(mExecuteCallback);
                break;
            }
            
            case R.id.CaptureModeBtnPlayback : {
                onPressStatusBtn(CAPUTRE);
                isMultiple = false;
                break;
            }
            
            case R.id.RecordModeBtnPlayback : {
                onPressStatusBtn(RECORD);
                isMultiple = false;
                break;
            }
            
            case R.id.PlaybackModeBtnPlayback : {
                onPressStatusBtn(PLAYBACK);
                isMultiple = false;
                break;
            }
            
            case R.id.MultiplePreviewBtnPlayback : {
                DJIDrone.getDjiCamera().enterMultiplePreviewMode(mExecuteCallback);
                onStatusChange(MULTIPLEPLAYBACK);
                isMultiple = true;
                break;
            }
            
            case R.id.MultiSelectPreviewBtnPlayback : {
                if (!isEdited) {
                    DJIDrone.getDjiCamera().enterMultipleEditMode(mExecuteCallback);
                    isEdited = true;
                } else {
                    DJIDrone.getDjiCamera().exitMultipleEditMode(mExecuteCallback);
                    isEdited = false;
                }
                isMultiple = true;
                break;
            }
            
            case R.id.DeleteBtnPlayback : {
                if (mCameraPlaybackState.playbackMode._equals(CameraPlaybackMode.Multiple_Media_Files_Display.value())) {
                    DJIDrone.getDjiCamera().deleteAllSelectedFiles(mExecuteCallback);
                    isEdited = false;
                } else {
                    DJIDrone.getDjiCamera().deleteCurrentPreviewFile(mExecuteCallback);
                }
                break;
            }
            
            case R.id.DownloadBtnPlayback : {
                File destDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Dji_Sdk_Test/");
                if (!destDir.exists()) {
                    destDir.mkdirs();
                }
                if (!mCameraPlaybackState.playbackMode._equals(CameraPlaybackMode.Multiple_Media_Files_Display.value()))
                    DJIDrone.getDjiCamera().downloadCurrentPreviewFile(destDir, mFileDownloadCallBack);
                else
                    DJIDrone.getDjiCamera().downloadAllSelectedFiles(destDir, mFileDownloadCallBack);
                break;
            }
            
            case R.id.PlayVideoBtnPlayback : {
                DJIDrone.getDjiCamera().startVideoPlayback(mExecuteCallback);
                
                break;
            }
            
            case R.id.PauseVideoBtnPlayback : {
                DJIDrone.getDjiCamera().pauseVideoPlayback(mExecuteCallback);
                
                break;
            }
        }
    }
    
    private void setResultToToast(String result){
        Toast.makeText(PlaybackProtocolActivity.this, result, Toast.LENGTH_SHORT).show();
    }
    
    private void onResultToast(String result) {
        String res = "The result is " + result;
        handler.sendMessage(handler.obtainMessage(SHOWTOAST, res));
    }
    
    private void onStatusChange(final int status) {
        PlaybackProtocolActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                switch (status) {
                    case CAPUTRE : {
                        mStartTakePhotoBtn.setVisibility(VISIBLE);
                        mStartRecordBtn.setVisibility(GONE);
                        mStopRecordBtn.setVisibility(GONE);
                        mMultiplePreviewModeBtn.setVisibility(GONE);
                        mMultiSelectPreviewModeBtn.setVisibility(GONE);
                        mDownloadBtn.setVisibility(GONE);
                        mDeleteBtn.setVisibility(GONE);
                        mGridView.setVisibility(GONE);
                        mPlayVideoBtn.setVisibility(GONE);
                        mPauseVideoBtn.setVisibility(GONE);
                        break;
                    }
                    
                    case RECORD : {
                        mStartTakePhotoBtn.setVisibility(GONE);
                        mStartRecordBtn.setVisibility(VISIBLE);
                        mStopRecordBtn.setVisibility(VISIBLE);
                        mMultiplePreviewModeBtn.setVisibility(GONE);
                        mMultiSelectPreviewModeBtn.setVisibility(GONE);
                        mDownloadBtn.setVisibility(GONE);
                        mDeleteBtn.setVisibility(GONE);
                        mGridView.setVisibility(GONE);
                        mPlayVideoBtn.setVisibility(GONE);
                        mPauseVideoBtn.setVisibility(GONE);
                        break;
                    }
                    
                    case PLAYBACK : {
                        mStartTakePhotoBtn.setVisibility(GONE);
                        mStartRecordBtn.setVisibility(GONE);
                        mStopRecordBtn.setVisibility(GONE);
                        mMultiplePreviewModeBtn.setVisibility(VISIBLE);
                        mMultiSelectPreviewModeBtn.setVisibility(GONE);
                        mDownloadBtn.setVisibility(VISIBLE);
                        mDeleteBtn.setVisibility(VISIBLE);
                        mGridView.setVisibility(GONE);
                        break;
                    }
                    
                    case MULTIPLEPLAYBACK : {
                        mStartTakePhotoBtn.setVisibility(GONE);
                        mStartRecordBtn.setVisibility(GONE);
                        mStopRecordBtn.setVisibility(GONE);
                        mMultiplePreviewModeBtn.setVisibility(GONE);
                        mMultiSelectPreviewModeBtn.setVisibility(VISIBLE);
                        mDownloadBtn.setVisibility(VISIBLE);
                        mDeleteBtn.setVisibility(VISIBLE);
                        mGridView.setVisibility(VISIBLE);
                    }
                }
            }
        });
    }
        
    private void onPressStatusBtn(final int status) {
        DJIDrone.getDjiCamera().setCameraMode(CameraMode.find(status), new DJIExecuteResultCallback() {
            @Override
            public void onResult(DJIError mErr) {
                if (DJIError.RESULT_OK == mErr.errorCode) {
                    onStatusChange(status);
                }
                onResultToast(mErr.errorDescription);
            }
        });
    }
    
    class ButtonAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        
        public ButtonAdapter (Context mContext) {
            super();
            inflater = LayoutInflater.from(mContext);
        }
        
        @Override
        public int getCount()
        {
            return 8;
        }

        @Override
        public Object getItem(int position)
        {
            return position;
        }
	
        @Override
        public long getItemId(int position)
        {
            return position;
        }
	
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            Button mBtn;
            final int p = position;
            if (null == convertView) {
                convertView = inflater.inflate(R.layout.button_gridview_item, null);
                mBtn = (Button)convertView.findViewById(R.id.TransparencyButton);
                
                mBtn.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v)
                    {
                        if (!isEdited) {
                            DJIDrone.getDjiCamera().enterSinglePreviewModeWithIndex(p, new DJIExecuteResultCallback() {

                                @Override
                                public void onResult(DJIError mErr)
                                {
                                    // TODO Auto-generated method stub
                                    if (mErr.errorCode == DJIError.RESULT_OK) {
                                        onStatusChange(PLAYBACK);
                                    }
                                }
                            });
                            isMultiple = false;
                            
                        } else {
                            DJIDrone.getDjiCamera().selectFileAtIndex(p, mExecuteCallback);
                        }
                    }
                    
                });
                convertView.setTag(mBtn);
            } else {
                mBtn = (Button)convertView.getTag();
            }
            return convertView;
        }
        
    }
    
    private void ShowDownloadProgressDialog() {
        if(mProgressDialog != null){
            mProgressDialog.show();
        }
    }

    private void HideDownloadProgressDialog() {
        if (null != mProgressDialog && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
    
    private void CreateProgressDialog() {
        mProgressDialog = new ProgressDialog(PlaybackProtocolActivity.this);
        mProgressDialog.setTitle(R.string.demo_progress_dialog_title);
        mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);        
    }
    
    private void isVideoPreview() {
        if (mCameraPlaybackState.mediaFileType.value() == CameraMediaFileType.Media_File_VIDEO.value()) {
            if (mCameraPlaybackState.videoPlayProgress == 0) {
                mPlayVideoBtn.setVisibility(VISIBLE);
                mPauseVideoBtn.setVisibility(GONE);
            } else {
                mPlayVideoBtn.setVisibility(GONE);
                mPauseVideoBtn.setVisibility(VISIBLE);
            }
        } else {
            mPlayVideoBtn.setVisibility(GONE);
            mPauseVideoBtn.setVisibility(GONE);
        }
    }
}
