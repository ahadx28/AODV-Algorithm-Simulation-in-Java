import javax.swing.*;
import java.awt.*;
import java.util.*;

public class GraphDisplay extends JPanel {
    GUI gui;
    Graph graph;
    public GraphDisplay(GUI gui, Graph graph) {
        this.gui = gui;
        this.graph = graph;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        // Calling the superclass JPanel method to clear the panel and ensure proper repainting
        super.paintComponent(graphics);

        // A map to hold colors assigned to each node based on its role (final, intermediary, or default)
        Map<Node, Color> nodeColors = new HashMap<>();

        // Determining the color for each node based on its presence in highlighted or intermediary paths
        for (Node node : graph.getAllNodes()) {
            boolean isFinal = gui.highlightedPath != null && gui.highlightedPath.contains(node);
            boolean isIntermediary = gui.intermediaryPath != null && gui.intermediaryPath.contains(node);

            if (isFinal) {
                nodeColors.put(node, Color.GREEN); // Node is part of the final path
            } else if (isIntermediary) {
                nodeColors.put(node, Color.BLUE);  // Node is part of an intermediary path
            } else {
                nodeColors.put(node, Color.LIGHT_GRAY); // Default color for all other nodes
            }
        }

        // Drawing edges between nodes
        for (Edge edge : graph.getEdges()) {
            Node source = edge.getSource();
            Node destination = edge.getDestination();

            // Retrieving the colors of the source and destination nodes
            Color sourceColor = nodeColors.get(source);
            Color destColor = nodeColors.get(destination);

            // Setting edge color based on if both nodes are in the same path type
            if (Color.GREEN.equals(sourceColor) && Color.GREEN.equals(destColor)) {
                graphics.setColor(Color.GREEN); // Both ends are in the final path
            } else if (Color.BLUE.equals(sourceColor) && Color.BLUE.equals(destColor)) {
                graphics.setColor(Color.BLUE);  // Both ends are in an intermediary path
            } else {
                graphics.setColor(Color.BLACK); // Default edge color
            }

            // Drawing the edge as a line between source and destination
            graphics.drawLine(source.x, source.y, destination.x, destination.y);
        }

        // Drawing the nodes after drawing edges so nodes appear on top
        for (Node node : graph.getAllNodes()) {
            Color nodeColor = nodeColors.get(node);
            graphics.setColor(nodeColor);

            // Filling an oval to represent the node
            graphics.fillOval(node.x - 15, node.y - 15, 30, 30);

            // Drawing the border of the node circle in black
            graphics.setColor(Color.BLACK);
            graphics.drawOval(node.x - 15, node.y - 15, 30, 30);

            // Drawing the node identifier text centered inside the circle
            FontMetrics metrics = graphics.getFontMetrics(graphics.getFont());
            int stringWidth = metrics.stringWidth(node.identifier);
            int stringHeight = metrics.getHeight();
            int textX = node.x - stringWidth / 2;
            int textY = node.y + stringHeight / 4;

            graphics.drawString(node.identifier, textX, textY);
        }
    }


    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getWidth(), getHeight());
    }
}