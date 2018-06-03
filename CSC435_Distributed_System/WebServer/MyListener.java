import java.io.*; //import io
import java.net.*;

class lWorker extends Thread{ //class definition
	Socket sock;			// class member
	lWorker (Socket s) {sock = s;} // constructor

	public void run(){
		// get I/O stream in/out from the socket
		PrintStream out = null;
		BufferedReader in = null;
		try{
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintStream(sock.getOutputStream());
			String sockdata;
		    while (true) {
		    	sockdata = in.readLine ();
		        if (sockdata != null) System.out.println(sockdata);
		        System.out.flush ();
		     }  
			//sock.close(); //close this one connection
		}catch(IOException ioe){System.out.println("Listen again...");}
	}
}

public class MyListener{
	public static void main(String a[]) throws IOException{
		// do not need to change
		int q_len = 6; 
		// can't go below 1100
		int port = 2540;
		//TCP/ip implementation
		Socket sock;
		
		ServerSocket servsock = new ServerSocket(port, q_len);

		System.out.println("Lanny's Port Listener listening at port 2540.\n");
	 	while (true) {
	 		sock = servsock.accept(); // wait for the next client connection
	 		new lWorker(sock).start(); // create worker to handle it
	 	}	
	}
}