package Chess;

import java.io.OutputStream;
import java.net.Socket;
import java.util.*;

// TI AMO 

public class GameHandler implements Runnable {
    private int gameId;
    private static ChessBoardViewer board;

    private Socket whiteSocket;
    Scanner whiteInput;
    OutputStream whiteOut;

    private Socket blackSocket;
    Scanner blackInput;
    OutputStream blackOut;

    private byte playerCount = 0;
    private ArrayList<String> moves;

    public GameHandler(int gameId) {
        this.gameId = gameId;
        this.moves = new ArrayList<>();
    }
    
    public void run() {
        board = new ChessBoardViewer(moves);

        try 
        {
            HandleMatchMaking();

            HandleMoves();
        } 
        catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public synchronized void addPlayer(Socket socket) {
        if (playerCount == 0) {
            whiteSocket = socket;
        } else if (playerCount == 1) {
            blackSocket = socket;
        }
        playerCount++;
        notifyAll();
    }

    private void HandleMoves() throws Exception {
        while (true)
        {
            String move;

            do
            {
                whiteOut.write(("PASS\n").getBytes());
                move = whiteInput.nextLine();
            }
            while (!CheckMoveFormat(move, true));

            SaveMove(move);
            blackOut.write((move + '\n').getBytes());

            do
            {
                blackOut.write(("PASS\n").getBytes());
                move = blackInput.nextLine();
            }
            while (!CheckMoveFormat(move, false));

            SaveMove(move);
            whiteOut.write((move + '\n').getBytes());
        }
    }

    private boolean SaveMove(String move) {
        moves.add(move);
        board.updateBoard();

        return true;
    }

    // format: Xpp-pp
    public static boolean CheckMoveFormat(String move, boolean isWhite) {
        char first = move.charAt(0);
        char[] start = { move.charAt(1), move.charAt(2) }; // { letter, digit }
        char[] end = { move.charAt(4), move.charAt(5) }; // { letter, digit }

        CheckMoveValid(first, start, end, isWhite);

        if (start[0] < 'a' || start[0] > 'h') 
            return false;
        
        if (start[1] < '1' || start[1] > '8') 
            return false;

        if (move.charAt(3) != '-') 
            return false;

        if (end[0] < 'a' || end[0] > 'h') 
            return false;
        
        if (end[1] < '1' || end[1] > '8') 
            return false;
        
        return true;
    }

    private static boolean CheckMoveValid(char piece, char[] start, char[] end, boolean isWhite)
    {
        switch (piece) {
            case 'P':
                return CheckPawnMove(start, end, isWhite);
            case 'R':
                return CheckRockMove(start, end);
            case 'B':
                return CheckBishopMove(start, end);
            case 'N':
                return CheckKnightMove(start, end);
            case 'K':
                return CheckKingMove(start, end);
            case 'Q':
                return CheckQueenMove(start, end);
        
            default:
                return false;
        }
    }

    private static boolean CheckPawnMove(char[] start, char[] end, boolean isWhite)
    {
        if (start[0] != end[0]) return false;

        if (end[0] < start[0] - 1 || end[0] > start[0] + 1) return false;

        boolean isInStartingPlace = (isWhite && start[1] == 2) || (!isWhite && start[1] == 7);

        char pieceInEndBox = board.getPiece(end);

        if (isWhite)
        {
            if (end[1] > (isInStartingPlace ? start[1] + 2 : start[1] + 1)) return false;
        }
        else
        {
            if (end[1] < (isInStartingPlace ? start[1] - 2 : start[1] - 1)) return false;
        }

        if (start[0] == end[0])
        {
            if (pieceInEndBox != "") return false;
        }
        else if (isWhite ? isPieceWhite(pieceInEndBox) : !isPieceWhite(pieceInEndBox)) return false;

        return true;
    }

    private static boolean CheckRockMove(char[] start, char[] end, boolean isWhite)
    {
        char pieceInEndBox = board.getPiece(end);

        if (start[0] != end[0] && start[1] != end[1]) return false;

        if(start[0] == end[0])
        {
            boolean moveUp = start[1] < end[1]; 
            int offsetMove = Math.abs(start[1] - end[1]);

            for(int i = 1; i < offsetMove; i++)
            {
                char arr = { start[0], (moveUp ? start[1] + i : start[1] - i) };
                if (board.getPiece(arr) != "") return false;
            }
        }

    }

    private static boolean CheckBishopMove(char[] start, char[] end)
    {
        
    }

    private static boolean isPieceWhite(char piece)
    {
        return piece >= 'A' && piece <= 'Z';
    }

    private void HandleMatchMaking() throws Exception {
        synchronized (this) {
            while (playerCount == 0) {
                wait();
            }
        }

        HandleWhiteConnection();

        synchronized (this) {
            while (playerCount == 1) {
                wait();
            }
        }

        HandleBlackConnection();

        whiteOut.write("Match Started.\n".getBytes());
        blackOut.write("Match Started.\n".getBytes());
    }

    private void HandleWhiteConnection() throws Exception {
        try {
            whiteInput = new Scanner(whiteSocket.getInputStream());
            whiteOut = whiteSocket.getOutputStream();

            whiteOut.write(("Id match: " + gameId + "\n").getBytes());
            whiteOut.write("You are white. Waiting for black...\n".getBytes());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }
    }

    private void HandleBlackConnection() throws Exception {
        try {
            blackInput = new Scanner(blackSocket.getInputStream());
            blackOut = blackSocket.getOutputStream();

            blackOut.write(("Id match: " + gameId + "\n").getBytes());
            blackOut.write("You are black.\n".getBytes());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public String toString() {
        return "GameHandler{" +
                "gameId=" + gameId +
                ", playerCount=" + playerCount +
                '}';
    }
}
