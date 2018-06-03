/* 
Lanny
Version 2.0 2018-02-18

Author: Clark Elliott, with ample help from the below web sources.

You are free to use this code in your assignment, but you MUST add
your own comments. Leave in the web source references.

This is pedagogical code and should not be considered current for secure applications.

The web sources:

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
*/

//some JAXB libraries
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

//import some encryption libraries
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

//import Some other uitilities

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.text.*;
import java.util.Base64;
import java.util.Arrays;
// Produces a 64-bye string representing 256 bits of the hash output. 4 bits per character
import java.security.MessageDigest; // SHA-256 producer that produce hash


public class Blockchain {
	//create a global queue to store the objects
	public static List<BlockRecord> global = new LinkedList<BlockRecord>();
	  
	  public static void main(String[] args) throws Exception {
	    //The PID number
	    int pnum;

	    if (args.length > 1) System.out.println("Extra function \n");

	    if (args.length < 1) pnum = 0;
	    else if (args[0].equals("0")) pnum = 0;
	    else if (args[0].equals("1")) pnum = 1;
	    else if (args[0].equals("2")) pnum = 2;
	    else pnum = 0;

	    // use PID to start a new process
	    Process p1 = new Process(pnum);

	    // start listners for blocks and chains
	    p1.listen();

	    // process needs to read the input as well
	    p1.readInput();
	    //cast the blocks
	    p1.cast();

	  }
}

//when start a new process, this class will get to work
class Process{
	  private static String FILENAME;
	  //indexes for input as Token, use int to present
	  private static final int iFNAME = 0;
	  private static final int iLNAME = 1;
	  private static final int iDOB = 2;
	  private static final int iSSNUM = 3;
	  private static final int iDIAG = 4;
	  private static final int iTREAT = 5;
	  private static final int iRX = 6;
	  
	  // create different data structures to store the record
	  public static Queue<BlockRecord> local = new LinkedList<BlockRecord>();
	  public static Queue<BlockRecord> unverified = new LinkedList<BlockRecord>();
	 
	  //initiate the process nums and port nums in the beginning 
	  int pnum;
	  int UnverifiedBlockPort;
	  int BlockChainPort;
	  static BlockRecord b = null;
	  static boolean added = true;

	  //process constructor
	  Process(int pnum)
	  {
	    this.pnum = pnum;
	    // to avoid port conflicts, set different nums to different port
	    this.UnverifiedBlockPort = 4710 + pnum;
	    this.BlockChainPort = 4820 + pnum;
	  }
	  
	  //unmarshal method
	  public BlockRecord unmarshal(String xml){
	    BlockRecord b1 = null;
	    try{
	      JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecord.class);
	      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
	      StringReader reader = new StringReader(xml);
	      b1 = (BlockRecord) jaxbUnmarshaller.unmarshal(reader);
	    } catch (Exception e) {System.out.println("unmarshal fail");}
	    return b1;
	  }
	  
	   //start the signData
	   public static byte[] signData(byte[] data, PrivateKey key) throws Exception {
			  Signature signer = Signature.getInstance("SHA1withRSA");
			  signer.initSign(key);
			  signer.update(data);
			  return (signer.sign());
		} 
		//end of the signData
	   
		//start the verifySig
		public static boolean verifySig(byte[] data, PublicKey key, byte[] sig) throws Exception {
			  Signature signer = Signature.getInstance("SHA1withRSA");
			  signer.initVerify(key);
			  signer.update(data);

			  return (signer.verify(sig));
		} 
		//end of verifySig()
		
		//start the generateKeyPair
		public static KeyPair generateKeyPair(long seed) throws Exception {
			  KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
			  SecureRandom rng = SecureRandom.getInstance("SHA1PRNG", "SUN");
			  rng.setSeed(seed);
			  keyGenerator.initialize(1024, rng);

			  return (keyGenerator.generateKeyPair());
		} 
		// end of generateKeyPair()
		
		//start encrypt()
		public static byte[] encrypt(String text, PublicKey key) {
			byte[] cipherText = null;
			try {
				final Cipher cipher = Cipher.getInstance(ALGORITHM); // Get RSA cipher object
			    cipher.init(Cipher.ENCRYPT_MODE, key);
			    cipherText = cipher.doFinal(text.getBytes());
			 } catch (Exception e) {
			    e.printStackTrace();
			 }
			    return cipherText;
		}
		//end of encrypt and start decrpt()

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
		}
		//end of decrypt
		
		public static String CSC435Block =
				"We will build this dynamically: <?xml version = \"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
		//encryption alg name
		public static final String ALGORITHM = "RSA"; 
		//Block header
		public static String SignedSHA256; 

		//Server listening for block/chain , then pass them to threads for processing
		class blockListener implements Runnable{
			public Boolean open = true;
		    int pnum = Process.this.pnum;
		    blockListener (){}
		    //run function that runs most of the work
	        public void run(){
		        int port = Process.this.UnverifiedBlockPort;
		        //the number of connections sever waits 
			    int q_len = 15; 

			    System.out.println("Lanny's Blockchain process " + pnum);
			    System.out.println("now listening for blocks at port: " + port);
	
			    Socket socket;
			    ServerSocket processServsock;
			    System.out.println("Port number: " + port);
			    try {
			          processServsock = new ServerSocket(port,q_len);
			          while (open){ 
			        	System.out.println("Process: " + pnum + " Block Server");
			    		//socket that accept connections 
			            socket = processServsock.accept();
			            //use a worker thread to work the socket 
			            new blockWorker(socket).start();
			          }
			          processServsock.close();
			        }catch (IOException x) {x.printStackTrace();}
		     }
		 }
		
		class chainListener implements Runnable{
			public Boolean open = true;
		    int pnum = Process.this.pnum;
		    chainListener (){}
		    //run function that runs most of the work
	        public void run(){
		        int port = Process.this.BlockChainPort;
		        //the number of connections sever waits 
			    int q_len = 15;
	
			    System.out.println("Lanny's Blockchain process " + pnum);
			    System.out.println("now listening for blocks at port: " + port);
	
			    Socket socket;
			    ServerSocket processServsock;
			    System.out.println("Port number: " + port);
			    try{
			    	processServsock = new ServerSocket(port,q_len);
			    	while (open){
			    		System.out.println("Process: " + pnum + " Chain Server");
			    		//socket that accept connections 
			            socket = processServsock.accept();
			            //use a worker thread to work the socket 
			            new chainWorker(socket).start();
			        }
			        processServsock.close();
			     }catch (IOException x) {x.printStackTrace();}
		     }
		}
		// Worker thread to work blocks 
		class blockWorker extends Thread
		{
	    //initiate variables
	    Socket socket;
	    BlockRecord b1;
	    Boolean run;

	    //set s to socket
	    blockWorker (Socket s) {socket = s;}
	    //run method that runs most of the work
	    public void run()
	    {
	    PrintStream out = null;
	    BufferedReader in = null;
	    //try/catch statements
	    try
	    {
	        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	        out = new PrintStream(socket.getOutputStream());
	        StringReader reader;	
	        b1= null;
	        String x1 = "";
	        String line = null;

	        System.out.println("Process " + Process.this.pnum + " unverified block");
	        // read the inputs
	        line = in.readLine();
	        //read all the lines
	        while (line != null)
	        {
	          x1 += line + "\n";
	          line = in.readLine();
	        }
	        try
	        {
	          b1 = Process.this.unmarshal(x1);

	        } catch (Exception e) {e.printStackTrace();}

	        // Process 0 saves the block
	        if (pnum == 0)
	        {
	          System.out.println("Writing " + b1.toString() + " to disk.");

	          // Write to disk
	          try(FileWriter fw = new FileWriter("xmlUnverifiedLedger.xml", true);
	          BufferedWriter bw = new BufferedWriter(fw);
	          PrintWriter outFile = new PrintWriter(bw))
	          {
	              outFile.print(x1);
	          } catch (IOException e) {}

	        }
	        socket.close();
	        this.work(b1);

	      //Exception handling
	      } catch (IOException | InterruptedException x){System.out.println("Server read error");}
	    }

	    public void work(BlockRecord block) throws InterruptedException // starts working on the process' unCheckChain
	    {
	      BlockRecord b3 = null;
	      Process.this.b = block;

	      b3 = processBlock(block);

	      if (!Process.this.local.contains(b3) && Process.this.added)
	      {
	        System.out.println("Adding block to local chain right now");
	        //add block to local chain
	        Process.this.local.add(b3);

	        System.out.println("this local chain :");
	        for (BlockRecord o : Process.this.local)
	        {
	          System.out.println(o.toString());
	        }
	        // Send out the local blockchain as a new blockchain
	        Process.this.multicastC(Process.this.local);
	      }

	      Process.this.added = true;
	      Process.this.b = null;

	    } 
	    // work ends

	    public BlockRecord processBlock(BlockRecord block) throws InterruptedException
	    {
	      // Check if the block is exit already
	        if (Blockchain.global.contains(block))
	        {
	        System.out.println("Block: " + block.toString() + " is in global");
	        return null;
	        }
	        BlockRecord b1 = null;
	        //generate unique id 
	        UUID idA = UUID.randomUUID();
	        String suuid = UUID.randomUUID().toString();
	        System.out.println("Unique Block ID: " + suuid + "\n");

	        Date date = new Date();
	        String T1 = String.format("%1$s %2$tF.%2$tT", "", date);
	        String TimeStampString = T1 + "." + pnum;
	        System.out.println("Timestamp: " + TimeStampString);

	        System.out.println("How much work we did: ");
	        int randval;
	        Random r = new Random();
	        for (int i=0; i<1000; i++){ // safety upper limit of 1000
	          Thread.sleep(100); // not really work, but OK for our purposes.
	          randval = r.nextInt(100); // Higher val = more work
	          if (randval < 4) {       // Lower threshold = more work
	    	System.out.println(i + " tenths of a second.\n");
	    	break;
	          }
	        }
	        try {
	          // deal with the lasthash
	          String LastHash;
	          System.out.println("localbc size:  " + Process.this.local.size());
	          System.out.println("localBc:  ");
	          for (BlockRecord d : Process.this.local)
	          {System.out.println(d.toString());}

	          // Set the last hash string
	          if (Process.this.local.size() > 0){
	            System.out.println("last hash");
	            LastHash = ((LinkedList<BlockRecord>) Process.this.local).getLast().getSignedSHA256();
	          }else{
	            LastHash = "";
	          }
	          block.setLastHash(LastHash);
	          // set the time stamp
	          block.setTimeStamp(TimeStampString);

	          String x1 = block.toXML();
	          CSC435Block = x1;

	          // generate block's SHA-256
	          MessageDigest md = MessageDigest.getInstance("SHA-256");
	          md.update (CSC435Block.getBytes());
	          byte byteData[] = md.digest();

	          // convert byte[] to hexdecimal
	          StringBuffer sb = new StringBuffer();
	          for (int i = 0; i < byteData.length; i++) {
	      sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
	          }

	          String SHA256String = sb.toString();

	          KeyPair keyPair = generateKeyPair(999);

	          byte[] digitalSignature = signData(SHA256String.getBytes(), keyPair.getPrivate());

	          boolean verified = verifySig(SHA256String.getBytes(), keyPair.getPublic(), digitalSignature);
	          System.out.println("Is signature verified?  " + verified + "\n");

	          System.out.println("Original SHA256 Hash: " + SHA256String + "\n");

	          // Sign the string
	          SignedSHA256 = Base64.getEncoder().encodeToString(digitalSignature);
	          System.out.println("The signed SHA-256 string: " + SignedSHA256 + "\n");
	          byte[] testSignature = Base64.getDecoder().decode(SignedSHA256);
	          System.out.println("Testing restore of signature: " + Arrays.equals(testSignature, digitalSignature));
	          verified = verifySig(SHA256String.getBytes(), keyPair.getPublic(), testSignature);
	          System.out.println("Has the restored signature been verified: " + verified + "\n");

	          String fullBlock = x1.substring(0,x1.indexOf("<blockID>")) +
	          "<SignedSHA256>" + SignedSHA256 + "</SignedSHA256>\n" +
	          "    <SHA256String>" + SHA256String + "</SHA256String>\n    " +
	        x1.substring(x1.indexOf("</blockID>"));

	          // Unmarshal the XML
	          b1 = Process.this.unmarshal(x1);

	          b1.setSHA256String(SHA256String);
	          b1.setSignedSHA256(SignedSHA256);
	          b1.setVerificationProcessID(Integer.toString(pnum));

	          // Encrypt hash
	          final byte[] cipherText = encrypt(SHA256String,keyPair.getPublic());

	          // Decrypt cipherText 
	          final String plainText = decrypt(cipherText, keyPair.getPrivate());

	          System.out.println("\nSome other function in case you want it:");
	          System.out.println("Encrypted Hash string: " + cipherText.toString());
	          System.out.println("Decrypted Hash string: " + plainText);

	        } catch (Exception e) {
	          e.printStackTrace();
	        }
	        return b1;

	    } 
	} 
	// class end

	// Worker thread to handle blocks recieved by the process
	class chainWorker extends Thread{
		    //s variables
		    Socket socket;
		    //set s to socket
		    chainWorker (Socket s) {socket = s;}

		    //run method that runs the work 
		    public void run(){
		    PrintStream out = null;
		    BufferedReader in = null;
		    //try/catch statements
		    try{
		        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		        out = new PrintStream(socket.getOutputStream());

		        //initiate XML string, data structure, read line, block
		        String x1 = ""; 
		        List<BlockRecord> l1 = new LinkedList<BlockRecord>(); 
		        String line = null; 
		        String b1 = ""; 
		        BlockRecord block = null;

		        System.out.println("Process " + Process.this.pnum + " recieving new chain.");

		        // read the chain as XML
		        line = in.readLine();
		        while (line != null)
		        {
		          x1 += line + "\n";
		          b1 += line + "\n";

		          //the block ends
		          if (line.equals("</blockRecord>")) 
		          {
		            try
		            {
		              block = Process.this.unmarshal(b1);

		            } catch (Exception e) {e.printStackTrace();}
		            l1.add(block);
		            b1 = "";
		          }
		          line = in.readLine();
		        }
		        // Process 0 writes ledger to disk
		        if (pnum == 0){
		          System.out.println("Global Blockchain size: " + Blockchain.global.size() + "\n");
		          System.out.println("Return size: " + l1.size() + "\n");
		          // Checks to make sure it is longer
		          if (Blockchain.global.size() < l1.size())
		          {
		            // add l1 block to global block
		            Blockchain.global.clear();
		            Blockchain.global.addAll(l1);

		            // Writes to ledger
		            BufferedWriter writer = new BufferedWriter(new FileWriter("BlockchainLedger.xml"));
		            writer.write(x1);
		            writer.close();

		            // Prints out new blockchain
		            System.out.println("Updated Blockchain object: ");
		            for (BlockRecord b : Blockchain.global)
		            {System.out.println(b.toString());
		            }
		          }
		        }

		        if (Process.this.local.size() < l1.size())
		        {
		          Process.this.local.clear();
		          Process.this.local.addAll(l1);
		        }

		        // add the working block, and then stop work/start over
		        if (Process.this.local.contains(Process.this.b))
		        {
		          Process.this.added = false;
		        }
		        socket.close();

		      //Exception handling
		      } catch (IOException x)
		        {
		          System.out.println("Server read error");
		          x.printStackTrace();
		        }
		    }
		} 
		// end of class 
	  public void readInput()
	  {
	    // Update copy global blockchain into local BlockChain
	    this.local.addAll(Blockchain.global);

	    System.out.println("Process# : " + pnum + " Ports: " + UnverifiedBlockPort + " " +
	           BlockChainPort + "\n");

	    switch(pnum){
	    case 1: FILENAME = "BlockInput1.txt"; break;
	    case 2: FILENAME = "BlockInput2.txt"; break;
	    default: FILENAME= "BlockInput0.txt"; break;
	    }

	    System.out.println("Input file: " + FILENAME);
	    try {
	      try (BufferedReader br = new BufferedReader(new FileReader(FILENAME))) {
		    String[] tokens = new String[10];
		    String x1;
		    String InputStr;
		    String suuid;
		    UUID idA;
	
		    JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecord.class);
		    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		    StringWriter sw = new StringWriter();
	
		    // CDE Make the output pretty printed:
		    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	
		    int n = 0;
		    while ((InputStr = br.readLine()) != null) {
		      //create an sample object to store the info
		      BlockRecord sample = new BlockRecord();
		      sample.setSHA256String("SHA string goes here...");
		      sample.setSignedSHA256("Signed SHA string goes here...");
		      // Create BlockID
		      idA = UUID.randomUUID();
		      suuid = new String(UUID.randomUUID().toString());
		      sample.setBlockID(suuid);
		      sample.setCreatingProcess("Process" + Integer.toString(pnum));
		      sample.setVerificationProcessID("coming soon");
		      sample.setLastHash("coming soon");
		      sample.setSolveString("coming soon");
		      
		      // Put data into file
		      tokens = InputStr.split(" +");
		      sample.setGDiag(tokens[iDIAG]);
		      sample.setFFname(tokens[iFNAME]);
		      sample.setGRx(tokens[iRX]);
		      sample.setFSSNum(tokens[iSSNUM]);
		      sample.setFDOB(tokens[iDOB]);
		      sample.setGTreat(tokens[iTREAT]);
		      sample.setFLname(tokens[iLNAME]);
		      this.unverified.add(sample);
		      n++;
		    }
		    System.out.println(n + " records read.");
		    System.out.println("\n");
		    System.out.println("Completed Reading Input!");
	
		    if (pnum == 0){
		    System.out.println("Creating BlockchainLedger...");
	
		    BufferedWriter writer = new BufferedWriter(new FileWriter("BlockchainLedger.xml"));
		    writer.write("");
		    writer.close();  }
		    System.out.println("Completed Writing Input!");
		    } catch (IOException e) {e.printStackTrace();}
	    } catch (Exception e) {e.printStackTrace();}
	  }// end of class
	  
	  //create server to listen to the verified blocks
	  public void listen() 
	  {
	    Process.blockListener bl;
	    bl  = this.new blockListener();
	    Thread t1 = new Thread(bl);
	    Process.chainListener cs;
	    cs = this.new chainListener();
	    Thread t2 = new Thread(cs);

	    t2.start();
	    t1.start();
	  }
	// multicast new blockchain to peer
	  public void multicastB(BlockRecord block) // Unverified
	  {
	      // send to a list of ports
	      Socket socket = null;
	      int[] ports = new int[] {4710, 4711, 4712};

	      PrintStream toServer;
	      String serverName = "localhost";
	      String CSC435Block = null;
	      try{
	        try{
	          CSC435Block = block.toXML();
	        } catch (Exception e) {e.printStackTrace();}

	        // connect to server port
	        for (int port : ports)
	        {
	          System.out.println("Casting " + block.toString() + " to " + serverName + " at " + port + ".");
	          socket = new Socket(serverName, port);
	          toServer = new PrintStream(socket.getOutputStream());
	          //sends block out
	          toServer.println(CSC435Block);
	          toServer.flush();
	          toServer.close();
        }
	        socket.close();
	      //Exception handeling
	      }catch (IOException x){
	        System.out.println ("multicastB error");
	      }
	      System.out.println("Completed multicastB" + block.toString() + ".");
	  } 
	  // end of class
	  
	  public void multicastC(Queue<BlockRecord> chain)
	  {
	    System.out.println("Started multicastChain() " + chain.toString() + "...");

	    Socket socket = null;
	    int[] ports = {4820,4821,4822};

	    PrintStream toServer;
	    String serverName = "localhost";
	    String newChain = ""; // Raw XML of the chain

	    // Create chain
	    for (BlockRecord block : chain)
	    { newChain += block.toXML();
	    }try{
	      // connect to server port
	      for (int port : ports){
	        System.out.println("Casting " + chain.toString() + " to " + serverName + " at " + port + ".");
	        socket = new Socket(serverName, port);
	        toServer = new PrintStream(socket.getOutputStream());
	        //sends chain out
	        toServer.println(newChain);
	        toServer.flush();
	        toServer.close();
	      }
	      socket.close();
	    //Exception handeling
	    } catch (IOException  x)
	    {System.out.println ("multicastC error");}

	    System.out.println("Complete multicastC " + chain.toString() + ".");
	  } 
	  //end of class
	  public void cast()
	  {
	    while (this.unverified.peek() != null)
	    {
	      multicastB(this.unverified.remove());
	    }
	  }
}

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
      String x1 = sw.toString();
      returnString = x1;
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