import java.util.Objects;

public class Edge {

    Node source;
    Node destination;

    public Edge(Node source, Node destination) {
        this.source = source;
        this.destination = destination;
    }

    public Node getSource(){
        return source;
    }

    public Node getDestination(){
        return destination;
    }

    @Override
    public boolean equals(Object object) {
        if(!(object instanceof  Edge)) {
            return false;
        }
        Edge edge = (Edge) object;

        return (this.source.equals(edge.source) && this.destination.equals(edge.destination));
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.source, this.destination);
    }
    @Override
    public String toString() {
        return source + " -> " + destination;
    }
}
