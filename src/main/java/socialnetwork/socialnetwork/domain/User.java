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

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof User)) {
            return false;
        }
        User user = (User) obj;
        return id.equals(user.id) && username.equals(user.username) && password.equals(user.password);
    }

    @Override
    public int hashCode() {
        return id;
    }
}
