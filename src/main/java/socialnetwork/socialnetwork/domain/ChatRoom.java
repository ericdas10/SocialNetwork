package socialnetwork.socialnetwork.domain;

import java.util.List;

public class ChatRoom extends Entity<Integer>{
    private String name;
    private final List<User> participants;
    private final List<Message> messages;

    public ChatRoom(String name, List<User> participants, List<Message> messages) {
        this.name = name;
        this.participants = participants;
        this.messages = messages;
    }

    public ChatRoom(Integer id, String name, List<User> participants, List<Message> messages) {
        super.setId(id);
        this.name = name;
        this.participants = participants;
        this.messages = messages;
    }

    public String getName() {
        return name;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setName(String name) {
        this.name = name;
    }
}
