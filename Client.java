package Chess;

import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
    private static Socket socket;
    private static String address;
    private static int port = 8080;

    private static Scanner scanner = new Scanner(System.in);
    private static Scanner socketIn;
    private static OutputStream socketOut;

    private static ArrayList<String> moves = new ArrayList<>();
    private static ChessBoardViewer board;

    public static void main(String[] args) {
        board = new ChessBoardViewer(moves);
        
        System.out.println("Get address: ");
        address = scanner.nextLine();

        try 
        {
            socket = new Socket(address, port);

            socketIn = new Scanner(socket.getInputStream());
            socketOut = socket.getOutputStream();

            while (!socket.isClosed()) {
                while (true) {
                    String response = socketIn.nextLine();

                    if (response.equals("PASS")) break; // your turn;

                    ReceiveMove(response);
                }

                SendMove();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void SendMove() throws Exception
    {
        System.out.println("It's your turn!");

        if (scanner.hasNextLine()) 
        {
            String move = scanner.nextLine() + "\n";
            
            while (!GameHandler.CheckMoveFormat(move))
            {
                System.out.println("Incorrect move format! Please try again:");
                move = scanner.nextLine() + '\n';
            }

            moves.add(move.trim());
            board.updateBoard();
            System.out.println(board.toString());

            socketOut.write(move.getBytes());
            System.out.println("Wait opponent move...");
            return;
        }
        else
        {
            System.out.println("Disconnetting from server...");
            socket.close();;
            return;
        }
    }

    public static void ReceiveMove(String move) throws Exception
    {
        if (GameHandler.CheckMoveFormat(move)) 
        {
            moves.add(move);
            board.updateBoard();
            System.out.println(board.toString());
            System.out.println(moves.size());
        }
        else
        {
            System.out.println(move);
        }
    }
    
    public static void UpdateBoard(String move)
    {

    }

    public static void Disconnect()
    {

    }
}
