package xyz.lateralus.components.audio;

public interface AudioRecorderDelegate {
    void onErrorMessage(String msg);
    void onFinishedRecording(byte[] data);
}
