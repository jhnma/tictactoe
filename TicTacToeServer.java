import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server class for Tic Tac Toe
 * 
 * @author Jonathan Ma
 * @version 1.0
 */

public class TicTacToeServer {

    private static Player[] board = new Player[9];
    static Player currentPlayer;
	
    /**
     * initialises the server
     * @param args
     * @throws Exception
     */
    
    public static void main(String[] args) throws Exception {
        try (var listener = new ServerSocket(5000)) {
            System.out.println("The Tic Tac Toe Server is Running...");
            ExecutorService pool = Executors.newFixedThreadPool(20);
            while (true) {
                pool.execute(new Player(listener.accept(), "X"));
                pool.execute(new Player(listener.accept(), "O"));
            }
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
    }

    private static class Player implements Runnable {
        String sign;
        Player opponent;
        Socket socket;
        Scanner input;
        PrintWriter output;
        static int readyCount = 0;

        public Player(Socket socket, String sign) {
            this.socket = socket;
            this.sign = sign;
        }

        @Override
        public void run() {
            try {
                setup();
                while (readyCount < 2) {
                	var command = input.nextLine();
                	if (command.startsWith("READY")) { readyCount++; }
                }
                while (input.hasNextLine()) {
                    var command = input.nextLine();
                    if (command.startsWith("QUIT")) {
                        return;
                    } else if (command.startsWith("MOVE")) {
                        int movedBlock = Integer.parseInt(command.substring(5));
                        try {
                            move(movedBlock, this);
                            output.println("VALID_MOVE");
                            opponent.output.println("OPPONENT_MOVED " + movedBlock);
                            if ((board[0] != null && board[0] == board[1] && board[0] == board[2])
                             || (board[3] != null && board[3] == board[4] && board[3] == board[5])
                             || (board[6] != null && board[6] == board[7] && board[6] == board[8])
                             || (board[0] != null && board[0] == board[3] && board[0] == board[6])
                             || (board[1] != null && board[1] == board[4] && board[1] == board[7])
                             || (board[2] != null && board[2] == board[5] && board[2] == board[8])
                             || (board[0] != null && board[0] == board[4] && board[0] == board[8])
                             || (board[2] != null && board[2] == board[4] && board[2] == board[6])) {
                                output.println("WIN");
                                opponent.output.println("LOSE");
                            } else if (board[0] != null && board[1] != null && board[2] != null && board[3] != null
                            		&& board[4] != null && board[5] != null && board[6] != null && board[7] != null
                            		&& board[8] != null) {
                                output.println("DRAW");
                                opponent.output.println("DRAW");
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (opponent != null && opponent.output != null) {
                    opponent.output.println("PLAYER_LEFT");
                }
                try { socket.close(); } catch (Exception ex) {}
            }
        }
        
        private synchronized void move(int location, Player player) {
            if (player != currentPlayer || player.opponent == null || board[location] != null) { throw new IllegalStateException(""); }
            board[location] = currentPlayer;
            currentPlayer = currentPlayer.opponent;
        }

        private void setup() throws IOException {
            input = new Scanner(socket.getInputStream());
            output = new PrintWriter(socket.getOutputStream(), true);
            output.println("WELCOME " + sign);            
            if (sign == "X") {
                currentPlayer = this;
            } else {
                opponent = currentPlayer;
                opponent.opponent = this;
            }
        }
    }
}