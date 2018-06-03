/*
Version 1.0 2017-09-03
Author: Elliot Trapp with ample help from Profess Clark Elliott and below web sources.
The web sources from Professor Clark's original code:
http://www.java2s.com/Code/Java/Security/SignatureSignAndVerify.htm
https://www.mkyong.com/java/java-digital-signatures-example/ (not so clear)
https://javadigest.wordpress.com/2012/08/26/rsa-encryption-example/
https://www.programcreek.com/java-api-examples/index.php?api=java.security.SecureRandom
https://www.mkyong.com/java/java-sha-hashing-example/
https://stackoverflow.com/questions/19818550/java-retrieve-the-actual-value-of-the-public-key-from-the-keypair-object
XML validator:
https://www.w3schools.com/xml/xml_validator.asp
XML / Object conversion:
https://www.mkyong.com/java/jaxb-hello-world-example/
/*--------------------------------------------------------
1. Elliot Trapp / 10/29/17
3. Precise command-line compilation examples / instructions:
e.g.:
> javac Blockchain.java
4. Precise examples / instructions to run this program:
In separate shell windows:
> java Blockchain 0
> java Blockchain 1
> java Blockchain 2
All acceptable commands are displayed on the various consoles.
----------------------------------------------------------*/

// Jaxb libraries
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.StringWriter;
import java.io.StringReader;
import java.io.*;

// Encryption libraries
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.NoSuchAlgorithmException;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.crypto.Cipher;
import java.util.Base64;


// Java.util libraries
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.AbstractQueue;

import java.text.*;
import java.net.*;
import java.io.*;
import java.util.Arrays;


// SHA-256 producer
import java.security.MessageDigest;


// Class that holds a record of the unverifiedBlocks and the globalBlockChain
public class Blockchain {
  public static LinkedList<BlockRecord> globalBlockChain = new LinkedList<BlockRecord>();

  // Default constructor
  Blockchain()
  {
  }

  // Main method
  public static void main(String[] args) throws Exception {

    // Will be the PID
    int pnum;

    if (args.length > 1) System.out.println("Special functionality is present \n");

    if (args.length < 1) pnum = 0;
    else if (args[0].equals("0")) pnum = 0;
    else if (args[0].equals("1")) pnum = 1;
    else if (args[0].equals("2")) pnum = 2;
    else pnum = 0;

    // Creates a process with PID based on argument
    Process p = new Process(pnum);

    // Start asynchronous listners and workers to listen for updated chains and blocks
    p.listen();

    // Tells the process to read it's input file
    p.getInput();

    p.cast();



  }
}


// Class for each process working on the blockchain
class Process {
  private static String FILENAME;

  private static final int iFNAME = 0;
  private static final int iLNAME = 1;
  private static final int iDOB = 2;
  private static final int iSSNUM = 3;
  private static final int iDIAG = 4;
  private static final int iTREAT = 5;
  private static final int iRX = 6;

  // A copy of the local blockChain for the process, gets updated when a new blockChain is recieved
  public static LinkedList<BlockRecord> localBlockChain = new LinkedList<BlockRecord>();
  public static Queue<BlockRecord> localUnverifiedBlocks = new LinkedList<BlockRecord>();
  int currentChainLength;
  int pnum;
  int UnverifiedBlockPort;
  int BlockChainPort;
  static BlockRecord workingBlock = null;
  static boolean send = true;

  // Default constructor
  Process(int pnum)
  {
    this.pnum = pnum;
    this.UnverifiedBlockPort = 4710 + pnum;
    this.BlockChainPort = 4810 + pnum;
    //this.localBlockChain = new LinkedList<BlockRecord>(); // Initialize the blockChain
    //this.localUnverifiedBlocks = new Queue<BlockRecord>(); // Initialize the unverifiedBlocks

  }

  public static String CSC435Block =
    "We will build this dynamically: <?xml version = \"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

  public static final String ALGORITHM = "RSA"; // Encryption algorithm

  public static String SignedSHA256; // Header for blocks

  // Server listening for blocks/chains recieved and then hands them off to threads for processing
  class processBlockListener implements Runnable
  {
      public Boolean run = true;
      int pnum = Process.this.pnum;

      processBlockListener (){}
        //Main loop, where all the real work is done
        public void run()
        {
          int port = Process.this.UnverifiedBlockPort;
          int q_len = 10; //how many connections the server will wait on at the exact same time

          System.out.println("Elliot Trapp's Blockchain process " + pnum);
          System.out.print("now listening for blocks at port: ");
          System.out.print( port + ".\n This class is based on Professor Clark Elliott's InetServer.");

          Socket socket;
          ServerSocket processServsock;

          System.out.println("Port number: " + port);

          try {

          processServsock = new ServerSocket(port,q_len);

          while (run)
          {

            System.out.println("Process: " + pnum + " Block Server Running...");

            //Accepts any connection made to the socket
            socket = processServsock.accept();
            //Creates a worker thread to handle the socket
            new processBlockWorker(socket).start();


          }

          processServsock.close();

        }
        catch (IOException x) {x.printStackTrace();}
      }

    }// END of CLASS processBlockListener

  class processChainListener implements Runnable
  {
      public Boolean run = true;
      int pnum = Process.this.pnum;
      processChainListener () {}

        //Main loop, where all the real work is done
        public void run()
        {
          int port = Process.this.BlockChainPort;
          int q_len = 10; //how many connections the server will wait on at the exact same time

          System.out.println("Elliot Trapp's Blockchain process " + pnum);
          System.out.print("now listening for verified chains at port: ");
          System.out.print( port + ".\n This class is based on Professor Clark Elliott's InetServer.");

          Socket socket;
          ServerSocket processServsock;

          try {

          processServsock = new ServerSocket(port,q_len);

          while (run)
          {

            System.out.println("Process: " + pnum +" Chain Server Running...");


            //Accepts any connection made to the socket
            socket = processServsock.accept();
            //Creates a worker thread to handle the socket
            new processChainWorker(socket).start();


          }
          processServsock.close();


        }
        catch (IOException x) {x.printStackTrace();}
      }

    }// END of CLASS processChainListener

  // Worker thread to handle blocks recieved by the process
  class processBlockWorker extends Thread
  {
    //Member variables
    Socket socket;
    BlockRecord returnBlock;
    Boolean run;

    //Constructor
    //Assigns argument s to member variable socket
    processBlockWorker (Socket s) {socket = s;}

    //Method to start the thread
    public void run()
    {
    //Initialize PrintStream & Buffer
    PrintStream out = null;
    BufferedReader in = null;

    //This is a real-time system so try/catch statements are helpful/essential
    try
    {

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintStream(socket.getOutputStream());

        BlockRecord returnBlock = null;
        String xmlBlock = "";
        String line = null;

        StringReader reader;

        System.out.println("Process " + Process.this.pnum + " recieving unverified block...");

        // Recieves the block as XML
        line = in.readLine();

        while (line != null)
        {
          xmlBlock += line + "\n";
          line = in.readLine();
        }
        try
        {
          returnBlock = Process.this.unmarshal(xmlBlock);

        } catch (Exception e) {e.printStackTrace();}

        // Process 0 put the block in the unverifiedBlocks
        if (pnum == 0)
        {
          System.out.println("Writing " + returnBlock.toString() + " to disk.");

          // Write to disk
          try(FileWriter fw = new FileWriter("xmlUnverifiedLedger.xml", true);
          BufferedWriter bw = new BufferedWriter(fw);
          PrintWriter outFile = new PrintWriter(bw))
          {
              outFile.print(xmlBlock);
          } catch (IOException e) {}

        }

        socket.close();

        this.work(returnBlock);


      //In case something goes wrong with accessing the server or i/o
      //Exception handeling
      } catch (IOException x)
        {
          System.out.println("Server read error");
          x.printStackTrace();
        }
    }

    public void work(BlockRecord block) // starts working on the process' unCheckChain
    {
      BlockRecord processedBlock = null;
      Process.this.workingBlock = block;

      // DEBGUG
      System.out.println("Started work() on: " + block.toString());
      // Process the block (hash and sign it)
      // workingBlock = this.localUnverifiedBlocks.get(0);
      processedBlock = processBlock(block);

      if (!Process.this.localBlockChain.contains(processedBlock) && Process.this.send)
      {
        System.out.println("Adding block to local chain");
        // Add it to the process' local blockchain
        Process.this.localBlockChain.add(processedBlock);

        System.out.println("this local chain :");
        for (BlockRecord o : Process.this.localBlockChain)
        {
          System.out.println(o.toString());
        }
        // Send out the local blockchain as a new blockchain
        Process.this.multicastChain(Process.this.localBlockChain);
      }

      Process.this.send = true;
      Process.this.workingBlock = null;

      // DEBGUG
      System.out.println("Completed work() on: " + block.toString());


    } // END OF work()

    public BlockRecord processBlock(BlockRecord block)
    {

      // Check to see if block is already in Blockchain **BAD CODE**
        if (Blockchain.globalBlockChain.contains(block))
        {
        System.out.println("Block: " + block.toString() + " already in global blockchain.");
        return null;
        }


        BlockRecord returnBlock = null;

        UUID idA = UUID.randomUUID();
        String suuid = UUID.randomUUID().toString();
        System.out.println("Unique Block ID: " + suuid + "\n");

        Date date = new Date();
        String T1 = String.format("%1$s %2$tF.%2$tT", "", date);
        String TimeStampString = T1 + "." + pnum;
        System.out.println("Timestamp: " + TimeStampString);

        System.out.println("How much work we did: ");

        // WORK PUZZLE:
        int numCheck = 100000;
        String solveString = "";


        // WORK PUZZLE:
    try {
      // Generate random String
      UUID uu;
      for (int i=0; i<1000; i++)
      {
        Thread.sleep(100);
        uu = UUID.randomUUID();
        int workHash = uu.hashCode();
        String randStr = new String(uu.toString());
        System.out.println(randStr);

        numCheck = workHash & 0xFFFF;
        System.out.println("numCheck: " + numCheck);
        solveString = randStr;
        if (numCheck < 1000)
        {
            System.out.println(i + " tenths of a second.\n");
            break;
        }
      }
        } catch (InterruptedException e) {}
        System.out.println("Final solveString: " + solveString);
        block.setSolveString(solveString);


        // PROFESSOR's CODE:
        //   int randval;
        //   Random r = new Random();
        //   for (int i=0; i<1000; i++){
        //     Thread.sleep(100);
        //     randval = r.nextInt(100);
        //     if (randval < 4) {
        // System.out.println(i + " tenths of a second.\n");
        // break;
        //     }
        //   }

        // END WORK PUZZLE

        try {

          String LastHash;

          System.out.println("localBlockchain size: " + Process.this.localBlockChain.size());
          System.out.println("localBlockchain: ");
          for (BlockRecord d : Process.this.localBlockChain)
          {
            System.out.println(d.toString());
          }


          // Set the last hash string
          if (Process.this.localBlockChain.size() > 0)
          {
            System.out.println("last hash set!");
            LastHash = Process.this.localBlockChain.getLast().getSignedSHA256();
          }
          else
          {
            //System.out.println("last hash set!");
            LastHash = "";
          }
          block.setLastHash(LastHash);
          // Set timestamp
          block.setTimeStamp(TimeStampString);

          String stringXML = block.toXML();
          CSC435Block = stringXML;

          // Get SHA-256 of block
          MessageDigest md = MessageDigest.getInstance("SHA-256");
          md.update (CSC435Block.getBytes());
          byte byteData[] = md.digest();

          // Convert to HEX
          StringBuffer sb = new StringBuffer();
          for (int i = 0; i < byteData.length; i++) {
      sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
          }

          String SHA256String = sb.toString();

          KeyPair keyPair = generateKeyPair(999);

          byte[] digitalSignature = signData(SHA256String.getBytes(), keyPair.getPrivate());

          boolean verified = verifySig(SHA256String.getBytes(), keyPair.getPublic(), digitalSignature);
          System.out.println("Has the signature been verified: " + verified + "\n");

          System.out.println("Original SHA256 Hash: " + SHA256String + "\n");

          // Sign the string

          SignedSHA256 = Base64.getEncoder().encodeToString(digitalSignature);
          System.out.println("The signed SHA-256 string: " + SignedSHA256 + "\n");
          byte[] testSignature = Base64.getDecoder().decode(SignedSHA256);
          System.out.println("Testing restore of signature: " + Arrays.equals(testSignature, digitalSignature));
          verified = verifySig(SHA256String.getBytes(), keyPair.getPublic(), testSignature);
          System.out.println("Has the restored signature been verified: " + verified + "\n");

          String fullBlock = stringXML.substring(0,stringXML.indexOf("<blockID>")) +
          "<SignedSHA256>" + SignedSHA256 + "</SignedSHA256>\n" +
          "    <SHA256String>" + SHA256String + "</SHA256String>\n    " +
        stringXML.substring(stringXML.indexOf("</blockID>"));

          // Unmarshal the XML
          returnBlock = Process.this.unmarshal(stringXML);

          returnBlock.setSHA256String(SHA256String);
          returnBlock.setSignedSHA256(SignedSHA256);
          returnBlock.setVerificationProcessID(Integer.toString(pnum));

          // DEBUG
          // Encrypt the hash string and create the cipherText
          final byte[] cipherText = encrypt(SHA256String,keyPair.getPublic());

          // Decrypt the cipherText using the private key to get the raw message
          final String plainText = decrypt(cipherText, keyPair.getPrivate());

          System.out.println("\nExtra functionality in case you want it:");
          System.out.println("Encrypted Hash string: " + cipherText.toString());
          System.out.println("Original (now decrypted) Hash string: " + plainText);

        } catch (Exception e) {
          e.printStackTrace();
        }
        return returnBlock;

    } // END OF processBlock()

    } // END OF class processBlockWorker

  // Worker thread to handle blocks recieved by the process
  class processChainWorker extends Thread
  {
    //Member variables
    Socket socket;

    //Constructor
    //Assigns argument s to member variable socket
    processChainWorker (Socket s) {socket = s;}

    //Method to start the thread
    public void run()
    {
    //Initialize PrintStream & Buffer
    PrintStream out = null;
    BufferedReader in = null;

    //This is a real-time system so try/catch statements are helpful/essential
    try
    {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintStream(socket.getOutputStream());


        String xmlChain = ""; // The raw XML string of the blockChain
        LinkedList<BlockRecord> returnChain = new LinkedList<BlockRecord>(); // The java object that it eventually built
        String line = null; // For reading each line that is sent
        String oneBlock = ""; // For storing each block in the chain
        BlockRecord block = null; // Java object of each block

        System.out.println("Process " + Process.this.pnum + " recieving new chain.");

        // Recieves the chain as XML
        line = in.readLine();

        while (line != null)
        {
          xmlChain += line + "\n";
          oneBlock += line + "\n";

          if (line.equals("</blockRecord>")) // reached the end of a block
          {
            try
            {
              block = Process.this.unmarshal(oneBlock);

            } catch (Exception e) {e.printStackTrace();}

            returnChain.add(block);

            oneBlock = "";
          }
          line = in.readLine();
        }
        // Process 0 is responsible for writing to the ledger and updating the globalBlockChain
        if (pnum == 0)
        {

          System.out.println("Global Blockchain size: " + Blockchain.globalBlockChain.size() + "\n");
          System.out.println("Return Blockchain size: " + returnChain.size() + "\n");

          // Checks to make sure it is longer
          if (Blockchain.globalBlockChain.size() < returnChain.size())
          {
            // Copy to globalBlockChain
            Blockchain.globalBlockChain.clear();
            Blockchain.globalBlockChain.addAll(returnChain);

            // Write to ledger
            BufferedWriter writer = new BufferedWriter(new FileWriter("BlockchainLedger.xml"));
            writer.write(xmlChain);
            writer.close();

            // Prints out new blockchain
            System.out.println("Updated Blockchain object: ");
            for (BlockRecord b : Blockchain.globalBlockChain)
            {
              System.out.println(b.toString());
            }
          }
        }

        if (Process.this.localBlockChain.size() < returnChain.size())
        {
          Process.this.localBlockChain.clear();
          Process.this.localBlockChain.addAll(returnChain);
        }

        // If we just added the block currently being worked on, stop work and start over
        if (Process.this.localBlockChain.contains(Process.this.workingBlock))
        {
          Process.this.send = false;
        }



        socket.close();

      //In case something goes wrong with accessing the server or i/o
      //Exception handeling
      } catch (IOException x)
        {
          System.out.println("Server read error");
          x.printStackTrace();
        }
    }
    } // END OF class processChainWorker

  public void getInput()
  {

    // Update copy global blockchain into localBlockChain
    this.localBlockChain.addAll(Blockchain.globalBlockChain);

    System.out.println("Process number: " + pnum + " Ports: " + UnverifiedBlockPort + " " +
           BlockChainPort + "\n");

    switch(pnum){
    case 1: FILENAME = "BlockInput1.txt"; break;
    case 2: FILENAME = "BlockInput2.txt"; break;
    default: FILENAME= "BlockInput0.txt"; break;
    }

    /*******************************************BEGAN READING INPUT*****************************************/

    System.out.println("Using input file: " + FILENAME);

    try {
      try (BufferedReader br = new BufferedReader(new FileReader(FILENAME))) {
    String[] tokens = new String[10];
    String stringXML;
    String InputLineStr;
    String suuid;
    UUID idA;

    JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecord.class);
    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
    StringWriter sw = new StringWriter();

    // CDE Make the output pretty printed:
    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

    int n = 0;
    while ((InputLineStr = br.readLine()) != null) {

      BlockRecord temp = new BlockRecord();

      temp.setSHA256String("SHA string goes here...");
      temp.setSignedSHA256("Signed SHA string goes here...");

      // Create BlockID
      idA = UUID.randomUUID();
      suuid = new String(UUID.randomUUID().toString());
      temp.setBlockID(suuid);
      temp.setCreatingProcess("Process" + Integer.toString(pnum));
      temp.setVerificationProcessID("To be set later...");
      temp.setLastHash("To be set later...");
      temp.setSolveString("To be set later...");

      // Put data into file
      tokens = InputLineStr.split(" +");
      temp.setFSSNum(tokens[iSSNUM]);
      temp.setFFname(tokens[iFNAME]);
      temp.setFLname(tokens[iLNAME]);
      temp.setFDOB(tokens[iDOB]);
      temp.setGDiag(tokens[iDIAG]);
      temp.setGTreat(tokens[iTREAT]);
      temp.setGRx(tokens[iRX]);
      this.localUnverifiedBlocks.add(temp);
      n++;
    }
    System.out.println(n + " records read.");

    System.out.println("\n");

    // stringXML = sw.toString();
    // for(int i=0; i < n; i++)
    // {
    //   jaxbMarshaller.marshal(this.localUnverifiedBlocks.get(i), sw);
    // }
    // String fullBlock = sw.toString();
    // String XMLHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
    // String cleanBlock = fullBlock.replace(XMLHeader, "");
    // // Show the string of concatenated, individual XML blocks:
    // String XMLBlock = XMLHeader + "\n<BlockLedger>" + cleanBlock + "</BlockLedger>";
    // //System.out.println(XMLBlock);
    //

    System.out.println("Completed Reading Input!");


    if (pnum == 0)
    {

    // System.out.println("Creating ledger of unverified blocks...");
    //
    // BufferedWriter writer = new BufferedWriter(new FileWriter("xmlUnverifiedLedger.xml"));
    // writer.write(XMLBlock);
    // writer.close();

    System.out.println("Creating BlockchainLedger...");

    BufferedWriter writer2 = new BufferedWriter(new FileWriter("BlockchainLedger.xml"));
    writer2.write("");
    writer2.close();

    }


    System.out.println("Completed Writing Input!");

    /*******************************************COMPLETED READING INPUT*****************************************/


    } catch (IOException e) {e.printStackTrace();}
    } catch (Exception e) {e.printStackTrace();}
  } // END OF getInput()

public void listen() // Spawns threads to listen at ports for verified blocks
{
  //CREATE SERVER TO LISTEN
  Process.processBlockListener blockServ;
  blockServ  = this.new processBlockListener();

  Thread t = new Thread(blockServ);

  Process.processChainListener chainServ;
  chainServ = this.new processChainListener();

  Thread d = new Thread(chainServ);


  d.start();
  t.start();

}

public void cast()
{
  while (this.localUnverifiedBlocks.peek() != null)
  {
    multicastBlock(this.localUnverifiedBlocks.remove());
  }
}




public void multicastChain(Queue<BlockRecord> chain)
{
  System.out.println("Started multicastChain() " + chain.toString() + "...");

  Socket socket = null;
  int[] ports = {4810,4811,4812};

  PrintStream toServer;
  String serverName = "localhost";
  String newChain = ""; // Raw XML of the chain

  // Create the raw XML chain
  for (BlockRecord block : chain)
  {
    newChain += block.toXML();
  }
  try
  {

    // Open connection to server port
    for (int port : ports)
    {
      System.out.println("Casting " + chain.toString() + " to " + serverName + " at " + port + ".");

      socket = new Socket(serverName, port);
      toServer = new PrintStream(socket.getOutputStream());
      //Sends the chain to each listener
      toServer.println(newChain);
      toServer.flush();

      toServer.close();
    }

    socket.close();
    //Exception handeling
  } catch (IOException  x)
  {
    System.out.println ("Server not running...");
  }


  System.out.println("...Completed multicastChain() " + chain.toString() + ".");
} // END of multicastChain()

// Sending out a new blockchain with a completed block
public void multicastBlock(BlockRecord block) // Unverified block?
{
    // send to a list of ips / ports

    Socket socket = null;
    int[] ports = new int[3];


    ports = new int[] {4710, 4711, 4712};

    PrintStream toServer;
    String serverName = "localhost";
    String CSC435Block = null;
    String stringXML;

    try
    {

      try
      {
        CSC435Block = block.toXML();

      } catch (Exception e) {e.printStackTrace();}


      // Open connection to server port
      for (int port : ports)
      {
        System.out.println("Casting " + block.toString() + " to " + serverName + " at " + port + ".");

        socket = new Socket(serverName, port);
        toServer = new PrintStream(socket.getOutputStream());


        //Sends the block to each listener
        toServer.println(CSC435Block);
        toServer.flush();

        toServer.close();
      }

      socket.close();
      //Exception handeling
    } catch (IOException x)
    {
      System.out.println ("Server not running...");
    }

    System.out.println("...Completed multicastBlock() " + block.toString() + ".");


} // END OF multicastBlock()

public static byte[] signData(byte[] data, PrivateKey key) throws Exception {
  Signature signer = Signature.getInstance("SHA1withRSA");
  signer.initSign(key);
  signer.update(data);
  return (signer.sign());
} // END OF signData()

public static boolean verifySig(byte[] data, PublicKey key, byte[] sig) throws Exception {
  Signature signer = Signature.getInstance("SHA1withRSA");
  signer.initVerify(key);
  signer.update(data);

  return (signer.verify(sig));
} // END OF verifySig()

public static KeyPair generateKeyPair(long seed) throws Exception {
  KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
  SecureRandom rng = SecureRandom.getInstance("SHA1PRNG", "SUN");
  rng.setSeed(seed);
  keyGenerator.initialize(1024, rng);

  return (keyGenerator.generateKeyPair());
} // END OF generateKeyPair()

public static byte[] encrypt(String text, PublicKey key) {
  byte[] cipherText = null;
  try {
    final Cipher cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE, key);
    cipherText = cipher.doFinal(text.getBytes());
  } catch (Exception e) {
    e.printStackTrace();
  }
  return cipherText;
} // END OF encrypt()

public static String decrypt(byte[] text, PrivateKey key) {
  byte[] decryptedText = null;
  try {
    final Cipher cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(Cipher.DECRYPT_MODE, key);
    decryptedText = cipher.doFinal(text);
  } catch (Exception ex) {
    ex.printStackTrace();
  }
  return new String(decryptedText);
} // END OF decrypt()

public BlockRecord unmarshal(String xml)
{
  BlockRecord returnBlock = null;
  try
  {
    StringReader reader;
    Unmarshaller jaxbUnmarshaller;
    JAXBContext jaxbContext;
    jaxbContext = JAXBContext.newInstance(BlockRecord.class);
    jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    reader = new StringReader(xml);
    returnBlock = (BlockRecord) jaxbUnmarshaller.unmarshal(reader);
  } catch (Exception e) {System.out.println("Problem with unmarshal()");}

  return returnBlock;
}

} // END OF CLASS PROCESS


@XmlRootElement
class BlockRecord{
  String LastHash;
  String SolveString;
  String SHA256String;
  String SignedSHA256;
  String BlockID;
  String VerificationProcessID;
  String CreatingProcess;
  String Fname;
  String Lname;
  String SSNum;
  String DOB;
  String Diag;
  String Treat;
  String Rx;
  String TimeStamp;

  public String toXML()
  {
    String returnString = "";

    try
    {

      // Tools
      JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecord.class);
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
      StringWriter sw = new StringWriter();

      // Clean it up
      jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

      // Do the actual marshaling
      jaxbMarshaller.marshal(this, sw);
      String stringXML = sw.toString();
      returnString = stringXML;
    } catch (Exception e) {System.out.println("Error converting to XML");}

    return returnString;

  }

  public String getTimeStamp() {return TimeStamp;}
  @XmlElement
    public void setTimeStamp(String TS){this.TimeStamp = TS;}

  public String getLastHash() {return LastHash;}
  @XmlElement
    public void setLastHash(String LH){this.LastHash = LH;}

  public String getSolveString() {return SolveString;}
  @XmlElement
    public void setSolveString(String SS){this.SolveString = SS;}

  public String getSHA256String() {return SHA256String;}
  @XmlElement
    public void setSHA256String(String SH){this.SHA256String = SH;}

  public String getSignedSHA256() {return SignedSHA256;}
  @XmlElement
    public void setSignedSHA256(String SH){this.SignedSHA256 = SH;}

  public String getCreatingProcess() {return CreatingProcess;}
  @XmlElement
    public void setCreatingProcess(String CP){this.CreatingProcess = CP;}

  public String getVerificationProcessID() {return VerificationProcessID;}
  @XmlElement
    public void setVerificationProcessID(String VID){this.VerificationProcessID = VID;}

  public String getBlockID() {return BlockID;}
  @XmlElement
    public void setBlockID(String BID){this.BlockID = BID;}

  public String getFSSNum() {return SSNum;}
  @XmlElement
    public void setFSSNum(String SS){this.SSNum = SS;}

  public String getFFname() {return Fname;}
  @XmlElement
    public void setFFname(String FN){this.Fname = FN;}

  public String getFLname() {return Lname;}
  @XmlElement
    public void setFLname(String LN){this.Lname = LN;}

  public String getFDOB() {return DOB;}
  @XmlElement
    public void setFDOB(String DOB){this.DOB = DOB;}

  public String getGDiag() {return Diag;}
  @XmlElement
    public void setGDiag(String D){this.Diag = D;}

  public String getGTreat() {return Treat;}
  @XmlElement
    public void setGTreat(String D){this.Treat = D;}

  public String getGRx() {return Rx;}
  @XmlElement
    public void setGRx(String D){this.Rx = D;}

  public String toString(){return getFLname() + " " + getBlockID();}

  public boolean equals(Object o)
  {
    if (!(o instanceof BlockRecord)) {return false;}

    BlockRecord b = (BlockRecord)o;

    return this.getBlockID().equals(b.getBlockID());
  }

}