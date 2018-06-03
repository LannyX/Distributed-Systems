import java.io.*;  // Get the Input Output libraries

public class ArgsSample {

  public static void main(String args[]) throws IOException {
    String port = "4520";
    String server = "localhost";
    
    switch (args.length) {
    case 1:
      port=args[0];
      break;
    case 2:
      port=args[0];
      server=args[1];
    default:
      System.out.println("Usage: %java ArgsSample <port> <server>\n");
      System.out.println("Using Server: " + server +  ", Port: " + port + "\n");
    }
  }
}