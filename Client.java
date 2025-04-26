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

    private static GameHandler gameHandler;
    private static ArrayList<String> moves = new ArrayList<>();

    private static int gameId;
    private static int team; // 0 -> white, 1 -> black

    public static void main(String[] args) {
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

                    HandleMessage(response);
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void HandleMessage(String mess) throws Exception
    {
        String[] arr = mess.split(",");

        switch (arr[0].charAt(0)) {
            case '0':
                InitializeMatch(Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
                break;
            case '1':
                System.out.println("Match started!");
                break;
            case 'M':
                SendMove();
                break;
            default:
                ReceiveMove(mess);
                break;
        }
    }

    private static void InitializeMatch(int gameId_, int team_)
    {
        gameId = gameId_;
        team = team_;

        gameHandler = new GameHandler(gameId_, moves, true);

        System.out.println("Game ID: " + gameId);
        System.out.println("Your team: " + (team == 0 ? "White" : "Black"));
    }

    public static void SendMove() throws Exception
    {
        System.out.println("It's your turn!");

        if (scanner.hasNextLine()) 
        {
            String move = scanner.nextLine() + "\n";
            
            while (!gameHandler.CheckMoveFormat(move, team == 0 ? true : false))
            {
                System.out.println("Incorrect move format! Please try again:");
                move = scanner.nextLine() + '\n';
            }

            moves.add(move.trim());
            gameHandler.UpdateBoard();

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
        if (gameHandler.CheckMoveFormat(move, team == 0 ? false : true)) 
        {
            moves.add(move);
            gameHandler.UpdateBoard();
            System.out.println(moves.size());
        }
    }
    
    public static void UpdateBoard(String move)
    {

    }

    public static void Disconnect()
    {

    }
}
