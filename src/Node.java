import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;

public class Node {

    String identifier;
    int sequenceNum;
    int hopCount;
    int x;
    int y;
    ArrayList<RoutingTable> reversePath;
    ArrayList<RoutingTable> forwardPath;
    ArrayList<RouteRequest> processedRequests;

    public Node(String identifier) {
        Random random = new Random();
        this.identifier = identifier;
        this.sequenceNum = 0;
        this.hopCount = 0;
        this.x = random.nextInt(800) + 80;
        this.y = random.nextInt(400) + 50;
        this.reversePath = new ArrayList<>();
        this.forwardPath = new ArrayList<>();
        this.processedRequests = new ArrayList<>();
    }

    @Override
    public boolean equals(Object object) {
        if(!(object instanceof Node)) {
            return false;
        }
        Node node = (Node) object;
        return this.identifier.equals(node.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.identifier);
    }

    @Override
    public String toString() {
        return identifier;
    }

    public RoutingTable getReversePathTo(Node source) {
        RoutingTable reversePath = null;

        for(RoutingTable path: this.reversePath) {
            if(path.destination.equals(source)) {
                reversePath = path;
                break;
            }
        }

        return reversePath;
    }

    public RoutingTable getForwardPathTo(Node destination) {
        RoutingTable forwardPath = null;
        for(RoutingTable path: this.forwardPath) {
            if(path.destination.equals(destination)) {
                forwardPath = path;
                break;
            }
        }
        return forwardPath;
    }

    public boolean removeForwardPathTo(Node destination) {

        Iterator<RoutingTable> iterator = forwardPath.iterator();
        while(iterator.hasNext()) {
            RoutingTable path = iterator.next();
            if(path.destination.equals(destination)) {
                iterator.remove();
                return true;
            }
        }

        return false;
    }

    public boolean removeReversePathTo(Node source) {

        Iterator<RoutingTable> iterator = reversePath.iterator();
        while(iterator.hasNext()) {
            RoutingTable path = iterator.next();
            if(path.destination.equals(source)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }
}
