package socialnetwork.socialnetwork.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notification extends Entity<Integer> {
    private final User toUser;
    private final User fromUser;
    private final LocalDateTime timestamp;
    private final String message;
    private boolean isRead;

    public Notification(Integer id, User toUser, User fromUser, LocalDateTime timestamp) {
        this.id = id;
        this.toUser = toUser;
        this.fromUser = fromUser;
        this.timestamp = timestamp;
        this.message = String.format("Ai primit o cerere de prietenie de la userul %s la data de %s.",
                fromUser.getUsername(),
                timestamp.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
        this.isRead = false;
    }

    public User getToUser() {
        return toUser;
    }

    public User getFromUser() {
        return fromUser;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
