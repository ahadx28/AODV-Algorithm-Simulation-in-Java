import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.util.List;

public class GUI extends JFrame {
    private Graph graph;
    private AODV aodv;
    private GraphDisplay graphDisplay;

    JTextPane logArea;
    public JComboBox<String> operationsDropdown;
    public JPanel pathContainer;
    public List<Node> highlightedPath; // List of nodes for the nodes in the path to visualize
    public List<Node> intermediaryPath; // List of nodes for the nodes in the intermediary path to visualize

    public GUI() {
        super("AODV Routing Simulator"); // Setting the title of the GUI window
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Setting the default behavior of close button action to terminate the program when clicked
        setSize(1080, 720); // Setting the size of the GUI window
        setLocationRelativeTo(null); // Setting the location of the window to the centre of the screen
        setLayout(new BorderLayout()); // Setting the layout to border layout

        graph = new Graph();
        aodv = new AODV(graph, this);
        graphDisplay = new GraphDisplay(this, graph);
        highlightedPath = null;
        intermediaryPath = null;

        setupUI();
        setVisible(true);
    }

    // Method to reset all the variables when a new graph is generated
    public void resetGraphView() {
        highlightedPath = null;
        intermediaryPath = null;
        pathContainer.removeAll();
        pathContainer.revalidate();
        pathContainer.repaint();
        graph.clearCurrentGraph();
        graphDisplay.repaint();

    }

    // Method to define the structure of the GUI
    private void setupUI() {
        logArea = new JTextPane(); // Initializing the log area for decision logs
        logArea.setEditable(false); // Making the log area non-editable
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 16)); // Setting the font
        JScrollPane logScroll = new JScrollPane(logArea); // Adding the log area to the JScrollPane in order to ame the log area scrollable
        logScroll.setPreferredSize(new Dimension(900, 150)); // Setting the size of the log scroll

        pathContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10)); // Initializing the path container to display the path found in green boxes
        pathContainer.setBorder(BorderFactory.createTitledBorder("Discovered Route"));

        graphDisplay.setPreferredSize(new Dimension(1000, 800)); // Setting the size of the graph displayed on the graph

        operationsDropdown = new JComboBox<>(new String[]{ // Creating the dropdown for the options
                "Choose Operation", "Find Route", "Send Data", "Add Node", "Add Edge", "Remove Node", "Remove Edge"
        });

        operationsDropdown.addActionListener(e -> { // Adding the action listener to the dropdown
            handleOperation((String) operationsDropdown.getSelectedItem());
            operationsDropdown.setSelectedIndex(0);
        });

        JButton defaultGraphButton = new JButton("Default Graph"); // Button for generating the graph from default.txt file
        JButton customGraphButton = new JButton("Custom Graph"); // Button for generating the custom graph

        defaultGraphButton.addActionListener(e -> {
            resetGraphView(); // Reset GUI data first

            // creating an input stream for default.txt
            // we are doing this so .jar file will be able to locate the default.txt file
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("Graph/default.txt")) {
                if (inputStream == null) {
                    appendLog("Could not load default graph — file not found in resources.");
                    return;
                }

                graph.generateGraph(inputStream); // Update generateGraph() to accept InputStream
                appendLog("DEFAULT GRAPH GENERATED!");
                graphDisplay.repaint(); // Displaying the graph

            } catch (IOException ex) {
                appendLog("Error reading default graph: " + ex.getMessage());
            }
        });


        customGraphButton.addActionListener(e -> { // Adding the action listener to the customGraphButton
            resetGraphView(); // Resting the GUI data first
            createCustomGraphFromInput();
            appendLog("CUSTOM GRAPH GENERATED!");
            graphDisplay.repaint(); // Displaying the graph
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10)); // Bottom panel for dropdown, default and custom graph buttons
        bottom.add(operationsDropdown);
        bottom.add(defaultGraphButton);
        bottom.add(customGraphButton);

        JTabbedPane tabbedPane = new JTabbedPane(); // Creating a structure with two separate tabs

        JPanel graphTab = new JPanel(new BorderLayout()); // Creating a separate tab for graph display
        graphTab.add(graphDisplay, BorderLayout.CENTER);
        graphTab.add(pathContainer, BorderLayout.NORTH);
        tabbedPane.addTab("Graph View", graphTab);

        JPanel logTab = new JPanel(new BorderLayout()); // creating a separate tab for displaying logs
        logTab.add(logScroll, BorderLayout.CENTER);
        tabbedPane.addTab("Logs", logTab);

        add(tabbedPane, BorderLayout.CENTER);

        // Creating a control panel at the bottom that is visible in both the tabs
        JPanel bottomControlContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottomControlContainer.add(operationsDropdown);
        bottomControlContainer.add(defaultGraphButton);
        bottomControlContainer.add(customGraphButton);
        add(bottomControlContainer, BorderLayout.SOUTH);

    }

    // Method for creating custom graph using user input for edges
    private void createCustomGraphFromInput() {
        try {
            // Using a writable, machine-independent location for .jar file
            String userHome = System.getProperty("user.home");
            File customGraphFile = new File(userHome, "custom.txt");

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(customGraphFile))) {
                int repeat;
                do {
                    String edge = "";
                    while (true) {
                        // taking the edges for the graph as input from the user
                        edge = JOptionPane.showInputDialog(this, "Enter the edge!");
                        if (edge == null) return;

                        // validating the user input
                        String[] nodes = edge.trim().split("\\s+");
                        if (nodes.length != 2) {
                            JOptionPane.showMessageDialog(this, "Enter two nodes with a space between them!");
                            continue;
                        }

                        String source = nodes[0].trim();
                        String destination = nodes[1].trim();

                        if (source.length() != 1 || destination.length() != 1) {
                            JOptionPane.showMessageDialog(this, "Use single character for a node!");
                            continue;
                        }

                        writer.write(source.toUpperCase() + " " + destination.toUpperCase());
                        writer.newLine();
                        break;
                    }

                    // Asking the suer if they want to enter another edge
                    int choice = JOptionPane.showConfirmDialog(this, "Add another edge?", "Continue?",
                            JOptionPane.YES_NO_OPTION);
                    repeat = (choice == JOptionPane.YES_OPTION) ? 1 : 0;

                } while (repeat == 1);
            }

            // Generate graph from the path where we saved the file
            graph.generateGraph(customGraphFile.getAbsolutePath());
            appendLog("CUSTOM GRAPH GENERATED!");

        } catch (IOException ex) {
            appendLog("Error writing the edges for custom graph: " + ex.getMessage());
        }
    }

    public void appendLog(String message) {

        StyledDocument styledDocument = logArea.getStyledDocument(); // Getting the styled text document for styling the logs
        StyleContext styleContext = new StyleContext(); // Creating a separate container for managing text styles to a part of text in style document
        AttributeSet normal = styleContext.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Bold, false); // creating a non-bold style
        AttributeSet bold = styleContext.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Bold, true); // Creating a bold style

        // Checking if the the message has these strings and making them bold
        if (message.startsWith("Forward Path:") || message.startsWith("Reverse Path:") || message.startsWith("Reversed Forward Path:")) {
            try {
                String boldText;
                if (message.startsWith("Forward Path:")) {
                    boldText = "Forward Path:";
                }
                else if(message.startsWith("Reverse Path:")) {
                    boldText = "Reverse Path:";
                }
                else {
                    boldText = "Reversed Forward Path:";
                }

                String remainingText = message.substring(boldText.length());
                styledDocument.insertString(styledDocument.getLength(), boldText, bold); // Making the boldText Strings bold
                styledDocument.insertString(styledDocument.getLength(), remainingText + "\n", normal); // Letting the remaining text remain normal/non-bold
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            logArea.setCaretPosition(styledDocument.getLength());
            return;
        }
        // Getting the node identifiers for making them bold
        String[] words = message.split("(?<=\\s)|(?=\\s)|(?<=\\W)|(?=\\W)");
        for (String word : words) {
            String trimmed = word.replaceAll("\\W", "");
            AttributeSet style = graph.containsNode(new Node(trimmed)) ? bold : normal;
            try {
                styledDocument.insertString(styledDocument.getLength(), word, style);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

        try {
            styledDocument.insertString(styledDocument.getLength(), "\n", normal);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        logArea.setCaretPosition(styledDocument.getLength());
    }

    // method for handling dropdown click event
    private void handleOperation(String operation) {
        switch(operation) {
            case "Find Route":
                handleFindRoute();
                break;
            case "Send Data":
                handleSendData();
                break;
            case "Add Node":
                handleAddNode();
                break;
            case "Remove Node":
                handleRemoveNode();
                break;
            case "Add Edge":
                handleAddEdge();
                break;
            case "Remove Edge":
                handleRemoveEdge();
                break;
        }
    }

    private void handleFindRoute() {
        Node source = getNodeInput("Enter Source Node!"); // getting the source node with helper method getNodeInput()
        Node destination = getNodeInput("Enter Destination Node!"); // getting the destination node with helper method getNodeInput()

        if (source != null && destination != null) {
            String path = aodv.findRoute(source, destination); // Getting the path

            displayRoute(path); // Displaying the path in green blocks

            if (path != null && !path.isEmpty() && !path.equals("DESTINATION UNREACHABLE!")) {
                setHighlightedPath(graph.pathToNodeList(path)); // Highlighting the nodes in the path in the graph
            } else {
                SwingUtilities.invokeLater(() -> appendLog("DESTINATION UNREACHABLE!"));
                setHighlightedPath(null);
            }
        }
    }


    private void handleSendData() {
        Node source = getNodeInput("Enter Source Node!"); // getting the source node with helper method getNodeInput()
        Node destination = getNodeInput("Enter Destination Node!"); // getting the destination node with helper method getNodeInput()

        if (source != null && destination != null) {
            appendLog("DATA TRANSFER START FROM " + source + " TO " +destination);
            String path = aodv.sendData(source, destination, true); // Getting the path

            if(path != null) {
                displayRoute(path); // Displaying the path in green blocks

                if ((!(path.contains("missing"))) && !path.isEmpty() && !path.equals("DESTINATION UNREACHABLE!")) {
                    setHighlightedPath(graph.pathToNodeList(path)); // Highlighting the nodes in the path in the graph
                    SwingUtilities.invokeLater(() -> appendLog(path));
                } else {
                    String recoveredPath = aodv.findRoute(source, destination); // Recovering the path
                    displayRoute(recoveredPath); // Displaying the recovered path in green blocks

                    if ((!(recoveredPath.contains("missing"))) && !recoveredPath.isEmpty() && !recoveredPath.equals("DESTINATION UNREACHABLE!")) {
                        setHighlightedPath(graph.pathToNodeList(recoveredPath)); // Highlighting the nodes in the recovered path in the graph
                    } else {
                        SwingUtilities.invokeLater(() -> appendLog("DESTINATION UNREACHABLE!"));
                        setHighlightedPath(null);
                    }
                }
            }
        }
    }

    private void handleAddNode() {
        String identifier = JOptionPane.showInputDialog(this, "Enter node identifier!"); // Getting the identifier of the node
        if (identifier != null && !identifier.isEmpty()) {
            Node node = new Node(identifier.trim().toUpperCase()); // Creating the node
            if (graph.addNode(node)) {
                appendLog("Node " + node + " added!");
                graphDisplay.repaint(); // Updating the graph with new node
                setIntermediaryPath(null);
                setHighlightedPath(null);
                displayRoute(null);
            } else {
                appendLog("Error while adding the node!");
            }
        }
    }

    private void handleRemoveNode() {
        Node node = getNodeInput("Enter node to remove!"); // Getting the node to be removed using helper method getNodeInput()
        if (node != null) {
            if (graph.removeNode(node)) { // Removing the node
                appendLog("Node " + node + " removed!");
                graphDisplay.repaint(); // Updating the graph with removed node
                setIntermediaryPath(null);
                setHighlightedPath(null);
                displayRoute(null);
            } else {
                appendLog("Error removing the node!");
            }
        }
    }

    private void handleAddEdge() {
        Node source = getNodeInput("Enter source node!"); // getting the source node with helper method getNodeInput()
        Node destination = getNodeInput("Enter destination node!"); // getting the destination node with helper method getNodeInput()
        if (source != null && destination != null) {
            if (graph.addEdge(source, destination)) { // Adding the edge to the graph
                appendLog("Edge added: " + source + " -> " + destination);
                graphDisplay.repaint(); // Updating the graph with new edge
                setIntermediaryPath(null);
                setHighlightedPath(null);
                displayRoute(null);
            } else {
                appendLog("Error while adding the edge!");
            }
        }
    }

    private void handleRemoveEdge() {
        Node source = getNodeInput("Enter source node!"); // getting the source node with helper method getNodeInput()
        Node destination = getNodeInput("Enter destination node!");  // getting the destination node with helper method getNodeInput()
        if (source != null && destination != null) {
            if (graph.removeEdge(source, destination)) { // Removing the edge from the graph
                appendLog("Edge removed: " + source + " -> " + destination);
                graphDisplay.repaint(); // Updating the graph with removed edge
                setIntermediaryPath(null);
                setHighlightedPath(null);
                displayRoute(null);
            } else {
                appendLog("Error while removing the edge!");
            }
        }
    }

    // A helper method for getting nodes for other methods
    private Node getNodeInput(String message) {
        String identifier = JOptionPane.showInputDialog(this, message);
        if (identifier != null && !identifier.isEmpty()) {
            Node node = graph.getNode(identifier.trim().toUpperCase());
            if (graph.containsNode(node)) {
                return node;
            }
            else {
                appendLog("Node '" + identifier + "' not found!");
            }
        }
        return null;
    }

    public void displayRoute(String path) {
        pathContainer.removeAll(); // Resetting the path container first

        if (path == null || path.isEmpty()) {
            // Resetting the view
            pathContainer.revalidate();
            pathContainer.repaint();
            return;
        }

        if (path.equals("DESTINATION UNREACHABLE!")) { // If the path reads Destination Unreachable, creating a lable ffor it to display on graph tab
            JLabel unreachableLabel = new JLabel("DESTINATION UNREACHABLE!");
            unreachableLabel.setOpaque(true);
            unreachableLabel.setBackground(Color.RED);
            unreachableLabel.setForeground(Color.WHITE);
            unreachableLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            unreachableLabel.setHorizontalAlignment(SwingConstants.CENTER);
            unreachableLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            unreachableLabel.setPreferredSize(new Dimension(250, 40));

            pathContainer.add(unreachableLabel);
            pathContainer.revalidate();
            pathContainer.repaint();
            return;
        }

        String[] nodes = path.split("->"); // Getting the nodes in the path in the array

        // Creating green boxes for the nodes in the graph and adding the identifier at the centre
        for (int i = 0; i < nodes.length; i++) {
            String node = nodes[i].trim();
            JLabel nodeLabel = new JLabel(node);
            nodeLabel.setOpaque(true);
            nodeLabel.setBackground(Color.GREEN);
            nodeLabel.setForeground(Color.BLACK);
            nodeLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            nodeLabel.setHorizontalAlignment(SwingConstants.CENTER);
            nodeLabel.setPreferredSize(new Dimension(40, 40));

            pathContainer.add(nodeLabel); // Adding the box to the path container

            if (i < nodes.length - 1) { // Creating the arrow to show from-to
                JLabel arrow = new JLabel("→");
                arrow.setFont(new Font("SansSerif", Font.BOLD, 16));
                arrow.setHorizontalAlignment(SwingConstants.CENTER);
                pathContainer.add(arrow);
            }
        }

        // Resetting the path container view
        pathContainer.revalidate();
        pathContainer.repaint();
    }


    // Method for highlighting the nodes in the path in the graph
    public void setHighlightedPath(List<Node> path) {
        this.highlightedPath = path;
        graphDisplay.repaint();
    }
    // Method for highlighting the nodes in the intermediary path in the graph
    public void setIntermediaryPath(List<Node> path) {
        this.intermediaryPath = path;
        graphDisplay.repaint();
    }
}
