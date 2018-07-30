package com.liuh.camerauselearn;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 如果想要是拍照得到的图片可以让手机中所有的app使用，图片文件可以保存在这些位置
 * getExternalStoragePublicDirectory(), with the DIRECTORY_PICTURES argument.
 * <p>
 * 如果只想让我们的应用可以使用它，图片存储位置为
 * getExternalFilesDir()、getFilesDir()
 * If you saved your photo to the directory provided by getExternalFilesDir(), the media scanner cannot access the files because they are private to your app
 * Files you save in the directories provided by getExternalFilesDir() or getFilesDir() are deleted when the user uninstalls your app.
 */
public class MainActivity extends AppCompatActivity {

    Camera camera;
    SurfaceView surfaceView;

    @BindView(R.id.iv_takepic)
    ImageView ivTakePic;
    @BindView(R.id.vv_video)
    VideoView vvVideo;

    static final int REQUEST_IMAGE_CAPTURE = 1;//拿到拍照的thumbnail图片
    static final int REQUEST_TAKE_PHOTO = 2;//拿到拍照的图片（高质量的）
    static final int REQUEST_VIDEO_CAPTURE = 3;//录视频

    private static final int REQUEST_CODE_PERMISSION = 4;

    String mCurrentPhotoPath;

    static final String[] PERMISSIONS_REQUEST_ALL = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    List<String> permissions_request_denied = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < PERMISSIONS_REQUEST_ALL.length; i++) {
                if (ContextCompat.checkSelfPermission(this, PERMISSIONS_REQUEST_ALL[i]) != PackageManager.PERMISSION_GRANTED) {
                    //如果未授权
                    permissions_request_denied.add(PERMISSIONS_REQUEST_ALL[i]);
                }
            }

            if (permissions_request_denied.size() > 0) {
                for (int i = 0; i < permissions_request_denied.size(); i++) {
                    ActivityCompat.requestPermissions(this,
                            permissions_request_denied.toArray(new String[permissions_request_denied.size()]),
                            REQUEST_CODE_PERMISSION);
                }
            }
        }
    }

    @OnClick({R.id.btn_takepic, R.id.btn_takepic_high_quality, R.id.btn_record_video, R.id.btn_camera_control})
    void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_takepic:
                //拍照(invokes an intent to capture a photo)
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //健壮性检查
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
                break;
            case R.id.btn_takepic_high_quality:
                dispatchTakePictureIntent();
                break;
            case R.id.btn_record_video:
                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
                }
                break;
            case R.id.btn_camera_control:
//                Intent intent = new Intent(this, CameraControlActivity.class);
//                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //使用这种方式拿到的Bitmap是比较小的，质量比较差的
            // This thumbnail image from "data" might be good for an icon, but not a lot more.
            // Dealing with a full-sized image takes a bit more work.
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ivTakePic.setImageBitmap(imageBitmap);
        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Log.e("----------", "拍摄高清图");
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ivTakePic.setImageBitmap(BitmapFactory.decodeFile(mCurrentPhotoPath));
        } else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {

            Uri videoUri = data.getData();
            Log.e("----------", "录制视频---videoUri：" + videoUri.toString());
            vvVideo.setVideoURI(videoUri);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        File image = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );


        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = image.getAbsolutePath();
//        return image;
        return null;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        String filename = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.CHINA)
                .format(new Date()) + ".png";
        File file = new File(Environment.getExternalStorageDirectory(), filename);
        mCurrentPhotoPath = file.getAbsolutePath();

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
//            File photoFile = null;
//            try {
//                photoFile = createImageFile();
//            } catch (IOException ex) {
//                // Error occurred while creating the File
//            }
            // Continue only if the File was successfully created
            if (file != null) {
                //版本兼容
                Uri photoURI = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    photoURI = FileProvider.getUriForFile(this,
                            "com.liuh.camerauselearn.fileprovider",
                            file);
                } else {
                    photoURI = Uri.fromFile(file);
                }

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSION) {
            Log.e("-------", "permissions.length : " + permissions.length + "grantResults.length : " + grantResults.length);

            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    permissions_request_denied.remove(permissions[i]);
                }
            }

            if (permissions_request_denied.size() == 0) {
                Toast.makeText(this, "所有需要的权限均已授权", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "还有需要的权限未授权", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
