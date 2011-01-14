package edu.iastate.pathwayaccess.celldesigner.biocyc

/**
 *  This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import edu.iastate.javacyco.*;
import edu.iastate.pathwayaccess.*;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JProgressBar;

import org.sbml.libsbml.XMLNode;
import org.sbml.libsbml.XMLToken;
import org.sbml.libsbml.XMLTriple;


import jp.sbi.celldesigner.plugin.*;

import jp.sbi.celldesigner.plugin.util.*;
import jp.fric.graphics.draw.GStructure;

public class BioCycPlugin extends PathwayAccessPlugin {
	
	JavacycConnection cyc;
	JProgressBar progressBar;
	HashSet<String> done;

	public BioCycPlugin()
	{
		super(new String[]{CONNECT,DOWNLOAD,COMMIT});
		done = new HashSet<String>();
	}
	
	protected void setMyColor()
	{
		myColor = Color.magenta;
	}
	
	public void connect()
	{
		String server = Prompt.userEnter("Connect to...","Enter BioCyc server ip/name");
		int port = Integer.parseInt(Prompt.userEnter("Connect to...","Enter port"));
		cyc = new JavacycConnection(server,port);
		cyc.selectOrganism("META");
		try {
			cyc.testConnection();
			this.connected = true;
			this.loggedIn = true;
		} catch (Exception e) {
			e.printStackTrace();
			this.loggedIn = false;
			this.connected = false;
			Prompt.errorMessage("Connection Problem", "There was a problem connecting to "+server+":"+port+". \nSomething about a "+e.getClass().getName()+": "+e.getMessage()+"...");
		}
		
	}
	
	public void login()
	{
		this.loggedIn = connected;
	}
	
	private void selectOrg()
    throws PtoolsErrorException{
		ArrayList orgs = new ArrayList();
		for(OrgStruct org : cyc.allOrgs())
		{
			orgs.add(org.getLocalID()+"  "+org.getSpecies());
		}
		String s = Prompt.userSelect("BioCyc","Select organsim",orgs);

		if ((s != null) && (s.length() > 0))
		{
		    cyc.selectOrganism(s.split("  ")[0]);
		    //System.out.println(s.split("  ")[0]);
		}
    }

	
	public void addPluginMenu() {
	}
	public void SBaseAdded(PluginSBase sbase) {
	}
	public void SBaseChanged(PluginSBase sbase) {
	}
	public void SBaseDeleted(PluginSBase sbase) {
	}
	public void modelOpened(PluginSBase sbase) {
	}
	public void modelSelectChanged(PluginSBase sbase) {
	}
	public void modelClosed(PluginSBase sbase) {
	}

	@Override
	protected void addModifier(Object rxn, Object participant) {
		if(rxn != null)
		{
			try
			{
				EnzymeReaction r = (EnzymeReaction)rxn;
				Catalysis cat = (Catalysis) Frame.create(cyc,Catalysis.GFPtype);
				if(participant instanceof Protein)
				{
					Protein p = (Protein)participant;
					cat.setEnzyme(p);
				}
				else
				{
					Frame f = (Frame)participant;
					cat.addActivator(f);
				}
				r.addCatalysis(cat);
				((Frame)participant).commit();
				cat.commit();
				r.commit();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	@Override
	protected void addProduct(Object rxn, Object participant, int stoich) {
		if(rxn != null)
		{
			try {
				((Reaction)rxn).addProduct((Frame)participant, stoich);
				String comp = ((Frame)participant).annotations.get("COMPARTMENT");
				if(comp!=null && comp.length()>0)
					cyc.putAnnotation(((Reaction)rxn).getLocalID(), "RIGHT", ((Frame)participant).getLocalID(), "COMPARTMENT", comp);
			} catch (PtoolsErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void addReactant(Object rxn, Object participant, int stoich) {
		if(rxn != null)
		{
			try {
				((Reaction)rxn).addReactant((Frame)participant, stoich);
				String comp = ((Frame)participant).annotations.get("COMPARTMENT");
				if(comp!=null && comp.length()>0)
					cyc.putAnnotation(((Reaction)rxn).getLocalID(), "LEFT", ((Frame)participant).getLocalID(), "COMPARTMENT", comp);
			} catch (PtoolsErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void addReactionToPathway(Object rxn, Object pwy) {
		if(rxn != null)
		{
			try {
				((Pathway)pwy).addReaction((Reaction)rxn);
			} catch (PtoolsErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	protected Object commit(Object o) {
		if(o != null)
		{
			try {
				((Frame)o).commit();
			} catch (PtoolsErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return o;
	}

	@Override
	protected void enrichReaction(PluginReaction CDrxn, Object rxn) {
		Reaction r = (Reaction)rxn;
		try {
			CDrxn.setReversible(r.isReversible());
			String comment = r.getComment();
			if(comment != null && comment.length()>0)
				this.addAnnotation("comment", CDrxn, comment);
			String ec = r.getEC();
			if(ec != null && ec.length()>0)
				this.addAnnotation("ec", CDrxn, ec);
//			for(Object cite : ((Frame)rxn).getSlotValues("CITATIONS"))
//			{
//				this.addAnnotation("citations", CDrxn, (String)cite);
//			}
		} catch (PtoolsErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void enrichSpeciesAlias(PluginSpeciesAlias spca, Object o,SelectPathwaysFrame frame) {
		try 
		{
			//this.backtracePathways((Frame)o,this.getSelectedModel(),true,frame);
			//if(this.findSpeciesAlias(getType(o),getName(o)) != null) return;
			if(!(o instanceof Protein || o instanceof Gene)) return;
			if(done.contains(getName(o))) return;
			//this.makePluginSpeciesAlias(o, false, frame);
			PluginReaction CDrxn = new PluginReaction();
			String name = "";
			Frame f = (Frame)o;
			this.attachProduct(CDrxn, f, 1, frame);
			if(f instanceof Complex)
			{
				ArrayList<Frame> comps = ((Complex)f).getComponents();
				if(comps.size()==0)
					return;
				String formationName = f.getCommonName()+"-formation";
				CDrxn.setName(formationName);
				String type = PluginReactionSymbolType.STATE_TRANSITION;
				//if(comps.size()>1)
				//		type = PluginReactionSymbolType.HETERODIMER_ASSOCIATION;
				CDrxn.setReactionType(type);
				for(Frame c : comps)
				{
					int stoich = this.getStoichiometry(null,c);
					PluginSpeciesReference ref = this.attachReactant(CDrxn, c, stoich, frame);
					this.enrichSpeciesAlias(ref.getAlias(), c, frame);
				}
			}
			else if(f instanceof Monomer)
			{
				ArrayList<Gene> genes = ((Monomer)f).getGenes();
				if(genes.size()==0) return;
				name = f.getCommonName()+"-translation";
				CDrxn.setName(name);
				CDrxn.setReactionType(PluginReactionSymbolType.TRANSLATION);
				CDrxn.setReversible(false);
				for(Gene g : genes)
				{
					PluginSpeciesReference ref = this.attachReactant(CDrxn, g, 1, frame);
					this.enrichSpeciesAlias(ref.getAlias(), g, frame);
				}
			}
			else if(f instanceof Gene)
			{
				ArrayList<TranscriptionUnit> tus = ((Gene)f).getTranscriptionUnits();
				if(tus.size()==0) return;
				name = f.getCommonName()+"-transcription";
				CDrxn.setName(name);
				CDrxn.setReactionType(PluginReactionSymbolType.TRANSCRIPTION);
				CDrxn.setReversible(false);
				for(TranscriptionUnit tu : ((Gene)f).getTranscriptionUnits())
				{
					this.attachReactant(CDrxn, tu, 1, frame);
//					Promoter pm = tu.getPromoter();
//					if(pm != null)
//					{
//						PluginModificationRegion reg = new PluginModificationRegion(spca.getGene());
//						reg.setType(type)
//						spca.getGene().addPluginModificationRegion(residue)
						
//						for(Frame a : pm.getActivators())
//						{
//							PluginModifierSpeciesReference ref = this.attachModifier(CDrxn,a,PluginReactionSymbolType.CATALYSIS,frame);
//							this.enrichSpeciesAlias(ref.getAlias(), a, frame);
//						}
//						for(Frame i : pm.getInhibitors())
//						{
//							PluginModifierSpeciesReference ref = this.attachModifier(CDrxn,i,PluginReactionSymbolType.INHIBITION,frame);
//							this.enrichSpeciesAlias(ref.getAlias(), i, frame);
//						}
//						Frame sigma = pm.getSigmaFactor();
//						if(sigma != null)
//						{
//							PluginModifierSpeciesReference ref = this.attachModifier(CDrxn,sigma,PluginReactionSymbolType.CATALYSIS,frame);
//							this.enrichSpeciesAlias(ref.getAlias(), sigma, frame);
//						}
//					}
				}
			}
			else return;
			String comment = ((Frame)o).getComment();
			if(comment != null && comment.length()>0)
				this.addAnnotation("comment", spca, comment);
//			for(Object cite : ((Frame)o).getSlotValues("CITATIONS"))
//			{
//				this.addAnnotation("citations", spca, (String)cite);
//			}
			done.add(getName(o));
			//CDrxn.setReversible(false);
			//this.getSelectedModel().addReaction(CDrxn);
			//this.notifySBaseAdded(CDrxn);
			CDrxn = this.addPluginReactionToModel(CDrxn, getLocation(o));
			if(name.length()>0) 
				this.addAnnotation(this.getClass().getName()+".NAMES", CDrxn, name);
		} 
		catch (PtoolsErrorException ex) 
		{
			ex.printStackTrace();
		}
	}

	@Override
	protected Object getId(Object o) {
		if(o instanceof OrgStruct)
			return((OrgStruct)o).getLocalID();
		else if(o instanceof String)
			return o;
		else
			return ((Frame)o).getLocalID();
	}
	
	@Override
	protected String getOriginalType(Object o)
	{
		return o.getClass().getName();
	}

	@Override
	protected String getLocation(Object o) {

		Frame f = (Frame)o;
		String locId = f.annotations.get("COMPARTMENT");
		String loc = null;
		try
		{
			if(locId!=null && locId.length()==0)
			{
				if(cyc.frameExists(locId))
					loc = Frame.load(this.cyc,locId).getCommonName();
				else loc = locId;
			}
			if(loc==null)
			{
				if(f instanceof Protein)
				{
					ArrayList<OntologyTerm> terms = new ArrayList<OntologyTerm>();
					terms.addAll(((Protein)f).getCellComponents());
					OntologyTerm l = OntologyTerm.getLowestCommonChild(cyc,terms);
					if(l==null)
					{
						terms = new ArrayList<OntologyTerm>();
						for(OntologyTerm t : (((Protein)f).getGOterms()))
						{
							if(t instanceof GOCellularComponent)
								terms.add(t);
						}
						l = OntologyTerm.getLowestCommonChild(cyc,terms);
					}
					if(l != null) loc = l.getCommonName();
				}
			}
			if(loc==null)
				//if(f instanceof TranscriptionUnit)
					//loc = NUCLEUS;
			if(loc==null || loc.equals("cytoplasm"))
			{
				loc = CYTOSOL;
			}
			loc = loc.replace("\"","");
			if(!loc.startsWith("extracell"))
			{
				if(loc.equals("unknown space"))
				{
					loc = CYTOSOL;
				}
				//if((f instanceof Compound))
				//	loc = "cytosol";
				//System.out.println(f.getCommonName()+" IN "+loc);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.out.println("Placing "+f.getLocalID()+" in "+CYTOSOL);
		}
		if(loc==null) loc = CYTOSOL;
		return loc;
	}

	@Override
	protected String getName(Object o) {
		try {
			if(o instanceof OrgStruct)
				return ((OrgStruct)o).getSpecies();
			else if(o instanceof String)
				return cyc.getSlotValue((String)o, "COMMON-NAME").replace("\"","");
			else
				return ((Frame)o).getCommonName();
		} catch (PtoolsErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ((Frame)o).getLocalID();
		}
	}

	@Override
	protected int getNumPathwayParticipants(Object pwy) {
		Pathway p = (Pathway)pwy;
		int rst = 1000;
		try {
			rst = p.numCompounds()+p.numEnzymes()+p.numGenes();
		} catch (PtoolsErrorException e) {
			e.printStackTrace();
			System.out.println("Returning "+rst+" participants for "+p.getLocalID());
		}
		return rst;
	}

	@Override
	protected Iterable getOrganisms() {
		try {
			return cyc.allOrgs();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Can't get all orgs");
			return new ArrayList();
		}
		
	}

	@Override
	protected Iterable getPathwayReactions(Object pwy) {
		try {
			return ((Pathway)pwy).getReactions();
		} catch (PtoolsErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ArrayList();
		}
	}

	@Override
	protected Iterable getPathwaysFromIds(ArrayList ids) {
		try {
			return Pathway.load(cyc, ids);
		} catch (PtoolsErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ArrayList();
		}
	}

	@Override
	protected Iterable getPathwaysFromOrganismId(Object orgId) {
		cyc.selectOrganism((String)orgId);
		try {
			return cyc.allPathways();
		} catch (PtoolsErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ArrayList();
		}
	}

	@Override
	protected ArrayList<ModifierReference> getReactionModifiers(Object rxn) {
		ArrayList<ModifierReference> rst = new ArrayList<ModifierReference>();
		HashSet<String> enzDone = new HashSet<String>();
		try
		{
			//System.out.println("Loading reaction: "+((Frame)rxn).getCommonName());
			if(rxn instanceof EnzymeReaction)
			{
				for(Frame f : ((EnzymeReaction)rxn).getCatalysis())
				{
					Catalysis c = (Catalysis)f;
					
					Protein p = c.getEnzyme();
					if(p==null || enzDone.contains(p.getCommonName())) continue;
					enzDone.add(p.getCommonName());
					rst.add(new ModifierReference(p,PluginReactionSymbolType.CATALYSIS,c.getKm()));
					
					for(Frame f2 : c.getCofactors())
					{
						rst.add(new ModifierReference(f2,PluginReactionSymbolType.PHYSICAL_STIMULATION));
					}
					for(Frame f2 : c.getProstheticGroups())
					{
						rst.add(new ModifierReference(f2,PluginReactionSymbolType.PHYSICAL_STIMULATION));
					}
					for(Frame f2 : c.getActivators())
					{
						rst.add(new ModifierReference(f2,PluginReactionSymbolType.TRIGGER));
					}
					for(Frame f2 : c.getInhibitors())
					{
						rst.add(new ModifierReference(f2,PluginReactionSymbolType.INHIBITION));
					}
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return rst;
	}

	@Override
	protected Iterable getReactionProducts(Object rxn) {
		try {
			return ((Reaction)rxn).getProducts();
		} catch (PtoolsErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ArrayList();
		}
	}

	@Override
	protected Iterable getReactionReactants(Object rxn) {
		try {
			return ((Reaction)rxn).getReactants();
		} catch (PtoolsErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ArrayList();
		}
	}

	@Override
	protected ArrayList<String> getSynonyms(Object o) {
		ArrayList<String> rst = new ArrayList<String>();
		try {
			for(String s : ((Frame)o).getSynonyms()) rst.add(s.replace("\"",""));
		} catch (PtoolsErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			return rst;
		}
	}

	@Override
	protected String getType(Object o) {
		//System.out.println(getName(o)+" "+getId(o)+" is a "+o.getClass().getName());
		if(o instanceof TranscriptionUnit) return PluginSpeciesSymbolType.GENE;
		if(o instanceof Gene) return PluginSpeciesSymbolType.RNA;
		if(o instanceof Complex) return PluginSpeciesSymbolType.COMPLEX;
		if(o instanceof Protein) return PluginSpeciesSymbolType.PROTEIN;
		if(o instanceof Compound) return PluginSpeciesSymbolType.SIMPLE_MOLECULE;
		if(o instanceof Reaction) return PluginReactionSymbolType.STATE_TRANSITION;
		if(o instanceof Catalysis) return PluginReactionSymbolType.CATALYSIS;
		if(o instanceof TransportReaction)return PluginReactionSymbolType.TRANSPORT;
		System.out.println("UNKNOWN: "+o.getClass().getName());
		return PluginSpeciesSymbolType.UNKNOWN;
	}
	
	protected String getJavaCycClass(PluginSBase sb) {
		//System.out.println(getName(o)+" "+getId(o)+" is a "+o.getClass().getName());
		if(sb instanceof PluginModel)
			return Pathway.GFPtype;
		if(sb instanceof PluginSpeciesAlias)
		{
			PluginSpeciesAlias o = (PluginSpeciesAlias)sb;
			if(o.getType().equals(PluginSpeciesSymbolType.GENE)) return TranscriptionUnit.GFPtype;
			if(o.getType().equals(PluginSpeciesSymbolType.RNA)) return Gene.GFPtype;
			if(o.getType().equals(PluginSpeciesSymbolType.COMPLEX)) return Complex.GFPtype;
			if(o.getType().equals(PluginSpeciesSymbolType.PROTEIN)) return Monomer.GFPtype;
			if(o.getType().equals(PluginSpeciesSymbolType.SIMPLE_MOLECULE)) return Compound.GFPtype;
			if(o.getType().equals(PluginSpeciesSymbolType.ION)) return "|Ions|";
			if(o.getType().equals(PluginSpeciesSymbolType.PHENOTYPE)) return Pathway.GFPtype;
			System.out.println("CAN'T FIND "+o.getType());
		}
		if(sb instanceof PluginReaction)
		{
			PluginReaction o = (PluginReaction)sb;
			if(o.getReactionType().equals(PluginReactionSymbolType.STATE_TRANSITION)) 
			{
				if(o.getNumModifiers()>0)
					return EnzymeReaction.GFPtype;
				else
					return Reaction.GFPtype;
			}
			if(o.getReactionType().equals(PluginReactionSymbolType.CATALYSIS)) return Catalysis.GFPtype;
			if(o.getReactionType().equals(PluginReactionSymbolType.TRANSPORT)) return TransportReaction.GFPtype;
			if(o.getReactionType().equals(PluginReactionSymbolType.TRANSCRIPTION)) return null;
			if(o.getReactionType().equals(PluginReactionSymbolType.TRANSLATION)) return null;
			System.out.println("CAN'T FIND "+o.getReactionType());
		}
		
		return OntologyTerm.GFPtype;
	}

	@Override
	protected Object initEntity(String comp, Object orgId, PluginSpeciesAlias spca) {
		Frame f = (Frame)initFrame(spca);
		try
		{
			Frame compFrame = lookup(null,comp,CellComponent.GFPtype);
			if(compFrame==null) 
				compFrame = lookup(null,comp,GOCellularComponent.GFPtype);
			else if(f instanceof Protein)
			{
				f.addSlotValue("LOCATIONS", compFrame.getLocalID());
				return f;
			}
			if(compFrame==null)
				f.annotations.put("COMPARTMENT", comp);
			else
			{
				f.annotations.put("COMPARTMENT", compFrame.getLocalID());
				if(f instanceof Protein)
					f.addSlotValue("GO-TERMS", compFrame.getLocalID());
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return f;
	}
	

	
	private Frame initFrame(PluginSBase sb)
	{
		Frame rst = null;
		String name = "";
		String GFPtype = getJavaCycClass(sb);
		if(sb instanceof PluginSpeciesAlias) name = ((PluginSpeciesAlias)sb).getName();
		else if(sb instanceof PluginReaction) name = ((PluginReaction)sb).getName();
		else if(sb instanceof PluginModel) name = ((PluginModel)sb).getName();
		else return null;
		System.out.println("INIT FOR "+GFPtype+":"+name);
		try {
			ArrayList<Frame> hits = cyc.search(name,GFPtype);
			if(hits.size()==0)
			{
				
				if(sb instanceof PluginModel)
				{
					LinkedHashMap<String,String> classMap = cyc.getPathwayOntology(false);
					String selection = Prompt.userSelect("Select Pathway Class", "Please select a class that fits "+name, new ArrayList(classMap.keySet()));
					if(selection==null) 
					{
						System.out.println("Can't load pathway ontology.");
						return null;
					}
					rst = Frame.create(cyc, classMap.get(selection));
				}
				else
				{
					rst = Frame.create(cyc,GFPtype);
				}
				ArrayList<String> newFrameIdList = new ArrayList<String>();
				newFrameIdList.add(rst.getLocalID());
				cyc.searchCache.put(name.toUpperCase(), newFrameIdList);
			}
			else if(hits.size()==1) 
			{
				rst = hits.get(0);
			}
			else
			{
				return lookup(hits,name,GFPtype);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		rst.setCommonName(name.length()>0 ? name : rst.getLocalID());
		this.commitNotes(sb, rst, "comment", "COMMENT",true);
		this.commitNotes(sb, rst, "ec", "EC-NUMBER",true);
		//this.commitNotes(sb, rst, "citations", "CITATIONS");
		return rst;
	}
	
	private Frame lookup(ArrayList<Frame> hits,String name,String GFPtype) throws PtoolsErrorException
	{
		if(hits==null) 
			hits = cyc.search(name,GFPtype);
		else if(hits.size()==1)
			return hits.get(0);
		else if(hits.size()>1)
		{
			TreeMap<String,Frame> hitMap = new TreeMap<String,Frame>(String.CASE_INSENSITIVE_ORDER);
			for(Frame f : hits) hitMap.put(f.getCommonName()+" ("+f.getLocalID()+")",f);
			String selection = Prompt.userSelect("Multiple Objects Found", name+" was found multiple times.  Which one is it?", new ArrayList<String>(hitMap.keySet()));
			if(selection != null) 
			{
				ArrayList<String> choice = new ArrayList<String>();
				choice.add(hitMap.get(selection).getLocalID());
				cyc.searchCache.put(name.toUpperCase(), choice);
				Prompt.infoMessage("", name+" is now mapped to "+cyc.searchCache.get(name.toUpperCase()).get(0)+" /"+cyc.searchCache.get(name.toUpperCase()).size());
				return hitMap.get(selection);
			}
		}
		return null;
	}
	
	private void commitNotes(PluginSBase sb,Frame f,String noteLabel,String slot,boolean useQuotes)
	{
		HashSet<String> comments = this.getAnnotation(noteLabel, sb);
		if(comments!=null)
			try {
				for(String comment : comments)
				{
					comment = useQuotes ? "\""+comment+"\"" : comment;
					f.addSlotValue(slot, comment);
				}
			} catch (PtoolsErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	@Override
	protected Object initPathway(PluginModel model, Object orgId) {
		cyc.selectOrganism((String)orgId);
		Pathway p = (Pathway)initFrame(model);
		p.clear();
		return p;
	}

	@Override
	protected Object initReaction(Object orgId, PluginReaction CDrxn) {
		try{
			//Biocyc dbs don't represent translation or transcription as "reactions" so don't handle those the normal way way.
			if(CDrxn.getReactionType().equals(PluginReactionSymbolType.TRANSCRIPTION))
			{
				//products
				for(int j=0; j<CDrxn.getNumReactants(); j++)
				{
					PluginSpeciesAlias spca = CDrxn.getReactant(j).getAlias();
					if(!spca.getType().equals(PluginSpeciesSymbolType.GENE))
					{
						System.out.println("ERROR!  You can't transcribe a "+spca.getType());
						continue;
					}
					TranscriptionUnit tu = (TranscriptionUnit)commit(initEntity(this.getContainingCompartment(spca, this.getSelectedModel()),orgId,spca));
		   			//reactants
					for(int k=0; k<CDrxn.getNumProducts(); k++)
					{
						PluginSpeciesAlias spca2 = CDrxn.getProduct(k).getAlias();
						if(!spca2.getType().equals(PluginSpeciesSymbolType.ANTISENSE_RNA) && !spca2.getType().equals(PluginSpeciesSymbolType.RNA))
						{
							System.out.println("ERROR!  You can't transcribe into a "+spca2.getType());
							continue;
						}
						Gene g = (Gene)initEntity(this.getContainingCompartment(spca2, this.getSelectedModel()),orgId,spca2);
						g.addTransciptionUnit(tu);
						g.commit();
					}
	    			//modifiers
	    			for(int k=0; k<CDrxn.getNumModifiers(); k++)
	    			{
	    				PluginSpeciesAlias spca2 = CDrxn.getModifier(k).getAlias();
	    				Frame mod = (Frame)initEntity(this.getContainingCompartment(spca, this.getSelectedModel()),orgId,spca2);
	    				tu.addActivator(mod);
	    				mod.commit();
	    			}
	    			tu.commit();
				}
				return null;
			}
			else if(CDrxn.getReactionType().equals(PluginReactionSymbolType.TRANSLATION))
			{
				//products
				for(int j=0; j<CDrxn.getNumReactants(); j++)
				{
					PluginSpeciesAlias spca = CDrxn.getReactant(j).getAlias();
					if(!spca.getType().equals(PluginSpeciesSymbolType.ANTISENSE_RNA) && !spca.getType().equals(PluginSpeciesSymbolType.RNA))
					{
						System.out.println("ERROR!  You can't translate a "+spca.getType());
						continue;
					}
					Gene g = (Gene)commit(initEntity(this.getContainingCompartment(spca, this.getSelectedModel()),orgId,spca));
		   			//reactants
					for(int k=0; k<CDrxn.getNumProducts(); k++)
					{
						PluginSpeciesAlias spca2 = CDrxn.getProduct(k).getAlias();
						if(!spca2.getType().equals(PluginSpeciesSymbolType.PROTEIN))
						{
							System.out.println("ERROR!  You can't translate into a "+spca2.getType());
							continue;
						}
						Monomer p = (Monomer)commit(initEntity(this.getContainingCompartment(spca2, this.getSelectedModel()),orgId,spca2));
						p.addGene(g);
						p.commit();
					}
					g.commit();
	    			//modifiers --alpha subunits?
//	    			for(int k=0; k<CDrxn.getNumModifiers(); k++)
//	    			{
//	    				PluginSpeciesAlias spca2 = CDrxn.getModifier(k).getAlias();
//	    				Frame mod = (Frame)commit(initEntity(this.getContainingCompartment(spca, this.getSelectedModel()),orgId,spca2));
//	    				tu.addActivator(mod);
//	    			}
				}
				return null;
			}
		}
		catch(Exception ex) 
		{
			ex.printStackTrace();
		}
		Reaction r = (Reaction)initFrame(CDrxn);
		try {
			r.clear();
			if(CDrxn.getReversible()) r.putSlotValue("REACTION-DIRECTION","REVERSIBLE");
			else r.putSlotValue("REACTION-DIRECTION","IRREVERSIBLE-LEFT-TO-RIGHT");
		} catch (PtoolsErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return r;
	}

	@Override
	protected boolean prevalidateCommit() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected int getStoichiometry(Object rxn, Object substrate) {
		Frame f = (Frame)substrate;
		int rst = 1;
		try
		{
			rst = f.annotations.containsKey("COEFFICIENT") ? Integer.parseInt(f.annotations.get("COEFFICIENT").replace("(","").replace(")","").trim()) : 1;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return rst;
	}
	
	@Override
	public void download(ArrayList pwIds,SelectPathwaysFrame frame)
	{
		done.clear();
		super.download(pwIds, frame);
		done.clear();
	}
	
	@Override
	public void commit()
	{
		super.commit();
		try {
			cyc.saveKB();
		} catch (PtoolsErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
