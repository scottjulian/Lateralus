package xyz.lateralus.components.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;


public class PhotoTaker {

    private static final String TAG = "LateralusCamera";
    //private static final String SAVE_PATH = Environment.getExternalStorageDirectory().toString() + "/tmp/";

    public static final String FRONT = "FRONT";
    public static final String BACK  = "BACK";

    private Context _ctx;
    private Camera  _cam;
    private static PhotoTakerListener _delegate;

    private Boolean _camOpen     = false;
    private Boolean _hasFrontCam = false;
    private Boolean _hasBackCam  = false;

    public PhotoTaker(Context ctx, PhotoTakerListener delegate){
        Log.d(TAG, "New CameraReader");
        _ctx = ctx;
        _delegate = delegate;
        _hasBackCam = (_ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA));
        _hasFrontCam = (_ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT));
    }

    public void takePhoto(String whichCam){
        whichCam = (whichCam.equals(FRONT)) ? FRONT : BACK;
        Log.d(TAG, "Starting to take picture with: " + whichCam);
        if(openCamera(whichCam)){
            try {
                Log.d(TAG, "Starting preview.");
                _cam.startPreview();
                Log.d(TAG, "Starting to take picture");
                _cam.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] photoBytes, Camera camera) {
                        Log.d(TAG, "Picture Taken!");
                        _delegate.onPhotoTaken(photoBytes);
                        releaseCamera();
                    }
                });
            }
            catch(Exception e){
                Log.d(TAG, "Take picture failed!");
                _delegate.onError("Take Photo failed");
                e.printStackTrace();
                releaseCamera();
            }
        }
    }

    private Boolean openCamera(String whichCam){
        if(_hasFrontCam || _hasBackCam) {
            Log.d(TAG, "Trying to open camera");
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
                        Log.d(TAG, "Camera Opened!");
                        break;
                    }
                    catch(RuntimeException e) {
                        Log.d(TAG, "Camera NOT Opened");
                        _delegate.onError("Could not open camera");
                        e.printStackTrace();
                    }
                }
            }
        }
        return _camOpen;
    }

    private void releaseCamera(){
        Log.d(TAG, "Attempting to release camera");
        if(_cam != null){
            _cam.release();
            _cam = null;
            _camOpen = false;
            Log.d(TAG, "Camera released");
        }
    }

}