import java.io.*;
import java.net.*;

public class MyTelnetClient{
  public static void main (String args[]) {
    String serverName;
    if (args.length < 1) serverName = "localhost";
    else serverName = args[0];

    Socket sock;
    BufferedReader fromServer;
    PrintStream toServer;
    String textFromServer;

    System.out.println("Lanny's MyTelnet Client, 1.0.\n");
    System.out.println("Using server: " + serverName + ", Port: 80");
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    try {
      sock = new Socket(serverName, 80);
      fromServer =
        new BufferedReader(new InputStreamReader(sock.getInputStream()));
      toServer = new PrintStream(sock.getOutputStream());

      String dataToSend;
      do {
        System.out.print
          ("Enter text to send to the server, <stop> to end: ");
        System.out.flush ();
        dataToSend = in.readLine ();
        if (dataToSend.indexOf("stop") < 0){
	  toServer.println(dataToSend);
	  toServer.flush();
	}
      } while (dataToSend.indexOf("stop") < 0);
      for (int i = 1; i <=20; i++){
        textFromServer = fromServer.readLine();
        if (textFromServer != null) System.out.println(textFromServer);
      }
      sock.close();
    } catch (IOException x) {x.printStackTrace ();}
  }
}