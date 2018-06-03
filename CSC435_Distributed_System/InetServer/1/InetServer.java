import java.io.*; //import io
import java.net.*;

class Worker extends Thread{ //class definition
	Socket sock;			// class member
	Worker (Socket s) {sock = s;} // constructor

	public void run(){
		// get I/O stream in/out from the socket
		PrintStream out = null;
		BufferedReader in = null;
		try{
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintStream(sock.getOutputStream());
			try{
				String name;
				name = in.readLine();
				System.out.println("Looking up" + name);
				printRemoteAddress(name, out);
			}catch(IOException x){
				System.out.println("Server read error");
				x.printStackTrace();
			}
			sock.close(); //close this one connection
		}catch(IOException ioe){System.out.println(ioe);}
	}

	static void printRemoteAddress(String name, PrintStream out) {
		try{//search for the name and address
			out.println("Looking up "+name+"...");
			InetAddress machine = InetAddress.getByName(name);
			out.println("Host name: " + machine.getHostName());		//host name
			out.println("Host IP : " + toText(machine.getAddress ()));
		}
		catch(UnknownHostException ex){
			out.println("Failed in attempting to look up" + name);
		}
	}
	// no need to change
	static String toText (byte ip[]) { /* Make portable for 128 bit format */
		 StringBuffer result = new StringBuffer();
		 for (int i = 0; i < ip.length; ++ i) {
			 if (i > 0) result.append (".");
			 result.append (0xff & ip[i]);
		 }
		 return result.toString ();
	}
}

public class InetServer{
	public static void main(String a[]) throws IOException{
		// do not need to change
		int q_len = 6; 
		// can't go below 1100
		int port = 2333;
		//TCP/ip implementation
		Socket sock;
		ServerSocket servsock = new ServerSocket(port, q_len);

		System.out.println("Lanny's Inet server 1.0 starting up, listening at port 2333.\n");
	 	while (true) {
	 		sock = servsock.accept(); // wait for the next client connection
	 		new Worker(sock).start(); // create worker to handle it
	 	}	
	}
}