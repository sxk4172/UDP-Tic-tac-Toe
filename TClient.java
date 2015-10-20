/*
 * TClient.java
 * 
 * Version: $Id: TClient.java, v 15.2.1 2014/08/12 23:50:41
 * 
 * Revisions: 
 * 		
 *   	Initial Revision
 * 
 */


import java.awt.*;
import java.awt.event.*;
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

public class TClient {
	JFrame frame = new JFrame();
	private JLabel id;
	DatagramPacket receivePacket;
	 DatagramPacket sendPacket;
	 byte[] sendData = new byte[1024];
     byte[] receiveData = new byte[1024];
	private JPanel boardPanel;
	private JButton sqCurrent;
	private char myMark;
	private boolean myTurn;;
	
	JButton board[] = new JButton[9];
	public TClient(String serverAddress) throws UnknownHostException, IOException {
		DatagramSocket clientSocket = new DatagramSocket();
	      InetAddress IPAddress = InetAddress.getByName("localhost");
	      
	     // String sentence = inFromUser.readLine();
	     // sendData = sentence.getBytes();
	      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 1510);
	      clientSocket.send(sendPacket);
	      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	      clientSocket.receive(receivePacket);
	     // String modifiedSentence = new String(receivePacket.getData());
	     // System.out.println("FROM SERVER:" + modifiedSentence);
	      //clientSocket.close();
		boardPanel = new JPanel();
		GridLayout layout = new GridLayout(3, 3, 10, 10);
		boardPanel.setLayout(layout);
		board = new JButton[9];

		for (int row = 0; row < board.length; row++) {

			board[row] = new JButton("");
			final int r = row;
			board[row].addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					sqCurrent = board[r];

				}
			});
			//boardP.add(board[row]);
		}

		id = new JLabel();
		frame.getContentPane().add(id, BorderLayout.NORTH);
		///frame.add(boardP, BorderLayout.CENTER);
		frame.setVisible(true);
		frame.setSize(300, 300);
	}

	public static void main(String args[]) throws UnknownHostException, IOException {
		String serverAddress = (args.length==0) ? "localhost" : args[1];
		TClient t = new TClient(serverAddress);
		t.execute();
	}

	public void execute() {
		String s =new String( receivePacket.getData());
		myMark = s.charAt(0);
		id.setText("You are player " + myMark );
		myTurn = (myMark == 'X' ? true : false);

		while (true) {
			String str =new String( receivePacket.getData());
			processMessage(str);
		}
	}

	public void processMessage(String s) {
		if (s.equals("Valid move.")) {

			sqCurrent.setText(Character.toString(myMark));

		} else if (s.equals("Invalid move, try again")) {
			myTurn = true;
		} else if (s.equals("Opponent moved")) {
			myTurn = true;
		}
	}

}
