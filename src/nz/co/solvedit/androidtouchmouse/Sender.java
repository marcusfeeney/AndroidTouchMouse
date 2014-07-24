/*
 * Author: Marcus Feeney, SolvedIt Ltd
 * File: Sender.java
 * 
 * Class which implements the transport of event and cursor data to TCP server.
 * 
 */

package nz.co.solvedit.androidtouchmouse;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

import android.content.SharedPreferences;

public class Sender {
	
    private String serverIP;
    private int serverPort;
    private String transmitMsg;
    private OnMessageReceived listener;

    public Sender(String msg, SharedPreferences prefs, OnMessageReceived listener) {
    	serverIP = prefs.getString("pref_server", "192.168.0.10");
    	serverPort = Integer.parseInt(prefs.getString("pref_port", "9000"));
        transmitMsg = msg;
        this.listener = listener;
    }

    public void run() {
		Socket socket = null;
		byte[] msgBytes = null;
        try {
            InetAddress serverAddr = InetAddress.getByName(serverIP);
            //System.out.print("Connecting...");
            socket = new Socket(serverAddr, serverPort);
            //System.out.println("Done");
        	OutputStream out = socket.getOutputStream();
        	DataOutputStream dos = new DataOutputStream(out);
        	msgBytes = transmitMsg.getBytes();
        	dos.write(msgBytes, 0, msgBytes.length);
            //System.out.println("Sent: " + transmitMsg);

        } catch (ConnectException e1) {
        	listener.messageReceived(e1.getMessage());
        } catch (IOException e) {
        	e.printStackTrace();
        } finally {
            try {
				socket.close();
            } catch (NullPointerException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				System.out.println("IO ERROR");
			} 
        }
    }

    public interface OnMessageReceived {
        public void messageReceived(String message);
    }

}
