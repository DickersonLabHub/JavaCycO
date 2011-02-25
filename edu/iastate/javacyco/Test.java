package edu.iastate.javacyco;
/**
 *  This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.Properties;
import java.util.TreeMap;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

		if(args.length!=3)
		{
			System.out.println("Usage: java -jar BioCyc2GML.jar SERVERNAME PORT ORGANISM");
			System.exit(0);
		}
		
		String server = args[0];
		Integer port = Integer.parseInt(args[1]);
		String org = args[2];
		
		JavacycConnection connection = new JavacycConnection(server,port);//"vitis.student.iastate.edu",4444);
		
		connection.selectOrganism(org);
		try
		{
			
			//Frame.load(connection, "RXN90W-231").print();
//			for(Frame f : connection.getAllGFPInstances(Pathway.GFPtype)) f.deleteFromKB();
//			for(Frame f : connection.getAllGFPInstances(Reaction.GFPtype)) f.deleteFromKB();
//			for(Frame f : connection.getAllGFPInstances(Catalysis.GFPtype)) f.deleteFromKB();
//			for(Frame f : connection.getAllGFPInstances(Monomer.GFPtype)) f.deleteFromKB();
//			for(Frame f : connection.getAllGFPInstances(Gene.GFPtype)) f.deleteFromKB();
			
//			HashMap<String,String> map = connection.getPathwayOntology(false);
//			for(String key : map.keySet())
//			{
//				System.out.println(key+"\t"+map.get(key));
//			}
//			for(OrgStruct org : connection.allOrgs())
//			{
//				System.out.println(org.getLocalID());
//			}
			
//			Network net = new Network("all_pathways_net");
//			for(Pathway p : Pathway.all(connection)) net.importNetwork(p.getNetwork());
//			net.printTab();
			
//			TreeSet<String> geneAccs = new TreeSet<String>();// {"AT5G36290","AT5G62000","AT5G58330","AT5G66240","AT5G64200"};
//			BufferedReader input =  new BufferedReader(new FileReader("/home/jlv/Desktop/plex/integration_grant_proposal/AS_example_genes.txt"));
//			String line = null; //not declared within while loop
//			while (( line = input.readLine()) != null){
//				geneAccs.add(line);
//			}
//			ArrayList<Frame> genes = connection.getAllGFPInstances(Gene.GFPtype);
//			HashMap<String,Frame> genesMap = new HashMap<String,Frame>();
//			for(Frame f : genes)
//			{
//				genesMap.put(f.ID,f);
//			}
//			System.out.println("geneMap loaded");
//			TreeMap<String,TreeSet<String>> pwys = new TreeMap<String,TreeSet<String>>();
//			HashMap<String,Pathway> pwysMap = new HashMap<String,Pathway>();
//			for(String geneAcc : geneAccs)
//			{
//				if(genesMap.containsKey(geneAcc))
//				{
//					Gene g = (Gene)(genesMap.get(geneAcc));
//					
//					for(Frame f : g.getPathways())
//					{
//						if(!pwys.containsKey(f.getCommonName())) pwys.put(f.getCommonName(),new TreeSet<String>());
//						pwys.get(f.getCommonName()).add(g.ID);
//						pwysMap.put(f.getCommonName(),(Pathway)f);
//					}
//				}
//			}
//			Network net = new Network("AS_example_net");
//			for(String pwy : pwys.keySet())
//			{
//				String list = "";
//				for(String gene : pwys.get(pwy)) 
//				{
//					list += gene+";";
//				}
//				System.out.println(pwy+"\t"+pwys.get(pwy).size()+"\t"+list);
//				if(pwys.get(pwy).size() > 5)
//				{
//					Network pnet = pwysMap.get(pwy).getNetwork();
//					net.importNetwork(pnet);
//					pnet.printTab();
//				}
//			}
//			net.printTab();
			

			//((Pathway)Pathway.load(connection,"PYRUVDEHYD-PWY")).getNetwork().writeGML(new PrintStream(new FileOutputStream("ECOCYC.gml")),true,true,true,false);
			
			
//			for(Frame f : connection.getAllGFPInstances(Regulation.GFPtype))
//			{
//				Regulation reg = (Regulation)f;
//				Boolean rel = reg.getMode();
//				String rels = "";
//				if(rel==null) rels = "?";
//				else if(rel) rels = "+";
//				else rels = "-";
//				System.out.println(reg.getRegulator().getCommonName()+" "+rels+" "+reg.getRegulatee().getCommonName());
//			}
			
			//connection.writeReactionNeighbors(System.out,"");
			
//			Frame.load(connection,"AK221689").print();
//			Frame.load(connection,"ENZRXN8TU-3017").print();
//			Frame.load(connection,"ENZRXN8TU-614").print();
			//for(Frame f : Frame.load(connection,"L-1-PHOSPHATIDYL-ETHANOLAMINE").getPathways())
			//	System.out.println(f.getLocalID()+"::"+f.getCommonName());

			//Network net = ((Pathway)Frame.load(connection,(String)(connection.allPathways().get(0)))).getNetwork();
//			Network net = ((Pathway)Frame.load(connection,"PWY-6213")).getNetwork();
//			net.writeGML(new PrintStream(System.out),true,true,true,false);
//			
			Network net = connection.getNetwork();
			
			net.writeGML(new PrintStream(new FileOutputStream(connection.getOrganismID()+".gml")),true,true,true,false,true,true);
			net.writeGML(new PrintStream(new FileOutputStream(connection.getOrganismID()+"_basic_unweighted_undirected.gml")),false,false,false,true,true,true);
			net.writeGML(new PrintStream(new FileOutputStream(connection.getOrganismID()+"_basic_weighted_undirected.gml")),false,true,false,true,true,true);
			net.writeGML(new PrintStream(new FileOutputStream(connection.getOrganismID()+"_basic_unweighted_directed.gml")),false,false,true,true,true,true);
			net.writeGML(new PrintStream(new FileOutputStream(connection.getOrganismID()+"_basic_weighted_directed.gml")),false,true,true,true,true,true);
			net.printGeneAltIDs();
			net.printSynonyms();
			connection.writeReactionNeighbors(new PrintStream(new FileOutputStream(connection.getOrganismID()+"_reactionNeighbors.tab")), "***PATHWAYS\t");
			connection.writeReactions(new PrintStream(new FileOutputStream(connection.getOrganismID()+"reactions")), "***PATHWAYS\t");
			
			
//			for(Frame f : connection.getAllGFPInstances(Gene.GFPtype))
//			{
//				//Regulation reg = (Regulation)f;
//				//System.out.println((reg.getRegulator()==null ? null : (reg.getRegulator().getCommonName()+"("+reg.getRegulator().getClass().getName()+")"))+"\t"+(reg.getRegulatee()==null ? null : (reg.getRegulatee().getCommonName()+"("+reg.getRegulatee().getClass().getName()+")")));
//				Gene g = (Gene)f;
//				
//				int a = g.getRegulatingGenes().size();
//				int b = g.getRegulatedGenes().size();
//				if(a>0 || b>0) System.out.println(a+"\t"+b);
//			}
			
//			System.out.println(((Gene)Frame.load(connection,"EG11017")).getECK());
//			((Gene)Frame.load(connection,"AT3G54990")).print();
//			((Gene)Frame.load(connection,"EG11017")).getTranscriptionUnits().get(0).print();
//			
//			REVERSIBLE	47
//			LEFT-TO-RIGHT	1235
//			RIGHT-TO-LEFT	123
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Caught a "+e.getClass().getName()+". Shutting down...");
		}
		finally
		{
			connection.close();
		}
		
	}
	
	private static void putChildren(OntologyTerm ont,String tabs) throws PtoolsErrorException
	{
		if(ont.getChildren().size()>=0)
		{
			System.out.println(tabs+ont.getChildren().size()+" - "+ ont.getCommonName() + " / "+ont.getLocalID());
			for(OntologyTerm child : ont.getChildren())
			{
				putChildren(child,tabs+"  ");
			}
			if(ont.getChildren().size()==0)
			{
				for(Frame f : ont.getInstances())
				{
					System.out.println(tabs+"\t"+":INSTANCE - "+f.getCommonName());
				}
			}
		}
	}
	


}
