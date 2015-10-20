/*
 * TServer.java
 * 
 * Version: $Id: TServer.java, v 15.2.1 2014/08/12 23:50:41
 * 
 * Revisions: 
 * 		
 *   	Initial Revision
 * 
 */

import java.net.*;
import java.io.*;
import javax.swing.*;



/**
 * This program implements the fnctionality of TicTacToe game
 * 
 * 
 * @author Sanika Kulkarni
 * @author Yashashree Kulkarni
 */
public class TServer extends JFrame {
	private byte[] board = new byte[9];
	private Player[] players = new Player[2];
	private  DatagramSocket serverSocket;
	//private DatagramPacket sendPacket;
	//public  byte[] receiveData;
	//public byte[] sendData;
	private int currentPlayer=0;

	public TServer() {

		try {
	         DatagramSocket serverSocket = new DatagramSocket(1510);
	        
                      //  serverSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Server awaiting connections");

	}

	// wait for two connections so game can be played
	public void execute() throws IOException {
		for (int i = 0; i < players.length; i++) {
				players[i] = new Player(serverSocket, this, i);
				players[i].start();
			
		}
		// Player X is suspended until Player O connects.
		// Resume player X now.
		synchronized (players[0]) {
			players[0].threadSuspended = false;
			players[0].notify();
		}
	}

	// Determine if a move is valid.
	// This method is synchronized because only one move can be
	// made at a time.
	public synchronized boolean validMove(int loc, int player) throws IOException {
		while (player != currentPlayer) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (!isOccupied(loc)) {
			board[loc] = (byte) (currentPlayer == 0 ? 'X' : 'O');
			currentPlayer = (currentPlayer + 1) % 2;
			players[currentPlayer].otherPlayerMoved(loc);
			notify(); // tell waiting player to continue
			return true;
		} else
			return false;
	}

	public boolean isOccupied(int loc) {
		if (board[loc] == 'X' || board[loc] == 'O')
			return true;
		else
			return false;
	}

	public boolean gameOver() {
		if((isOccupied(0) && board[0]==board[1] && board[1]==board[2] ) ||
			(isOccupied(3) && board[3]==board[4] && board[4]==board[5]) ||
			(isOccupied(6) && board[6]==board[7] && board[7]==board[8]) ||
			(isOccupied(0) && board[0]==board[3] && board[3]==board[6]) ||
			(isOccupied(1) && board[1]==board[4] && board[4]==board[7])||
			(isOccupied(2) && board[2]==board[5] && board[5]==board[8])){
			return true;
		}
			return false;
	}

	public boolean boardFull() {
		for(int i=0;i<board.length;i++){
			if(!isOccupied(i)){
				return false;
			}
		}
		return true;
	}

	public static void main(String args[]) throws IOException {
		TServer game = new TServer();
		game.execute();
	}
}

// Player class to manage each Player as a thread
class Player extends Thread {
	private DatagramSocket serverSocket;
	DatagramPacket receivePacket;
	DatagramPacket sendPacket;
	//private DataInputStream input;
	//private DataOutputStream output;
	 byte[] receiveData = new byte[1024];
     byte[] sendData = new byte[1024];
     
	private TServer control;
	private int number;
	private char mark;
	protected boolean threadSuspended = true;

	public Player(DatagramSocket serverSocket, TServer t, int num) throws IOException {
		mark = (num == 0 ? 'X' : 'O');
		this.serverSocket = serverSocket;
		
		control = t;
		number = num;
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	    // serverSocket.receive(receivePacket);
	     InetAddress IPAddress = InetAddress.getByName("localhost");
	     int port = receivePacket.getPort();
	     DatagramPacket sendPacket =
	             new DatagramPacket(sendData, sendData.length, IPAddress, 1510);
	}

	public void otherPlayerMoved(int loc) throws IOException {
		sendData = ("Opponent moved").getBytes();
		serverSocket.send(sendPacket);
	}

	public void run() {
		boolean done = false;
		try {
			String s = Character.toString(mark);
			sendData = s.getBytes();
			serverSocket.send(sendPacket);
			sendData= ("Player "
					+ (number == 0 ? "X connected\n"
							: "O connected, please wait\n")).getBytes();
			serverSocket.send(sendPacket);
			// wait for another player to arrive
			if (mark == 'X') {
				sendData=("Waiting for another player").getBytes();
				serverSocket.send(sendPacket);
				try {
					synchronized (this) {
						while (threadSuspended)
							wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				sendData=("Other player connected. Your move.").getBytes();
				serverSocket.send(sendPacket);
			}
			// Play game
			while (!done) {
				int location = Integer.parseInt(receiveData.toString());
				
				if (control.validMove(location, number)) {
					//control.display("loc: " + location);
					sendData = ("Valid Move").getBytes();
					serverSocket.send(sendPacket);
					//output.writeUTF("Valid move.");
				} else
					//output.writeUTF("Invalid move, try again");
					sendData = ("Invalid move, try again").getBytes();
				serverSocket.send(sendPacket);
					if (control.gameOver()){
					done = true;
					JOptionPane.showMessageDialog(null, "Wins!!!");
					//output.writeUTF("Wins");
				}
				else if(control.boardFull()) {
					JOptionPane.showMessageDialog(null, "Tie!!!");
				}
			}
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}