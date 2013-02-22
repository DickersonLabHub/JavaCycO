/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.iastate.javacyco;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

/**
 *
 * @author jlv
 */
public class GetGML {

	public static void main(String[] args) {
		if(args.length<4)
		{
			System.out.println("Usage: GetGML SERVER PORT ORGANISM PATHWAY [MAP_FILE [MAPPED_ATTRIBUTE_NAME]]");
			System.exit(0);
		}
		String server = args[0];
		int port  = Integer.parseInt(args[1]);
		String org = args[2];
		String pathway = args[3];
		String mapFilename = null;
		String mappedAttName = "mappedAttribute";
		if(args.length>4) mapFilename = args[4];
		if(args.length>5) mappedAttName = args[5];




		JavacycConnection connection = new JavacycConnection(server,port);
		connection.selectOrganism(org);
		try
		{
			ArrayList<Frame> rst = connection.search(pathway, Pathway.GFPtype);
			if(rst.size()==0)
			{
				System.out.println("No hits");
			}
			else if(rst.size()>1)
			{
				System.out.println("Found multiple pathways:");
				for(Frame f : rst)
				{
					System.out.println("\t"+f.getLocalID()+"  "+f.getCommonName());
				}
			}
			else
			{
				Pathway pwy = (Pathway)(connection.search(pathway, Pathway.GFPtype).get(0));
				Network net = pwy.getNetwork();
				if(mapFilename != null)
				{
					BufferedReader mapFileReader = new BufferedReader(new FileReader(mapFilename));
					String line = null;
					HashMap<String,String> map = new HashMap<String,String>();
					while((line=mapFileReader.readLine()) != null)
					{
						String[] lineParts = line.split("\t");
						map.put(lineParts[0],lineParts[1]);
					}
					net.addMappedAttribute(mappedAttName, map);
				}
				net.writeGML(System.out,true,true,true,true,false,false);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

}
