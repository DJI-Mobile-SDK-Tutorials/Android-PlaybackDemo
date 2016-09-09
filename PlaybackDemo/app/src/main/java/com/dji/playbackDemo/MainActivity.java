package com.dji.playbackDemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.SurfaceTexture;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.io.File;
import dji.common.camera.CameraSystemState;
import dji.common.camera.DJICameraSettingsDef;
import dji.common.product.Model;
import dji.common.util.DJICommonCallbacks;
import dji.sdk.camera.DJICamera;
import dji.sdk.camera.DJIPlaybackManager;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.base.DJIBaseProduct;
import dji.common.error.DJIError;

public class MainActivity extends Activity implements TextureView.SurfaceTextureListener,View.OnClickListener {

    private static final String TAG = MainActivity.class.getName();

    protected DJICamera.CameraReceivedVideoDataCallback mReceivedVideoDataCallBack = null;

    // Codec for video live view
    protected DJICodecManager mCodecManager = null;

    protected TextureView mVideoSurface = null;
    private Button mCaptureBtn, mShootPhotoModeBtn, mRecordVideoModeBtn, mPlaybackModeBtn;
    private Button mSingleBtn, mMultiPreBtn, mSelectBtn, mSelectAllBtn, mDeleteBtn, mDownloadBtn;
    private Button mStopVideoBtn, mPreviousBtn, mNextBtn, mPlayVideoBtn;
    private Button mPreviewBtn1, mPreviewBtn2, mPreviewBtn3, mPreviewBtn4, mPreviewBtn5, mPreviewBtn6, mPreviewBtn7, mPreviewBtn8;
    private ToggleButton mRecordBtn;
    private TextView recordingTime;
    private final int SHOWTOAST = 1;
    private final int SHOW_DOWNLOAD_PROGRESS_DIALOG = 2;
    private final int HIDE_DOWNLOAD_PROGRESS_DIALOG = 3;

    private boolean isSinglePreview = true;
    private DJIPlaybackManager.DJICameraPlaybackState playbackState;
    private DJICamera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initUI();

        // The callback for receiving the raw H264 video data for camera live view
        mReceivedVideoDataCallBack = new DJICamera.CameraReceivedVideoDataCallback() {

            @Override
            public void onResult(byte[] videoBuffer, int size) {
                if(mCodecManager != null){
                    // Send the raw H264 video data to codec manager for decoding
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                }else {
                    Log.e(TAG, "mCodecManager is null");
                }
            }
        };

    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
        initPreviewer();
        initCameraCallBacks();
        if(mVideoSurface == null) {
            Log.e(TAG, "mVideoSurface is null");
        }
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        uninitPreviewer();
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    public void onReturn(View view){
        Log.e(TAG, "onReturn");
        this.finish();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        uninitPreviewer();
        super.onDestroy();
    }

    private void initUI() {
        // init mVideoSurface
        mVideoSurface = (TextureView)findViewById(R.id.video_previewer_surface);

        recordingTime = (TextView) findViewById(R.id.timer);
        mCaptureBtn = (Button) findViewById(R.id.btn_capture);
        mRecordBtn = (ToggleButton) findViewById(R.id.btn_record);
        mShootPhotoModeBtn = (Button) findViewById(R.id.btn_shoot_photo_mode);
        mRecordVideoModeBtn = (Button) findViewById(R.id.btn_record_video_mode);
        mPlaybackModeBtn = (Button) findViewById(R.id.btn_playback_mode);

        mSingleBtn = (Button) findViewById(R.id.btn_single_btn);
        mMultiPreBtn = (Button) findViewById(R.id.btn_multi_pre_btn);
        mSelectBtn = (Button) findViewById(R.id.btn_select_btn);
        mSelectAllBtn = (Button) findViewById(R.id.btn_select_all_btn);
        mDeleteBtn = (Button) findViewById(R.id.btn_delete_btn);
        mDownloadBtn = (Button) findViewById(R.id.btn_download_btn);
        mPreviousBtn = (Button) findViewById(R.id.btn_previous_btn);
        mNextBtn = (Button) findViewById(R.id.btn_next_btn);
        mPlayVideoBtn = (Button) findViewById(R.id.btn_playVideo_btn);
        mStopVideoBtn = (Button) findViewById(R.id.btn_stop_btn);

        mPreviewBtn1 = (Button) findViewById(R.id.preview_button1);
        mPreviewBtn2 = (Button) findViewById(R.id.preview_button2);
        mPreviewBtn3 = (Button) findViewById(R.id.preview_button3);
        mPreviewBtn4 = (Button) findViewById(R.id.preview_button4);
        mPreviewBtn5 = (Button) findViewById(R.id.preview_button5);
        mPreviewBtn6 = (Button) findViewById(R.id.preview_button6);
        mPreviewBtn7 = (Button) findViewById(R.id.preview_button7);
        mPreviewBtn8 = (Button) findViewById(R.id.preview_button8);

        if (null != mVideoSurface) {
            mVideoSurface.setSurfaceTextureListener(this);
        }

        mCaptureBtn.setOnClickListener(this);
        mRecordBtn.setOnClickListener(this);
        mShootPhotoModeBtn.setOnClickListener(this);
        mRecordVideoModeBtn.setOnClickListener(this);
        mPlaybackModeBtn.setOnClickListener(this);

        mSingleBtn.setOnClickListener(this);
        mMultiPreBtn.setOnClickListener(this);
        mSelectBtn.setOnClickListener(this);
        mSelectAllBtn.setOnClickListener(this);
        mDeleteBtn.setOnClickListener(this);
        mDownloadBtn.setOnClickListener(this);
        mPreviousBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mPlayVideoBtn.setOnClickListener(this);
        mStopVideoBtn.setOnClickListener(this);

        mPreviewBtn1.setOnClickListener(this);
        mPreviewBtn2.setOnClickListener(this);
        mPreviewBtn3.setOnClickListener(this);
        mPreviewBtn4.setOnClickListener(this);
        mPreviewBtn5.setOnClickListener(this);
        mPreviewBtn6.setOnClickListener(this);
        mPreviewBtn7.setOnClickListener(this);
        mPreviewBtn8.setOnClickListener(this);

        recordingTime.setVisibility(View.INVISIBLE);
        mPlayVideoBtn.setVisibility(View.INVISIBLE);
        mStopVideoBtn.setVisibility(View.INVISIBLE);

        mRecordBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startRecord();
                } else {
                    stopRecord();
                }
            }
        });

        createProgressDialog();
    }

    protected void initCameraCallBacks() {
        if (PlaybackDemoApplication.isPlaybackAvailable()){

            if (mCamera != null) {
                mCamera.setDJICameraUpdatedSystemStateCallback(new DJICamera.CameraUpdatedSystemStateCallback() {
                    @Override
                    public void onResult(CameraSystemState cameraSystemState) {
                        if (null != cameraSystemState) {

                            int recordTime = cameraSystemState.getCurrentVideoRecordingTimeInSeconds();
                            int minutes = (recordTime % 3600) / 60;
                            int seconds = recordTime % 60;

                            final String timeString = String.format("%02d:%02d", minutes, seconds);
                            final boolean isVideoRecording = cameraSystemState.isRecording();

                            final CameraSystemState cameraState = cameraSystemState;
                            MainActivity.this.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {

                                    recordingTime.setText(timeString);

                                /*
                                 * Update recordingTime TextView visibility and mRecordBtn's check state
                                 */
                                    if (isVideoRecording) {
                                        recordingTime.setVisibility(View.VISIBLE);
                                    } else {
                                        recordingTime.setVisibility(View.INVISIBLE);
                                    }

                                    if(cameraState.getCameraMode() != DJICameraSettingsDef.CameraMode.Playback){
                                        mPlayVideoBtn.setVisibility(View.INVISIBLE);
                                        mStopVideoBtn.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });

                        }
                    }
                });

                mCamera.getPlayback().setDJICameraPlayBackStateCallBack(
                        new DJIPlaybackManager.DJICameraPlayBackStateCallBack() {
                            @Override
                            public void onResult(DJIPlaybackManager.DJICameraPlaybackState
                                                         djiCameraPlaybackState) {

                                if (djiCameraPlaybackState.playbackMode.equals(DJICameraSettingsDef.
                                        CameraPlaybackMode.MultipleMediaFilesDisplay) ||
                                        djiCameraPlaybackState.playbackMode.equals(DJICameraSettingsDef.
                                                CameraPlaybackMode.MediaFilesDownload) ||
                                        djiCameraPlaybackState.playbackMode.equals(DJICameraSettingsDef.
                                                CameraPlaybackMode.MultipleMediaFilesDelete)) {
                                    isSinglePreview = false;
                                } else {
                                    isSinglePreview = true;
                                }

                                playbackState = djiCameraPlaybackState;

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        if (playbackState.playbackMode.equals(DJICameraSettingsDef.CameraPlaybackMode.SinglePhotoPlayback)) {

                                            if (playbackState.mediaFileType.equals(DJICameraSettingsDef.CameraMediaFileType.VIDEO)){
                                                mPlayVideoBtn.setVisibility(View.VISIBLE);
                                                mStopVideoBtn.setVisibility(View.INVISIBLE);

                                            }else if(playbackState.mediaFileType.equals(DJICameraSettingsDef.CameraMediaFileType.DNG) || playbackState.mediaFileType.equals(DJICameraSettingsDef.CameraMediaFileType.JPEG))
                                            {
                                                mPlayVideoBtn.setVisibility(View.INVISIBLE);
                                                mStopVideoBtn.setVisibility(View.INVISIBLE);
                                            }
                                        }else if(playbackState.playbackMode.equals(DJICameraSettingsDef.CameraPlaybackMode.SingleVideoPlaybackStart)){
                                            mPlayVideoBtn.setVisibility(View.INVISIBLE);
                                            mStopVideoBtn.setVisibility(View.VISIBLE);
                                        }else if(playbackState.playbackMode.equals(DJICameraSettingsDef.CameraPlaybackMode.MultipleMediaFilesDisplay)){
                                            mPlayVideoBtn.setVisibility(View.INVISIBLE);
                                            mStopVideoBtn.setVisibility(View.INVISIBLE);
                                        }else if(playbackState.playbackMode.equals(DJICameraSettingsDef.CameraPlaybackMode.MultipleMediaFilesDelete)){
                                            mPlayVideoBtn.setVisibility(View.INVISIBLE);
                                            mStopVideoBtn.setVisibility(View.INVISIBLE);
                                        }

                                        if (playbackState.playbackMode.equals(DJICameraSettingsDef.CameraPlaybackMode.MultipleMediaFilesDisplay)){
                                            mSelectBtn.setText("Select");
                                        }else if(playbackState.playbackMode.equals(DJICameraSettingsDef.CameraPlaybackMode.MultipleMediaFilesDelete)){
                                            mSelectBtn.setText("Cancel");
                                        }
                                    }
                                });

                            }
                        }
                );
            }
        }

    }

    private void initPreviewer() {

        DJIBaseProduct product = PlaybackDemoApplication.getProductInstance();

        if (product == null || !product.isConnected()) {
            showToast(getString(R.string.disconnected));
        } else {
            if (null != mVideoSurface) {
                mVideoSurface.setSurfaceTextureListener(this);
            }

            if (!product.getModel().equals(Model.UnknownAircraft)) {

                mCamera = product.getCamera();
                if (mCamera != null){
                    // Set the callback
                    mCamera.setDJICameraReceivedVideoDataCallback(mReceivedVideoDataCallBack);
                }
            }
        }
    }

    private void uninitPreviewer() {

        if (PlaybackDemoApplication.isCameraModuleAvailable()){
            if (mCamera != null){
                // Reset the callback
                mCamera.setDJICameraReceivedVideoDataCallback(null);
            }
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureAvailable");
        if (mCodecManager == null) {
            mCodecManager = new DJICodecManager(this, surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.e(TAG,"onSurfaceTextureDestroyed");
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }

        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private ProgressDialog mDownloadDialog;

    private void createProgressDialog() {

        mDownloadDialog = new ProgressDialog(MainActivity.this);
        mDownloadDialog.setTitle("Downloading File");
        mDownloadDialog.setIcon(android.R.drawable.ic_dialog_info);
        mDownloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDownloadDialog.setCanceledOnTouchOutside(false);
        mDownloadDialog.setCancelable(false);
    }

    private void ShowDownloadProgressDialog() {
        if (mDownloadDialog != null) {
            mDownloadDialog.show();
        }
    }

    private void HideDownloadProgressDialog() {
        if (null != mDownloadDialog && mDownloadDialog.isShowing()) {
            mDownloadDialog.dismiss();
        }
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SHOWTOAST:
                    showToast((String) msg.obj);
                    break;
                case SHOW_DOWNLOAD_PROGRESS_DIALOG:
                    ShowDownloadProgressDialog();
                    break;
                case HIDE_DOWNLOAD_PROGRESS_DIALOG:
                    HideDownloadProgressDialog();
                    break;
                default:
                    break;
            }
            return false;
        }
    });


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_playVideo_btn:{
                mCamera.getPlayback().startVideoPlayback();
                break;
            }
            case R.id.btn_capture:{
                captureAction();
                break;
            }
            case R.id.btn_shoot_photo_mode:{
                switchCameraMode(DJICameraSettingsDef.CameraMode.ShootPhoto);
                break;
            }
            case R.id.btn_record_video_mode:{
                switchCameraMode(DJICameraSettingsDef.CameraMode.RecordVideo);
                break;
            }
            case R.id.btn_playback_mode:{
                switchCameraMode(DJICameraSettingsDef.CameraMode.Playback);
                break;
            }
            case R.id.btn_single_btn:{
                if (!isSinglePreview)
                    mCamera.getPlayback().enterSinglePreviewModeWithIndex(0);
                break;
            }
            case R.id.btn_multi_pre_btn:{
                if (isSinglePreview)
                    mCamera.getPlayback().enterMultiplePreviewMode();
                break;
            }
            case R.id.btn_stop_btn:{
                mCamera.getPlayback().stopVideoPlayback();
                break;
            }
            case R.id.btn_select_btn:{

                if (playbackState == null){
                    break;
                }
                if (playbackState.playbackMode.equals(DJICameraSettingsDef.
                        CameraPlaybackMode.MultipleMediaFilesDisplay)){

                    mCamera.getPlayback().enterMultipleEditMode();

                }else if (playbackState.playbackMode.equals(DJICameraSettingsDef.
                        CameraPlaybackMode.MultipleMediaFilesDelete)) {
                    mCamera.getPlayback().exitMultipleEditMode();
                }

                break;
            }
            case R.id.btn_select_all_btn:{
                if (playbackState == null){
                    break;
                }
                if (playbackState.isAllFilesInPageSelected){
                    mCamera.getPlayback().unselectAllFilesInPage();
                }else{
                    mCamera.getPlayback().selectAllFilesInPage();
                }
                break;
            }
            case R.id.btn_delete_btn:{

                if (playbackState == null){
                    break;
                }
                if (playbackState.playbackMode.equals(DJICameraSettingsDef.CameraPlaybackMode.MultipleMediaFilesDelete)) {
                    mCamera.getPlayback().deleteAllSelectedFiles();

                }else if(playbackState.playbackMode.equals(DJICameraSettingsDef.CameraPlaybackMode.SinglePhotoPlayback)){
                    mCamera.getPlayback().deleteCurrentPreviewFile();
                }
                break;
            }
            case R.id.btn_download_btn:{

                if (playbackState == null){
                    break;
                }

                File destDir =
                        new File(Environment.getExternalStorageDirectory().getPath() + "/DJI_PlaybackDemo/");
                if(playbackState.playbackMode.equals(DJICameraSettingsDef.CameraPlaybackMode.SinglePhotoPlayback)) {
                    mCamera.getPlayback().downloadCurrentPreviewFile(destDir, new DJIPlaybackManager.CameraFileDownloadCallback() {
                        @Override
                        public void onStart() {
                            handler.sendMessage(handler.obtainMessage(SHOW_DOWNLOAD_PROGRESS_DIALOG, null));
                            if (mDownloadDialog != null) {
                                mDownloadDialog.setProgress(0);
                            }
                            handler.sendMessage(handler.obtainMessage(SHOWTOAST, "download OnStart"));
                        }

                        @Override
                        public void onEnd() {
                            handler.sendMessage(handler.obtainMessage(HIDE_DOWNLOAD_PROGRESS_DIALOG, null));
                            handler.sendMessage(handler.obtainMessage(SHOWTOAST, "download OnEnd"));
                        }

                        @Override
                        public void onError(Exception exception) {
                            handler.sendMessage(handler.obtainMessage(HIDE_DOWNLOAD_PROGRESS_DIALOG, null));
                            handler.sendMessage(handler.obtainMessage(SHOWTOAST,
                                    "download Current Preview File OnError :" + exception.toString()));
                        }

                        @Override
                        public void onProgressUpdate(int progress) {
                            if (mDownloadDialog != null) {
                                mDownloadDialog.setProgress(progress);
                            }
                        }
                    });

                }else if (playbackState.playbackMode.equals(DJICameraSettingsDef.CameraPlaybackMode.MultipleMediaFilesDelete)) {
                    mCamera.getPlayback().downloadSelectedFiles(destDir, new DJIPlaybackManager.CameraFileDownloadCallback() {

                        @Override
                        public void onStart() {
                            handler.sendMessage(handler.obtainMessage(SHOW_DOWNLOAD_PROGRESS_DIALOG, null));
                            if (mDownloadDialog != null) {
                                mDownloadDialog.setProgress(0);
                            }
                            handler.sendMessage(handler.obtainMessage(SHOWTOAST, "download OnStart"));
                        }

                        @Override
                        public void onEnd() {
                            handler.sendMessage(handler.obtainMessage(HIDE_DOWNLOAD_PROGRESS_DIALOG, null));
                            handler.sendMessage(handler.obtainMessage(SHOWTOAST, "download OnEnd"));
                        }

                        @Override
                        public void onError(Exception exception) {
                            handler.sendMessage(handler.obtainMessage(HIDE_DOWNLOAD_PROGRESS_DIALOG, null));
                            handler.sendMessage(handler.obtainMessage(SHOWTOAST,
                                    "download selected files OnError :" + exception.toString()));
                        }

                        @Override
                        public void onProgressUpdate(int progress) {
                            if (mDownloadDialog != null) {
                                mDownloadDialog.setProgress(progress);
                            }
                        }
                    });
                }

                break;
            }
            case R.id.btn_previous_btn:{
                if (isSinglePreview){
                    mCamera.getPlayback().singlePreviewPreviousPage();
                }
                else{
                    mCamera.getPlayback().multiplePreviewPreviousPage();
                }
                break;
            }
            case R.id.btn_next_btn:{
                if (isSinglePreview) {
                    mCamera.getPlayback().singlePreviewNextPage();
                }
                else {
                    mCamera.getPlayback().multiplePreviewNextPage();
                }
                break;
            }
            case R.id.preview_button1:{
                previewBtnAction(0);
                break;
            }
            case R.id.preview_button2:{
                previewBtnAction(1);
                break;
            }
            case R.id.preview_button3:{
                previewBtnAction(2);
                break;
            }
            case R.id.preview_button4:{
                previewBtnAction(3);
                break;
            }case R.id.preview_button5:{
                previewBtnAction(4);
                break;
            }
            case R.id.preview_button6:{
                previewBtnAction(5);
                break;
            }
            case R.id.preview_button7:{
                previewBtnAction(6);
                break;
            }
            case R.id.preview_button8:{
                previewBtnAction(7);
                break;
            }
            default:
                break;
        }
    }

    private void previewBtnAction(int var){
        if ((playbackState != null) && (mCamera != null)){
            if (playbackState.playbackMode.equals(DJICameraSettingsDef.CameraPlaybackMode.MultipleMediaFilesDelete)){
                mCamera.getPlayback().toggleFileSelectionAtIndex(var);
            }else if(playbackState.playbackMode.equals(DJICameraSettingsDef.CameraPlaybackMode.MultipleMediaFilesDisplay)){
                mCamera.getPlayback().enterSinglePreviewModeWithIndex(var);
            }
        }
    }

    private void switchCameraMode(DJICameraSettingsDef.CameraMode cameraMode){

        if (mCamera != null) {
            mCamera.setCameraMode(cameraMode, new DJICommonCallbacks.DJICompletionCallback() {
                @Override
                public void onResult(DJIError error) {

                    if (error == null) {
                        showToast("Switch Camera Mode Succeeded");
                    } else {
                        showToast(error.getDescription());
                    }
                }
            });
        }
    }

    // Method for taking photo
    private void captureAction(){

        DJICameraSettingsDef.CameraMode cameraMode = DJICameraSettingsDef.CameraMode.ShootPhoto;

        if (mCamera != null) {

            DJICameraSettingsDef.CameraShootPhotoMode photoMode = DJICameraSettingsDef.CameraShootPhotoMode.Single; // Set the camera capture mode as Single mode

            mCamera.startShootPhoto(photoMode, new DJICommonCallbacks.DJICompletionCallback() {
                @Override
                public void onResult(DJIError error) {
                    if (error == null) {
                        showToast("take photo: success");
                    } else {
                        showToast(error.getDescription());
                    }
                }
            });  // Execute the startShootPhoto API
        }
    }

    // Method for starting recording
    private void startRecord(){

        DJICameraSettingsDef.CameraMode cameraMode = DJICameraSettingsDef.CameraMode.RecordVideo;
        if (mCamera != null) {
            mCamera.startRecordVideo(new DJICommonCallbacks.DJICompletionCallback() {
                @Override
                public void onResult(DJIError error)
                {
                    if (error == null) {
                        showToast("Record video: success");
                    }else {
                        showToast(error.getDescription());
                    }
                }
            }); // Execute the startRecordVideo API
        }
    }

    // Method for stopping recording
    private void stopRecord(){

        if (mCamera != null) {
            mCamera.stopRecordVideo(new DJICommonCallbacks.DJICompletionCallback() {
                @Override
                public void onResult(DJIError error)
                {
                    if(error == null) {
                        showToast("Stop recording: success");
                    }else {
                        showToast(error.getDescription());
                    }
                }
            }); // Execute the stopRecordVideo API

        }
    }
}
