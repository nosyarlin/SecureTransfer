import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Random;

public class ClientCP2 {

	public static void main(String[] args) {

    	String filename = "rr.txt";

		int numBytes = 0;

		Socket clientSocket;

        DataOutputStream toServer;
        DataInputStream fromServer;

    	FileInputStream fileInputStream;
        BufferedInputStream bufferedFileInputStream;

		int acknowledgement = 0;

		long timeStarted = System.nanoTime();

		Random random = new Random();
		random.setSeed(timeStarted);

		try {
			// Prepare cert
			InputStream fis = new FileInputStream("CA.cer");
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate CA = (X509Certificate)cf.generateCertificate(fis);
			X509Certificate ServerCert;

			System.out.println("Establishing connection to server...");

			// Connect to server and get the input and output streams
			clientSocket = new Socket("10.12.90.176", 4321);
			toServer = new DataOutputStream(clientSocket.getOutputStream());
			fromServer = new DataInputStream(clientSocket.getInputStream());

			// Send nonce
			System.out.println("Sending nonce to server...");
			int nonce = random.nextInt();
			toServer.writeInt(4);
			toServer.writeInt(nonce);

			// Receive encrypted nonce
			System.out.println("Receiving encrypted nonce...");
			numBytes = fromServer.readInt();
			byte[] encrypted_nonce = new byte[numBytes];
			fromServer.read(encrypted_nonce, 0, numBytes);

			// Ask for certification
			System.out.println("Asking for certificate...");
			toServer.writeInt(5);

			// Read certificate
			System.out.println("Receiving certificate...");
			numBytes = fromServer.readInt();
			byte[] certBytes = new byte[numBytes];
			fromServer.readFully(certBytes,0,numBytes);
			ServerCert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBytes));

			// Verify that cert is correct
			System.out.println("Verifying certificate...");
			PublicKey CAKey = CA.getPublicKey();
			ServerCert.checkValidity();
			ServerCert.verify(CAKey);

			// Verify nonce
			System.out.println("Verifying encrypted nonce...");
			Cipher decipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			PublicKey publicKey = ServerCert.getPublicKey();
			decipher.init(Cipher.DECRYPT_MODE, publicKey);
			byte[] testNonceBytes = decipher.doFinal(encrypted_nonce);
			byte[] nonceBytes = ByteBuffer.allocate(4).putInt(nonce).array();

			// If nonce is correct
			if (Arrays.equals(nonceBytes, testNonceBytes)) {

				// Generate sessionKey
				System.out.println("Generating Session Key...");
				KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
				SecureRandom secureRandom = new SecureRandom();
				keyGenerator.init(secureRandom);
				SecretKey sessionKey = keyGenerator.generateKey();
				byte[] encryptedSessionKey = cipher.doFinal(sessionKey.getEncoded());

				System.out.println("Sending Session Key " + sessionKey.toString() );
				toServer.writeInt(6);
				toServer.writeInt(encryptedSessionKey.length);
				toServer.write(encryptedSessionKey);



				System.out.println("Sending file...");

				// Prepare cipher
				Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				cipher.init(Cipher.ENCRYPT_MODE, publicKey);

				// Send the filename
				toServer.writeInt(0);
				byte[] filenameBytes = filename.getBytes();

				toServer.writeInt(filenameBytes.length);
				toServer.write(filenameBytes);
				toServer.flush();

				// Open the file
				fileInputStream = new FileInputStream(filename);
				bufferedFileInputStream = new BufferedInputStream(fileInputStream);

				byte [] fromFileBuffer = new byte[117];

				// Send the file
				int i = 1;
				for (boolean fileEnded = false; !fileEnded;) {
					System.out.println("Sending block " + i);
					numBytes = bufferedFileInputStream.read(fromFileBuffer);
					fileEnded = numBytes < 117;

					toServer.writeInt(1);
					byte[] encryptedBuffer = cipher.doFinal(fromFileBuffer);
					toServer.writeInt(encryptedBuffer.length);
					toServer.write(encryptedBuffer, 0, encryptedBuffer.length);
					toServer.flush();
					i++;
				}
				System.out.println("File sent.");

				bufferedFileInputStream.close();
				fileInputStream.close();
			}

			System.out.println("Closing connection...");
			toServer.writeInt(2);
			acknowledgement = fromServer.readInt();

		} catch (Exception e) {e.printStackTrace();}

		if(acknowledgement ==2 )
		{
			long timeTaken = System.nanoTime() - timeStarted;
			System.out.println("Program took: " + timeTaken/1000000.0 + "ms to run");
		}
	}
}
