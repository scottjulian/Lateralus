package xyz.lateralus.components.audio;


import android.media.MediaRecorder;

import java.util.Timer;
import java.util.TimerTask;

import xyz.lateralus.components.io.FileSystem;


public class AudioRecorder {
    private static final String TAG = "AudioRecorder";

    private MediaRecorder _recorder;
    private AudioRecorderDelegate _delegate;

    private Timer _timer;
    private String _savedFilePath;
    private int _durationMillis;
    private static final int MAX_DURATION = 1000 * 60 * 5; // 5 minutes

    public AudioRecorder(AudioRecorderDelegate delegate){
        _delegate = delegate;
    }

    public void startRecordingMic(int seconds){
        try {
            _savedFilePath = FileSystem.TMP_DIR + generateMp4FileName();
            _durationMillis = ((seconds*1000) < MAX_DURATION) ? (seconds*1000) : MAX_DURATION;

            _recorder = new MediaRecorder();
            _recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            _recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            _recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            _recorder.setMaxDuration(_durationMillis);
            _recorder.setOutputFile(_savedFilePath);
            _recorder.prepare();
            _recorder.start();

            startTimer();
        }
        catch(Exception e){

        }
    }

    public void stopRecording(){
        if(_recorder != null){
            _recorder.stop();
            _recorder.release();
            _recorder = null;
            if(FileSystem.doesFileExist(_savedFilePath)){
                processRecording();
            }
            else{
                _delegate.onErrorMessage("Could not find recording file");
            }
        }
        FileSystem.deleteFile(_savedFilePath);
    }

    private void processRecording(){
        byte[] recordingData = FileSystem.getByteArrayOfFile(_savedFilePath);
        if(recordingData != null){
            _delegate.onFinishedRecording(recordingData);
        }
        else {
            _delegate.onErrorMessage("Could not read byte[] data of audio recording");
        }
    }

    private void startTimer(){
        if(_timer == null){
            _timer = new Timer();
        }
        _timer.purge();
        _timer.schedule(new TimerTask() {
            @Override
            public void run() {
                stopRecording();
            }
        }, _durationMillis - 1000);
    }

    private void stopTimer(){
        if(_timer != null){
            _timer.cancel();
            _timer = null;
        }
    }

    private static String generateMp4FileName(){
        return String.valueOf(System.currentTimeMillis()) + ".mp4";
    }
}
