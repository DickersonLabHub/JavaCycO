package edu.iastate.javacyco;
import java.io.BufferedReader;
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

import unixdomainsocket.UnixDomainSocket;


import javacyco.Frame;
import javacyco.JavacycConnection;


public class JavacycServer
{
	
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
    
    private Boolean verbose = false;
    private Boolean log = false;
    int port;
    
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
        				"Usage: java -Djava.library.path=path/to/libunixdomainsocket.so -cp path/to/javacyc.jar -jar JavacycServer.jar [-port PORTNUMBER (default: 4444)] [-verbose] [-log]");
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
	    		listen(port);
	    	} 
	    	catch (Exception e)
	    	{
	    	    System.out.println("Caught exception "+e.getMessage());
	    	    e.printStackTrace();
	    	    
	    	    try
	    	    {
	    	    	listen(port);
	    	    }
	    	    catch(Exception e2)
	    	    {
	    	    	System.out.println("Caught another exception...panicking");
	    	    	e.printStackTrace();
	    	    	System.exit(1);
	    	    }
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
    
    private void listen(int port) throws Exception
    {
    	if(verbose) System.out.println("Total mem: "+Runtime.getRuntime().totalMemory());
    	
    	PrintWriter toClient;
		BufferedReader fromClient;
    	
		if(verbose) System.out.println("Server waiting to accept on port "+port);
	    clientSocket = serverSocket.accept();
	
    	toClient = new PrintWriter(clientSocket.getOutputStream(), true);
		fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		
		String line;
		
		if(verbose) System.out.println("Server reading query line from client "+clientSocket.getInetAddress());
		line = fromClient.readLine();
		if(verbose) System.out.println("Server received query from client: "+line);
		if(log) logStream.println(dateFormat.format(new Date())+"\t"+clientSocket.getInetAddress()+"\t"+line);
		
		if(line != null && line.startsWith("***"))
		{
			
			String localQuery = line.substring(3);
			String[] localQueryParts = localQuery.split(":",2);
			String localFunction = localQueryParts[0];
			String[] localFunctionParams = localQueryParts.length>1 ? localQueryParts[1].split(",") : null;
			if(localFunction.equals("SEARCH"))
			{
				String name = localFunctionParams[0];
				String GFPtype = localFunctionParams[1];
				String org = localFunctionParams[2];
				if(!org.equals(localConnection.getOrganismID()))
					localConnection.selectOrganism(org);
				ArrayList<Frame> rst = localConnection.search(name, GFPtype, true);
				ArrayList<String> ids = new ArrayList<String>();
				for(Frame f : rst)
				{
					ids.add(f.getLocalID());
				}
				String resp = JavacycConnection.ArrayList2LispList(ids);
				if(verbose) System.out.println("Response for server side query: "+resp);
				toClient.println(resp);
			}
			else if(localFunction.equals("PO"))
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
		clientSocket.close();
    }


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
    
}