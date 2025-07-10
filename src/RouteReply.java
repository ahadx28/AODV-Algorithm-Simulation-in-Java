public class RouteReply {

    Node source;
    Node destination;
    int destSequenceNum;
    int hopCount;
    int broadcastID;


    public RouteReply(Node source, Node destination, int destSequenceNum, int broadcastID, int hopCount) {
        this.source = source;
        this.destination = destination;
        this.broadcastID = broadcastID;
        this.hopCount = hopCount;
        this.destSequenceNum = destSequenceNum;
    }

    @Override
    public String toString() {
        return "Source: " + source + " Destination: " + destination + " Destination Sequence Number: " + destSequenceNum + " Hop Count: " + hopCount + " Broadcast ID: " + broadcastID;
    }
}