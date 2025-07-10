public class RoutingTable {

    Node destination;
    Node nextHop;
    int hopCount;
    int destSequenceNum;

    public RoutingTable(Node destination, Node nextHop, int hopCount, int destSequenceNum) {
        this.destination = destination;
        this.nextHop = nextHop;
        this.hopCount = hopCount;
        this.destSequenceNum = destSequenceNum;
    }

    @Override
    public String toString() {
        return "Destination: " + destination + " Next Hop: " + nextHop + " Hop Count: " + hopCount + " Dest Sequence Num " + destSequenceNum;
    }
}
