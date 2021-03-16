import java.awt.Font;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Scanner;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Client class for Tic Tac Toe
 * 
 * @author Jonathan Ma
 * @version 1.0
 *
 */

public class TicTacToeClient {

	private JFrame frame = new JFrame("Tic Tac Toe");
	private JLabel messageLabel = new JLabel("Enter your player name...");

	private JTextField txt_name;
	private JButton btn_submit;

	private Square[] board = new Square[9];
	private Square currentSquare;

	private Socket socket;
	private Scanner input;
	private PrintWriter output;

	public TicTacToeClient() throws Exception {

		socket = new Socket("127.0.0.1", 5000);
		input = new Scanner(socket.getInputStream());
		output = new PrintWriter(socket.getOutputStream(), true);
		
		frame.getContentPane().add(messageLabel, BorderLayout.NORTH);

		var boardPanel = new JPanel();
		boardPanel.setLayout(new GridLayout(3, 3, 2, 2));
		for (int i = 0; i < 9; i++) {
			final int j = i;
			board[i] = new Square();
			board[i].addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					currentSquare = board[j];
					output.println("MOVE " + j);
				}
			});
			boardPanel.add(board[i]);
		}
		frame.getContentPane().add(boardPanel, BorderLayout.CENTER);
		JPanel bottomPanel = new JPanel();
		txt_name = new JTextField(20);
		btn_submit = new JButton("Submit");
		btn_submit.addActionListener(new submitListener());
		bottomPanel.add(txt_name);
		bottomPanel.add(btn_submit);
		frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

		JMenuBar MenuBar = new JMenuBar();
		JMenu control = new JMenu("Control");
		JMenuItem control_exit = new JMenuItem("Exit");
		control_exit.addActionListener(new exitListener());
		JMenu help = new JMenu("Help");
		JMenuItem help_instructions = new JMenuItem("Instructions");
		help_instructions.addActionListener(new instructionsListener());
		control.add(control_exit);
		MenuBar.add(control);
		help.add(help_instructions);
		MenuBar.add(help);
		frame.setJMenuBar(MenuBar);
	}

	/** 
	 * Method starting the client side.
	 * 
	 * @Exception IOException
	 */
	public void go() {
		try {
			var response = input.nextLine();
			var mark = response.charAt(8);
			var opponentMark = mark == 'X' ? 'O' : 'X';
			while (input.hasNextLine()) {
				response = input.nextLine();
				if (response.startsWith("VALID_MOVE")) {
					messageLabel.setText("Valid move, wait for your opponent.");
					currentSquare.setText(mark);
					currentSquare.repaint();
				} else if (response.startsWith("OPPONENT_MOVED")) {
					var loc = Integer.parseInt(response.substring(15));
					board[loc].setText(opponentMark);
					board[loc].repaint();
					messageLabel.setText("Your opponent has moved, now is your turn");
				} else if (response.startsWith("MESSAGE")) {
					messageLabel.setText(response.substring(8));
				} else if (response.startsWith("WIN")) {
					JOptionPane.showMessageDialog(frame, "Congratulations. You win.");
					System.exit(0);
				} else if (response.startsWith("LOSE")) {
					JOptionPane.showMessageDialog(frame, "You lose.");
					System.exit(0);
				} else if (response.startsWith("DRAW")) {
					JOptionPane.showMessageDialog(frame, "Draw.");
					System.exit(0);
				} else if (response.startsWith("PLAYER_LEFT")) {
					JOptionPane.showMessageDialog(frame, "Game Ends. One of the players left.");
					System.exit(0);
				}
			}
			output.println("QUIT");
			} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try { socket.close	(); } catch (Exception ex) {}
		}
	}

	static class Square extends JPanel {
		JLabel label = new JLabel();

		public Square() {
			setBackground(Color.white);
			setLayout(new GridBagLayout());
			label.setFont(new Font("sans-serif", Font.BOLD, 40));
			add(label);
		}

		public void setText(char text) {
			if (text == 'X') {
				label.setForeground(Color.GREEN);
			} else {
				label.setForeground(Color.RED);
			}
			label.setText(text + "");
		}
	}
	
	private class exitListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}

	private class instructionsListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JOptionPane.showMessageDialog(frame, "Some information about the game:\nCriteria for a valid move:\n"
				+ "- The move is not occupied by any mark.\n- The move is made in the player's turn.\n"
				+ "- The move is made within the 3 x 3 board.\nThe game would continue and switch among the opposite\nplayer until it reaches either one of the following conditions:\n"
				+ "- Player 1 wins.\n- Player 2 wins.\n- Draw.");
		}
	}

	private class submitListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String username = txt_name.getText();
			messageLabel.setText("WELCOME " + username);
			frame.setTitle("Tic Tac Toe-Player: " + username);
			txt_name.setEditable(false);
			btn_submit.setEnabled(false);
			output.println("READY " + username);
		}
	}

	public static void main(String[] args) throws Exception {
		TicTacToeClient client = new TicTacToeClient();
		client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.frame.setSize(370, 420);
		client.frame.setVisible(true);
		client.go();
	}
}