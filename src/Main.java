import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class NQueen{
    Scanner input = new Scanner(System.in);

    private static int N;
    private List<String[][]> solutions = Collections.synchronizedList(new ArrayList<>());
    private Semaphore threadSemaphore;
    private int currentRow = 0;
    // 3l4an lma kol thread y3ml update kolo y4of fe nfs el w2t
    private AtomicBoolean solutionFound = new AtomicBoolean(false);


    NQueen (int N){
        this.N = N;
//    Board = new String[N][N];
//    for(int i = 0 ; i<N ; i++)
//        {
//        for(int j = 0 ; j<N ; j++)
//            {
//                Board[i][j] = "0";
//            }
//        System.out.println();
//        }
    }
    public static void PrintBoard(String[][] board) {
        for (String[] row : board) {
            for (String cell : row) System.out.print(cell + " ");
            System.out.println();
        }
    }

    public static boolean IsSafe(String[][] board, int row, int col) {
        // Check left of the row
        for(int i = col-1; i >= 0; i--) {
            if(board[row][i].equals("Q")) {
                return false;
            }
        }

        // Check above of the column
        for(int r = row-1; r >= 0; r--) {
            if(board[r][col].equals("Q")) {
                return false;
            }
        }

        // Check left upper diagonal
        int r1 = row-1;
        int c1 = col-1;
        while(r1 >= 0 && c1 >= 0) {
            if(board[r1][c1].equals("Q")) {
                return false;
            }
            r1--;
            c1--;
        }

        // Check left lower diagonal
        int r2 = row+1;
        int c2 = col-1;
        while(r2 < N && c2 >= 0) {
            if(board[r2][c2].equals("Q")) {
                return false;
            }
            r2++;
            c2--;
        }

        return true;
    }
    // public static void Play()
    // {
    //     Scanner input = new Scanner(System.in);
    //     String place = new String();
    //     int i = 0;
    //     int row , col;
    //     System.out.println("Welcome to NQueen Game - _ -");
    //     System.out.println("the initial Board is: ");
    //     PrintBoard();
    //     System.out.println("Now We will start the Game please the index like matrix to put 'Q' on this place");
    //     System.out.println("Ex. to index the first place enter col is '0' and row is '0'...... ");
    //     while(i < N)
    //     {
    //         System.out.format("Enter tha place to put the 'Q':");
    //         place = input.next();
    //         row = place.charAt(0) - '0';
    //         col = place.charAt(1) - '0';
    //         if(IsSafe(NQueen.Board , col , row))
    //         {
    //             Board[row][col] = String.valueOf('Q');
    //             System.out.println("AMAZING\nNow the Board is: ");
    //             PrintBoard();
    //             i++;
    //         }
    //         else
    //         {
    //             System.out.println("invalid input you can't enter 'Q' here!!!\n Try Again");
    //         }

    //     }

    //     System.out.println("the final Board (Solution) is:");
    //     PrintBoard();
    //     System.out.println("Good Bye -_-");

    // }



    public void backtrackingSolve(String[][] board, int col, int threadId) {
        if (solutionFound.get()) return;

        if (col >= N) {
            String[][] sol = new String[N][N];
            for (int i = 0; i < N; i++) sol[i] = board[i].clone();
            solutions.add(sol);
            solutionFound.set(true);
            return;
        }

        for (int row = 0; row < N; row++) {
            if (solutionFound.get()) return;
            if (IsSafe(board, row, col)) {
                board[row][col] = "Q";
                backtrackingSolve(board, col + 1, threadId);
                board[row][col] = "0";
            }
        }
    }

    class Worker extends Thread {
        int threadId;
        private boolean working = true;

        Worker(int id) {
            this.threadId = id;
        }

        @Override
        public void run() {
            System.out.println("Thread " + threadId + " started");

            while (working) {
                try {
                    threadSemaphore.acquire();

                    int rowToProcess = -1;
                    //to prevent Race Condition
                    synchronized (NQueen.this) {
                        if (solutionFound.get() || currentRow >= N) {
                            working = false;
                            threadSemaphore.release();
                            break;
                        }
                        rowToProcess = currentRow++;

                    }


                    if (rowToProcess != -1) {
                        String[][] board = new String[N][N];
                        for (int i = 0; i < N; i++) Arrays.fill(board[i], "0");

                        board[rowToProcess][0] = "Q";
                        System.out.println("Thread " + threadId + " exploring: Queen at (" + rowToProcess + ",0)");
                        backtrackingSolve(board, 1, threadId);
                        System.out.println("Thread " + threadId + " finished row " + rowToProcess);
                    }

                    threadSemaphore.release();
                    Thread.sleep(10);

                } catch (InterruptedException e) {
                    System.out.println("Thread " + threadId + " interrupted");
                    working = false;
                }
            }
            System.out.println("Thread " + threadId + " finished all work!");
        }

        public void stopWorker() {
            this.working = false;
        }
    }

    public void startMultiThreaded() {
        //get my available number of processes
        int processors = Runtime.getRuntime().availableProcessors();
        System.out.println("Available processors: " + processors);
        //to optimize number of threads
        int numThreads = Math.min(processors, N);
        //create a Semaphore that controls how many threads are allowed to run at the same time
        threadSemaphore = new Semaphore(numThreads);
        System.out.println("Creating " + numThreads + " threads with Semaphore control");
        //create many worker threads as i specified and starts them.
        Thread[] workers = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            workers[i] = new Worker(i);
            workers[i].start();
        }
        // bygm3 kol el threads w yshof l2a el 7l wla l2
        for (Thread t : workers) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.out.println("Main thread interrupted");
            }
        }

        System.out.println("\n=== FINAL RESULTS ===");
        System.out.println("All threads finished.");

//        int count = 1;
//        for (String[][] sol : solutions) {
//            System.out.println("Solution " + count++ + ":");
//            PrintBoard(sol);
//            System.out.println();
//        }
        // print first solution
        String[][] firstSolution = solutions.get(0);
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                System.out.print(firstSolution[i][j] + " ");
            }
            System.out.println();
        }
    }

}
void main(String[] args) {
    Scanner sc = new Scanner(System.in);
    System.out.print("Enter number of Queens: ");
    int N = sc.nextInt();
    if (N < 4) {
        System.out.println("N must be >= 4");
        return;
    }
    NQueen game = new NQueen(N);
    game.startMultiThreaded();
    sc.close();
}

