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

import java.io.*; //import io
import java.net.*;
import java.util.Scanner;

public class JokeClient {
	static String serverName;
	static String name;
	public static void main(String args[]) throws IOException{
		//set server name to local host
		if(args.length < 1){
			serverName = "localhost";
		}else{
		serverName = args[0];
		} //get server name, to print later

		System.out.println("Lanny's Joke Client, 1.0.\n");
		System.out.println("The server name is  " + serverName + ", and at 2333 Port"); //print out server name and the port number
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		//ask client's name
		System.out.println("Here, you need to enter your name so I know who you are, or enter quit to you know, quit； ");
		name = in.readLine (); //read clients' name

		try{
			 String command;
			 do{
			 		//ask client to input
				 	System.out.println("Hello! "+ name+ "! Let me tell you a joke or proverb, hit (Enter)! or (quit) to end: ");
				 	System.out.flush ();
				 	command = in.readLine();
				 	
				 	if (command.indexOf("quit") < 0){
				 		contentFromServer(command);
				 	}
				 		
			 	
			 	}while (command.indexOf("quit") < 0); // // when user type quit, quit
			 	System.out.println ("User cancelled.");
			 }catch (IOException x) {x.printStackTrace ();}
 	}
	
	
	static void contentFromServer (String command){ /*a method using name to get address*/
		 Socket sock;
		 BufferedReader fromServer;
		 PrintStream toServer;
		 String tFromServer;
		 
		 try{
			 //connect to server number 2333 port
			 sock = new Socket(serverName, 2333);

			 // input and output generator, should be different from client
			 fromServer =
			 new BufferedReader(new InputStreamReader(sock.getInputStream()));
			 // receive data 
			 toServer = new PrintStream(sock.getOutputStream());
			 
			 // send machine name / ip address to server
			 toServer.println(command); 
			 toServer.flush();
			 // read response from the server
			 // send information to server
			tFromServer = fromServer.readLine();
			if (tFromServer != null) System.out.println(name + "! check this out: " + tFromServer);
				sock.close(); //shut socket down
			} 
			catch (IOException x) { //check for the error
				System.out.println ("ioe exception occurs.");
				x.printStackTrace ();
			}
	}
}
