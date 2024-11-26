//package socialnetwork.socialnetwork.service;
//
//import socialnetwork.socialnetwork.domain.Friendship;
//import socialnetwork.socialnetwork.domain.FriendshipStatus;
//import socialnetwork.socialnetwork.domain.User;
//import socialnetwork.socialnetwork.repository.AbstractRepo;
//import socialnetwork.socialnetwork.repository.FriendshipRepoDB;
//
//import java.io.IOException;
//import java.util.*;
//import java.util.stream.StreamSupport;
//
//public class GraphService {
//    private final FriendshipService friendshipService;
//
//    public GraphService(FriendshipService friendshipService) {
//        this.friendshipService = friendshipService;
//    }
//
//    private Map<Integer, List<Integer>> buildFriendshipGraph(List<Friendship> allFriendships) {
//        Map<Integer, List<Integer>> graph = new HashMap<>();
//
//        for (Friendship friendship : allFriendships) {
//            if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
//                Integer user1 = friendship.getUser1().getId();
//                Integer user2 = friendship.getUser2().getId();
//
//                graph.computeIfAbsent(user1, _ -> new ArrayList<>()).add(user2);
//                graph.computeIfAbsent(user2, _ -> new ArrayList<>()).add(user1);
//            }
//        }
//
//        return graph;
//    }
//
//    public List<List<User>> findAllCommunities() throws IOException {
//        List<Friendship> allFriendships = StreamSupport.stream(friendshipRepo.findAll().spliterator(), false)
//                .toList();
//        Map<Integer, List<Integer>> friendshipGraph = buildFriendshipGraph(allFriendships);
//
//        Set<Integer> visited = new HashSet<>();
//        List<List<User>> allCommunities = new ArrayList<>();
//
//        for (Integer id : friendshipGraph.keySet()) {
//            if (!visited.contains(id)) {
//                List<User> currentCommunity = new ArrayList<>();
//                dfs(id, friendshipGraph, visited, currentCommunity);
//                allCommunities.add(currentCommunity);
//            }
//        }
//
//        return allCommunities;
//    }
//
//    private void dfs(Integer username, Map<Integer, List<Integer>> graph, Set<Integer> visited, List<User> currentCommunity) throws IOException {
//        visited.add(username);
//        User user = findUserById(username);
//
//        if (user != null) {
//            currentCommunity.add(user);
//
//            for (Integer neighbor : graph.getOrDefault(username, new ArrayList<>())) {
//                if (!visited.contains(neighbor)) {
//                    dfs(neighbor, graph, visited, currentCommunity);
//                }
//            }
//        }
//    }
//
//    public void displayMostSociableCommunity() throws IOException {
//        List<User> mostSociableCommunity = findMostSociableCommunity();
//
//        if (mostSociableCommunity.isEmpty()) {
//            System.out.println("No sociable community found.");
//        } else {
//            System.out.println("Most sociable community:");
//            mostSociableCommunity.forEach(user -> System.out.println(" - " + user.getUsername()));
//        }
//    }
//
//    public List<User> findMostSociableCommunity() throws IOException {
//        List<List<User>> allCommunities = findAllCommunities();
//        List<User> mostSociableCommunity = new ArrayList<>();
//        int longestPathLength = 0;
//
//        for (List<User> community : allCommunities) {
//            Map<Integer, List<Integer>> communityGraph = buildCommunityGraph(community);
//            if (communityGraph.isEmpty()) continue;
//
//            Integer startUser = community.get(0).getId();
//
//            FriendshipService.DFSResult firstDFS = dfsFindFarthestNode(startUser, communityGraph, new HashSet<>());
//
//            FriendshipService.DFSResult secondDFS = dfsFindFarthestNode(firstDFS.farthestNode, communityGraph, new HashSet<>());
//
//            if (secondDFS.maxDepth > longestPathLength) {
//                longestPathLength = secondDFS.maxDepth;
//                mostSociableCommunity = community;
//            }
//        }
//
//        return mostSociableCommunity;
//    }
//
//    private Map<Integer, List<Integer>> buildCommunityGraph(List<User> community) throws IOException {
//        Map<Integer, List<Integer>> communityGraph = new HashMap<>();
//        List<Friendship> friendships = (List<Friendship>) friendshipRepo.findAll();
//        Set<Integer> communityUsernames = new HashSet<>();
//
//        for (User user : community) {
//            communityUsernames.add(user.getId());
//            communityGraph.put(user.getId(), new ArrayList<>());
//        }
//
//        for (Friendship friendship : friendships) {
//            if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
//                Integer user1 = friendship.getUser1().getId();
//                Integer user2 = friendship.getUser2().getId();
//
//                if (communityUsernames.contains(user1) && communityUsernames.contains(user2)) {
//                    communityGraph.get(user1).add(user2);
//                    communityGraph.get(user2).add(user1);
//                }
//            }
//        }
//
//        return communityGraph;
//    }
//
//    private FriendshipService.DFSResult dfsFindFarthestNode(Integer start, Map<Integer, List<Integer>> graph, Set<Integer> visited) {
//        visited.add(start);
//        FriendshipService.DFSResult result = new FriendshipService.DFSResult(start, 0);
//
//        for (Integer neighbor : graph.getOrDefault(start, new ArrayList<>())) {
//            if (!visited.contains(neighbor)) {
//                FriendshipService.DFSResult depthResult = dfsFindFarthestNode(neighbor, graph, visited);
//                if (depthResult.maxDepth + 1 > result.maxDepth) {
//                    result.maxDepth = depthResult.maxDepth + 1;
//                    result.farthestNode = depthResult.farthestNode;
//                }
//            }
//        }
//
//        return result;
//    }
//
//    private static class DFSResult {
//        Integer farthestNode;
//        int maxDepth;
//
//        public DFSResult(Integer farthestNode, int maxDepth) {
//            this.farthestNode = farthestNode;
//            this.maxDepth = maxDepth;
//        }
//    }
//}
