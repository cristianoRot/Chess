package Chess;

import java.io.OutputStream;
import java.net.Socket;
import java.util.*;

public class GameHandler implements Runnable {
    private int gameId;
    private ChessBoardViewer board;

    private Socket whiteSocket;
    Scanner whiteInput;
    OutputStream whiteOut;

    private Socket blackSocket;
    Scanner blackInput;
    OutputStream blackOut;

    private byte playerCount = 0;
    private ArrayList<String> moves;

    public GameHandler(int gameId, ArrayList<String> moves, boolean visible) {
        this.gameId = gameId;
        this.moves = moves;
        board = new ChessBoardViewer(moves, visible);
    }
    
    public void run() {
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

        whiteOut.write("1\n".getBytes());
        blackOut.write("1\n".getBytes());
    }

    private void HandleWhiteConnection() throws Exception {
        try {
            whiteInput = new Scanner(whiteSocket.getInputStream());
            whiteOut = whiteSocket.getOutputStream();

            whiteOut.write(("0," + gameId + ",0\n").getBytes());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }
    }

    private void HandleBlackConnection() throws Exception {
        try {
            blackInput = new Scanner(blackSocket.getInputStream());
            blackOut = blackSocket.getOutputStream();

            blackOut.write(("0," + gameId + ",1\n").getBytes());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void HandleMoves() throws Exception {
        while (true)
        {
            String move;

            do
            {
                whiteOut.write(("M\n").getBytes());
                move = whiteInput.nextLine();
            }
            while (!CheckMoveFormat(move, true));

            SaveMove(move);
            blackOut.write((move + '\n').getBytes());

            do
            {
                blackOut.write(("M\n").getBytes());
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
    public boolean CheckMoveFormat(String move, boolean isWhite) {
        char first = move.charAt(0);
        int[] start = { move.charAt(1) - 'a', 8 - Character.getNumericValue(move.charAt(2)) }; // { letter, digit }
        int[] end = { move.charAt(4) - 'a', 8 - Character.getNumericValue(move.charAt(5)) }; // { letter, digit }

        if (start[0] < 0 || start[0] > 7) 
            return false;
        
        if (start[1] < 0 || start[1] > 7)
            return false;

        if (move.charAt(3) != '-') 
            return false;

        if (end[0] < 0 || end[0] > 7) 
            return false;
        
        if (end[1] < 0 || end[1] > 7) 
            return false;

        return CheckMoveValid(first, start, end, isWhite);
    }

    private boolean CheckMoveValid(char piece, int[] start, int[] end, boolean isWhite)
    {
        if (start[0] == end[0] && start[1] == end[1])
        {
            System.out.println("Error: Start and end positions are the same!");
            return false;
        }
        
        if (board.getPiece(start) != piece)
        {
            System.out.println("Error: Piece in start position does not match the piece being moved!");
            return false;
        }

        switch (Character.toUpperCase(piece)) {
            case 'P':
                return CheckPawnMove(start, end, isWhite);
            case 'R':
                return CheckRockMove(start, end, isWhite);
            case 'B':
                return CheckBishopMove(start, end, isWhite);
            case 'N':
                return CheckKnightMove(start, end, isWhite);
            case 'K':
                return CheckKingMove(start, end, isWhite);
            case 'Q':
                return CheckQueenMove(start, end, isWhite);
        
            default:
                return false;
        }
    }

    private boolean CheckPawnMove(int[] start, int[] end, boolean isWhite)
    {
        if (end[0] < start[0] - 1 || end[0] > start[0] + 1) return false; // check the horizontal range 

        boolean isInStartingPlace = (isWhite && start[1] == 6) || (!isWhite && start[1] == 1);

        char pieceInEndBox = board.getPiece(end);

        if (isWhite)
        {
            if (end[1] < (isInStartingPlace ? start[1] - 2 : start[1] - 1))
            {
                System.out.println("Pawn step is too long!");
                return false;
            }
        }
        else
        {
            if (end[1] > (isInStartingPlace ? start[1] + 2 : start[1] + 1))
            {
                System.out.println("Pawn step is too long!");
                return false;
            }
        }

        if (start[0] == end[0])
        {
            if (pieceInEndBox != '\0')
            {
                System.out.println("Box is already occupied by " + pieceInEndBox);
                return false;
            }
        }
        else if (
                board.getPiece(end) == '\0' ||
                isWhite ? isPieceWhite(pieceInEndBox) : !isPieceWhite(pieceInEndBox)
            ) 
            return false;

        return true;
    }

    private boolean CheckRockMove(int[] start, int[] end, boolean isWhite)
    {
        char pieceInEndBox = board.getPiece(end);
        boolean isMovingHorizontal = start[1] != end[1];

        if (start[0] != end[0] && isMovingHorizontal) return false; // check that rock moves in a straight way

        int offsetX = end[1] - start[1] - 1;
        int offsetY = end[0] - start[0] - 1;
        int step = end[0] < start[0] || end[1] < start[1] ? -1 : 1;

        for (int i = step; Math.abs(i) < Math.abs(offsetX); i += step)
        {
            int[] arr = { start[0] + i, start[1] };
            if (board.getPiece(arr) != '\0') return false;
        }

        for (int i = step; Math.abs(i) < Math.abs(offsetY); i += step)
        {
            int[] arr = { start[0], start[1] + i };
            if (board.getPiece(arr) != '\0') return false;
        }


        if (isWhite ? isPieceWhite(pieceInEndBox) : !isPieceWhite(pieceInEndBox)) return false;

        return true;
    }

    private boolean CheckBishopMove(int[] start, int[] end, boolean isWhite)
    {
        return true;
    }

    private boolean CheckKnightMove(int[] start, int[] end, boolean isWhite) 
    { 
        return true; 
    }

    private boolean CheckKingMove(int[] start, int[] end, boolean isWhite)
    { 
        return true; 
    }

    private boolean CheckQueenMove(int[] start, int[] end, boolean isWhite)
    { 
        return true; 
    }

    private boolean isPieceWhite(char piece)
    {
        return piece >= 'A' && piece <= 'Z';
    }

    public void UpdateBoard() {
        board.updateBoard();
    }

    public String toString() {
        return "GameHandler{" +
                "gameId=" + gameId +
                ", playerCount=" + playerCount +
                '}';
    }
}
