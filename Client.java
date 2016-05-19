// Client code

import java.net.*;
import java.io.*;

public class Client {
	DataOutputStream dos = null;
	DataInputStream dis = null;
	Socket s = null;
	Boolean clientOnline = false;

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Invalid parameters, input should be like :\nClient 127.0.0.1 <Port number>");
			System.exit(0);
		}
		Client clt = new Client();
		String hostIP = args[0];
		int hostPort = Integer.parseInt(args[1]);
		clt.startClient(hostIP, hostPort);
	}

	public void startClient(String hostIP, int hostPort) {
		try {
			System.out.println(">>>>To exit client, enter 'exit'<<<<");
			s = new Socket(hostIP, hostPort);
			dos = new DataOutputStream(s.getOutputStream());
			dis = new DataInputStream(s.getInputStream());
			String writeStr = "no message";
			clientOnline = true;			// turn on online flag

			recvMessage recvr = new recvMessage();
			new Thread(recvr).start();

			sendMessage sendr = new sendMessage();
			new Thread(sendr).start();

		} catch (IOException e) {
			System.out.println("Error");
			e.printStackTrace();
		}
	}

	class sendMessage implements Runnable {				// send message in a thread
		public void run() {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				String writeStr = "no message";
				while (clientOnline) {
					if (writeStr.equals("exit")) {
						clientOnline = false;
						break;
					}
					writeStr = br.readLine();
					dos.writeUTF(writeStr);
					dos.flush();
				}
				Thread.currentThread().sleep(500);
				dos.close();
				s.close();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (SocketException e) {
				System.out.println("Socket log out");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	class recvMessage implements Runnable {				// receive message in another thread
		public void run() {
			try {
				String recvMsgStr = null;
				while (clientOnline) {
					recvMsgStr = dis.readUTF();
					System.out.println("<<<" + recvMsgStr);
				}
			} catch (SocketException e) {
				System.out.println("Socket log out");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
