/* 

Version 1.0 2017-09-03

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

/* CDE: The JAXB libraries: */
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.StringWriter;
import java.io.StringReader;

/* CDE: The encryption needed for signing the hash: */

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

/* CDE Some other uitilities: */

import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.text.*;
import java.util.Base64;
import java.util.Arrays;
// Produces a 64-bye string representing 256 bits of the hash output. 4 bits per character
import java.security.MessageDigest; // To produce the SHA-256 hash.


@XmlRootElement
class BlockRecord{
  /* Examples of block fields: */
  String VerificationProcessID;
  String PreviousHash;
  String BlockID;
  String Fname;
  String Lname;
  String SSNum;
  String DOB;

  /* Examples of accessors for the BlockRecord fields: */
  public String getSSNum() {return SSNum;}
  @XmlElement
    public void setSSNum (String SS){this.SSNum = SS;}

  public String getFname() {return Fname;}
  @XmlElement
    public void setFname (String FN){this.Fname = FN;}

  public String getLname() {return Lname;}
  @XmlElement
    public void setLname (String LN){this.Lname = LN;}

  public String getVerificationProcessID() {return VerificationProcessID;}
  @XmlElement
    public void setVerificationProcessID(String VID){this.VerificationProcessID = VID;}

  public String getBlockID() {return BlockID;}
  @XmlElement
    public void setBlockID(String BID){this.BlockID = BID;}
}

/*  Starting point for the BlockRecord:

<?xml version="1.0" encoding="UTF-8"?>      
<BlockRecord>
  <SIGNED-SHA256> [B@5f150435 </SIGNED-SHA256> <!-- Verification procees SignedSHA-256-String  -->
  <SHA-256-String> 63b95d9c17799463acb7d37c85f255a511f23d7588d871375d0119ba4a96a </SHA-256-String>
  <!-- Start SHA-256 Data that was hashed -->
  <VerificationProcessID> 1 </VerificationProcessID> <!-- Process that is verifying this block, for credit-->
  <PreviousHash> From the previous block in the chain </PreviousHash>
  <Seed> Your random 256 bit string </Seed> <!-- guess the value to complete the work-->
  <BlockNum> 1 </BlockNum> <!-- increment with each block prepended -->
  <BlockID> UUID </BlockID> <!-- Unique identifier for this block -->
  <SignedBlockID> BlockID signed by creating process </SignedBlockID> <!-- Creating process signature -->
  <CreatingProcessID> 0 </CreatingProcessID> <!-- Process that made the ledger entry -->
  <TimeStamp> 2017-09-01.10:26:35 </TimeStamp>
  <DataHash> The creating process SHA-256 hash of the input data </DataHash> <!-- for auditing if Secret Key exposed -->
  <FName> Joseph </FName>
  <LName> Ng </LName>
  <DOB> 1995.06.22 </DOB> <!-- date of birth -->
  <SSNUM> 987-65-4321 </SSNUM>
  <Diagnosis> Measels </Diagnosis>
  <Treatment> Bedrest </Treatment>
  <Rx> aspirin </Rx>
  <Notes> Use for debugging and extension </Notes>
<!-- End SHA-256 Data that was hashed -->
</BlockRecord>
*/


public class BlockH {

  public static byte[] signData(byte[] data, PrivateKey key) throws Exception {
    Signature signer = Signature.getInstance("SHA1withRSA");
    signer.initSign(key);
    signer.update(data);
    return (signer.sign());
  }

  public static boolean verifySig(byte[] data, PublicKey key, byte[] sig) throws Exception {
    Signature signer = Signature.getInstance("SHA1withRSA");
    signer.initVerify(key);
    signer.update(data);

    return (signer.verify(sig));
  }

  public static KeyPair generateKeyPair(long seed) throws Exception {
    KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
    SecureRandom rng = SecureRandom.getInstance("SHA1PRNG", "SUN");
    rng.setSeed(seed);
    keyGenerator.initialize(1024, rng);

    return (keyGenerator.generateKeyPair());
  }


  public static String CSC435Block =
    "We will build this dynamically: <?xml version = \"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
    
  public static final String ALGORITHM = "RSA"; /* Name of encryption algorithm used */

  /* Header fields for the block: */
  public static String SignedSHA256;

  /* CDE NOTE: we do not need this method for the CSC435 blockchain assignment. */
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

  /* CDE NOTE: we do not need this method for the CSC435 blockchain assignment. */
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

  public static void main(String[] args) throws Exception {

    /* CDE: Process numbers and port numbers to be used: */
    int pnum;
    int UnverifiedBlockPort;
    int BlockChainPort;

    /* CDE If you want to trigger bragging rights functionality... */
    if (args.length > 1) System.out.println("Special functionality is present \n");

    if (args.length < 1) pnum = 0;
    else if (args[0].equals("0")) pnum = 0;
    else if (args[0].equals("1")) pnum = 1;
    else if (args[0].equals("2")) pnum = 2;
    else pnum = 0; /* Default for badly formed argument */
    UnverifiedBlockPort = 4710 + pnum;
    BlockChainPort = 4810 + pnum;

    System.out.println("Process number: " + pnum + " Ports: " + UnverifiedBlockPort + " " + 
		       BlockChainPort + "\n");

    /* CDE: Example of generating a unique blockID. This would also be signed by creating process: */
    UUID idA = UUID.randomUUID();
    String suuid = UUID.randomUUID().toString();
    System.out.println("Unique Block ID: " + suuid + "\n");

    /* CDE For the timestamp in the block entry: */
    Date date = new Date();
    //String T1 = String.format("%1$s %2$tF.%2$tT", "Timestamp:", date);
    String T1 = String.format("%1$s %2$tF.%2$tT", "", date);
    String TimeStampString = T1 + "." + pnum + "\n"; // No timestamp collisions!
    System.out.println("Timestamp: " + TimeStampString);

    /* CDE: Here is a way for us to simulate computational "work" */
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

      /* CDE put some data into the block record: */
      BlockRecord blockRecord = new BlockRecord();
      blockRecord.setVerificationProcessID("Process2");
      blockRecord.setBlockID(suuid);
      blockRecord.setSSNum("123-45-6789");
      blockRecord.setFname("Joseph");
      blockRecord.setLname("Chang");

      /* The XML conversion tools: */
      JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecord.class);
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
      StringWriter sw = new StringWriter();
      
      // CDE Make the output pretty printed:
      jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      
      /* CDE We marshal the block object into an XML string so it can be sent over the network: */
      jaxbMarshaller.marshal(blockRecord, sw);
      String stringXML = sw.toString();
      CSC435Block = stringXML;

      /* Make the SHA-256 Digest of the block: */
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      md.update (CSC435Block.getBytes());
      byte byteData[] = md.digest();

      // CDE: Convert the byte[] to hex format. THIS IS NOT VERFIED CODE:
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

      /* Add the SHA256String to the header for the block. We turn the
	 byte[] signature into a string so that it can be placed into
	 the block, but also show how to return the string to a
	 byte[], which you'll need if you want to use it later.
	 Thanks Hugh Thomas for the fix! */

      SignedSHA256 = Base64.getEncoder().encodeToString(digitalSignature);
      System.out.println("The signed SHA-256 string: " + SignedSHA256 + "\n");
      byte[] testSignature = Base64.getDecoder().decode(SignedSHA256);
      System.out.println("Testing restore of signature: " + Arrays.equals(testSignature, digitalSignature));
      verified = verifySig(SHA256String.getBytes(), keyPair.getPublic(), testSignature);
      System.out.println("Has the restored signature been verified: " + verified + "\n");

      String fullBlock = stringXML.substring(0,stringXML.indexOf("<blockID>")) +
      "<SignedSHA256>" + SignedSHA256 + "</SignedSHA256>\n" +
	"    <SHA256String>" + SHA256String + "</SHA256String>\n    " +
	stringXML.substring(stringXML.indexOf("<blockID>"));

      System.out.println(fullBlock); // Show what it looks like.

      /* CDE Here's how we put the XML back into java object form: */
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      StringReader reader = new StringReader(stringXML);
      
      BlockRecord blockRecord2 = (BlockRecord) jaxbUnmarshaller.unmarshal(reader);
      
      System.out.println("SSNum: " + blockRecord2.getSSNum()); // Show a piece of the new block object

      /* CDE: In case you want to use it for something, here we encrypt a
	 string, then decrypt it, using the same public key technology. These
	 techniques are not needed for the basic CSC435 assignment. Note that this
	 methocd is intended for 117 bytes or less to pass session keys: */

      /* CDE: Encrypt the hash string using the public key.  */
      final byte[] cipherText = encrypt(SHA256String,keyPair.getPublic());
      
      // CDE: Decrypt the ciphertext using the private key:
      final String plainText = decrypt(cipherText, keyPair.getPrivate());

      System.out.println("\nExtra functionality in case you want it:");
      System.out.println("Encrypted Hash string: " + Base64.getEncoder().encodeToString(cipherText));
      System.out.println("Original (now decrypted) Hash string: " + plainText);
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}