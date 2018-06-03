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
import java.util.List;
import java.util.Random;
import java.util.LinkedList;


class Worker extends Thread{ //class definition
	Socket sock;			// class member
	static String name;
	String command;
	static List<String> jokeList = new LinkedList<>();
	static List<String> proList = new LinkedList<>();
	
	Worker (Socket s) {
		sock = s;
	} // constructor
	

	public void run(){
		// get I/O stream in/out from the socket
		PrintStream out = null;
		BufferedReader in = null;
		try{
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintStream(sock.getOutputStream());
			try{
				String state = in.readLine();
				if(state.equals("")){
					// get state from mode server
					command = mWorker.command;
					System.out.println("Right now runs under " + command + " Mode.");
					printJorP(command, out);
				}

			}catch(IOException x){
				System.out.println("Server read error");
				x.printStackTrace();
			}
			sock.close(); //close this one connection
		}catch(IOException ioe){System.out.println(ioe);}
		
		
	}
	
	static void printJorP(String command, PrintStream out) {
		String output;

		//print different output depends on the mode
		if(command.equals("j")){
			output = jokeRandom();
			out.println(output);
		}
		else if (command.equals("p")){
			output = proRandom();
			out.println(output);
		}
		else if (command.equals("m")){
			output = "Right now, under maintenance";
			out.println(output); 
		}
		else {
			output = "Wrong input";
		}
		
		System.out.println(output);
	} 

	static String jokeRandom() {
		String joke;
		Random rand = new Random();
		int ranNum;
		
		if(jokeList.isEmpty()){
			//add jokes to the set/ list
			//from funkidsjokes.com
			jokeList.add("JA: Why can’t Cinderella play soccer?  Because she always runs away from the ball.");
			jokeList.add("JB: How do soccer players stay cool during games?  They stand near the fans.");
			jokeList.add("JC: Why do soccer players do so well in school? They know how to use their heads.");
			jokeList.add("JD: What runs around a soccer field but never moves? A fence!");

			ranNum= rand.nextInt(jokeList.size());
			joke = jokeList.get(ranNum);
			jokeList.remove(ranNum);
		}else{
			ranNum= rand.nextInt(jokeList.size());
			joke = jokeList.get(ranNum);
			jokeList.remove(ranNum);
		}
		return joke ;
	}
	
	static String proRandom() {
		String pro;
		Random rand = new Random();
		int ranNum;
		
		if(proList.isEmpty()){
			//add proverbs to the set/ list
			//from https://addicted2success.com/quotes/30-famous-quotes-that-will-inspire-success-in-you/
			proList.add("PA: If you don’t build your dream, someone else will hire you to help them build theirs.");
			proList.add("PB: Great minds discuss ideas; average minds discuss events; small minds discuss people.");
			proList.add("PC: I have not failed. I’ve just found 10,000 ways that won’t work.");
			proList.add("PD: No one can make you feel inferior without your consent.");

			ranNum= rand.nextInt(proList.size());
			pro = proList.get(ranNum);
			proList.remove(ranNum);
		}else{		
			ranNum= rand.nextInt(proList.size());
			pro = proList.get(ranNum);
			proList.remove(ranNum);
		}
		return pro ;
	}
}

class mServer implements Runnable{
	//set the switch to true
	public static boolean adSwitch = true;
	
	public void run(){
		int q_len = 6; 
		// can't go below 1100
		int Port = 2334;
		//TCP/ip implementation
		Socket Sock;
		
		try{
			//Create a server socket listening at port 2334
			ServerSocket mservsock = new ServerSocket(Port, q_len);
			while(adSwitch){
				Sock = mservsock.accept();
				new mWorker(Sock).start();
			}
		}catch(IOException x){
			System.out.println(x);
		}
	}
}
//mode worker, to determine return either joke or proverb
class mWorker extends Thread{
	Socket sock;			// class member
	static String command = "j";
	mWorker (Socket s) {sock = s;} // constructor
	
	public void run(){
		// get I/O stream in/out from the socket
		PrintStream out = null;
		BufferedReader in = null;
		try{
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintStream(sock.getOutputStream());
			try{
				//get the what mode user want
				command = in.readLine();
				
				//Three modes depends on the input
				if(command.equals("j")){ //if 'j' is current state, enter joke mode
					out.println("Jokes");
					System.out.println("Joke mode");
				}
				
				else if(command.equals("p")){ //if 'p' is current state, enter joke mode
					out.println("Proverbs");
					System.out.println("Proverb mode");
				}
				
				else if(command.equals("m")){ //if 'm' is current state, enter joke mode
					out.println("Maintenance");
					System.out.println("Maintenance mode");
				}else{
					out.println("I don't understand what do you want, please enter: 'j' or 'p' or 'm': ");//neither of those three
					System.out.println("please enter: 'j' or 'p' or 'm': ");
				}

			}catch(IOException x){
				System.out.println(x);
			}
			sock.close(); //close this connection
		}catch(IOException ioe){System.out.println(ioe);}
	}
}



public class JokeServer{
	public static void main(String a[]) throws IOException{

		// do not need to change
		int len = 6; 
		// can't go below 1100
		int port = 2333;
		//TCP/ip implementation
		Socket sock;
		
		mServer mServer = new mServer();
		Thread mThread = new Thread(mServer);
		mThread.start();
		
		ServerSocket servsock = new ServerSocket(port, len);

		System.out.println("Lanny's Joke server 1.0 starting up, listening at port 2333.\n");
	 	while (true) {
	 		sock = servsock.accept(); // wait for future connections
	 		new Worker(sock).start(); // create server worker to do the main things
	 	}

	}
}