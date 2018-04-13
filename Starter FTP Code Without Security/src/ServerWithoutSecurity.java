import javax.crypto.Cipher;
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

public class ServerWithoutSecurity {

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
				if (packetType == 5) {
					System.out.println("Sending certificate...");
					byte[] encoded = CAcert.getEncoded();
					toClient.writeInt(encoded.length);
					toClient.write(encoded);
				}

				// If the packet is for transferring the filename
				if (packetType == 0) {

					System.out.println("Receiving file...");

					int numBytes = fromClient.readInt();
					byte [] filename = new byte[numBytes];
					fromClient.readFully(filename, 0, numBytes);

					fileOutputStream = new FileOutputStream("recv/"+new String(filename, 0, numBytes));
					bufferedFileOutputStream = new BufferedOutputStream(fileOutputStream);

				// If the packet is for transferring a chunk of the file
				} else if (packetType == 1) {

					int numBytes = fromClient.readInt();
					byte [] block = new byte[numBytes];
					fromClient.readFully(block, 0, numBytes);

					if (numBytes > 0)
						bufferedFileOutputStream.write(block, 0, numBytes);

				// If the packet is for end of file
				} else if (packetType == 2) {

					System.out.println("Closing connection...");

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
