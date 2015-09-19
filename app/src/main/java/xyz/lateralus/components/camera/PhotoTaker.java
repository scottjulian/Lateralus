package xyz.lateralus.components.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;

import java.util.List;


public class PhotoTaker {

    private static final String TAG = "LateralusCamera";
    //private static final String SAVE_PATH = Environment.getExternalStorageDirectory().toString() + "/tmp/";

    public static final String FRONT = "FRONT";
    public static final String BACK  = "BACK";

    public static final int MIN_WIDTH  = 640;
    public static final int MIN_HEIGHT = 360;
    public static final int MAX_AREA   = 2560*1080;

    public static final String WIDE_16X9  = "16:9";
    public static final String WIDE_16X10 = "16:10";

    private Context _ctx;
    private Camera  _cam;
    private static PhotoTakerDelegate _delegate;

    private Boolean _camOpen     = false;
    private Boolean _hasFrontCam = false;
    private Boolean _hasBackCam  = false;

    public PhotoTaker(Context ctx, PhotoTakerDelegate delegate){
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
                        if(_camOpen){
                            Log.d(TAG, "Camera Opened!");
                            Camera.Parameters params = _cam.getParameters();
                            //Size _selectedSize = getSmallestWidescreen(params.getSupportedPictureSizes());
                            Size _selectedSize = getSmallestSize(params.getSupportedPictureSizes());
                            if(_selectedSize != null) {
                                params.set("orientation", "landscape");
                                params.setPictureSize(_selectedSize.width, _selectedSize.height);
                                _cam.setParameters(params);
                            }
                        }
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

    private Size getSmallestSize(List<Size> sizes){
        int area = MAX_AREA;
        Size selected = null;
        for(Size s : sizes){
            if(isMinimumSize(s.width, s.height) && (s.width * s.height < area)){
                selected = s;
                area = s.width * s.height;
            }
        }
        return selected;
    }

    private Size getSmallestWidescreen(List<Size> sizes){
        int area = MAX_AREA;
        Size selected = null;
        for(Size s : sizes){
            // select the smallest widescreen
            if(isWidescreen(s.width, s.height) && isMinimumSize(s.width, s.height) && (s.width * s.height < area)){
                selected = s;
                area = s.width * s.height;
            }
        }
        return selected;
    }

    private boolean isMinimumSize(int width, int height) {
        return (width >= MIN_WIDTH && height >= MIN_HEIGHT);
    }

    private boolean isWidescreen(int width, int height){
        int gcd = gcd(width, height);
        String ratio = String.valueOf(width/gcd) + ":" + String.valueOf(height/gcd);
        return (ratio.equals(WIDE_16X10) || ratio.equals(WIDE_16X9));
    }

    private int gcd(int a, int b){
        if(b == 0) {
            return a;
        }
        return gcd (b, a%b);
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