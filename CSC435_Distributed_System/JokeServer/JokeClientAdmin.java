/*--------------------------------------------------------

1. Name / Date: Lanny Xu /Jan 21

2. Java version used, if not the official version for the class:

e.g. build 1.5.0_06-b05

3. Precise command-line compilation examples / instructions:

e.g.:

> javac JokeServer.java


4. Precise examples / instructions to run this program:

e.g.:

In separate shell windows:

> java JokeServer
> java JokeClient
> java JokeClientAdmin

All acceptable commands are displayed on the various consoles.

This runs across machines, in which case you have to pass the IP address of
the server to the clients. For exmaple, if the server is running at
140.192.1.22 then you would type:

> java JokeClient 140.192.1.22
> java JokeClientAdmin 140.192.1.22

5. List of files needed for running the program.

e.g.:

 a. checklist.html
 b. JokeServer.java
 c. JokeClient.java
 d. JokeClientAdmin.java

5. Notes:


----------------------------------------------------------*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class JokeClientAdmin {
	public static void main(String[] args) {
	
		String serverName;
		//set server name to local host
		if (args.length < 1){
			serverName = "localhost";
		}else {
		serverName = args[0];	//get server name, to print later
		}	

		System.out.println(" Xu's JokeClientAdmin, 1.0\n");// print out my server version and name
		System.out.println("The server name is  " + serverName + ", and at 2334 Port"); //print out server name and the port number
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		try {
			 String mode;
			 do {
			 	System.out.print("joke mode enter 'j', proverb mode enter 'p', maintenance mode enter 'm': ");
			 	System.out.flush ();
			 	mode = in.readLine (); // read the input
			 if (mode.indexOf("quit") < 0)
			 	command(mode, serverName); //print the server address
			 } while (mode.indexOf("quit") < 0); // when user type quit, however, quitSmoking.com is unbearable 
			 	System.out.println ("Cancelled by user request.");
			 } catch (IOException y) 
			 	{y.printStackTrace ();} 
			 }

		static void command (String mode, String serverName){ /*a method using name to get address*/
			 Socket sock;
			 BufferedReader fromServer;
			 PrintStream toServer;
			 String tFromServer;
			 
			 try{
				 //connect to server number 2334 port
				sock = new Socket(serverName, 2334);

				 // input and output generator, should be different from client
				fromServer =
				new BufferedReader(new InputStreamReader(sock.getInputStream()));
				// receive data 
				toServer = new PrintStream(sock.getOutputStream());
				 // send information to server
				toServer.println(mode);
				toServer.flush();

				tFromServer = fromServer.readLine();
				if (tFromServer != null) System.out.println(tFromServer);

				sock.close(); //shut socket down
				} 
			 	catch (IOException x) { //check for the error
				System.out.println ("ioe exception occurs.");
				x.printStackTrace ();
			 	}
		}
}