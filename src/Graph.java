import java.io.*;
import java.util.*;

public class Graph {

    private Map<Node, Set<Edge>> adjacencyList; // A map from node to set of edges for storing the graph

    public Graph() {
        this.adjacencyList = new HashMap<>();
    }

    // Method for adding the node
    public boolean addNode(Node node) {
        if(!adjacencyList.containsKey(node)) {
            adjacencyList.put(node, new HashSet<>());
            return true;
        }
        return false;
    }

    // Method dor adding the edge
    public boolean addEdge(Node source, Node destination) {
        if(!(adjacencyList.containsKey(source)) || !(adjacencyList.containsKey(destination))) {
            return false;
        }
        Edge edge1 = new Edge(source, destination); // Creating the edge from source to destination
        Edge edge2 = new Edge(destination, source); // Creating the edge from destination to source
        Set<Edge> sourceEdges = adjacencyList.get(source); // getting the set of edges of the source node
        Set<Edge> destinationEdges = adjacencyList.get(destination); // getting the set of edges of the destination node
        sourceEdges.add(edge1); // Adding the edge to the set of edges of the source
        destinationEdges.add(edge2);  // Adding the edge to the set of edges of the destination
        return true;
    }

    // Method for removing the edge
    public boolean removeEdge(Node source, Node destination) {
        if(!(adjacencyList.containsKey(source) && adjacencyList.containsKey(destination))) {
            return false;
        }
        Edge edge1 = new Edge(source, destination); // Creating the edge from source to destination to check if it exists
        Edge edge2 = new Edge(destination, source); // Creating the edge from destination to source to check if it exists
        Set<Edge> sourcesEdges = adjacencyList.get(source); // getting the set of edges of the source node
        Set<Edge> destinationEdges = adjacencyList.get(destination); // getting the set of edges of the destination node
        sourcesEdges.remove(edge1); // removing the edge from the set of edges of the source
        destinationEdges.remove(edge2); // removing the edge from the set of edges of the destination
        return true;
    }

    // Method for removing the node from the graph
    public boolean removeNode(Node node) {
        if(!(adjacencyList.containsKey(node))) {
            return false;
        }
        // traversing through the values of the adjacency list
        for (Set<Edge> setsOfEdges : adjacencyList.values()) {
            Iterator<Edge> singleSet = setsOfEdges.iterator(); // Creating an iterator for the set of edges
            while (singleSet.hasNext()) { // Traversing through the iterator
                Edge edge = singleSet.next(); // Getting the current edge
                if (edge.source.equals(node) || edge.destination.equals(node)) { // Checking if the edge contains the node as a source or a destination
                    singleSet.remove(); // Removing the edge if condition fulfills
                }
            }
        }
        adjacencyList.remove(node); // Removing the node finally
        return true;
    }

    // Method for getting the neighbours of a node
    public ArrayList<Node> getNeighboursOf(Node node) {
        if(node == null || (!(adjacencyList.containsKey(node)))) {
            return null;
        }

        Set<Edge> edges = adjacencyList.get(node); // Getting the set of edges of node
        ArrayList<Node> neighbours = new ArrayList<>(); // Creating an ArrayList of nodes to hold the neighbours

        // Traversing through the set of edges
        for(Edge edge: edges) {
            neighbours.add(edge.destination); // Adding the destination to the list of neighbours
        }

        return neighbours; // Returning the list of neighbours
    }

    // Method for getting a specific node from the adjacency list
    public Node getNode(String identifier) {
        for(Node node: adjacencyList.keySet()) {
            if(node.identifier.equals(identifier)) {
                return node;
            }
        }
        return null;
    }

    // Method for Generating the graph from .txt files
    public void generateGraph(String edgeFile) {
        // Wrapping the file reader for edge file around Buffered Reader to read full lines together
        try(BufferedReader buffer = new BufferedReader(new FileReader(edgeFile))) {
            String line;
            while((line = buffer.readLine()) != null) {
                String[] edge = line.split(" "); // Splitting the line with a space and adding the nodes in the array of Strings
                if(edge.length == 2) {
                    String source = edge[0]; // getting the first element in the array as the source
                    String destination = edge[1]; // getting the second element in the array as the destination

                    Node sourceNode = getNode(source); // getting the source node from the adjacency list
                    Node destinationNode = getNode(destination); // getting the destination node from the adjacency list

                    // creating the source node if it does not exist in the adjacency list
                    if(sourceNode == null) {
                        sourceNode = new Node(source);
                        addNode(sourceNode);
                    }
                    // creating the destination node if it does not exist in the adjacency list
                    if(destinationNode == null) {
                        destinationNode = new Node(destination);
                        addNode(destinationNode);
                    }

                    addEdge(sourceNode, destinationNode); // Creating the edge between the source and destination
                }
            }
        } catch(IOException e) {

        }
    }

    // Overloading the generateGraph() method so that it can also generate graphs using input stream
    // we specifically did this for .jar file otherwise, the .jar file won't be able to locate the default.txt file
    public void generateGraph(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while((line = reader.readLine()) != null) {
                String[] edge = line.split(" "); // Splitting the line with a space and adding the nodes in the array of Strings
                if(edge.length == 2) {
                    String source = edge[0]; // getting the first element in the array as the source
                    String destination = edge[1]; // getting the second element in the array as the destination

                    Node sourceNode = getNode(source); // getting the source node from the adjacency list
                    Node destinationNode = getNode(destination); // getting the destination node from the adjacency list

                    // creating the source node if it does not exist in the adjacency list
                    if(sourceNode == null) {
                        sourceNode = new Node(source);
                        addNode(sourceNode);
                    }
                    // creating the destination node if it does not exist in the adjacency list
                    if(destinationNode == null) {
                        destinationNode = new Node(destination);
                        addNode(destinationNode);
                    }

                    addEdge(sourceNode, destinationNode); // Creating the edge between the source and destination
                }
            }
        }
    }

    public void displayGraph() {
        for (Map.Entry<Node, Set<Edge>> node : adjacencyList.entrySet()) {
            System.out.print(node.getKey().identifier + " -> [ ");
            for (Edge edge : node.getValue()) {
                System.out.print(edge.source.identifier + edge.destination.identifier + " ");
            }
            System.out.print("]");
            System.out.println();
        }
    }

    // method for retrieving all the nodes in the adjacency list
    public Set<Node> getAllNodes() {
        return adjacencyList.keySet();
    }

    // method for checking if a specific node exists in the adjacency list
    public boolean containsNode(Node node) {
        return adjacencyList.containsKey(node);
    }

    // method for checking if a specific edge exists in the adjacency list
    public boolean containsEdge(Edge edge) {
        for (Set<Edge> setsOfEdges : adjacencyList.values()) {
            if(setsOfEdges.contains(edge)) {
                return true;
            }
        }
        return false;
    }

    // Method for getting all the nodes included in a path
    public List<Node> pathToNodeList(String path) {
        List<Node> list = new ArrayList<>();
        for (String identifier : path.split("->")) { // Traversing through the array of Strings created by splitting the path with ->
            Node node = getNode(identifier.trim()); // getting the node
            if (node != null) {
                list.add(node); // Adding the node to the list
            }
        }
        return list;
    }

    // Method for getting all the edges in the adjacency list for coloring the relevant ones in GraphDisplay class
    public Set<Edge> getEdges() {
        Set<Edge> uniqueEdges = new HashSet<>(); // For storing the unique edges
        Set<String> visitedPairs = new HashSet<>(); // For avoid edges that have been added to unique edges

        for (Set<Edge> edgeSet : adjacencyList.values()) { // Traversing through the values of adjacency lost
            for (Edge edge : edgeSet) { // traversing through the edges in the current set of edges
                String direct = edge.source.identifier + "-" + edge.destination.identifier; // creating a string to represent the edge from source to destination
                String reverse = edge.destination.identifier + "-" + edge.source.identifier; // creating a string to represent the edge from destination to source
                // Checking if direct and reverse edges have been added to the unique set of edges
                if (!visitedPairs.contains(direct) && !visitedPairs.contains(reverse)) {
                    uniqueEdges.add(edge); // Adding the edge to unique edges
                    visitedPairs.add(direct); // Adding the direct edge to visited pairs
                }
            }
        }

        return uniqueEdges;
    }

    // Method for clearing the adjacency list executed when a new graph is generated
    public void clearCurrentGraph() {
        adjacencyList.clear();
    }
}
