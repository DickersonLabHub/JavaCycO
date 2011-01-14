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


import java.awt.Color;
import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
A very handy class for keeping track of Networks of Frames.
@author John Van Hemert
*/
/**
 * @author jlv
 *
 */
public class Network
{
	private String name;
	private ArrayList<Edge> edges;
	private HashSet<Frame> nodes;
	private HashSet<String> nodeIDs,edgeCodes;
	private HashMap<String,Integer> GMLids;
	private HashMap<String,HashMap<String,ArrayList<String>>> nodeAtts;
	
	public static String REACTANT = "REACTANT";
	public static String PRODUCT = "PRODUCT";
	public static String CATALYSIS = "CATALYSIS\t1\t-";
	public static String COFACTOR = "COFACTOR\t1\t-";
	public static String PROSTHETICGROUP = "PROSTHETICGROUP\t1\t-";
	public static String CATALYSISACTIVATION = "CATALYSISACTIVATION\t1\t-";
	public static String CATALYSISINHIBITION = "CATALYSISINHIBITION\t1\t-";
	public static String COMPLEX_FORMATION = "COMPLEX_FORMATION";
	public static String TRANSCRIPTION = "TRANSCRIPTION\t1\tnucleus";
	public static String TRANSLATION = "TRANSLATION\t1\tcytosol";
	public static String PROMOTERACTIVATION = "PROMOTERACTIVATION\t1\tnucleus";
	public static String PROMOTERINHIBITION = "PROMOTERINHIBITION\t1\tnucleus";
	public static String SIGMAFACTOR = "SIGMAFACTOR\t1\tnucleus";
	public static String PROMOTER = "PROMOTER\t1\tnucleus";
	
	public Network(String name)
	{
		edges = new ArrayList<Edge>();
		nodes = new HashSet<Frame>();
		nodeIDs = new HashSet<String>();
		edgeCodes = new HashSet<String>();
		GMLids = new HashMap<String,Integer>();
		this.name = name;
		nodeAtts = new HashMap<String,HashMap<String,ArrayList<String>>>();
	}
	
	public void addNodeAtt(String nodeID,String key, String value)
	{
		if(nodeIDs.contains(nodeID))
		{
			if(!nodeAtts.containsKey(nodeID)) nodeAtts.put(nodeID, new HashMap<String,ArrayList<String>>());
			if(!nodeAtts.get(nodeID).containsKey(key)) nodeAtts.get(nodeID).put(key,new ArrayList<String>());
			nodeAtts.get(nodeID).get(key).add(value);
		}
	}
	
	public static String ArrayList2GMLList(ArrayList aList)
	{
		if(aList.size()==1)
		{
			if(aList.get(0) instanceof ArrayList) return ArrayList2GMLList((ArrayList)aList.get(0));
			else return cleanString(aList.get(0),true);
			
		}
		String rst = "[";
		int i=1;
		for(Object item : aList)
		{				
			if(item instanceof ArrayList)
				rst += " "+i+" "+ArrayList2GMLList((ArrayList)item);
			else
				rst += " "+i+" "+cleanString(item,true);
			i++;
		}
		return rst+" ] ";
	}
	
	public static String ArrayList2textList(ArrayList aList)
	{
		if(aList.size()==1)
		{
			if(aList.get(0) instanceof ArrayList) return ArrayList2textList((ArrayList)aList.get(0));
			else return cleanString(aList.get(0),false);
			
		}
		String rst = "";
		int i=1;
		for(Object item : aList)
		{				
			if(item instanceof ArrayList)
				rst += "::"+ArrayList2textList((ArrayList)item);
			else
				rst += "::"+cleanString(item,false);
			i++;
		}
		return rst.replace("::::","::").replace("::::","::").replaceAll("^::","");
	}
	
	public static String cleanString(Object o,boolean GMLlists)
	{
		String quote = GMLlists ? "\"" : "";
		String s = o.toString();
		s = s.replace("\"","");
		DecimalFormat df = new DecimalFormat("0.0");
		if(o instanceof String)
		{
			try
			{
				double i = Double.parseDouble(s);
				return df.format(i);
			}
			catch(Exception e)
			{
				s = quote+s.replace("\"","\\\"").replaceAll("[^\\x00-\\x7F]", "")+quote;
			}
		}
		return s.replace("::::","::").replace("::::","::").replaceAll("^::","");
	}
	
	/**
	 * Calls writeGML(w,true)
	 * @param w the PrintStream to write to.
	 * @throws PtoolsErrorException
	 */
	public void writeGML(PrintStream w)
	throws PtoolsErrorException {
		writeGML(w,true,true,true,true);
	}
	
	
	/**
	 * Write the Network in GML format, which can be read by Cytoscape and other Python, R, and Java libraries.
	 * @param w the PrintStream to write to.
	 * @param rich if true, writes slot values that are lists as nested lists as well as Cytoscape-specific graphics attributes. Else, omits slot values that are lists and graphics properties.
	 * @param weights if true, writes stoichiometry coefficients as edge weights.  else, all edges are weighted 1.
	 * @param directed if true, writes a directed graph and reversible reaction edges are duplicated in reverse.  else, an undirected graph is written.
	 * @param directed if true, writes sublists as nested GML lists.  else, write.
	 * @throws PtoolsErrorException
	 */
	public void writeGML(PrintStream w,boolean rich,boolean weights,boolean directed,boolean GMLlists)
	throws PtoolsErrorException {
		String quote = GMLlists ? "" : "\"";
		System.out.println("Writing "+name+" [rich,weights,directed] = "+rich+","+weights+","+directed);
		HashMap<String,ArrayList<String>> pathwayMembership = new HashMap<String,ArrayList<String>>();
		HashMap<String,Integer> reactionDirections = new HashMap<String,Integer>();
		w.println("Creator \"JavaCycO\"");
		w.println("Version 1");
		w.println("graph [\n\tlabel \""+this.name+"\"\n\tdirected "+(directed ? "1" : "0"));
		for(Frame f : this.nodes)
		{
			w.println("\tnode [");
			w.println("\t\tid "+GMLids.get(f.getLocalID()));
			w.println("\t\tlabel "+quote+cleanString(f.getLocalID(),GMLlists)+quote);
			w.println("\t\tCOMMON_NAME "+quote+cleanString(f.getCommonName(),GMLlists)+quote);
			w.println("\t\tclass "+quote+cleanString(f.getClass().getName().replace("javacyco.",""),GMLlists)+quote);
			if(rich)
			{
				for(String slot : f.getSlots().keySet())
				{
					if(slot.equals("COMMON-NAME")) continue;
					ArrayList val = f.getSlotValues(slot);
					if(val.size()==0) continue;
					w.print("\t\t"+slot.replace("-","_").replace(":","").replace("?","").replace("+","_")+" ");
					w.println(quote + (GMLlists ? ArrayList2GMLList(val) : ArrayList2textList(val))+quote);
				}
				String type = "rectangle";
				String fill = Integer.toHexString(Color.CYAN.getRGB() & 0x00ffffff );
				if(f instanceof Compound)
				{
					type = "hexagon";
					fill = Integer.toHexString(Color.GREEN.getRGB() & 0x00ffffff );
				}
				else if(f instanceof Reaction)
				{
					type = "ellipse";
					fill = Integer.toHexString(Color.LIGHT_GRAY.getRGB() & 0x00ffffff );
					String dir = f.getSlotValue("REACTION-DIRECTION");
					if(!reactionDirections.containsKey(dir)) reactionDirections.put(dir,0);
					reactionDirections.put(dir,reactionDirections.get(dir)+1);
				}
				else if(f instanceof Gene)
				{
					fill = Integer.toHexString(Color.YELLOW.getRGB() & 0x00ffffff );
				}
				w.println("\t\tgraphics [ type "+type+" fill \"#"+fill+"\" ]");
				HashSet<String> pwys = new HashSet<String>();
				for(Frame pwy : f.getPathways())
				{
					pwys.add(pwy.getLocalID()+"--"+pwy.getCommonName());
				}
				for(String pwyName : pwys)
				{
					if(!pathwayMembership.containsKey(pwyName)) pathwayMembership.put(pwyName,new ArrayList<String>());
					pathwayMembership.get(pwyName).add(f.getLocalID());
				}
				w.println("\t\tpathway "+quote+(GMLlists ? ArrayList2GMLList(new ArrayList<String>(pwys)) : ArrayList2textList(new ArrayList<String>(pwys)))+quote);
			}
			w.println("\t]");
		}
		for(Network.Edge e : this.edges)
		{
			w.println("\tedge [");
			//String[] infoParts = e.info.split("\t");//infoParts[0]
			w.println("\t\tlabel \""+e.attributes.get("type")+"\"");
			w.println("\t\tsource "+GMLids.get(e.source.getLocalID()));
			w.println("\t\ttarget "+GMLids.get(e.target.getLocalID()));
			if(weights) w.println("\t\tweight "+e.attributes.get("stoichiometry"));
			if(rich)
			{
				HashSet<String> pwys = new HashSet<String>();
				for(Frame pwy : e.source.getPathways())
					pwys.add(pwy.getLocalID()+"--"+pwy.getCommonName());
				for(Frame pwy : e.target.getPathways())
					pwys.add(pwy.getLocalID()+"--"+pwy.getCommonName());
				w.println("\t\tpathway "+quote+(GMLlists ? ArrayList2GMLList(new ArrayList<String>(pwys)) : ArrayList2textList(new ArrayList<String>(pwys)))+quote);
			}
			w.println("\t]");
			if(directed)
			{
				if(e.source instanceof Reaction)
				{
					if(((Reaction)e.source).isReversible())
					{
						w.println("\tedge [");
						w.println("\t\tlabel \""+e.attributes.get("type")+"\"");
						w.println("\t\tsource "+GMLids.get(e.target.getLocalID()));
						w.println("\t\ttarget "+GMLids.get(e.source.getLocalID()));
						if(weights) w.println("\t\tweight "+e.attributes.get("stoichiometry"));
						w.println("\t]");
					}
				}
				else if(e.target instanceof Reaction)
				{
					if(((Reaction)e.target).isReversible())
					{
						w.println("\tedge [");
						w.println("\t\tlabel \""+e.attributes.get("type")+"\"");
						w.println("\t\tsource "+GMLids.get(e.target.getLocalID()));
						w.println("\t\ttarget "+GMLids.get(e.source.getLocalID()));
						if(weights) w.println("\t\tweight "+e.attributes.get("stoichiometry"));
						w.println("\t]");
					}
				}
			}

		}
		w.println("]");
		for(String dir : reactionDirections.keySet())
		{
			System.out.println(dir+"\t"+reactionDirections.get(dir));
		}
		for(String pwyName : pathwayMembership.keySet())
		{
			System.out.print("***PATHWAYS\t"+pwyName.replace(" ","_"));
			for(String id : pathwayMembership.get(pwyName))
			{
				System.out.print("\t"+id);
			}
			System.out.println("");
		}
	}
	
	/**
	Set this Network's name.
	@param newName the new name of this Network.
	*/
	public void setName(String newName)
	{
		name = newName;
	}
	
	/**
	@return this Network's name.
	*/
	public String getName()
	{
		return name;
	}
	
	/**
	@return the Set of all nodes (Frames) in this Network.
	*/
	public Set<Frame> getNodes()
	{
		return nodes;
	}
	
	/**
	Import another Network into this one by combining the two.
	@param net the Network to import into this one.
	*/
	public void importNetwork(Network net)
	throws PtoolsErrorException {
		for(Edge importEdge : net.getEdges())
		{
			if(!edgeCodes.contains(importEdge.getCode()))
			{
				this.addEdge(importEdge.getSource(),importEdge.getTarget(),importEdge.getInfo());
			}
		}
	}
	
	/**
	Add an edge to this Network.  Does not add the edge if the new edge's getCode() value is already in the Network.
	@param f the Frame at the "source" of the arrow.  f refers to "from"
	@param t the Frame at the "target" of the arrow.  t refers to "to"
	@param e any String to be associated with this Edge.  When printing, tab-delimited files are written, so tab-delimited edge 
	attributes fit well here.
	*/
	public void addEdge(Frame f, Frame t, String e)
	throws PtoolsErrorException {
		Edge newEdge = new Edge(f,t,e);
		if(!edgeCodes.contains(newEdge.getCode()))
		{
			edgeCodes.add(newEdge.getCode());
			edges.add(newEdge);
			addNode(f,false);
			addNode(t,false);
		}
	}
	
	/**
	@return an ArrayList of all the Edges in this Network.
	*/
	public ArrayList<Edge> getEdges()
	{
		return edges;
	}
	
	/**
	Add a Frame to this Network, overwriting any node already in the Network with the same ID.
	@param node the Frame to add to this Network.
	*/
	public void addNode(Frame node)
	throws PtoolsErrorException {
		addNode(node,true);
	}
	
	/**
	Add a Frame to this Network.
	@param node the Frame to add to this Network.
	@param overwrite if true, overwrites any node already in the Network with the same ID else does not overwrite.
	*/
	public void addNode(Frame node,boolean overwrite)
	throws PtoolsErrorException {
		if(!overwrite && this.containsNode(node))
			return;
		nodes.add(node);
		nodeIDs.add(node.getLocalID());
		if(!GMLids.containsKey(node.getLocalID()))
		{
			GMLids.put(node.getLocalID(),GMLids.size());
		}
		//System.out.println(nodes.size());
	}
	
	/**
	Print the Network's structure to a file as a tab-delimited file where Edges are represented as "from	edgeattributes	to"
	The file is named using the name given to this Network following by "_structure.tab".
	*/
	public void printStructureTab()
	{
		PrintStream o = null;
		try
		{
			o = new PrintStream(new File(name+"_structure.tab"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
		
		//Print Headers
		o.println("Source"+"\t"+"InteractionType"+"\t"+"Stoichiometry"+"\t"+"Compartment"+"\t"+"Target"+"\t"+"pathway");
		
		for(Edge eg : edges)
		{
			eg.print(o);
		}
	}
	
	/**
	Print all Frames and important attributes to a tab-delimited file named by this Network's name plus "_node_atts.tab".
	Prints common names, comments, and javacyc class names
	*/
	public void printNodeAttributesTab()
	throws PtoolsErrorException {
		PrintStream o = null;
		try
		{
			o = new PrintStream(new File(name+"_node_atts.tab"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
		
		//Print Headers
		o.println("EcoCycID"+"\t"+"CommonName"+"\t"+"Comment"+"\t"+"Type"+"\t"+"pathway"+"\t"+"SuperPathways");
		
		for(Frame node : nodes)
		{
			//Get the pathways this element can be found in
			
//			ArrayList<Frame> pways = new ArrayList<Frame>();
//			for (Frame pway : node.getPathways()) {
//				//Add each pathway to the list
//				if (!pways.contains(pway)) pways.add(pway);
//				//Add superpathways to the list
//				for (Frame sPway : pway.getPathways()) if (!pways.contains(sPway)) pways.add(sPway);
//			}
			
//			String inPathways = "";
//			String superPathways = "";
			
//			for (Frame pway : pways) {
//				
//				if (!(pway instanceof Pathway)) {
//					System.out.println(pway.ID);
//					break;
//				}
//				
//				if (((Pathway)pway).isSuperPathway()) {
//					superPathways = superPathways + pway.ID + "::";
//				} else {
//					inPathways = inPathways + pway.ID + "::";
//				}
//			}
			
			///////
			String inPathways = "";
			String superPathways = "";
			
			for (Frame pway : node.pathways) {
				//Check for odd non-pathway frames
				if (!(pway instanceof Pathway)) {
					System.out.println(pway.ID);
					break;
				}
				
				if (((Pathway)pway).isSuperPathway()) {
					superPathways = superPathways + pway.ID + "::";
				} else {
					inPathways = inPathways + pway.ID + "::";
				}
			}
			///////
			
			if (inPathways.length()==0) inPathways = null;
			//else inPathways = inPathways.substring(0, inPathways.length()-2);
			
			if (superPathways.length()==0) superPathways = null;
			//else superPathways = superPathways.substring(0, superPathways.length()-2);
			
			o.println(node.getLocalID()+"\t"+node.getCommonName()+"\t"+node.getComment()+"\t"+node.getClass().getName().replace("javacyc.", "")+"\t"+inPathways+"\t"+superPathways);
		}
	}
	
	/**
	Calls all the print methods:
		printStructureTab();
		printNodeAttributesTab();
		printSynonyms();
		printGeneAltIDs();
	*/
	public void printTab()
	throws PtoolsErrorException {
		printStructureTab();
		printNodeAttributesTab();
		printSynonyms();
		printGeneAltIDs();
	}
	
	/**
	@param f the Frame to check for in this Network.
	@return true if this Network contains a node Frame with the same ID as the input Frame. else returns false.
	*/
	public boolean containsNode(Frame f)
	{
		return nodeIDs.contains(f.getLocalID());
	}
	
	/**
	@param id the ID to check for in this Network's Frame nodes.
	@return true if this Network contains a node Frame whose ID is the same as the input. else returns false.
	*/
	public boolean containsNode(String id)
	{
		return nodeIDs.contains(id);
	}
	
	/**
	Print all alternate names and synonyms for all node Frames in this Network to a tab-delimited filed named by this Network's 
	name plus "_node_synonyms.tab"
	*/
	public void printSynonyms()
	throws PtoolsErrorException {
		PrintStream o = null;
		try
		{
			o = new PrintStream(new File(name+"_node_synonyms.tab"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
		
		//Print Headers
		o.println("EcoCycID"+"\t"+"Synonyms");
		
		for(Frame f : this.nodes)
		{
			o.print(f.getLocalID());
			if(f.getLocalID().contains("&") && f.getLocalID().contains(";")) continue;
			try
			{
				for(String s : f.getSynonyms())
					o.print("\t"+s);
				for(String s : f.getNames())
					o.print("\t"+s);
				o.print("\n");
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.exit(0);
			}
		}
	}
	
	public ArrayList toPredecessorsList()
	{
		ArrayList rst = new ArrayList();
		for(Edge eg : edges)
		{
			ArrayList pair = new ArrayList();
			pair.add(eg.target.getLocalID());
			pair.add(eg.source.getLocalID());
			rst.add(pair);
		}
		return rst;
	}
	
	/**
	Print all alternate IDs for node Frames that are Genes in this Network to a tab-delimited filed named by this Network's 
	name plus "_gene_alt_ids.tab"
	*/
	public void printGeneAltIDs()
	throws PtoolsErrorException {
		PrintStream o = null;
		try
		{
			o = new PrintStream(new File(name+"_gene_alt_ids.tab"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
		
		//Print Headers
		o.println("EcoCycID"+"\t"+"CommonName"+"\t"+"BNumber"+"\t"+"ECK");
		
		for(Frame f : this.nodes)
		{
			if(!(f instanceof Gene) || f.getLocalID().contains("&") && f.getLocalID().contains(";")) continue;
			try
			{
				Gene g = (Gene)f;
				o.println(g.getLocalID()+"\t"+g.getCommonName()+"\t"+g.getBNumber()+"\t"+g.getECK());
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.exit(0);
			}
		}
	}
	
	/**
	Calls the getPathways() function on every node in the network. Adds pathways to edges such that each edge contains the intersection of the
	pathway list of its source and target.
	Pathway information added here is intended to allow the Cytoscape plugin "Subgraph Creator" to create subgraphs based on node pathways.  This
	plugin requires both nodes and edges to have a "pathway" attribute.
	@throws PtoolsErrorException 
	*/
	public void loadNetworkPathwayInfo() throws PtoolsErrorException {
		Set<Frame> nodes = this.getNodes();
		//Call getPathways on each node
		for (Frame node : nodes) {
			node.getPathways();
		}
		
		//Handle nodes for which getPathways does not return pathway information. Reaction nodes in complex formation use the intersection of
		//pathway information for the adjacent nodes
		for (Frame node : nodes) {
			if (node.ID.endsWith("-formation")) {
				//Take all pathways of the first upstream neighbor and save the intersection of the pathways of all other upstream
				//and downstream neighbors
				Set<Frame> intersection = new HashSet<Frame>();
				boolean first = true;
				for (Frame upStreamFrame : this.getUpstreamNeighbors(node)) {
					if (first) {
						intersection.addAll(upStreamFrame.pathways);
						first = false;
					} else {
						intersection.retainAll(upStreamFrame.pathways);
					}
				}
				for (Frame upStreamFrame : this.getDownstreamNeighbors(node)) intersection.retainAll(upStreamFrame.pathways);
				node.pathways.addAll(intersection);
			}
		}
		
		//Pathway information for edges is the intersection of the pathway information of the source and target nodes
		for (Edge edge : this.getEdges()) {
			for (Frame pathway : edge.getSource().pathways) {
				if (edge.getTarget().pathways.contains(pathway) && !edge.pathways.contains(pathway.ID)) edge.pathways.add(pathway.ID);
			}
		}
	}
	
	/**
	Return all nodes that appear as a source in an edge for which this node is a target.
	*/
	public ArrayList<Frame> getUpstreamNeighbors(Frame node) {
		ArrayList<Frame> sources = new ArrayList<Frame>();
		for (Edge edge : this.getEdges()) {
			if (edge.getTarget().ID.equals(node.ID) && !sources.contains(edge.getSource())) {
				sources.add(edge.getSource());
			}
		}
		return sources;
	}
	
	/**
	Return all nodes that appear as a target in an edge for which this node is a source.
	*/
	public ArrayList<Frame> getDownstreamNeighbors(Frame node) {
		ArrayList<Frame> targets = new ArrayList<Frame>();
		for (Edge edge : this.getEdges()) {
			if (edge.getSource().ID.equals(node.ID) && !targets.contains(edge.getTarget())) {
				targets.add(edge.getTarget());
			}
		}
		return targets;
	}
	
	
	/**
	A class used by Network.  Simply contains a source Frame, a target Frame, and a String of arbitrary info.
	*/
	public class Edge
	{
		private Frame source,target;
		private String info;
		public ArrayList<String> pathways = new ArrayList<String>();
		public HashMap<String,String> attributes;
		public String[] defaultAttLabels = {"type","stoichiometry","compartment"};
		
		
		public Edge(Frame f,Frame t,String e)
		{
			source = f;
			target = t;
			info = e;
			attributes = new HashMap<String,String>();
			int i=0;
			for(String val : e.split("\t"))
			{
				if(i<defaultAttLabels.length)
				{
					if(i==1)
					{
						try
						{
							val = val.replace("(","").replace(")","").replace(" ","").replace("\"","").replace("|","").replaceAll("[a-z]","1");
							double test = Double.parseDouble(val);
							//val = cleanString(val);
						}
						catch(Exception ex)
						{
							System.out.println(f.getLocalID()+" -> "+t.getLocalID()+" :: "+e+" ["+val+"]");
						}
					}
					attributes.put(defaultAttLabels[i],val);
				}
				i++;
			}
		}
		
		public String getCode()
		{
			return source.getLocalID()+target.getLocalID();
		}
		
		public Frame getSource()
		{
			return source;
		}
		
		public void setSource(Frame s)
		{
			source=s;
		}
		
		public Frame getTarget()
		{
			return target;
		}
		
		public void setTarget(Frame s)
		{
			target=s;
		}
		
		/**
		Print "source	info	target"
		*/
		public void print(PrintStream o)
		{
			String inPathways = "";
			String superPathways = "";
			
			for (String pway : this.pathways) {
				inPathways = inPathways + pway + "::";
			}
			
			if (inPathways.length()==0) inPathways = null;
			//else inPathways = inPathways.substring(0, inPathways.length()-2);
			
			o.println(source.getLocalID()+"\t"+info+"\t"+target.getLocalID()+"\t"+inPathways);
		}
		
		public void setInfo(String s)
		{
			info = s;
		}
		
		public String getInfo()
		{
			return info;
		}
	}
}