//package socialnetwork.socialnetwork.repository;
//
//import socialnetwork.socialnetwork.domain.User;
//
//import java.io.*;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//public class InFileRepo implements AbstractRepo<Integer, User>{
//    private static final String FILE_NAME = "users.txt";
//
//
//    @Override
//    public Optional<User> save(User user) throws IOException {
//        // Verificăm dacă există deja un utilizator cu același username
//        if (findByUsername(user.getUsername()).isPresent()) {
//            return Optional.empty(); // utilizatorul există deja
//        }
//
//        // Scriem direct utilizatorul în fișier
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
//            writer.write(user.getId() + "," + user.getUsername() + "," + user.getPassword());
//            writer.newLine();
//        }
//        return Optional.of(user);
//    }
//
//    @Override
//    public Optional<User> remove(Integer id) throws IOException {
//        List<User> remainingUsers = new ArrayList<>();
//        Optional<User> userToRemove = Optional.empty();
//
//        // Citim fiecare linie și păstrăm utilizatorii care nu au ID-ul specificat
//        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                String[] data = line.split(",");
//                if (data.length == 3) {
//                    Integer userId = Integer.parseInt(data[0]);
//                    String username = data[1];
//                    String password = data[2];
//
//                    if (userId.equals(id)) {
//                        userToRemove = Optional.of(new User(userId, username, password));
//                    } else {
//                        remainingUsers.add(new User(userId, username, password));
//                    }
//                }
//            }
//        }
//
//        // Suprascriem fișierul cu utilizatorii rămași
//        if (userToRemove.isPresent()) {
//            saveAll(remainingUsers);
//        }
//        return userToRemove;
//    }
//
//    @Override
//    public Iterable<User> findAll() throws IOException {
//        List<User> users = new ArrayList<>();
//        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                String[] data = line.split(",");
//                if (data.length == 3) {
//                    Integer id = Integer.parseInt(data[0]);
//                    String username = data[1];
//                    String password = data[2];
//                    users.add(new User(id, username, password));
//                }
//            }
//        }
//        return users;
//    }
//
//    @Override
//    public Optional<User> findOne(Integer id) throws IOException {
//        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                String[] data = line.split(",");
//                if (data.length == 3) {
//                    Integer userId = Integer.parseInt(data[0]);
//                    if (userId.equals(id)) {
//                        String username = data[1];
//                        String password = data[2];
//                        return Optional.of(new User(userId, username, password));
//                    }
//                }
//            }
//        }
//        return Optional.empty();
//    }
//
//    public Optional<User> findByUsername(String username) throws IOException {
//        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                String[] data = line.split(",");
//                if (data.length == 3) {
//                    String userUsername = data[1];
//                    if (userUsername.equals(username)) {
//                        Integer id = Integer.parseInt(data[0]);
//                        String password = data[2];
//                        return Optional.of(new User(id, userUsername, password));
//                    }
//                }
//            }
//        }
//        return Optional.empty();
//    }
//
//    private void saveAll(List<User> users) throws IOException {
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
//            for (User user : users) {
//                writer.write(user.getId() + "," + user.getUsername() + "," + user.getPassword());
//                writer.newLine();
//            }
//        }
//    }
//}
