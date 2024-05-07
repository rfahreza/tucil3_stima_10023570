import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class WordLadderSolver {

    private static final String DICTIONARY_FILE = "dict.txt";
    private static Set<String> dictionary;

    public static void main(String[] args) {
        loadDictionary();

        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("Enter start word:");
            String startWord = scanner.nextLine().toLowerCase();
            if (!isValidWord(startWord)) {
                System.out.println("Invalid start word!");
                return;
            }

            System.out.println("Enter end word:");
            String endWord = scanner.nextLine().toLowerCase();
            if (!isValidWord(endWord)) {
                System.out.println("Invalid end word!");
                return;
            }

            System.out.println("Choose algorithm (UCS/Greedy/A*):");
            String algorithm = scanner.nextLine().toUpperCase();

            long startTime = System.currentTimeMillis();
            List<String> path = findPath(startWord, endWord, algorithm);
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            if (path.isEmpty()) {
                System.out.println("No path found.");
            } else {
                System.out.println("Path: " + path);
                System.out.println("Nodes visited: " + path.size());
                System.out.println("Execution time: " + executionTime + " milliseconds");
            }
        } finally {
            scanner.close(); // Tutup scanner setelah selesai digunakan
        }
    }

    private static List<String> findPath(String startWord, String endWord, String algorithm) {
        switch (algorithm) {
            case "UCS":
                return ucs(startWord, endWord);
            case "GREEDY":
                return greedyBestFirstSearch(startWord, endWord);
            case "A*":
                return aStar(startWord, endWord);
            default:
                System.out.println("Invalid algorithm choice.");
                return new ArrayList<>();
        }
    }

    private static List<String> ucs(String startWord, String endWord) {
        Queue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(Node::getCost));
        Map<String, Integer> costSoFar = new HashMap<>();
        Map<String, String> cameFrom = new HashMap<>();
        
        queue.add(new Node(startWord, 0));
        costSoFar.put(startWord, 0);

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            if (current.word.equals(endWord)) {
                return reconstructPath(cameFrom, endWord);
            }

            for (String neighbor : getNeighbors(current.word)) {
                int newCost = costSoFar.get(current.word) + 1;
                if (!costSoFar.containsKey(neighbor) || newCost < costSoFar.get(neighbor)) {
                    costSoFar.put(neighbor, newCost);
                    queue.add(new Node(neighbor, newCost));
                    cameFrom.put(neighbor, current.word);
                }
            }
        }

        return new ArrayList<>();
    }

    private static List<String> greedyBestFirstSearch(String startWord, String endWord) {
        Queue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(n -> heuristic(n.word, endWord)));
        Map<String, String> cameFrom = new HashMap<>();
        Set<String> visited = new HashSet<>();
        
        queue.add(new Node(startWord, 0));

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            if (current.word.equals(endWord)) {
                return reconstructPath(cameFrom, endWord);
            }

            visited.add(current.word);

            for (String neighbor : getNeighbors(current.word)) {
                if (!visited.contains(neighbor)) {
                    queue.add(new Node(neighbor, heuristic(neighbor, endWord)));
                    cameFrom.put(neighbor, current.word);
                }
            }
        }

        return new ArrayList<>();
    }

    private static List<String> aStar(String startWord, String endWord) {
        Queue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(n -> n.cost + heuristic(n.word, endWord)));
        Map<String, Integer> costSoFar = new HashMap<>();
        Map<String, String> cameFrom = new HashMap<>();
        
        queue.add(new Node(startWord, 0));
        costSoFar.put(startWord, 0);

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            if (current.word.equals(endWord)) {
                return reconstructPath(cameFrom, endWord);
            }

            for (String neighbor : getNeighbors(current.word)) {
                int newCost = costSoFar.get(current.word) + 1;
                if (!costSoFar.containsKey(neighbor) || newCost < costSoFar.get(neighbor)) {
                    costSoFar.put(neighbor, newCost);
                    int priority = newCost + heuristic(neighbor, endWord);
                    queue.add(new Node(neighbor, newCost, priority));
                    cameFrom.put(neighbor, current.word);
                }
            }
        }

        return new ArrayList<>();
    }

    private static List<String> reconstructPath(Map<String, String> cameFrom, String endWord) {
        List<String> path = new ArrayList<>();
        String current = endWord;
        while (cameFrom.containsKey(current)) {
            path.add(0, current);
            current = cameFrom.get(current);
        }
        path.add(0, current);
        return path;
    }

    private static List<String> getNeighbors(String word) {
        List<String> neighbors = new ArrayList<>();
        char[] chars = word.toCharArray();
        for (int i = 0; i < word.length(); i++) {
            char originalChar = chars[i];
            for (char c = 'a'; c <= 'z'; c++) {
                if (c != originalChar) {
                    chars[i] = c;
                    String neighbor = new String(chars);
                    if (isValidWord(neighbor)) {
                        neighbors.add(neighbor);
                    }
                }
            }
            chars[i] = originalChar;
        }
        return neighbors;
    }

    private static int heuristic(String word, String target) {
        int count = 0;
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) != target.charAt(i)) {
                count++;
            }
        }
        return count;
    }

    private static void loadDictionary() {
        dictionary = new HashSet<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(DICTIONARY_FILE));
            String word;
            while ((word = reader.readLine()) != null) {
                dictionary.add(word.toLowerCase());
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Error loading dictionary: " + e.getMessage());
        }
    }

    private static boolean isValidWord(String word) {
        return dictionary.contains(word.toLowerCase());
    }

    static class Node {
        String word;
        int cost;
        int priority;

        Node(String word, int cost) {
            this.word = word;
            this.cost = cost;
        }

        Node(String word, int cost, int priority) {
            this.word = word;
            this.cost = cost;
            this.priority = priority;
        }

        public int getCost() {
            return cost;
        }
    }
}
