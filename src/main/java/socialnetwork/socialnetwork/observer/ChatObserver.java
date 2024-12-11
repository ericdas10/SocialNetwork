package socialnetwork.socialnetwork.observer;

import socialnetwork.socialnetwork.domain.Message;

public interface ChatObserver {
    void onNewMessage(int chatRoomId, Message message);
}
