package edu.iastate.javacyco;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.File;

import java.util.Date;
import java.util.LinkedHashMap;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.net.*;
import java.util.ArrayList;


public class JavacycServer
{
	private enum ConnectionState { CHALLENGE, PROCESS, CLOSE; }
	
    private UnixDomainSocket uds; // J-BUDS Unix domain socket
    private String ptoolsSocketName; // name of the socket
    private PrintWriter out; // output to the Pathway Tools server
    private BufferedReader in; // input from the Pathway Tools server
    
    private File logFile;
    private PrintStream logStream,lispOut,lispIn;
	
    private final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
    
    ServerSocket serverSocket;
    Socket clientSocket;
    
    private final String logFileName = "JavacycServer_log.txt";
    
    private final String lispOutFileName = "lisp_out_log.txt";
    private final String lispInFileName = "lisp_in_log.txt";
    
    private String authFile = "";
    
    private Boolean verbose = false;
    private Boolean log = false;
    int port;
    
    int authAttempts = 1;
    
    JavacycConnection localConnection;
    
    public static void main(String[] args)
    {
    	JavacycServer javacycServer = new JavacycServer();
    	javacycServer.start(args);
    }
    
	public JavacycServer()
	{
		ptoolsSocketName = "/tmp/ptools-socket";
		localConnection = new JavacycConnection();
    }
    
    public void start(String[] args)
    {
    	port = 4444;
    	
    	for(int i=0; i<args.length; i++)
    	{
        	if(args[i].equals("-help") || args[i].equals("--help") || args[i].equals("-h"))
        	{
        		System.out.println("REQUIRES javacyc.jar and\n" +
        				"JNI compilation of UnixDomainSocket_UnixDomainSocket.c to libunixdomainsocket.so (use javacyc.jar/compile_native_socket.sh)\n" +
        				"Must run on Linux\n" +
        				"Usage: java -Djava.library.path=path/to/libunixdomainsocket.so -cp path/to/javacyc.jar -jar JavacycServer.jar [-port PORTNUMBER (default: 4444)] [-verbose] [-log] [-authFile authFileLocation]");
        		System.exit(1);
        	}
    		if(args[i].equals("-verbose"))
    			verbose = true;
    		else if(args[i].equals("-log"))
    		{
    			log = true;
    		}
    		else if(args[i].equals("-port"))
    			port = Integer.parseInt(args[i+1]);
    		else if (args[i].equals("-authFile"))
    			authFile = args[i+1];
    	}

    	if(verbose) System.out.println("verbose mode");

    	
    	if(log)
    	{
    		System.out.println("logging to "+logFileName);
        	logFile = new File(logFileName);
        	try
        	{
        		logStream = new PrintStream(logFile);
        		logStream.println(dateFormat.format(new Date())+"\tlocalhost\tStarting up");
        		
        		lispIn = new PrintStream(lispInFileName);
        		lispOut = new PrintStream(lispOutFileName);
        	}
        	catch(Exception e)
        	{
        		e.printStackTrace();
        		System.exit(0);
        	}
    	}
   		
    	try
    	{
    		serverSocket = new ServerSocket(port);
    	}
    	catch (Exception e)
    	{
    	    System.out.println("Could not listen on port: "+port);
    	    System.exit(-1);
    	}
    	
    	while(true)
    	{
	    	try
	    	{
	    		if(verbose) System.out.println("Total mem: "+Runtime.getRuntime().totalMemory());
	    		if(verbose) System.out.println("Server waiting to accept on port "+port);
	    		clientSocket = serverSocket.accept();
	    	    PrintWriter toClient = new PrintWriter(clientSocket.getOutputStream(), true);
	    	    BufferedReader fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	        	
	    	    ConnectionState state;
	    	    if (authFile.equals(""))
	    	    	state = ConnectionState.PROCESS;
	    	    else
	    	    	state = ConnectionState.CHALLENGE;
	        	
	        	boolean clientDisconnect = false;
	        	while(!clientDisconnect) {
		    		switch (state) {
			    		case CHALLENGE:
			    			for (int i = 0; i < authAttempts; i++) {
			    				state = authenticateClient(fromClient, toClient);
			    				if (state == ConnectionState.PROCESS) break;
			    			}
			    			if (state == ConnectionState.PROCESS) break;
			    			else {
				    			if(verbose) System.out.println("Failed to authenticate 3 times");
				    			state = ConnectionState.CLOSE;
				    			break;
			    			}
			    		case PROCESS:
			    			state = processCommand(fromClient, toClient);
			    			break;
			    		case CLOSE:
			    			if(verbose) System.out.println("Closing connection to client");
			    			clientDisconnect = true;
			    			toClient.println(JavacycProtocol.CLOSE_CONNECTION); toClient.flush();
			    			break;
		    		}
	        	}
	        	clientSocket.close();
	    	} 
	    	catch (Exception e)
	    	{
	    		System.out.println("Caught exception...panicking");
    	    	e.printStackTrace();
    	    	System.exit(1);
	    	}
    	}

    }
    
    public void finalize()
    {
    	try
    	{
	    	if(!clientSocket.isClosed())
	    		clientSocket.close();
	    	if(!serverSocket.isClosed())
	    		serverSocket.close();
	    	if(logStream != null)
	    	{
	    		logStream.println(dateFormat.format(new Date())+"\tlocalhost\tShutting down");
	    		logStream.close();
	    		
	    		lispOut.close();
	    		lispIn.close();
	    	}
    	}
    	catch(IOException e)
    	{
    		e.printStackTrace();
    	}
    	finally
    	{
    		try
    		{
    			super.finalize();
    		}
    		catch(Throwable t)
    		{
    			t.printStackTrace();
    		}
    	}
    }
    
//    private void listen(int port) throws Exception
//    {
//    	if(verbose) System.out.println("Total mem: "+Runtime.getRuntime().totalMemory());
//    	
//    	PrintWriter toClient;
//		BufferedReader fromClient;
//    	
//		if(verbose) System.out.println("Server waiting to accept on port "+port);
//	    clientSocket = serverSocket.accept();
//	
//    	toClient = new PrintWriter(clientSocket.getOutputStream(), true);
//		fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//		
//		String line;
//		
//		if(verbose) System.out.println("Server reading query line from client "+clientSocket.getInetAddress());
//		line = fromClient.readLine();
//		if(verbose) System.out.println("Server received query from client: "+line);
//		if(log) logStream.println(dateFormat.format(new Date())+"\t"+clientSocket.getInetAddress()+"\t"+line);
//		
//		if(line != null && line.startsWith("***")) {
//			String localQuery = line.substring(3);
//			String[] localQueryParts = localQuery.split(":",2);
//			String localFunction = localQueryParts[0];
//			String[] localFunctionParams = localQueryParts.length>1 ? localQueryParts[1].split(",") : null;
//			if(localFunction.equals("PO"))
//			{
//				String org = localFunctionParams[0];
//				if(!org.equals(localConnection.getOrganismID()))
//					localConnection.selectOrganism(org);
//				ArrayList<String> pairs = new ArrayList<String>();
//				LinkedHashMap<String,String> classMap = localConnection.getPathwayOntology(true);
//				toClient.println("(");
//				for(String key : classMap.keySet())
//				{
//					String s = "\""+key+":"+classMap.get(key)+"\"";
//					pairs.add(s);
//					System.out.println(s);
//					toClient.println(s);
//				}
//				toClient.println(")");
//
////				String resp = JavacycConnection.ArrayList2LispList(pairs);
////				if(verbose) System.out.println("Response for server side query: "+resp);
////				toClient.println(resp);
//			}
//		}
//		else
//		{
//			makeSocket();
//			if(verbose) System.out.println("Server writing query line to ptools socket");
//			out.println(line);
//			if(log) lispOut.println(line);
//			
//			int i=0;
//			if(verbose) System.out.println("Server reading response line "+i+" from ptools socket");
//			line = in.readLine();
//	
//			while(line != null)
//			{
//				if(verbose) System.out.println("Server writing response line "+i+" to client: "+line);
//				if(log) lispIn.println(line);
//				toClient.println(line);
//				i++;
//				if(verbose) System.out.println("Server reading response line "+i+" from ptools socket");
//				line = in.readLine();
//			}
//			closeSocket();
//		}
//		clientSocket.close();
//    }


	 /**
    	Get a socket connection with Pathway Tools using a Unix domain
    	socket.
	 */
	 private void makeSocket() {
		try {
		    // Create socket and connect to the server
		    uds = new UnixDomainSocket(ptoolsSocketName);
		    out = new PrintWriter(uds.getOutputStream(), true);
		    in = new BufferedReader(
					    new InputStreamReader(uds.getInputStream()));
		} catch (IOException e) { 
		    e.printStackTrace();
		    throw new RuntimeException(); 
		}
	 }

	 /**
	    Close the socket connection with Pathway Tools.
	    @throws IOException if the socket connection cannot be closed
	 */
	 private void closeSocket() {
		try {
		    uds.close();
		    out.close();
		    in.close();
		} catch (IOException e) {
		    e.printStackTrace();
		    throw new RuntimeException(); 
		}
	 }
    
 	private ConnectionState processCommand(BufferedReader fromClient, PrintWriter toClient) throws Exception {
 		if(verbose) System.out.println("Total mem: "+Runtime.getRuntime().totalMemory());
		if(verbose) System.out.println("Server reading query line from client "+clientSocket.getInetAddress());
		String line = fromClient.readLine();
		if(verbose) System.out.println("Server received query from client: "+line);
		if(log) logStream.println(dateFormat.format(new Date())+"\t"+clientSocket.getInetAddress()+"\t"+line);
		
		if (line != null && line.startsWith(JavacycProtocol.CLOSE_CONNECTION)) {
			return ConnectionState.CLOSE;
		} else if(line != null && line.startsWith("***")) {
			String localQuery = line.substring(3);
			String[] localQueryParts = localQuery.split(":",2);
			String localFunction = localQueryParts[0];
			String[] localFunctionParams = localQueryParts.length>1 ? localQueryParts[1].split(",") : null;
			if(localFunction.equals("PO"))
			{
				String org = localFunctionParams[0];
				if(!org.equals(localConnection.getOrganismID()))
					localConnection.selectOrganism(org);
				ArrayList<String> pairs = new ArrayList<String>();
				LinkedHashMap<String,String> classMap = localConnection.getPathwayOntology(true);
				toClient.println("(");
				for(String key : classMap.keySet())
				{
					String s = "\""+key+":"+classMap.get(key)+"\"";
					pairs.add(s);
					System.out.println(s);
					toClient.println(s);
				}
				toClient.println(")");

//				String resp = JavacycConnection.ArrayList2LispList(pairs);
//				if(verbose) System.out.println("Response for server side query: "+resp);
//				toClient.println(resp);
			}
		}
		else
		{
			makeSocket();
			if(verbose) System.out.println("Server writing query line to ptools socket");
			out.println(line);
			if(log) lispOut.println(line);
			
			int i=0;
			if(verbose) System.out.println("Server reading response line "+i+" from ptools socket");
			line = in.readLine();
	
			while(line != null)
			{
				if(verbose) System.out.println("Server writing response line "+i+" to client: "+line);
				if(log) lispIn.println(line);
				toClient.println(line);
				i++;
				if(verbose) System.out.println("Server reading response line "+i+" from ptools socket");
				line = in.readLine();
			}
			closeSocket();
		}
		return ConnectionState.CLOSE;
 	}
 	
 	@SuppressWarnings("unused")
	private Frame loadFrameServerSide(String frameID) {
 		//TODO is it possible to load the frame object server side faster than over remote connection, then simply serialize the frame and send it to client?
 		// will the whole process be faster?
 		
 		if(verbose) System.out.println("Loading serializable frame: " + frameID);
 		
		makeSocket();
		
		/**
		 * 1)
		 * (WITH-ORGANISM (ORG-ID 'CORN)
                    (MAPCAR #'OBJECT-NAME
                            (GET-INSTANCE-ALL-TYPES 'GLC-6-P)))
                            
		 * 2)
		 * (WITH-ORGANISM (ORG-ID 'CORN)
                    (OBJECT-NAME (GET-FRAME-TYPE 'GLC-6-P)))
                    
		 * 3)
		 * (WITH-ORGANISM (ORG-ID 'CORN)
                    (MAPCAR #'OBJECT-NAME (GET-FRAME-SLOTS 'GLC-6-P)))
                    
		 * 4)
		 * (WITH-ORGANISM (ORG-ID 'CORN)
                    (MAPCAR #'OBJECT-NAME
                            (GET-SLOT-VALUES 'GLC-6-P 'SYNONYM-SLOTS)))
         * (WITH-ORGANISM (ORG-ID 'CORN)
                    (MAPCAR #'OBJECT-NAME
                            (GET-ALL-ANNOTS 'GLC-6-P 'SYNONYM-SLOTS
                                            'N+1-NAME)))
         *** repeat
		 *
		 * 5)
		 * 
		 *
		 */
		
//		localConnection.getFrameSlots(frameID); //TODO
//		out.println(line);
//		line = in.readLine();
//
//		while(line != null) {
//			toClient.println(line);
//			line = in.readLine();
//		}
		closeSocket();
		
		return null;
 	}
 	
 	private ConnectionState authenticateClient(BufferedReader fromClient, PrintWriter toClient) throws IOException {
 		if(verbose) System.out.println("Attempting to authenticate client");
 		String user, pw;
 		if(verbose) System.out.println("Requesting user name");
 		toClient.println(JavacycProtocol.REQUEST_USERNAME); toClient.flush();
 		user = fromClient.readLine();
 		if(verbose) System.out.println("Requesting password");
 		toClient.println(JavacycProtocol.REQUEST_PASSWORD); toClient.flush();
 		pw = fromClient.readLine();
 		
 		if (validUser(user, pw)) {
 			if(verbose) System.out.println("Authentication successful");
 			toClient.println(JavacycProtocol.LOGIN_SUCCESS); toClient.flush();
 			return ConnectionState.PROCESS;
 		} else {
 			if(verbose) System.out.println("Authentication failed");
 			toClient.println(JavacycProtocol.LOGIN_FAIL); toClient.flush();
 			return ConnectionState.CHALLENGE;
 		}
 	}
 	
 	private boolean validUser(String user, String password) {
 		if (user == null || user.equals("")) return false;
 		String userPassword = getUserPassword(user);
 		if (userPassword == null) return false;
 		if (password.equals(userPassword)) return true;
 		else return false;
 	}
 	
 	private String getUserPassword(String user) {
 		BufferedReader br = null;
 		try {
 			br = new BufferedReader(new FileReader(authFile));
 			String line;
			while ((line = br.readLine()) != null) {
				String[] entry = line.split(",");
				if (entry[0].equals(user)) {
					return entry[1];
				}
			}
 		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
 	}
}