//package socialnetwork.socialnetwork.repository;
//
//import socialnetwork.socialnetwork.domain.*;
//
//import java.io.*;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//public class FriendshipRepo implements AbstractRepo<Integer, Friendship> {
//    private final String filePath;
//    private final List<User> users;
//
//    public FriendshipRepo(String filePath, List<User> users) {
//        this.filePath = filePath;
//        this.users = users;
//    }
//
//    @Override
//    public Optional<Friendship> save(Friendship friendship) throws IOException {
//        List<Friendship> friendships = (List<Friendship>) findAll();
//        if (friendships.stream().anyMatch(friendship1 -> friendship1.equals(friendship))) {
//            return Optional.empty();
//        }
//
//        // Setăm un ID unic și secvențial pentru fiecare prietenie
////        friendship.setId(IdGenerator.generateFriendshipId());
//
//        // Salvăm prietenia în fișier
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
//            writer.write(friendship.getId() + "," +
//                    friendship.getUser1().getId() + "," +
//                    friendship.getUser2().getId() + "," +
//                    friendship.getStatus().toString());
//            writer.newLine();
//        }
//
//        return Optional.of(friendship);
//    }
//
//    @Override
//    public Iterable<Friendship> findAll() throws IOException {
//        List<Friendship> friendships = new ArrayList<>();
//        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                String[] data = line.split(",");
//                if (data.length == 4) {
//                    Integer id = Integer.parseInt(data[0]);
//                    Integer user1Id = Integer.parseInt(data[1]);
//                    Integer user2Id = Integer.parseInt(data[2]);
//                    FriendshipStatus status = FriendshipStatus.valueOf(data[3]);
//
//                    // Creăm obiectele utilizatorilor
//                    User user1 = findById(user1Id);
//                    User user2 = findById(user2Id);
//
//                    // Adăugăm prietenia la lista rezultată
//                    if (user1 != null && user2 != null) {
//                        friendships.add(new Friendship(id, user1, user2, status));
//                    }
//                }
//            }
//        }
//        return friendships;
//    }
//
//    @Override
//    public Optional<Friendship> findOne(Integer id) throws IOException {
//        List<Friendship> friendships = (List<Friendship>)findAll();
//        return friendships.stream()
//                .filter(friendship -> friendship.getId().equals(id))
//                .findFirst();
//    }
//
//    @Override
//    public Optional<Friendship> remove(Integer id) throws IOException {
//        List<Friendship> friendships = new ArrayList<>((List<Friendship>) findAll());
//        Optional<Friendship> friendshipToRemove = friendships.stream()
//                .filter(friendship -> friendship.getId().equals(id))
//                .findFirst();
//
//        if (friendshipToRemove.isPresent()) {
//            friendships.remove(friendshipToRemove.get());
//            saveAll(friendships);  // Re-salvăm toate prieteniile fără cea eliminată
//        }
//
//        return friendshipToRemove;
//    }
//
//    private void saveAll(List<Friendship> friendships) throws IOException {
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
//            for (Friendship friendship : friendships) {
//                writer.write(friendship.getId() + "," +
//                        friendship.getUser1().getId() + "," +
//                        friendship.getUser2().getId() + "," +
//                        friendship.getStatus());
//                writer.newLine();
//            }
//        }
//    }
//
//    public User findById(Integer id) {
//        return users.stream()
//                .filter(user -> user.getId().equals(id))
//                .findFirst()
//                .orElse(null);
//    }
//}
