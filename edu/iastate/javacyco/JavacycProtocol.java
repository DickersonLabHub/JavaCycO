package edu.iastate.javacyco;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import unixdomainsocket.UnixDomainSocket;






public class JavacycProtocol
{
    private UnixDomainSocket uds; // J-BUDS Unix domain socket
    private String ptoolsSocketName; // name of the socket
    private PrintWriter out; // output to the Pathway Tools server
    private BufferedReader in; // input from the Pathway Tools server
    
	public JavacycProtocol()
	{
		ptoolsSocketName = "/tmp/ptools-socket";
	}
	
	public String passToBioCyc(String q)
	{
		String rst = "";
		String line;
		makeSocket();
		try
		{
			System.out.println("protocol passing query");
			out.println(q);
			System.out.println("protocol retreiving response");
			line = in.readLine();
			while(line != null)
			{
				rst += " "+line;
				line = in.readLine();
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		closeSocket();
		return rst;
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
 
 private String retrieveResultsString()
 {
	try
	{
	    ArrayList<String> results = new ArrayList();
	    String readStr = in.readLine();
	    while (readStr != null)
		{
		    // DEBUG ONLY
		    //System.out.println(readStr);

		    results.add(readStr);
		    readStr = in.readLine();
		}
	    String retStr = (String)results.get(0);

	    // DEBUG
	    //System.out.println("0th element: " + (String)results.get(0));

	    // If retStr is surrounded by quotation marks, remove them
	    if ((retStr.startsWith("\"")) && (retStr.endsWith("\"")))
		{
		    int endIndex = retStr.length() - 1;
		    return retStr.substring(1, endIndex);
		}
	    else
		{
		    return retStr;
		}
	}
	catch (IOException e)
	    {
		e.printStackTrace();
	    }
	return null; // if an IOException has occured
 }

}