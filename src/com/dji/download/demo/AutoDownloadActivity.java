/**   
 * TODO
 * @Title       : AutoDownloadActivity.java 
 * @Package     : com.dji.download.demo 
 * @author 	    : DJI Software
 * @date        : 2015年8月6日 上午11:54:16 
 * @version     : 2.0.0
 */


package com.dji.download.demo;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import com.dji.download.demo.PlaybackProtocolActivity.Task;
import com.dji.mediadownloaddemo.R;

import dji.log.DJILogHelper;
import dji.midware.data.manager.P3.ServiceManager;
import dji.publics.DJIUI.DJIGLSurfaceView;
import dji.sdk.api.DJIDrone;
import dji.sdk.api.DJIError;
import dji.sdk.api.Camera.DJICameraSettingsTypeDef.CameraMode;
import dji.sdk.api.DJIDroneTypeDef.DJIDroneType;
import dji.sdk.interfaces.DJICameraModeCallBack;
import dji.sdk.interfaces.DJIExecuteResultCallback;
import dji.sdk.interfaces.DJIFileDownloadCallBack;
import dji.sdk.interfaces.DJIGerneralListener;
import dji.sdk.interfaces.DJIReceivedVideoDataCallBack;
import dji.sdk.widget.DjiGLSurfaceView;
import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/** 
 * <h3>TODO</h3>
 * @author      : DJI Software
 * @date        : 2015年8月6日 上午11:54:16 
 * @version     : V1.0
 */

public class AutoDownloadActivity extends Activity implements OnClickListener
{
    private final static String TAG = "AutoDownloadActivity";
    
    private final static int SHOWDIALOG = 0;
    private final static int SHOWTOAST = 1;
    private final static int ENTERMULTIPLEPLAYBACK = 2;
    private final static int ENTERMULTIPLEEDIT = 3;
    private final static int SELECTFIRSTFILE = 4;
    private final static int DOWNLOADIT = 5;
    private final static int SHOWDOWNLOADDIALOG = 6;
    private final static int CLOSEDOWNLOADDIALOG = 7;
    
    private Timer mTimer;
    
    private Button mAutoDownloadBtn;
    
    private TextView mConnectStatusTextView;
    
    private ProgressDialog mProgressDialog;
    
    private DjiGLSurfaceView mDjiGLSurfaceView;
    
    private DJIReceivedVideoDataCallBack mReceivedVideoDataCallBack;
    private DJIFileDownloadCallBack mFileDownloadCallBack;
    
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
                
                case ENTERMULTIPLEPLAYBACK : {
                    DJIDrone.getDjiCamera().enterMultiplePreviewMode(new DJIExecuteResultCallback() {
                        
                        @Override
                        public void onResult(DJIError mErr)
                        {
                            // TODO Auto-generated method stub
                            if (mErr.errorCode == DJIError.RESULT_OK) {
                                handler.sendEmptyMessageDelayed(ENTERMULTIPLEEDIT, 2000);
                            }
                        }
                    });
                    break;
                }
                
                case ENTERMULTIPLEEDIT : {
                    DJIDrone.getDjiCamera().enterMultipleEditMode(new DJIExecuteResultCallback() {
                        
                        @Override
                        public void onResult(DJIError mErr)
                        {
                            // TODO Auto-generated method stub
                            if (mErr.errorCode == DJIError.RESULT_OK) {
                                handler.sendEmptyMessageDelayed(SELECTFIRSTFILE, 2000);
                            }
                        }
                    });
                    break;
                }
                
                case SELECTFIRSTFILE : {
                    DJIDrone.getDjiCamera().selectFileAtIndex(0, new DJIExecuteResultCallback() {
                        
                        @Override
                        public void onResult(DJIError mErr)
                        {
                            // TODO Auto-generated method stub
                            if (mErr.errorCode == DJIError.RESULT_OK) {
                                handler.sendEmptyMessageDelayed(DOWNLOADIT, 2000);
                            }
                        }
                    });
                    break;
                }
                
                case DOWNLOADIT : {
                    File destDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Dji_Sdk_Test/");
                    if (!destDir.exists()) {
                        destDir.mkdirs();
                    }
                    DJIDrone.getDjiCamera().downloadAllSelectedFiles(destDir, mFileDownloadCallBack);
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
    

    private void checkConnectState() {
        AutoDownloadActivity.this.runOnUiThread(new Runnable() {

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
    
    
    class Task extends TimerTask {
        @Override
        public void run() {
            checkConnectState();
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_download_protocol);
        
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
        
        mAutoDownloadBtn = (Button)findViewById(R.id.AutoDownloadBtn);
        mConnectStatusTextView = (TextView)findViewById(R.id.ConnectStateItTextView);
        
        mAutoDownloadBtn.setOnClickListener(this);
        
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
                
                DJILogHelper.getInstance().LOGD("", "download OnEnd",true,false);
                
                DJIDrone.getDjiCamera().finishDownloadAllSelectedFiles(new DJIExecuteResultCallback() {
                    
                    @Override
                    public void onResult(DJIError mErr)
                    {
                        // TODO Auto-generated method stub
                        DJILogHelper.getInstance().LOGD("", "download finishDownloadAllSelectedFiles:"+mErr.errorDescription,true,false);
                    }
                });
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
        
        CreateProgressDialog();
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
    
    private void setResultToToast(String result){
        Toast.makeText(AutoDownloadActivity.this, result, Toast.LENGTH_SHORT).show();
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

    /** 
     * @param v 
     * @see android.view.View.OnClickListener#onClick(android.view.View) 
     */ 	
    @Override
    public void onClick(View v)
    {
        // TODO Auto-generated method stub
        DJIDrone.getDjiCamera().setCameraMode(CameraMode.Camera_PlayBack_Mode, new DJIExecuteResultCallback() {
            
            @Override
            public void onResult(DJIError mErr)
            {
                // TODO Auto-generated method stub
                if(mErr.errorCode == DJIError.RESULT_OK) {
                    handler.sendEmptyMessageDelayed(ENTERMULTIPLEPLAYBACK, 2000);
                }
            }
        });
    }
    
    public void onReturn(View view){
        Log.d(TAG ,"onReturn");  
        this.finish();
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
    
    @Override
    protected void onDestroy() {
        onUnInitSDK();
        super.onDestroy();
    }
    
    private void onUnInitSDK(){
        DJIDrone.disconnectToDrone();
    }
    
    private void CreateProgressDialog() {
        mProgressDialog = new ProgressDialog(AutoDownloadActivity.this);
        mProgressDialog.setTitle(R.string.demo_progress_dialog_title);
        mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);        
    }
}
