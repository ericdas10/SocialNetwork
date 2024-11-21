package socialnetwork.socialnetwork.domain;

import java.time.LocalDate;

public class Friendship extends Entity<Integer> {

    private final User user1;
    private final User user2;
    private FriendshipStatus status;
    private final LocalDate date;

    public Friendship(Integer id, User user1, User user2, FriendshipStatus status, LocalDate date) {
        this.id = id;
        this.user1 = user1;
        this.user2 = user2;
        this.status = status;
        this.date = date;
    }

    public Friendship(Integer id,User user1, User user2, LocalDate date){
        this(id, user1, user2, FriendshipStatus.PENDING, date); //atribuie status de PENDING automat
    }


    public User getUser1() {
        return user1;
    }

    public User getUser2() {
        return user2;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public void setStatus(FriendshipStatus status) {
        this.status = status;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Friendship that = (Friendship) o;
        return (user1.equals(that.user1) && user2.equals(that.user2)) ||
                (user1.equals(that.user2) && user2.equals(that.user1));
    }

    @Override
    public int hashCode(){
        return user1.hashCode() + user2.hashCode();
    }

    @Override
    public String toString() {
        return user1 + " is friends with " + user2 + " (" + status + ")";
    }
}
