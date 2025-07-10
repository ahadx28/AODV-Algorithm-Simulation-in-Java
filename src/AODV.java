import java.util.ArrayList;
import java.util.*;
import javax.swing.*;

public class AODV {

    private Map<Integer, RouteRequest> routeRequests;
    private int broadcastId;
    private Graph graph;
    private GUI gui;

    public AODV(Graph graph, GUI gui) {
        broadcastId = 0;
        routeRequests = new HashMap<>();
        this.graph = graph;
        this.gui = gui;
    }

    public String findRoute(Node source, Node destination) {

        // Checking if source already has a path to desyination

        if(source.getForwardPathTo(destination) != null) {
            log(source + " already has a fresh route to " + destination);
            log("Data Transfer Starts from " + source + " to " + destination);
            // handling the data transfer
            return handleDataTransfer(source, destination, null);
        }

        RouteRequest routeRequest = getRouteRequest(source, destination);

        if(routeRequest == null) {
            log("Route Request Not found!");
            return null;
        }

        // incrementing the sequence number of the source before it starts to broadcast rreq as per aodv algorithm
        ++source.sequenceNum;

        // Adding the reverse path from source to source itself for logic implementation
        RoutingTable reversePath = new RoutingTable(source, source, 0, routeRequest.destSequenceNum);
        source.reversePath.add(reversePath);

        Node start = source;

        // Creating q queue to hold the intermediary nodes which is used for rreq flooding
        Queue<Node> intermediaryNodes = new LinkedList<>();

        // Creating an ArrayList to hold the neighbours of current node for processing the rreq
        ArrayList<Node> neighbours;

        // Creating a List of nodes to hold the nodes involved in finding the route to visualize in the graph
        List<Node> intermediaryPath = new ArrayList<>();

        log("Starting to find the route from " + source + " to " + destination);

        while(start != null) {
            neighbours = graph.getNeighboursOf(start);

            intermediaryPath.add(start);

            for(Node neighbour: neighbours) {

                // Checking if the neighbour has already processed the rreq
                if(neighbour.processedRequests.contains(routeRequest)) {
                    log(neighbour + " again received the RREQ! from " + start);

                    RoutingTable reversePathToSource = neighbour.getReversePathTo(source);

                    if(((start.hopCount+1) >= reversePathToSource.hopCount)) {
                        log(neighbour + "'s current hop count is not greater than the previously calculated");
                        log("Skipping the processing for " + neighbour);
                        continue;
                    }
                }
                else {
                    if(neighbour.equals(source)) {
                        continue;
                    }
                    log(neighbour + " received the RREQ from " + start);
                    log(neighbour + " is processing the RREQ");
                }
                intermediaryPath.add(neighbour);

                if(neighbour.getReversePathTo(source) == null) {

                    // Creating and adding the reverse path to the source to the neighbour
                    reversePath = new RoutingTable(source, start, start.hopCount+1, routeRequest.destSequenceNum);
                    neighbour.reversePath.add(reversePath);

                    log(neighbour + " recorded the reverse path to " + source);
                    log("Reverse Path: " + reversePath);
                }
                else {
                    log(neighbour + " already has a reverse path to " + source);
                    log("Reverse Path: " + neighbour.getReversePathTo(source));
                    log("Skipping adding reverse path");
                }

                // Updating the hop count of the neighbour accordingly
                neighbour.hopCount = start.hopCount+1;

                // add the rreq to the processed requests of the neighbour
                neighbour.processedRequests.add(routeRequest);

                // Checking if neighbour is the destination and then sending route reply to source
                if(neighbour.equals(destination)) {
                    log(neighbour + " equals destination!");
                    log(neighbour + " itself is sending RREP!");
                    ++neighbour.sequenceNum;
                    sendRouteReply(neighbour);
                    log("Data Transfer Starts from " + source + " to " + destination);
                    // handling the data transfer
                    return handleDataTransfer(source, destination, intermediaryPath);
                }

                // Checking if the neighbour has a path to destination through the list of forward paths it has
                RoutingTable forwardPath = neighbour.getForwardPathTo(destination);
                log("Checking if " + neighbour + " has a path to the destination " + destination + " with a greater or equal destination sequence number");
                if(forwardPath != null) {
                    if(routeRequest.destination.equals(forwardPath.destination) && routeRequest.destSequenceNum <= forwardPath.destSequenceNum) {

                        log(neighbour + " has a path to " + destination);
                        log("Validating the path!");

                        // Checking if the path has no broken links and missing nodes
                        List<String> path = getPathFromTo(neighbour, destination);

                        if(path != null) {
                            if (!(path.contains(source.toString()))) {
                                log("Path is valid!");
                                log(neighbour + " is sending RREP on behalf of " + destination);

                                // Incrementing the sequence number of the neighbour as per the aodv algorithm
                                ++neighbour.sequenceNum;
                                // Sending route reply to the source
                                sendRouteReply(neighbour);
                                log("Data Transfer Starts from " + source + " to " + destination);
                                // handling the data transfer
                                return handleDataTransfer(source, destination, intermediaryPath);
                            }
                            else {
                                log("Path is invalid!");
                            }
                        }
                    }
                }
                log(neighbour + " Forwarding the RREQ!");

                // Adding the neighbour to the intermediary node in order for it to broadcast rreq to its neighbours
                intermediaryNodes.add(neighbour);
            }

            // Getting the current node from the intermediary node
            start = intermediaryNodes.poll();
        }

        // Clearing the temporary data after the execution of the method
        clearState();

        return "DESTINATION UNREACHABLE!";
    }

    private String handleDataTransfer(Node source, Node destination, List<Node> intermediaryPath) {

        // Getting the ultimate path found by the findRoute()

        String pathDiscovered = sendData(source, destination, false);

        // Checking if the path discovery is successful
        String[] parts = pathDiscovered.split(":");
        if(parts.length == 1) {
            if (gui != null && intermediaryPath != null) {
                gui.setIntermediaryPath(intermediaryPath);
            }
            log(pathDiscovered);
            return pathDiscovered;
        }
        // Handling the missing edge route error
        else if(pathDiscovered.contains("missing edge")) {
            Node current = graph.getNode(parts[1]);
            Node nextHop = graph.getNode(parts[2]);
            handleRouteError(source, destination, current, nextHop);
            String recoveredPath = findRoute(source, destination);
            if (gui != null) {
                gui.setIntermediaryPath(intermediaryPath);
            }
            return recoveredPath;
        }

        // Handling missing node route error
        else {
            String nextHop = parts[1];
            handleRouteError(nextHop);
            String recoveredPath = findRoute(source, destination);
            if (gui != null) {
                gui.setIntermediaryPath(intermediaryPath);
            }
            return recoveredPath;
        }
    }

    // Helper method to validate the path
    private List<String> getPathFromTo(Node source, Node destination) {
        List<String> path = new ArrayList<>();

        Node current = source;
        path.add(current.toString());

        while(!(current.equals(destination))) {
            if(current.getForwardPathTo(destination) == null) {
                log("Broken Link Detected!");
                log("Path is invalid!");
                source.removeForwardPathTo(destination);
                return null;
            }
            else if(!graph.containsNode(current.getForwardPathTo(destination).nextHop)) {
                log("Missing Node Detected!");
                log("Path is invalid!");
                source.removeForwardPathTo(destination);
                return null;
            }
            else if(!graph.containsEdge(new Edge(current, current.getForwardPathTo(destination).nextHop))) {
                log("Broken Link Detected!");
                log("Path is invalid!");
                log(source + " " + destination + " " + current);

                // sending the route error msg and removing the broken path
                sendRouteErrorMessageTo(source, destination, current, "non-established");
                return null;
            }
            path.add(current.getForwardPathTo(destination).nextHop.toString());
            current = current.getForwardPathTo(destination).nextHop;
        }

        log("PATH " + path);
        return path;
    }

    private RouteRequest createRouteRequest(Node source, Node destination) {
        if((!graph.containsNode(source) || !graph.containsNode(destination)) || source.equals(destination)) {
            return null;
        }
        int destSequenceNum = 0;

        // Getting the destination sequence number from list of forward paths of the source if it has
        for(RoutingTable path: source.forwardPath) {
            if(path.destination.equals(destination)) {
                destSequenceNum = path.destSequenceNum;
            }
        }

        // Creating the route request
        RouteRequest routeRequest = new RouteRequest(source, destination, source.sequenceNum, destSequenceNum, ++broadcastId);
        routeRequests.put(broadcastId, routeRequest);
        return routeRequest;
    }

    private RouteRequest getRouteRequest(Node source, Node destination) {
        return createRouteRequest(source, destination);
    }

    private void sendRouteReply(Node sender) {
        Node source = sender.processedRequests.get(0).source;
        Node destination = sender.processedRequests.get(0).destination;
        RouteReply routeReply = createRouteReply(sender);


        log("Route Reply Transfer Starts!");
        log(sender + " is sending RREP to " + source);

        Node current;
        Node nextHop;
        int destSequenceNum;
        int hopCount;
        RoutingTable forwardPath;
        RoutingTable reversedForwardPath;
        int reversedForwardPathHopCount = sender.getReversePathTo(source).hopCount;

        // Initializing the variables if the sender is an intermediary node sending the route reply on behalf of the destination
        if(!(sender.equals(destination))) {
            current = sender.getReversePathTo(source).nextHop; // Starting from just before the sender in the path
            nextHop = sender;
            destSequenceNum = sender.getForwardPathTo(destination).destSequenceNum; // Getting the destination sequence number from the sender's forward path
            hopCount = sender.getForwardPathTo(destination).hopCount + 1; // incrementing the hop count
        }

        // Initializing the variables if the sender is the destination itself
        else {
            current = sender; // Starting from the destination
            nextHop = sender;
            hopCount = 0; // Hop count is zero
            destSequenceNum = sender.sequenceNum; // Getting the destination's sequence number
        }

        do {
            if(current.getForwardPathTo(destination) == null) {
                forwardPath = new RoutingTable(destination, nextHop, hopCount, destSequenceNum); // Creating the forward path
                routeReply.hopCount = hopCount; // Updating the hop count in route reply
                current.forwardPath.add(forwardPath);
                log(current + " recorded the forward path to " + destination);
                log("Forward Path: " + forwardPath);
            }
            else {
                log(current + " already has a forward path to " + destination);
                log("Forward Path: " + current.getForwardPathTo(destination));
                log("Skipping adding forward path to " + current);
            }
            nextHop = current;
            current = current.getReversePathTo(source).nextHop;
            if(sender.equals(destination)) {
                if(nextHop.getForwardPathTo(source) == null) {
                    reversedForwardPath = new RoutingTable(source, current, reversedForwardPathHopCount, source.sequenceNum); // Creating reversed forward path to the source for bidirectional route
                    nextHop.forwardPath.add(reversedForwardPath);
                    log(nextHop + " recorded the reversed forward path to " + source);
                    log("Reversed Forward Path: " + reversedForwardPath);
                    reversedForwardPathHopCount--;
                }
                else {
                    log(nextHop + " already has a reversed forward path to " + source);
                    log("Reversed Forward Path: " + nextHop.getForwardPathTo(source));
                    log("Skipping adding reversed forward path to " + source);
                }
            }
            ++hopCount;
        } while (!nextHop.equals(source));

        if(!(sender.equals(destination))) {
            // Sending the gratuitous reply to the destination in order for the destination to also be able to send data to the source without a separate rreq
            sendGratuitousReply(sender);
        }
        clearState();
    }

    private void sendGratuitousReply(Node sender) {
        Node source = sender.processedRequests.get(0).source;
        Node destination = sender.processedRequests.get(0).destination;

        log(sender + " is sending gratuitous reply to " + destination);

        Node current = source;
        Node nextHop = source;
        int hopCount = 0;
        RoutingTable forwardPath;

        do {
            // Adding the reversed forward paths to the nodes
            if(current.getForwardPathTo(source) == null) {
                forwardPath = new RoutingTable(source, nextHop, hopCount, source.sequenceNum);
                current.forwardPath.add(forwardPath);
                log(current + " recorded the forward path to " + source);
                log("Forward Path: " + forwardPath);
            }
            else {
                log(current + " already has a forward path to " + source);
                log("Forward Path: " + current.getForwardPathTo(source));
                log("Skipping adding forward path to " + current);
            }
            nextHop = current;
            current = current.getForwardPathTo(destination).nextHop;
            hopCount++;
        } while(!nextHop.equals(destination));
    }

    private RouteReply createRouteReply(Node sender) {

        Node source = sender.processedRequests.get(0).source;
        Node destination = sender.processedRequests.get(0).destination;
        int broadcastId = sender.processedRequests.get(0).broadcastID;
        RouteReply routeReply;

        // creating the route reply for the intermediary node to send to the source
        // Getting the destination sequence number from the forward path of sender to the destination
        if(!(sender.equals(destination))) {
            int destSequenceNum = sender.getForwardPathTo(destination).destSequenceNum;
            routeReply = new RouteReply(source, destination, destSequenceNum, broadcastId, 0);
        }

        // Creating the route reply for the destination to send to the source
        else {
            routeReply = new RouteReply(source, destination, sender.sequenceNum, broadcastId, 0);
        }

        return routeReply;
    }

    // Method for clearing temporary data
    private void clearState() {
        for(Node node: graph.getAllNodes()) {
            node.processedRequests.clear();
            node.hopCount = 0;
        }
        routeRequests.clear();
    }

    public String sendData(Node source, Node destination, boolean handleError) {
        if(source.getForwardPathTo(destination) == null) {
            log("No Route exists Between " + source + " and " + destination);
            return null;
        }
        Node current = source;
        RoutingTable forwardPath;
        String pathDiscovered = source + " -> ";
        List<Node> pathList = new ArrayList<>(); // A list of nodes to hold each node in the path for visualization in GUI
        pathList.add(source);

        while(!(current.equals(destination))) {
            forwardPath = current.getForwardPathTo(destination);

            if(forwardPath == null) {
                log("There is no route established from " + source + " to " + destination);
                log("Discover the route using 'find route' option in the dropdown!");
                break;
            }
            // Checking if the next node in the path is missing
            if(graph.containsNode(forwardPath.nextHop)) {

                // Checking if the current edge is missing
                if(graph.containsEdge(new Edge(current, forwardPath.nextHop))) {
                    if(!(forwardPath.nextHop.equals(destination))) {
                        pathDiscovered = pathDiscovered.concat(forwardPath.nextHop + " -> ");
                    }
                    else {
                        pathDiscovered = pathDiscovered.concat(forwardPath.nextHop.toString());
                    }
                    current = forwardPath.nextHop;
                    pathList.add(current);
                }
                else {
                    // Checking if it has to handle the route error
                    if(handleError) {
                        // Handling the missing edge route error if handleError is true
                        handleRouteError(source, destination, current, forwardPath.nextHop);
                    }
                    return "missing edge:" + current + ":" + forwardPath.nextHop;
                }
            }
            else {
                // Checking if it has to handle the route error
                if(handleError) {
                    // Handling the missing node route error if handleError is true
                    handleRouteError(forwardPath.nextHop.toString());
                }
                return "missing node:" + forwardPath.nextHop;
            }
        }

        if (gui != null){
            gui.setHighlightedPath(pathList);
        }

        return pathDiscovered;
    }

    // Overloaded method to handle missing edge route error
    private void handleRouteError(Node source, Node destination, Node current, Node nextHop) {
        System.out.println();
        log("ROUTE ERROR!");
        log("EDGE BETWEEN " + current + " and " + nextHop + " DOES NOT EXIST ANYMORE!");
        log(current + " IS SENDING ROUTE ERROR MESSAGE TO " + source);

        // Sending route error msg to the source
        sendRouteErrorMessageTo(source, destination, current, "established");
        log("Re-initiating the request!");
    }
    // Overloaded method to handle missing node route error
    private void handleRouteError(String nextHop) {
        System.out.println();
        log("ROUTE ERROR!");
        log(nextHop + " DOES NOT EXIST ANYMORE!");
        log("RESETTING ALL THE ROUTES!");
        resetAllRoutes(); // Clearing all the data
        log("RE-INITIATING THE ROUTE REQUEST!");
    }

    private RouteError createRouteErrorMessage(Node destination) {
        return new RouteError(destination, destination.sequenceNum);
    }

    private void sendRouteErrorMessageTo(Node source, Node destination, Node currentNode, String routeType) {
        RouteError routeError = createRouteErrorMessage(destination);
        currentNode.sequenceNum++;
        Node previous;
        Node current;
        RoutingTable nextPath;

        // Checking if the route is established via findRoute
        if(routeType.equals("established")) {

            // Checking if the route was requested to be found
            if (currentNode.getReversePathTo(source) != null) {
                previous = currentNode;
                current = currentNode.getForwardPathTo(destination).nextHop; // Starting from the next node in the path
                // Removing the forward paths to the destination and reversed forward paths to the source
                do {
                    nextPath = current.getForwardPathTo(destination);
                    current.removeForwardPathTo(destination);
                    current.removeForwardPathTo(source);

                    current = nextPath.nextHop;

                } while (!(current.equals(destination)));
                current.removeForwardPathTo(destination);
                current.removeForwardPathTo(source);

                // Going backwards and removing the forward paths to the destination and reversed forward paths to the source
                do {
                    previous.removeForwardPathTo(destination);
                    previous.removeForwardPathTo(source);
                    previous = previous.getReversePathTo(source).nextHop;
                } while (!(previous.equals(source)));
                previous.removeForwardPathTo(destination);
                previous.removeForwardPathTo(source);
            }

            // For the path that was implicitly established using reversed forward paths
            else {
                current = currentNode; // Starting from the current node
                previous = current.getForwardPathTo(destination).nextHop;

                // Removing the forward and reversed forward paths
                do {
                    nextPath = current.getForwardPathTo(source);
                    current.removeForwardPathTo(source);
                    current.removeForwardPathTo(destination);
                    current = nextPath.nextHop;
                } while (!(current.equals(source)));
                current.removeForwardPathTo(source);
                current.removeForwardPathTo(destination);

                // Going backwards and removing the forward and reversed forward paths
                do {
                    nextPath = previous.getForwardPathTo(destination);
                    previous.removeForwardPathTo(destination);
                    previous.removeForwardPathTo(source);
                    previous = nextPath.nextHop;
                } while (!(previous.equals(destination)));
                previous.removeForwardPathTo(destination);
                previous.removeForwardPathTo(source);
            }

            // Clearing all the reverse paths from every node in the graph in order to avoid cyclic paths while re-initiating the route find request
            for(Node node: graph.getAllNodes()) {
                node.reversePath.clear();
            }
        }

        // For the path an intermediary node has to the destination
        else {
            previous = currentNode; // Starting from the current node

            // Removing the forward paths
            do {
                nextPath = previous.getForwardPathTo(destination);
                previous.removeForwardPathTo(destination);
                previous = nextPath.nextHop;
            } while (!(previous.equals(destination)));
            previous.removeForwardPathTo(destination);
        }
    }

    // Method for clearing all the data
    private void resetAllRoutes() {
        for(Node node: graph.getAllNodes()) {
            node.processedRequests.clear();
            node.forwardPath.clear();
            node.reversePath.clear();
            node.hopCount = 0;
            node.sequenceNum = 0;
        }
    }

    // Method for logging routing decisions on the gui window
    private void log(String message) {
        if (gui != null) {
            SwingUtilities.invokeLater(() -> gui.appendLog(message));
        } else {
            System.out.println(message);
        }
    }

}
