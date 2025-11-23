package my_f_inp_outp_project;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class GoogleWebGraph {
    private int numNodes;
    private int numEdges;
    private boolean[][] adjacencyMatrix;
    private List<List<Integer>> adjacencyList;
    
    public GoogleWebGraph(String filename) {
        readGraphData(filename);
    }
    
    /**
     * Part 2: Read the graph data using Scanner (as shown in instructions)
     */
    private void readGraphData(String filename) {
        // abs path to file
        String absolutePath = "G:/Computational Thinking and Big Data/Eclipse/eclipse_workspace/my_f_inp_outp_project/src/my_f_inp_outp_project/web-google.mtx";
        File f = new File(absolutePath);
        
        System.out.println("Looking for file at: " + f.getAbsolutePath());
        System.out.println("File exists: " + f.exists());
        
        if (!f.exists()) {
            System.out.println("File not found! Using relative path as fallback...");
            f = new File(filename);
        }
        
        int lineCounter = 0;
        
        try {
            Scanner sc = new Scanner(f);
            List<int[]> edgePairs = new ArrayList<>();
            
            while(sc.hasNextLine()) {
                String line = sc.nextLine();
                lineCounter++;
                
                // Skip comment lines (start with %)
                if (line.startsWith("%")) {
                    continue;
                }
                
                // Split by whitespace (as shown in instructions)
                String[] tokens = line.split("\\s+");
                
                // First data line: "1299 1299 2773"
                if (tokens.length == 3 && numNodes == 0) {
                    numNodes = Integer.parseInt(tokens[0]);
                    numEdges = Integer.parseInt(tokens[2]);
                    
                    // Initialize both representations
                    initializeDataStructures();
                    continue;
                }
                
                // Edge lines: "333 1"
                if (tokens.length >= 2) {
                    int source = Integer.parseInt(tokens[0]);
                    int target = Integer.parseInt(tokens[1]);
                    edgePairs.add(new int[]{source, target});
                }
            }
            
            sc.close();
            
            // Build both graph representations
            buildAdjacencyMatrix(edgePairs);
            buildAdjacencyList(edgePairs);
            
            System.out.println("Successfully read " + edgePairs.size() + " edges");
            
        } catch (FileNotFoundException ex) {
            System.out.println("File " + f + " not found.");
            // Ініціалізуємо пусті структури даних щоб уникнути NullPointerException
            initializeEmptyStructures();
        }
    }
    
    private void initializeEmptyStructures() {
        this.numNodes = 0;
        this.numEdges = 0;
        this.adjacencyMatrix = new boolean[0][0];
        this.adjacencyList = new ArrayList<>();
    }
    
    private void initializeDataStructures() {
        // Adjacency Matrix: size 1300x1300 (1-based indexing)
        adjacencyMatrix = new boolean[numNodes + 1][numNodes + 1];
        
        // Adjacency List: array of lists, size 1300 (1-based indexing)
        adjacencyList = new ArrayList<>(numNodes + 1);
        for (int i = 0; i <= numNodes; i++) {
            adjacencyList.add(new ArrayList<>());
        }
    }
    
    /**
     * Exercise 1: Create adjacency matrix representation
     * A 2D array where matrix[i][j] = true if there's an edge from i to j
     */
    private void buildAdjacencyMatrix(List<int[]> edgePairs) {
        for (int[] edge : edgePairs) {
            int source = edge[0];
            int target = edge[1];
            adjacencyMatrix[source][target] = true;
        }
        System.out.println("Adjacency matrix created: " + numNodes + "x" + numNodes);
    }
    
    /**
     * Exercise 2: Create adjacency list representation  
     * A list of lists where list[i] contains all neighbors of node i
     */
    private void buildAdjacencyList(List<int[]> edgePairs) {
        for (int[] edge : edgePairs) {
            int source = edge[0];
            int target = edge[1];
            adjacencyList.get(source).add(target);
        }
        System.out.println("Adjacency list created for " + numNodes + " nodes");
    }
    
    /**
     * Part 3: Answer the questions
     */
    public void analyzeGraph() {
        System.out.println("\n=== GRAPH ANALYSIS ===");
        
        if (numNodes == 0) {
            System.out.println("Cannot analyze - no graph data loaded");
            return;
        }
        
        // Question 1: Node with highest out-degree
        int maxOutDegree = 0;
        int maxOutNode = 0;
        for (int i = 1; i <= numNodes; i++) {
            int outDegree = adjacencyList.get(i).size();
            if (outDegree > maxOutDegree) {
                maxOutDegree = outDegree;
                maxOutNode = i;
            }
        }
        System.out.println("1. Node with highest OUT-degree: " + maxOutNode);
        System.out.println("   Out-degree: " + maxOutDegree);
        
        // Question 2: Node with highest in-degree
        int[] inDegrees = new int[numNodes + 1];
        for (int i = 1; i <= numNodes; i++) {
            for (int neighbor : adjacencyList.get(i)) {
                inDegrees[neighbor]++;
            }
        }
        
        int maxInDegree = 0;
        int maxInNode = 0;
        for (int i = 1; i <= numNodes; i++) {
            if (inDegrees[i] > maxInDegree) {
                maxInDegree = inDegrees[i];
                maxInNode = i;
            }
        }
        System.out.println("2. Node with highest IN-degree: " + maxInNode);
        System.out.println("   In-degree: " + maxInDegree);
        
        // Question 3: Memory comparison
        long matrixMemory = (long) (numNodes + 1) * (numNodes + 1); // bytes for boolean matrix
        long listMemory = 0;
        for (int i = 1; i <= numNodes; i++) {
            listMemory += adjacencyList.get(i).size() * 4 + 20; // approximate
        }
        
        System.out.println("3. Memory Usage Comparison:");
        System.out.println("   Adjacency Matrix: ~" + matrixMemory + " bytes");
        System.out.println("   Adjacency List: ~" + listMemory + " bytes");
        System.out.println("   List uses " + String.format("%.1f", (double)listMemory/matrixMemory*100) + "% of matrix memory");
    }
    
    /**
     * Test methods to verify our implementations
     */
    public void testImplementations() {
        System.out.println("\n=== TESTING IMPLEMENTATIONS ===");
        
        if (numNodes == 0) {
            System.out.println("Cannot test - no graph data loaded");
            return;
        }
        
        // Test a known edge from the file description
        System.out.println("Testing edge from 333 to 1: " + hasEdge(333, 1));
        System.out.println("Testing edge from 808 to 1: " + hasEdge(808, 1));
        
        // Test adjacency list for node 333
        System.out.println("Neighbors of node 333: " + getNeighbors(333).size() + " neighbors");
    }
    
    // Helper methods
    public boolean hasEdge(int source, int target) {
        if (adjacencyMatrix == null || source >= adjacencyMatrix.length || target >= adjacencyMatrix[0].length) {
            return false;
        }
        return adjacencyMatrix[source][target];
    }
    
    public List<Integer> getNeighbors(int node) {
        if (adjacencyList == null || node >= adjacencyList.size()) {
            return new ArrayList<>();
        }
        return adjacencyList.get(node);
    }
    
    /**
     * Main method - run everything
     */
    public static void main(String[] args) {
        // Step 3: Run the analysis
        GoogleWebGraph graph = new GoogleWebGraph("web-google.mtx");
        
        // Step 4: Answer the questions
        graph.analyzeGraph();
        
        // Step 5: Verify implementations work
        graph.testImplementations();
    }
}
