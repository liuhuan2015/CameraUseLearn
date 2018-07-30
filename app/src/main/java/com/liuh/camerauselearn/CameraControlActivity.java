package com.liuh.camerauselearn;

import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import butterknife.ButterKnife;

public class CameraControlActivity extends AppCompatActivity {

//    private Preview mPreview;

    private Camera mCamera;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_control);
        ButterKnife.bind(this);
    }


    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;

        try {
            releaseCameraAndPreview();
            mCamera = Camera.open(id);
            qOpened = (mCamera != null);
        } catch (Exception e) {
            Log.e("--------", "failed to open Camera(打开相机失败)");
            e.printStackTrace();
        }

        return qOpened;
    }

    private void releaseCameraAndPreview() {
//        mPreview.setCamera(null);
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
}
