package socialnetwork.socialnetwork.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import socialnetwork.socialnetwork.domain.*;
import socialnetwork.socialnetwork.repository.*;

public class FriendshipService {
    private final FriendshipRepoDB friendshipRepo;
    private final AbstractRepo<Integer, User> userRepo;

    public FriendshipService(FriendshipRepoDB friendshipRepo, AbstractRepo<Integer, User> userRepo) {
        this.friendshipRepo = friendshipRepo;
        this.userRepo = userRepo;
    }


    public void addFriend(Integer id1, Integer id2) throws IOException {
        List<User> users = (List<User>)userRepo.findAll();

        Optional<User> user1Opt = users.stream()
                .filter(user -> user.getId().equals(id1))
                .findFirst();
        Optional<User> user2Opt = users.stream()
                .filter(user -> user.getId().equals(id2))
                .findFirst();

        LocalDate date = LocalDate.now();

        if (user1Opt.isPresent() && user2Opt.isPresent()) {
            Friendship friendship = new Friendship(1 + findLastId(), user1Opt.get(), user2Opt.get(), date);
            friendshipRepo.save(friendship);
            System.out.println("Friend request sent from " + user1Opt.get().getUsername() + " to " + user2Opt.get().getUsername());
        } else {
            throw new IOException("One or both users not found.");
        }
    }


    public void acceptFriendRequest(Integer id1, Integer id2) throws IOException {
        StreamSupport.stream(friendshipRepo.findAll().spliterator(), false)
                .filter(friendship -> isPendingFriendshipBetweenUsers(friendship, id1, id2))
                .findFirst()
                .ifPresentOrElse(friendship -> {
                    try {
                        friendship.setStatus(FriendshipStatus.ACCEPTED);
                        friendshipRepo.remove(friendship.getId());
                        friendshipRepo.save(friendship);
                        System.out.println("Friend request between " + friendshipRepo.findUserById(id1).get().getUsername() + " and " + friendshipRepo.findUserById(id2).get().getUsername() + " accepted.");
                    } catch (IOException e) {
                        System.err.println("Failed to update friendship status: " + e.getMessage());
                    }
                }, () -> {
                    try {
                        throw new IOException("No pending friend request found between these users.");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }


    public void removeFriend(Integer id1, Integer id2) throws IOException {
        StreamSupport.stream(friendshipRepo.findAll().spliterator(), false)
                .filter(friendship -> isFriendshipBetweenUsers(friendship, id1, id2))
                .findFirst()
                .ifPresentOrElse(friendship -> {
                    try {
                        friendshipRepo.remove(friendship.getId());
                        System.out.println("Friendship between " + friendshipRepo.findUserById(id1).get().getUsername() + " and " + friendshipRepo.findUserById(id2).get().getUsername() + " removed.");
                    } catch (IOException e) {
                        System.err.println("Failed to remove friendship: " + e.getMessage());
                    }
                }, () -> {
                    try {
                        throw new IOException("No friendship found between these users.");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private Map<Integer, List<Integer>> buildFriendshipGraph(List<Friendship> allFriendships) {
        Map<Integer, List<Integer>> graph = new HashMap<>();

        for (Friendship friendship : allFriendships) {
            if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
                Integer user1 = friendship.getUser1().getId();
                Integer user2 = friendship.getUser2().getId();

                graph.computeIfAbsent(user1, _ -> new ArrayList<>()).add(user2);
                graph.computeIfAbsent(user2, _ -> new ArrayList<>()).add(user1);
            }
        }

        return graph;
    }

    public List<List<User>> findAllCommunities() throws IOException {
        List<Friendship> allFriendships = StreamSupport.stream(friendshipRepo.findAll().spliterator(), false)
                .toList();
        Map<Integer, List<Integer>> friendshipGraph = buildFriendshipGraph(allFriendships);

        Set<Integer> visited = new HashSet<>();
        List<List<User>> allCommunities = new ArrayList<>();

        for (Integer id : friendshipGraph.keySet()) {
            if (!visited.contains(id)) {
                List<User> currentCommunity = new ArrayList<>();
                dfs(id, friendshipGraph, visited, currentCommunity);
                allCommunities.add(currentCommunity);
            }
        }

        return allCommunities;
    }

    private void dfs(Integer username, Map<Integer, List<Integer>> graph, Set<Integer> visited, List<User> currentCommunity) throws IOException {
        visited.add(username);
        User user = findUserById(username);

        if (user != null) {
            currentCommunity.add(user);

            for (Integer neighbor : graph.getOrDefault(username, new ArrayList<>())) {
                if (!visited.contains(neighbor)) {
                    dfs(neighbor, graph, visited, currentCommunity);
                }
            }
        }
    }

    public void displayMostSociableCommunity() throws IOException {
        List<User> mostSociableCommunity = findMostSociableCommunity();

        if (mostSociableCommunity.isEmpty()) {
            System.out.println("No sociable community found.");
        } else {
            System.out.println("Most sociable community:");
            mostSociableCommunity.forEach(user -> System.out.println(" - " + user.getUsername()));
        }
    }



    private boolean isPendingFriendshipBetweenUsers(Friendship friendship, Integer id1, Integer id2) {
        return (friendship.getUser1().getId().equals(id1) && friendship.getUser2().getId().equals(id2)
                || friendship.getUser1().getId().equals(id2) && friendship.getUser2().getId().equals(id1))
                && friendship.getStatus() == FriendshipStatus.PENDING;
    }

    private boolean isFriendshipBetweenUsers(Friendship friendship, Integer id1, Integer id2) {
        return (friendship.getUser1().getId().equals(id1) && friendship.getUser2().getId().equals(id2)
                || friendship.getUser1().getId().equals(id2) && friendship.getUser2().getId().equals(id1));
    }

    public User findUserById(Integer id) throws IOException {
        return StreamSupport.stream(userRepo.findAll().spliterator(), false)
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public User findUserByUsername(String username) throws IOException {
        return StreamSupport.stream(userRepo.findAll().spliterator(), false)
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    public void garbageFrindships(User user) throws IOException {
        List<Friendship> allFriendships = StreamSupport.stream(friendshipRepo.findAll().spliterator(), false)
                .toList();

        System.out.println("Garbage delete started\n");
        for (Friendship friendship: allFriendships){
            //System.out.println("entered in for\n");
            if (friendship.getUser1().getId().equals(user.getId()) || friendship.getUser2().getId().equals(user.getId())){
                System.out.println("started deleting\n");
                friendshipRepo.remove(friendship.getId());
                System.out.println("delete\n");
            }
        }
    }

    public List<User> findMostSociableCommunity() throws IOException {
        List<List<User>> allCommunities = findAllCommunities();
        List<User> mostSociableCommunity = new ArrayList<>();
        int longestPathLength = 0;

        for (List<User> community : allCommunities) {
            Map<Integer, List<Integer>> communityGraph = buildCommunityGraph(community);
            if (communityGraph.isEmpty()) continue;

            Integer startUser = community.get(0).getId();

            DFSResult firstDFS = dfsFindFarthestNode(startUser, communityGraph, new HashSet<>());

            DFSResult secondDFS = dfsFindFarthestNode(firstDFS.farthestNode, communityGraph, new HashSet<>());

            if (secondDFS.maxDepth > longestPathLength) {
                longestPathLength = secondDFS.maxDepth;
                mostSociableCommunity = community;
            }
        }

        return mostSociableCommunity;
    }

    private Map<Integer, List<Integer>> buildCommunityGraph(List<User> community) throws IOException {
        Map<Integer, List<Integer>> communityGraph = new HashMap<>();
        List<Friendship> friendships = (List<Friendship>) friendshipRepo.findAll();
        Set<Integer> communityUsernames = new HashSet<>();

        for (User user : community) {
            communityUsernames.add(user.getId());
            communityGraph.put(user.getId(), new ArrayList<>());
        }

        for (Friendship friendship : friendships) {
            if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
                Integer user1 = friendship.getUser1().getId();
                Integer user2 = friendship.getUser2().getId();

                if (communityUsernames.contains(user1) && communityUsernames.contains(user2)) {
                    communityGraph.get(user1).add(user2);
                    communityGraph.get(user2).add(user1);
                }
            }
        }

        return communityGraph;
    }

    private DFSResult dfsFindFarthestNode(Integer start, Map<Integer, List<Integer>> graph, Set<Integer> visited) {
        visited.add(start);
        DFSResult result = new DFSResult(start, 0);

        for (Integer neighbor : graph.getOrDefault(start, new ArrayList<>())) {
            if (!visited.contains(neighbor)) {
                DFSResult depthResult = dfsFindFarthestNode(neighbor, graph, visited);
                if (depthResult.maxDepth + 1 > result.maxDepth) {
                    result.maxDepth = depthResult.maxDepth + 1;
                    result.farthestNode = depthResult.farthestNode;
                }
            }
        }

        return result;
    }

    public List<String> getFriends(String currentUser) throws IOException {
        User user = findUserByUsername(currentUser);
        return StreamSupport.stream(friendshipRepo.findAll().spliterator(), false)
                .filter(friendship -> friendship.getStatus() == FriendshipStatus.ACCEPTED &&
                        (friendship.getUser1().getId().equals(user.getId()) || friendship.getUser2().getId().equals(user.getId())))
                .map(friendship -> friendship.getUser1().getId().equals(user.getId()) ? friendship.getUser2().getUsername() : friendship.getUser1().getUsername())
                .collect(Collectors.toList());
    }

    public List<String> getPendingRequests(String currentUser) throws IOException {
        User user = findUserByUsername(currentUser);
        return StreamSupport.stream(friendshipRepo.findAll().spliterator(), false)
                .filter(friendship -> friendship.getStatus() == FriendshipStatus.PENDING && friendship.getUser2().getId().equals(user.getId()))
                .map(friendship -> friendship.getUser1().getUsername())
                .collect(Collectors.toList());
    }

    private static class DFSResult {
        Integer farthestNode;
        int maxDepth;

        public DFSResult(Integer farthestNode, int maxDepth) {
            this.farthestNode = farthestNode;
            this.maxDepth = maxDepth;
        }
    }

    private int findLastId() throws IOException {
        List<Friendship> friendships = (List<Friendship>) friendshipRepo.findAll();

        if (friendships.isEmpty()){
            return 0;
        }else {
            return friendships.size();
        }
    }

}

