import java.util.Objects;

public class RouteRequest {

    Node source;
    Node destination;
    int sourceSequenceNum;
    int destSequenceNum;
    int hopCount;
    int broadcastID;

    public RouteRequest(Node source, Node destination, int sourceSequenceNum, int destSequenceNum, int broadcastID) {

        this.source = source;
        this.destination = destination;
        this.sourceSequenceNum = sourceSequenceNum;
        this.destSequenceNum = destSequenceNum;
        this.broadcastID = broadcastID;
        this.hopCount = 0;
    }

    @Override
    public boolean equals(Object object) {
        if(!(object instanceof RouteRequest routeRequest)) {
            return false;
        }
        return (this.broadcastID == routeRequest.broadcastID && this.source.equals(routeRequest.source));
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.broadcastID, this.source);
    }

    @Override
    public String toString() {
        return "Source: " + source + " Destination: " + destination + " Source Sequence Number: " + sourceSequenceNum + " Destination Sequence Number: " + destSequenceNum + " Hop Count: " + hopCount + " Broadcast ID: " + broadcastID;
    }

}
