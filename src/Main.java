import java.util.*;

public class NQueen{
    Scanner input = new Scanner(System.in);

    private static int N;
    private static String[][] Board;
    NQueen (int N){
        this.N = N;
        Board = new String[N][N];
        for(int i = 0 ; i<N ; i++)
        {
            for(int j = 0 ; j<N ; j++)
            {
                Board[i][j] = "0";
            }
            System.out.println();
        }
    }
    public static void PrintBoard()
    {
        for(int i = 0 ; i<N ; i++)
        {
            for(int j = 0 ; j<N ; j++)
            {
                System.out.format(Board[i][j] + " ");
            }
            System.out.println();
        }
    }
    public static boolean IsSafe(String[][] board , int col , int row )
    {
        // check left of the row 3shan yt2kd en row mfhosh "Q"
        // hysht8l left l2n asln mafesh right f hsht8l 3la al abl bs
        for(int i = col-1 ; i >= 0 ; i--)
        {

            if(board[row][i].equals("Q"))
            {
                return false;
            }
        }
        // check left of the column 3shan yt2kd en column mfhosh "Q"
        // hysht8l left l2n asln mafesh right f hsht8l 3la al abl bs
        for(int Row = row-1 ; Row >= 0 ; Row--)
        {

            if(board[Row][col].equals("Q"))
            {
                return false;
            }
        }

        // check Left upper diagonal
        int r = row;
        int c = col;
        while (r >= 0 && c >= 0)
        {
            if(board[r][c].equals("Q"))
            {
                return false;
            }
            r--;
            c--;
        }

        // check Left Lower diagonal
        int R = row;
        int C = col;
        while(C >= 0 && R < N)
        {
            if(board[R][C].equals("Q"))
            {
                return false;
            }
            System.out.println("Row is" + r);
            System.out.println("col is" + r);
            R++;
            C--;
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



    public boolean backtrackingSolver(String[][] board, int col, int threadID) {

        if (col >= N) {
            System.out.println("\nThread " + threadID + " found a solution:");
//            PrintBoard();
            return true;
        }

        for (int row = 0; row < N; row++) {
            if (IsSafe(board, row, col)) {
                board[row][col] = "Q";

                if (backtrackingSolver(board, col + 1, threadID)) return true;

                board[row][col] = "0";
            }
        }
        return false;
    }


    class Worker extends Thread {
        int startCol;

        Worker(int col) { this.startCol = col; }

        public void run() {
            String[][] board = new String[N][N];
            for (int i = 0; i < N; i++)
                Arrays.fill(board[i], "0");

            board[0][startCol] = "Q";
            System.out.println("Thread starting at column " + startCol);
            backtrackingSolver(board, 1, startCol);
        }
    }


}






void main() {
    Scanner input = new Scanner(System.in);
    System.out.print("Enter number of queens: ");
    int n = input.nextInt();

    NQueen queen = new NQueen(n);

    Thread[] workers = new Thread[n];

    for (int i = 0; i < n; i++) {
        workers[i] = queen.new Worker(i);
        workers[i].start();
    }

    for (Thread t : workers) {
        try { t.join(); } catch (Exception e) {}
    }

    System.out.println("All threads finished.");
}

