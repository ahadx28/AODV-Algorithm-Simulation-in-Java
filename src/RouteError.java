public class RouteError {

    Node destination;
    int destSequenceNum;

    public RouteError(Node destination, int destSequenceNum) {
        this.destSequenceNum = destSequenceNum;
        this.destination = destination;
    }
}
