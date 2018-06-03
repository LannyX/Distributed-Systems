import java.io.*; //import io
import java.net.*;

public class InetClient{
	public static void main(String args[]){
		String serverName;
		if(args.length < 1)serverName = "localhost";
		else serverName = args[0];

		System.out.println("Lanny's Inet Client, 1.0.\n");
		System.out.println("Using server: " + serverName + ", Port: 2333");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		try{
			 String name;
			 do{
			 System.out.print
			 ("Enter a hostname or an IP address, (quit) to end: ");
			 System.out.flush ();
			 name = in.readLine (); //read input
			 if (name.indexOf("quit") < 0)
			 	getRemoteAddress(name, serverName); //print the server address
			 }while (name.indexOf("quit") < 0); // // when user type quit, quit
			 	System.out.println ("Cancelled by user request.");
			 }catch (IOException x) {x.printStackTrace ();}
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
	static void getRemoteAddress (String name, String serverName){ /*a method using name to get address*/
		Socket sock;
		BufferedReader fromServer;
		PrintStream toServer;			 
		String textFromServer;
			 
		try{
		/*open connection to server port*/				
		sock = new Socket(serverName, 2333);
		// create filter I/O streams
		fromServer =
			new BufferedReader(new InputStreamReader(sock.getInputStream()));
		toServer = new PrintStream(sock.getOutputStream());
		// send machine name / ip address to server
		toServer.println(name); toServer.flush();
		// read 2 or 3 lines of response from the server
		for (int i = 1; i <=3; i++){
			textFromServer = fromServer.readLine();
			if (textFromServer != null) System.out.println(textFromServer);
		}
		sock.close(); //close the socket
		}catch (IOException x) { /*check for the error*/
			System.out.println ("Socket error.");
			x.printStackTrace ();
		}
	}
}
