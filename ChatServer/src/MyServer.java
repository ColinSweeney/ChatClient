import java.io.*;
import java.net.*;
import java.util.Vector;

public class MyServer {
	private ServerSocket   _server;
	private Vector<Socket> _sockets;

	public MyServer() {
		_sockets = new Vector<Socket>();

        try {
            _server = new ServerSocket(7777);
            System.out.println("Created server with IP address: +" +
            				   InetAddress.getLocalHost().getHostAddress() +
            				   " on port: 7777");
        }
        catch (IOException ex) {
            System.err.println("Server creation failed: " + ex);
            System.exit(1);
        }

        new AcceptThread().start();
        
        while (true) {
        	for (int i = 0; i < _sockets.size(); ++i) {
        		try { 
        			Socket socket = _sockets.get(i);
        			if (socket.isClosed()) {
        				socket.close();
        				_sockets.remove(i--);
        				continue;
        			}

        			InputStream input     = socket.getInputStream();  
        			int         available = input.available();
        			if (available > 0) {
        				byte[] bytes = new byte[available];
        				input.read(bytes);

        				System.out.println("Sending message: " + new String(bytes));
        				
        				for (int j = 0; j < _sockets.size(); ++j) {
        					try {
        						OutputStream output = _sockets.get(j).getOutputStream(); 
        						output.write(bytes);
        						output.flush();
        					}
        					catch (IOException ex) { _sockets.get(j).close(); }
        				}
        			}
        			
        			Thread.sleep(100);
        		}
        		catch (IOException ex) {
        			System.err.println("Message failed: " + ex);
        		}
				catch (InterruptedException ex) {
					System.err.println("Sleep interrupted " + ex);
				}
        	}	
		}
	}
	
	private class AcceptThread extends Thread {
		public void run() {
			while (true) {
		        try {
		        	Socket socket = _server.accept(); 
		            _sockets.add(socket);

		            System.out.println("Accepted connection: " +
		            				   socket.getInetAddress().getHostAddress());

        			Thread.sleep(100);
		        } 
		        catch (IOException ex) {
		            System.err.println("Accept failed: " + ex);
		        }
				catch (InterruptedException ex) {
					System.err.println("Sleep interrupted " + ex);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		new MyServer();
	}
}
