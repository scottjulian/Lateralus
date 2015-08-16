package xyz.lateralus.components.camera;


public interface PhotoTakerListener {
    void onPhotoTaken(byte[] photoBytes);
    void onError(String msg);
}
