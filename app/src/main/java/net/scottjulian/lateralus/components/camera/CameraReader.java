package net.scottjulian.lateralus.components.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

import net.scottjulian.lateralus.components.io.FileSystem;


public class CameraReader{

    private static final String LOG = "LateralusCamera";
    private static final String SAVE_PATH = Environment.getExternalStorageDirectory().toString() + "/tmp/";

    public static final String FRONT = "FRONT";
    public static final String BACK  = "BACK";

    private Context _ctx;
    private Camera  _cam;

    private Boolean _camOpen     = false;
    private Boolean _hasFrontCam = false;
    private Boolean _hasBackCam  = false;

    public CameraReader(Context ctx){
        Log.d(LOG, "New CameraReader");
        _ctx = ctx;
        _hasBackCam = (_ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA));
        _hasFrontCam = (_ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT));
    }

    public void takePicture(String whichCam){
        whichCam = (whichCam.equals(FRONT)) ? FRONT : BACK;
        Log.d(LOG, "Starting to take picture with: " + whichCam);
        if(openCamera(whichCam)){
            try {
                Log.d(LOG, "Starting preview.");
                _cam.startPreview();
                Log.d(LOG, "Starting to take picture");
                _cam.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes, Camera camera) {
                        Log.d(LOG, "Picture Taken!");
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 5;
                        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                        saveImage(image);
                        releaseCamera();
                    }
                });
                //releaseCamera();
            }
            catch(Exception e){
                Log.d(LOG, "Take picture failed!");
                e.printStackTrace();
                releaseCamera();
            }
        }
    }

    private Boolean openCamera(String whichCam){
        if(_hasFrontCam || _hasBackCam) {
            Log.d(LOG, "Trying to open camera");
            _cam = null;
            Camera.CameraInfo camInfo = new Camera.CameraInfo();

            int facing = Camera.CameraInfo.CAMERA_FACING_BACK;
            if((whichCam == FRONT && _hasFrontCam) || (!_hasBackCam && _hasFrontCam)){
                facing = Camera.CameraInfo.CAMERA_FACING_FRONT;
            }

            for(int camId = 0; camId < Camera.getNumberOfCameras(); camId++) {
                Camera.getCameraInfo(camId, camInfo);
                if(camInfo.facing == facing) {
                    try {
                        _cam = Camera.open(camId);
                        _camOpen = (_cam != null);
                        Log.d(LOG, "Camera Opened!");
                        break;
                    }
                    catch(RuntimeException e) {
                        Log.d(LOG, "Camera NOT Opened");
                        e.printStackTrace();
                    }
                }
            }
        }
        return _camOpen;
    }

    private void releaseCamera(){
        Log.d(LOG, "Attempting to release camera");
        if(_cam != null){
            _cam.release();
            _cam = null;
            _camOpen = false;
            Log.d(LOG, "Camera released");
        }
    }

    private void saveImage(Bitmap img){
        String fileName = String.valueOf(System.currentTimeMillis()) + ".jpg";
        Boolean saved = FileSystem.saveJpg(img, SAVE_PATH, fileName);
        if(saved){
            //TODO email then delete?
        }
        else{
            //TODO pic wasnt saved, fire off msg?
        }
    }

}