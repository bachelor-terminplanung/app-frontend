package at.terminplaner;

public interface EventIdCallback {
    void onEventIdReceived(int eventId);
    void onError(String errorMessage);
}
