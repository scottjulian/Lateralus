package xyz.lateralus.components.camera;


public interface PhotoTakerDelegate {
    void onPhotoTaken(byte[] photoBytes);
    void onError(String msg);
}
