package socialnetwork.socialnetwork.domain;

import java.time.LocalDateTime;
import java.util.List;

public class Message extends Entity<Integer>{
    private User from;
    private List<User> to;
    private String message;
    private LocalDateTime date;
    private Message reply;
    private MessageType type;
    private int chatRoomId;

    public Message(User from, List<User> to, String message, LocalDateTime date, MessageType type) {
        this.from = from;
        this.to = to;
        this.message = message;
        this.date = date;
        this.type = type;
        this.reply = null;
    }

    public Message(User from, List<User> to, String message, MessageType type) {
        this.from = from;
        this.to = to;
        this.message = message;
        this.date = LocalDateTime.now();
        this.reply = null;
        this.type = type;
    }

    public void setChatRoomId(int chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public int getChatRoomId() {
        return chatRoomId;
    }

    public User getFrom() {
        return from;
    }

    public List<User> getTo() {
        return to;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public void setTo(List<User> to) {
        this.to = to;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Message getReply() {
        return reply;
    }

    public void setReply(Message reply) {
        this.reply = reply;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Message{" +
                "from=" + from +
                ", to=" + to +
                ", message='" + message + '\'' +
                ", date=" + date +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Message message = (Message) obj;
        return from.equals(message.from) && to.equals(message.to) && this.message.equals(message.message) && date.equals(message.date);
    }

    @Override
    public int hashCode() {
        return from.hashCode() + to.hashCode() + message.hashCode() + date.hashCode();
    }

}
