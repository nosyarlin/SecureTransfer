import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

public class ServerCP1 {

	public static void main(String[] args) throws Exception {

		int port = 4321;

		ServerSocket welcomeSocket;
		Socket connectionSocket;
		DataOutputStream toClient;
		DataInputStream fromClient;

		FileOutputStream fileOutputStream = null;
		BufferedOutputStream bufferedFileOutputStream = null;

		// Prepare cert
		InputStream fis = new FileInputStream("server.crt");
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate CAcert =(X509Certificate)cf.generateCertificate(fis);

		// Prepare public key
		PublicKey publicKey = CAcert.getPublicKey();

		// Prepare private key
		String privateKeyFileName = "privateServer.der";
		Path path = Paths.get(privateKeyFileName);
		byte[] privKeyByteArray = Files.readAllBytes(path);

		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privKeyByteArray);

		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

		// Prepare cipher
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE,  privateKey);

		// Prepare decipher
		Cipher decipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		decipher.init(Cipher.DECRYPT_MODE, privateKey);

		SecretKeySpec decryptedSecretKey = new SecretKeySpec(new byte[16],"AES");
		Cipher decipher2 = Cipher.getInstance("AES/CBC/PKCS5Padding");


		try {
			// Create connection
			welcomeSocket = new ServerSocket(port);
			System.out.println("Expecting a connection....");
			connectionSocket = welcomeSocket.accept();
			System.out.println("Connection established.");
			fromClient = new DataInputStream(connectionSocket.getInputStream());
			toClient = new DataOutputStream(connectionSocket.getOutputStream());

			while (!connectionSocket.isClosed()) {

				int packetType = fromClient.readInt();

				// If the packet is for transferring a nonce
				if (packetType == 4) {
					System.out.println("Receiving nonce...");

					// encrypt the nonce
					int nonce = fromClient.readInt();
					byte[] nonceByte = ByteBuffer.allocate(4).putInt(nonce).array();
					byte[] encrypted = cipher.doFinal(nonceByte);

					// return encrypted nonce
					System.out.println("Returning encrypted nonce...");
					toClient.writeInt(encrypted.length);
					toClient.write(encrypted);
				}

				// If the packet is asking for certificate
				else if (packetType == 5) {
					System.out.println("Sending certificate...");
					byte[] encoded = CAcert.getEncoded();
					toClient.writeInt(encoded.length);
					toClient.write(encoded);
				}

				// If the packet is for verifying message via sessionKey
				else if(packetType == 6){
					System.out.println("Recieving Session Key...");
					int byteSize = fromClient.readInt();
					byte[] encryptedKey = new byte[byteSize];
					fromClient.readFully(encryptedKey,0,byteSize);

					// Deciphering the sessino key
					Cipher decryption = Cipher.getInstance("RSA/ECB/PKCS1Padding");
					decryption.init(Cipher.DECRYPT_MODE, privateKey);
					decryptedSecretKey = new SecretKeySpec(decryption.doFinal(encryptedKey),"AES");

					System.out.printf("Session Key is %s%n", decryptedSecretKey.toString());
					decipher2.init(Cipher.DECRYPT_MODE, decryptedSecretKey,new IvParameterSpec(new byte[16]));
				}

				// If the packet is for transferring the filename
				else if (packetType == 0) {

					System.out.println("Receiving file...");

					int numBytes = fromClient.readInt();
					byte[] filename = new byte[numBytes];
					fromClient.readFully(filename, 0, numBytes);

					fileOutputStream = new FileOutputStream(new String(filename, 0, numBytes));
					bufferedFileOutputStream = new BufferedOutputStream(fileOutputStream);

				// If the packet is for transferring a chunk of the file
				} else if (packetType == 1) {

					int numBytes = fromClient.readInt();
					byte[] encrypted_block = new byte[numBytes];
					int total = 0;

					// keep reading until everything has been received
					while (total < numBytes) {
						total += fromClient.read(encrypted_block, total, numBytes-total);
					}
					// decrypt file
					// CP2
					//byte[] block = decipher2.doFinal(encrypted_block);
					// CP1
					byte[] block = decipher.doFinal(encrypted_block);

					if (numBytes > 0)
						bufferedFileOutputStream.write(block, 0, block.length);
				}

				// if the packet is for closing connection
				else if (packetType == 2) {
					System.out.println("Closing connection...");
					toClient.writeInt(2);

					if (bufferedFileOutputStream != null) bufferedFileOutputStream.close();
					if (bufferedFileOutputStream != null) fileOutputStream.close();
					fromClient.close();
					toClient.close();
					connectionSocket.close();
				}
			}
		} catch (Exception e) {e.printStackTrace();}

	}

}