package socialnetwork.socialnetwork.domain;

public class User extends Entity<Integer> {
    private String username;
    private String password;

    public User(Integer id, String username, String password){
        this.id = id;
        this.password = password;
        this.username = username;
    }

    public User(String username, String password){
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
