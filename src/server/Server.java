package server;
import util.Parser;
import util.Privileges;
import util.ResponseCode;

import javax.net.ServerSocketFactory;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;
import java.io.*;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Server implements Runnable {

	private ServerSocket serverSocket = null;
	private static int numConnectedClients = 0;
	private Authenticator auth;

	public Server(ServerSocket ss) throws IOException {
		serverSocket = ss;
		newListener();
		try {
			auth = new Authenticator();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {

		try {
			SSLSocket socket = (SSLSocket) serverSocket.accept();
			newListener();
			SSLSession session = socket.getSession();
			X509Certificate cert = (X509Certificate) session.getPeerCertificateChain()[0];
			
			int startIndex = cert.getSubjectDN().getName().indexOf("CN=") + 3;
			int endIndex   = cert.getSubjectDN().getName().indexOf(", C=");
			String name = cert.getSubjectDN().getName().substring(startIndex, endIndex);
			
			if (session.getPeerCertificateChain().length != 3) {
				Logger.getLogger().auditConnection(name, ResponseCode.Failure);
				socket.close();
				return;
			}
			Logger.getLogger().auditConnection(name, ResponseCode.Success);

			numConnectedClients++;
			System.out.println("client connected: " + numConnectedClients + " concurrent connection(s)");
			System.out.println("Cipher suite: " + session.getCipherSuite());
			
			PrintWriter out = null;
			BufferedReader in = null;
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String clientMsg = null;
			while ((clientMsg = in.readLine()) != null) {
				String[] arguments = Parser.parseLine(clientMsg);
				
				if (arguments.length < 2) {
					String data = Parser.formatNewLine(Authenticator.Failure);
					sendResponse(name, out, null, Privileges.Unknown, data);
					continue;
				}
				
				Privileges request = null;
				try {
					request = Privileges.fromInteger(Integer.parseInt(arguments[0]));
					
				} catch (NumberFormatException e) {
					String data = Parser.formatNewLine(Authenticator.Failure);
					sendResponse(name, out, arguments[1], Privileges.Unknown, data);
					continue;
				}
				
				if (request == Privileges.Write) {
					arguments[0] = Privileges.Read.toString();
				}
				String[] response = getResponse(arguments, cert);

				String data = Parser.formatNewLine(response);
				
				sendResponse(name, out, arguments[1], request, data);
				// Should the option for Writing be chosen, the server will wait
				// for the file data to be written
				if (request == Privileges.Write
						&& ResponseCode.fromInteger(Integer.parseInt(response[0])) == ResponseCode.Success) {
					// System.out.println("Entering second write phase.");
					awaitWriteResponse(out, in, arguments, request, response, cert);
				}
//				Logger.getLogger().auditAction(name, arguments[1], request, 
//						ResponseCode.fromInteger(Integer.parseInt(response[0])));

			}
			in.close();
			out.close();
			socket.close();
			numConnectedClients--;
			
			Logger.getLogger().auditDisconnection(name);
			
			System.out.println("client disconnected");
			System.out.println(numConnectedClients + " concurrent connection(s)\n");
		} catch (IOException e) {
			System.out.println("Client died: " + e.getMessage());
			e.printStackTrace();
			return;
		}
	}

	private void sendResponse(String clientName, PrintWriter out, String fileName,
			Privileges request, String data) throws IOException {
		Logger.getLogger().auditAction(clientName, fileName, request, ResponseCode.Failure);
		out.println(data);
		out.flush();
	}

	private void awaitWriteResponse(PrintWriter out, BufferedReader in, String[] arguments, Privileges request,
			String[] response, X509Certificate cert) throws IOException {
		String clientMsg;
		if ((clientMsg = in.readLine()) != null) {
			System.out.println("Msg: " + clientMsg);

			if (ResponseCode.fromInteger(Integer.parseInt(response[0])) == ResponseCode.Success) {
				String filename = arguments[1];
				arguments = Parser.parseLine(clientMsg);

				List<String> list = new ArrayList<String>(Arrays.asList(arguments));
				list.add(0, filename);
				list.add(0, request.toString());
				String[] writeInput = list.toArray(new String[list.size()]);
				response[0] = getResponse(writeInput, cert)[0];
				out.println(response[0]);
				out.flush();
			}
		}
	}

	private String[] getResponse(String[] arguments, X509Certificate cert) {
		return auth.authenticateAndRetrieveData(Privileges.fromInteger(Integer.parseInt(arguments[0])),
				Parser.generateUserFromCert(cert), arguments);
	}

	private void newListener() {
		(new Thread(this)).start();
	} // calls run()

	public static void main(String args[]) {
		// args = new String [1];
		// args[0] = "9876";

		System.out.println("\nServer Started\n");
		int port = -1;
		if (args.length >= 1) {
			port = Integer.parseInt(args[0]);
		}
		String type = "TLS";
		try {

			ServerSocketFactory ssf = getServerSocketFactory(type);
			ServerSocket ss = ssf.createServerSocket(port);
			((SSLServerSocket) ss).setNeedClientAuth(true); // enables client
															// authentication
			new Server(ss);
		} catch (IOException e) {
			System.out.println("Unable to start Server: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static ServerSocketFactory getServerSocketFactory(String type) {
		if (type.equals("TLS")) {
			SSLServerSocketFactory ssf = null;
			try { // set up key manager to perform server authentication
				SSLContext ctx = SSLContext.getInstance("TLS");
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
				KeyStore ks = KeyStore.getInstance("JKS");
				KeyStore ts = KeyStore.getInstance("JKS");
				char[] password = "password".toCharArray();

				ks.load(new FileInputStream(Variables.SERVER_FOLDER + "serverkeystore"), password); // keystore
																									// password
																									// (storepass)
				ts.load(new FileInputStream(Variables.SERVER_FOLDER + "servertruststore"), password); // truststore
																										// password
																										// (storepass)

				kmf.init(ks, password); // certificate password (keypass)
				tmf.init(ts); // possible to use keystore as truststore here
				ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
				ssf = ctx.getServerSocketFactory();
				return ssf;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return ServerSocketFactory.getDefault();
		}
		return null;
	}
}
