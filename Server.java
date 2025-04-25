package Chess;
import java.net.ServerSocket;
import java.util.*;

public class Server {
    private static int currentGameId = 0;
    private static Queue<GameHandler> waitingGames = new LinkedList<>();

    public static void main(String[] args) {
        try (var serverSocket = new ServerSocket(8080)) 
        {
            System.out.println("Listening on port 8080...");

            while (true)
            {
                var socket = serverSocket.accept();

                System.out.println("Client connected. " + socket.getPort());

                GameHandler game = waitingGames.poll();

                if (game == null)
                {
                    game = new GameHandler(incrementGameId());
                    game.addPlayer(socket);
                    waitingGames.add(game);

                    System.out.println(game.toString());

                    new Thread(game).start();
                }
                else
                {
                    game.addPlayer(socket);
                    System.out.println(game.toString());
                }
            }
        } 
        catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static int incrementGameId() {
        return currentGameId++;
    }
}
