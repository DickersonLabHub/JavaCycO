/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.iastate.javacyco;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

/**
 *
 * @author jlv
 */
public class NetworkExporter {

	public static void main(String[] args) {
		if(args.length<4)
		{
			System.out.println("Usage: NetworkExporter.jar SERVER PORT ORGANISM [PATHWAY|all] [gml|xgmml] [MAP_FILE [MAPPED_ATTRIBUTE_NAME]]");
			System.exit(0);
		}
		String server = args[0];
		int port  = Integer.parseInt(args[1]);
		String org = args[2];
		String pathway = args[3];
		String format = "gml";
		String mapFilename = null;
		String mappedAttName = "mappedAttribute";
		if(args.length>4) format = args[4];
		if(args.length>5) mapFilename = args[5];
		if(args.length>6) mappedAttName = args[6];




		JavacycConnection connection = new JavacycConnection(server,port);
		connection.selectOrganism(org);
		try
		{
			if(pathway.equals("all"))
			{
				Network net = connection.getNetwork();
				net.writeGML(new PrintStream(new FileOutputStream(connection.getOrganismID()+".gml")),true,true,true,false,true,true);
				//net.writeGML(new PrintStream(new FileOutputStream(connection.getOrganismID()+"_basic_unweighted_undirected.gml")),false,false,false,true,true,true);
				//net.writeGML(new PrintStream(new FileOutputStream(connection.getOrganismID()+"_basic_weighted_undirected.gml")),false,true,false,true,true,true);
				net.writeGML(new PrintStream(new FileOutputStream(connection.getOrganismID()+"_basic_unweighted_directed.gml")),false,false,true,true,true,true);
				//net.writeGML(new PrintStream(new FileOutputStream(connection.getOrganismID()+"_basic_weighted_directed.gml")),false,true,true,true,true,true);
				net.printGeneAltIDs();
				net.printSynonyms();
				connection.writeReactionNeighbors(new PrintStream(new FileOutputStream(connection.getOrganismID()+"_reaction_ORGS.tab")), "***PATHWAYS\t");
				//connection.writeReactions(new PrintStream(new FileOutputStream(connection.getOrganismID()+"_reactions")), "***PATHWAYS\t");
			}
			else
			{
				Pathway pwy = (Pathway)Pathway.load(connection, pathway);
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
				if(format.equals("gml"))
				{
					net.writeGML(System.out,true,true,true,true,false,true);
				}
				else
				{
					net.writeXGMML(System.out,true,true,true,true,false,true);
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

}
