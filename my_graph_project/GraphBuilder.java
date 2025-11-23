package my_graph_project;

import java.util.ArrayList;

class Graph {
    // Your code here
	ArrayList<Node> nodes;
	ArrayList<Edge> edges;
	
	 Graph()
	{
		this.nodes = new ArrayList<Node>();
		this.edges = new ArrayList<Edge>();
	}
	
	 // add a node to the graph network, the node should have a name at minimum, 
	 // and then any other data you wish to store in a node
	 
	void addNode(String name) 
	{
		Node mynode = new Node(name);
		this.nodes.add(mynode);
	}
	
	// remove the node with a given name from the graph network if it exists
	void removeNode(String name) 
	{
	    for (int i = nodes.size() - 1; i >= 0; i--) 
	    {
	        if (nodes.get(i).name.equals(name)) 
	        {
	            nodes.remove(i);
	        }
	    }
	}
	
	// returns the Node object with a given name from the graph network if it exists. Returns null otherwise
	Node getNode(String name)
	{
		for (int i = 0; i < nodes.size(); i++) 
	    {
	        if (nodes.get(i).name.equals(name)) 
	        {
	            return nodes.get(i);
	        }
	    }
		return null;

	}
	
	//connect two nodes with given names together if they exist
	
	void addEdge(String node1, String node2) {
        // check if they exist
        Node n1 = getNode(node1);
        Node n2 = getNode(node2);
        
        if (n1 == null) {
            System.out.println("node " + node1 + " doesn't exist!");
            return;
        }
        if (n2 == null) {
            System.out.println("node " + node2 + " doesn't exist!");
            return;
        }
        
        Edge myedge = new Edge(n1, n2); 
        this.edges.add(myedge);
    }
	
	/**
     * Returns true if there is an edge from node1 to node2, false otherwise
     */
    boolean hasEdge(String node1, String node2) {
        // Find the actual Node objects first
        Node n1 = getNode(node1);
        Node n2 = getNode(node2);
        
        // If either node doesn't exist, there can't be an edge
        if (n1 == null || n2 == null) {
            return false;
        }
        
        // Check all edges to see if any connect these two nodes
        for (Edge edge : edges) {
            // Check both directions (node1->node2 OR node2->node1)
            // Depending on if your graph is directed or undirected
            if ((edge.node1.name.equals(node1) && edge.node2.name.equals(node2)) ||
                (edge.node1.name.equals(node2) && edge.node2.name.equals(node1))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Remove the edge between the given nodes if one exists
     */
    void removeEdge(String node1, String node2) {
        // We need to iterate from the end to avoid index shifting when removing
        for (int i = edges.size() - 1; i >= 0; i--) {
            Edge edge = edges.get(i);
            
            // Check both directions (undirected graph)
            if ((edge.node1.name.equals(node1) && edge.node2.name.equals(node2)) ||
                (edge.node1.name.equals(node2) && edge.node2.name.equals(node1))) {
                edges.remove(i);
                // If you want to remove only one edge, add: break;
            }
        }
    }
    
    /**
     * Display all nodes and edges in a readable way
     */
    void printStructure() {
        System.out.println("=== GRAPH STRUCTURE ===");
        
        // Print all nodes
        System.out.println("Nodes (" + nodes.size() + "):");
        for (int i = 0; i < nodes.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + nodes.get(i).name);
        }
        
        // Print all edges
        System.out.println("\nEdges (" + edges.size() + "):");
        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);
            System.out.println("  " + (i + 1) + ". " + edge.node1.name + " <-> " + edge.node2.name);
        }
        
        // Alternative: Print connections per node
        System.out.println("\nConnections:");
        for (Node node : nodes) {
            System.out.print("  " + node.name + " -> ");
            boolean hasConnections = false;
            
            for (Edge edge : edges) {
                if (edge.node1.name.equals(node.name)) {
                    System.out.print(edge.node2.name + " ");
                    hasConnections = true;
                } else if (edge.node2.name.equals(node.name)) {
                    System.out.print(edge.node1.name + " ");
                    hasConnections = true;
                }
            }
            
            if (!hasConnections) {
                System.out.print("(no connections)");
            }
            System.out.println();
        }
        System.out.println("=======================");
    }
	
	
}
 
class Node {
    // Your code here
	String name;
	
	
	 Node(String name)
	{
		this.name = name;	
	}
	
}


// Any additional classes here
class Edge {
    Node node1;
    Node node2;
    
    Edge(Node node1, Node node2) {
        this.node1 = node1;
        this.node2 = node2;
    }
}


/*
Testing code for Activity 11 of CompX
*/
class GraphBuilder {
public static void main(String[] args){
	Graph graph = new Graph();
    
    // Add nodes
    graph.addNode("A");
    graph.addNode("B");
    graph.addNode("C");
    
    // Add edges
    graph.addEdge("A", "B");
    graph.addEdge("B", "C");
    
    // Test the new methods
    System.out.println("Edge A-B exists: " + graph.hasEdge("A", "B")); // true
    System.out.println("Edge A-C exists: " + graph.hasEdge("A", "C")); // false
    
    // Print structure
    graph.printStructure();
    
    // Remove an edge
    graph.removeEdge("A", "B");
    System.out.println("After removal - Edge A-B exists: " + graph.hasEdge("A", "B")); // false
    
    // Print structure again
    graph.printStructure();
}
}
