package com.example.nnn;

import com.example.nnn.nqueen;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloApplication extends Application {
    private GridPane boardGrid;
    private TextField nInput;
    private Button solveButton;
    private Button resetButton;
    private TextArea statusArea;
    private Label titleLabel;
    private nqueen nQueenSolver;
    private ExecutorService executor;
    private int currentN = 0;
    private VBox threadBoardsContainer;
    private FlowPane threadBoardsFlowPane;
    private java.util.Map<Integer, GridPane> threadBoardGrids = new java.util.concurrent.ConcurrentHashMap<>();
    private java.util.Map<Integer, Label> threadLabels = new java.util.concurrent.ConcurrentHashMap<>();
    private java.util.Map<Integer, VBox> threadBoardBoxes = new java.util.concurrent.ConcurrentHashMap<>();
    private int numThreads = 0;
    private Integer solutionThreadId = null;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("N-Queen Solver - Multi-threaded");

        // Main container
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: #f5f5f5;");

        // Title
        titleLabel = new Label("N-Queen Problem Solver");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        // Input section
        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER);
        Label nLabel = new Label("Number of Queens (N):");
        nLabel.setFont(Font.font(14));
        nInput = new TextField();
        nInput.setPromptText("Enter N (>= 4)");
        nInput.setPrefWidth(100);
        nInput.setText("8");

        solveButton = new Button("Solve");
        solveButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20;");
        solveButton.setOnAction(e -> startSolving());

        resetButton = new Button("Reset");
        resetButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20;");
        resetButton.setOnAction(e -> reset());
        resetButton.setDisable(true);

        inputBox.getChildren().addAll(nLabel, nInput, solveButton, resetButton);

        // Thread boards section
        VBox threadBoardsSection = new VBox(10);
        threadBoardsSection.setAlignment(Pos.CENTER);
        Label threadBoardsLabel = new Label("Thread Boards (Step-by-Step Progress)");
        threadBoardsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        threadBoardsFlowPane = new FlowPane(50, 15);
        threadBoardsFlowPane.setAlignment(Pos.CENTER);
        threadBoardsFlowPane.setPadding(new Insets(10));
        threadBoardsFlowPane.setPrefWrapLength(800);

        threadBoardsContainer = new VBox();
        threadBoardsContainer.getChildren().add(threadBoardsFlowPane);
        threadBoardsContainer.setAlignment(Pos.TOP_CENTER);

        ScrollPane threadScrollPane = new ScrollPane(threadBoardsContainer);
        threadScrollPane.setFitToWidth(true);
        threadScrollPane.setPrefSize(800, 600);
        threadScrollPane.setStyle("-fx-background-color: transparent;");

        threadBoardsSection.getChildren().addAll(threadBoardsLabel, threadScrollPane);

        // Status section
        VBox statusContainer = new VBox(10);
        Label statusLabel = new Label("Status & Thread Information");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        statusArea = new TextArea();
        statusArea.setEditable(false);
        statusArea.setPrefRowCount(8);
        statusArea.setWrapText(true);
        statusArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");

        statusContainer.getChildren().addAll(statusLabel, statusArea);

        // Add all to main container
        mainContainer.getChildren().addAll(titleLabel, inputBox, threadBoardsSection, statusContainer);

        Scene scene = new Scene(mainContainer, 1000, 1000);
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();

        // Initialize empty board
        initializeBoard(8);

        // Handle window close
        primaryStage.setOnCloseRequest(e -> {
            if (executor != null && !executor.isShutdown()) {
                executor.shutdown();
            }
        });
    }

    private void initializeBoard(int n) {
        currentN = n;
        // Clear existing thread boards
        if (threadBoardsFlowPane != null) {
            threadBoardsFlowPane.getChildren().clear();
        }
        threadBoardGrids.clear();
        threadLabels.clear();
        threadBoardBoxes.clear();
        solutionThreadId = null;
    }

    private void createThreadBoard(int threadId, int n) {
        Platform.runLater(() -> {
            // Create container for this thread's board
            VBox threadBoardBox = new VBox(5);
            threadBoardBox.setAlignment(Pos.CENTER);
            threadBoardBox.setPadding(new Insets(10));
            threadBoardBox.setStyle("-fx-background-color: #e8e8e8; -fx-border-color: #888; -fx-border-width: 2; -fx-border-radius: 5;");
            threadBoardBox.setMinWidth(550);
            threadBoardBox.setMaxWidth(250);

            // Create label for thread ID
            Label threadLabel = new Label("Thread " + threadId);
            threadLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            threadLabel.setStyle("-fx-text-fill: #2c3e50;");
            threadLabels.put(threadId, threadLabel);

            // Create grid for this thread
            GridPane threadGrid = new GridPane();
            threadGrid.setAlignment(Pos.CENTER);
            threadGrid.setHgap(2);
            threadGrid.setVgap(2);

            int cellSize = Math.max(25, Math.min(50, 400 / n));

            // Initialize empty board
            for (int row = 0; row < n; row++) {
                for (int col = 0; col < n; col++) {
                    StackPane cell = new StackPane();
                    cell.setPrefSize(cellSize, cellSize);

                    // Alternate colors for chessboard pattern
                    if ((row + col) % 2 == 0) {
                        cell.setStyle("-fx-background-color: #f0d9b5; -fx-border-color: #8b4513; -fx-border-width: 1;");
                    } else {
                        cell.setStyle("-fx-background-color: #b58863; -fx-border-color: #8b4513; -fx-border-width: 1;");
                    }

                    threadGrid.add(cell, col, row);
                }
            }

            threadBoardGrids.put(threadId, threadGrid);
            threadBoardBoxes.put(threadId, threadBoardBox);
            threadBoardBox.getChildren().addAll(threadLabel, threadGrid);
            if (threadBoardsFlowPane != null) {
                threadBoardsFlowPane.getChildren().add(threadBoardBox);
            }
        });
    }

    private void updateThreadBoard(int threadId, String[][] board) {
        Platform.runLater(() -> {
            GridPane threadGrid = threadBoardGrids.get(threadId);
            if (threadGrid == null) {
                // Create board if it doesn't exist
                createThreadBoard(threadId, board.length);
                threadGrid = threadBoardGrids.get(threadId);
            }

            threadGrid.getChildren().clear();
            int n = board.length;
            int cellSize = Math.max(25, Math.min(50, 400 / n));

            for (int row = 0; row < n; row++) {
                for (int col = 0; col < n; col++) {
                    StackPane cell = new StackPane();
                    cell.setPrefSize(cellSize, cellSize);

                    // Alternate colors for chessboard pattern
                    if ((row + col) % 2 == 0) {
                        cell.setStyle("-fx-background-color: #f0d9b5; -fx-border-color: #8b4513; -fx-border-width: 1;");
                    } else {
                        cell.setStyle("-fx-background-color: #b58863; -fx-border-color: #8b4513; -fx-border-width: 1;");
                    }

                    // Add queen if present
                    if (board[row][col].equals("Q")) {
                        Circle queen = new Circle(cellSize * 0.3);
                        queen.setFill(Color.BLACK);
                        queen.setStroke(Color.BLACK);
                        queen.setStrokeWidth(2);
                        cell.getChildren().add(queen);
                    }

                    threadGrid.add(cell, col, row);
                }
            }

            // Update thread label to show it's active
            Label threadLabel = threadLabels.get(threadId);
            if (threadLabel != null) {
                threadLabel.setText("Thread " + threadId + " (Running)");
                threadLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }
        });
    }

    private void updateBoard(int threadId, String[][] solution) {
        // Mark the thread that found the solution with green background
        Platform.runLater(() -> {
            // Only mark the first thread that finds the solution
            if (solutionThreadId == null) {
                solutionThreadId = threadId;

                // Set green background for the thread that found the solution
                VBox threadBoardBox = threadBoardBoxes.get(threadId);
                if (threadBoardBox != null) {
                    threadBoardBox.setStyle("-fx-background-color: #90EE90; -fx-border-color: #2ecc71; -fx-border-width: 3; -fx-border-radius: 5;");
                }

                // Update the label
                Label threadLabel = threadLabels.get(threadId);
                if (threadLabel != null) {
                    threadLabel.setText("Thread " + threadId + " (Solution Found!)");
                    threadLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void startSolving() {
        try {
            int n = Integer.parseInt(nInput.getText());
            if (n < 4) {
                showAlert("Invalid Input", "N must be at least 4!");
                return;
            }
            if (n > 20) {
                showAlert("Warning", "Large values of N may take a very long time to solve!");
            }

            solveButton.setDisable(true);
            resetButton.setDisable(false);
            nInput.setDisable(true);
            statusArea.clear();

            initializeBoard(n);
            statusArea.appendText("Initializing N-Queen solver for N = " + n + "...\n");

            // Create solver
            nQueenSolver = new nqueen(n);

            // Calculate number of threads that will be created
            int processors = Runtime.getRuntime().availableProcessors();
            numThreads = Math.min(processors, n);
            statusArea.appendText("Will create " + numThreads + " threads.\n");

            // Set up callbacks
            nQueenSolver.setOnSolutionFound((threadId, solution) -> {
                updateBoard(threadId, solution);
                statusArea.appendText("\n✓ SOLUTION FOUND by Thread " + threadId + "!\n");
                statusArea.appendText("Displaying the solution on the thread boards.\n");
            });

            nQueenSolver.setOnStatusUpdate(status -> {
                statusArea.appendText(status + "\n");
                // Auto-scroll to bottom
                Platform.runLater(() -> {
                    statusArea.setScrollTop(Double.MAX_VALUE);
                });
            });

            // Set up step-by-step board update callback
            nQueenSolver.setOnThreadBoardUpdate((threadId, board) -> {
                updateThreadBoard(threadId, board);
            });

            // Pre-create thread boards
            for (int i = 0; i < numThreads; i++) {
                createThreadBoard(i, n);
            }

            // Run solver in background thread
            executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    nQueenSolver.startMultiThreaded();
                    Platform.runLater(() -> {
                        solveButton.setDisable(false);
                        nInput.setDisable(false);
                        statusArea.appendText("\n=== Solving Complete ===\n");
                        // Mark all threads as finished
                        for (Integer threadId : threadLabels.keySet()) {
                            Label threadLabel = threadLabels.get(threadId);
                            if (threadLabel != null && !threadLabel.getText().contains("Solution Found")) {
                                threadLabel.setText("Thread " + threadId + " (Finished)");
                                threadLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: normal;");
                            }
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        statusArea.appendText("Error: " + e.getMessage() + "\n");
                        solveButton.setDisable(false);
                        nInput.setDisable(false);
                    });
                }
            });

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number!");
        }
    }

    private void reset() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        solveButton.setDisable(false);
        resetButton.setDisable(true);
        nInput.setDisable(false);
        statusArea.clear();
        initializeBoard(Integer.parseInt(nInput.getText()));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
