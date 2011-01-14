package edu.iastate.pathwayaccess.celldesigner.metnet

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
import edu.iastate.metnet.*;
import edu.iastate.metnet.edit.*;
import edu.iastate.metnet.util.*;

import edu.iastate.pathwayaccess.*;


import jp.sbi.celldesigner.plugin.*;
import jp.sbi.celldesigner.plugin.DataObject.PluginRealLineInformationDataObjOfReactionLink;
import jp.sbi.celldesigner.plugin.util.PluginCompartmentSymbolType;
import jp.sbi.celldesigner.plugin.util.PluginReactionSymbolType;
import jp.sbi.celldesigner.plugin.util.PluginSpeciesSymbolType;

import jp.sbi.celldesigner.MyFileManager;
import jp.fric.graphics.multiwindow.ContentsFiler;
import jp.fric.io.util.FilePather;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.ArrayList;

import java.awt.Color;
import java.io.*;

import javax.swing.JProgressBar;

import org.sbml.libsbml.XMLNode;
import org.sbml.libsbml.XMLToken;

import pathwayaccess.Prompt;

public class MetNetPlugin extends PathwayAccessPlugin {

	private HashMap<String,String> compNameMap;
	private ArrayList<String> metNetCompartments;
	
	public MetNetPlugin()
	{
		super(new String[]{LOGIN,DOWNLOAD,COMMIT});		
		this.loggedIn = true;
		compNameMap = new HashMap<String,String>();
		metNetCompartments = new ArrayList<String>();
		this.setUpNameRelations();
	}
	
	protected void setMyColor()
	{
		myColor = new Color(200,200,0);
	}
	
	public void setUpNameRelations()
	{
		typeMap = new Relation();
		//species types
		typeMap.addMapping(PluginSpeciesSymbolType.PROTEIN_RECEPTOR,"polypeptide");
		typeMap.addMapping(PluginSpeciesSymbolType.PROTEIN_TRUNCATED,"polypeptide");
		typeMap.addMapping(PluginSpeciesSymbolType.PROTEIN_GENERIC,"polypeptide");
		typeMap.addMapping(PluginSpeciesSymbolType.PROTEIN_ION_CHANNEL,"polypeptide");
		typeMap.addMapping(PluginSpeciesSymbolType.PROTEIN,"polypeptide");
		typeMap.addMapping(PluginSpeciesSymbolType.COMPLEX,"protein complex");
		
		typeMap.addMapping(PluginSpeciesSymbolType.GENE,"Cis-element");
		typeMap.addMapping(PluginSpeciesSymbolType.GENE,"gene");
		
		typeMap.addMapping(PluginSpeciesSymbolType.ANTISENSE_RNA,"RNA");
		typeMap.addMapping(PluginSpeciesSymbolType.RNA,"RNA");

		typeMap.addMapping(PluginSpeciesSymbolType.DEGRADED,"metabolite");
		typeMap.addMapping(PluginSpeciesSymbolType.DRUG,"metabolite");
		typeMap.addMapping(PluginSpeciesSymbolType.ION,"metabolite");
		typeMap.addMapping(PluginSpeciesSymbolType.SIMPLE_MOLECULE,"metabolite");

		typeMap.addMapping(PluginSpeciesSymbolType.PHENOTYPE,"environment");
		
		
		//interaction types
		typeMap.addMapping(PluginReactionSymbolType.UNKNOWN_CATALYSIS,"Catalysis");
		
		
		//typeMap.addMapping(PluginReactionSymbolType.ADD_PRODUCT,"Diffusion");
		//typeMap.addMapping(PluginReactionSymbolType.ADD_REACTANT,"Bind");
		typeMap.addMapping(PluginReactionSymbolType.DISSOCIATION,"Diffusion");
		
		typeMap.addMapping(PluginReactionSymbolType.STATE_TRANSITION,"Composition-OR");
		typeMap.addMapping(PluginReactionSymbolType.INHIBITION,"Negative regulation");
		typeMap.addMapping(PluginReactionSymbolType.PHYSICAL_STIMULATION,"Positive regulation");
		typeMap.addMapping(PluginReactionSymbolType.STATE_TRANSITION,"Others");
		typeMap.addMapping(PluginReactionSymbolType.STATE_TRANSITION,"Catalysis");
		typeMap.addMapping(PluginReactionSymbolType.STATE_TRANSITION,"Composition-AND");
		typeMap.addMapping(PluginReactionSymbolType.STATE_TRANSITION,"Enzymatic reaction");
		typeMap.addMapping(PluginReactionSymbolType.TRANSCRIPTION,"Transcription");
		typeMap.addMapping(PluginReactionSymbolType.TRANSLATION,"Translation");
		typeMap.addMapping(PluginReactionSymbolType.TRANSPORT,"Transport");
		typeMap.addMapping(PluginReactionSymbolType.TRIGGER,"Positive regulation");
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
	
	public void login()
	{
		String id = Prompt.userEnter("Username","Enter MetNet username");
		if(id==null) 
		{
			loginError();
			return;
		}
		String pass = Prompt.userEnterPassword("Enter MetNet User Password");
		if(pass==null) 
		{
			loginError();
			return;
		}
		//System.out.println("trying to login: "+id+","+pass);
		if(Auth.Authenticate(id, pass))
    	{
    		System.out.println("Access granted for "+id); 
    		this.loggedIn = true;
    	}
    	else
    	{
    		loginError();
    	}
	}
	
	private void loginError()
	{
		Prompt.errorMessage("Cannot login to MetNet", "You are not logged in as a priviledged user to MetNet.\nYou will remain logged in as a guest and can download data.");

	}
	
	public void connect()
	{
		this.connected = true;
		for(CellLocation loc : CellLocation.search())
		{
			compNameMap.put(loc.name,loc.name);
			metNetCompartments.add(loc.name);
		}
	}
	
	private LocalEntity CD2MN(PluginSpeciesAlias s, String org, PluginModel model)
    {
		PluginCompartment comp = this.getPluginCompartment(this.getContainingCompartment(s,model));
		String compName = "not assigned";
		if(comp!=null) compName = this.compNameMap.get(comp.getName());
		if(compName.length()==0 || compName.equals(this.UNKNOWN)) compName = "not assigned";
		if(!compNameMap.containsKey(compName))
		{
			compNameMap.put(compName, Prompt.userSelect("Unknown Cell Location/Compartment", "MetNet does not have a "+compName+". \nPlease select an alternative.", metNetCompartments));
		}
		compName = compNameMap.get(compName);
		return CD2MN(s,org,compName);
    }
    
    private LocalEntity CD2MN(PluginSpeciesAlias s, String org, String compName)
    {
    	//System.out.println("CD2MN s");
    	LocalEntity LocalEntity;
    	Entity ent;
    	String id = s.getSpecies().getId().replace("_space_",".");
		String name = s.getName().replace("_space_"," ");
		
		HashSet<String> types = this.getAnnotation(this.getClass().getName()+".TYPE", s);
		String type;
		if(types!=null && types.size()>0 && !types.iterator().next().contains("org."))
			type = types.iterator().next();
		else type = CD2MN(s.getType());
		
		if(type.equals("metabolite"))
		{
			ent = Entity_E.identify(name, type, Organism.Unknown);
			if(ent==null)
			{
				ent = Entity_E.create(name, type);
			}
		}
		else
		{
			ent = Entity_E.identify(name, type, org);
			if(ent==null)
			{
				ent = Entity_E.create(name, type, org);
			}
		}
		
		LocalEntity = LocalEntity_E.identify(ent, compName);
		if(LocalEntity==null) 
		{
			LocalEntity = LocalEntity_E.create(ent,compName);
		}
		
		return LocalEntity;
    }
    
    private String CD2MN(String type)
    {
    	if(typeMap.containsKey(type))
    		return typeMap.get(type);
    	else
    		return "others";
    }
    
    private String MN2CD(String type)
    {
    	if(typeMap.containsKey(type))
    		return typeMap.get(type);
    	//System.out.println(type);
    	System.out.println(type);
    	return "UNKNOWN_TRANSITION";
    }

	@Override
	protected void addModifier(Object rxn, Object participant) {
		Interaction_E ixn = (Interaction_E)rxn;
		LocalEntity c = (LocalEntity)participant;
		ixn.AttachModifier(c);
	}
	
	@Override
	protected String getOriginalType(Object o)
	{
		if(o instanceof Entity) return ((Entity)o).type;
		else if(o instanceof LocalEntity) return ((LocalEntity)o).type;
		else if(o instanceof Interaction) return ((Interaction)o).type;
		else return o.getClass().getName();
	}

	@Override
	protected void addProduct(Object rxn, Object participant, int stoich) {
		Interaction_E ixn = (Interaction_E)rxn;
		LocalEntity c = (LocalEntity)participant;
		ixn.AttachProduct(stoich,c);
	}

	@Override
	protected void addReactant(Object rxn, Object participant, int stoich) {
		Interaction_E ixn = (Interaction_E)rxn;
		LocalEntity c = (LocalEntity)participant;
		ixn.AttachReactant(stoich,c);
	}

	@Override
	protected void addReactionToPathway(Object rxn, Object pwy) {
		Pathway_E pwye = (Pathway_E)pwy;
		Interaction ixn = (Interaction)rxn;
		pwye.attachInteraction(ixn);
	}

	@Override
	protected Object commit(Object o) {
		if(o instanceof Pathway) 
		{
			String pwName = ((Pathway)o).name;
			String org = ((Pathway)o).organism;
			Pathway identifiedPwy =  Pathway.identify(pwName, Organism.identify(org));
			Pathway_E pw;
			if(identifiedPwy!=null )
			{
				
				if(Prompt.confirm("Pathway Exists for "+org+"!", "Do you really want to delete "+pwName+" and recreate it in MetNet?"))
				{
					pw = new Pathway_E(identifiedPwy.id);
					pw.delete(true);
				}
				else return null;
			}
			((Pathway_E)o).add();
		}
		else if(o instanceof Interaction_E) ((Interaction_E)o).Add();
		return o;
	}

	@Override
	protected void enrichReaction(PluginReaction CDrxn, Object rxn) {
		if(rxn instanceof Interaction)
		{
			Interaction ixn = (Interaction)rxn;
			if(ixn.confidence != null) 
			{
				this.addAnnotation("confidence", CDrxn, ixn.confidence);
				//System.out.println("conf: "+ixn.confidence);
			}
			if(ixn.ec != null) 
			{
				this.addAnnotation("ec", CDrxn, ixn.ec);
				//System.out.println("conf: "+ixn.confidence);
			}
		}
	}

	@Override
	protected void enrichSpeciesAlias(PluginSpeciesAlias spca, Object o,SelectPathwaysFrame frame) {
//		spca.getSpecies().setNotes("METNET");
//		this.notifySBaseChanged(spca.getSpecies());
		if(o instanceof LocalEntity)
		{
			LocalEntity e = (LocalEntity)o;
			if(e.creator != null) this.addAnnotation("creator", spca, e.creator);
			if(e.source != null) this.addAnnotation("source", spca, e.source);
			if(e.created != null) this.addAnnotation("created", spca, e.created.toString());
		}
	}

	@Override
	protected Object getId(Object o) {
		if(o instanceof Pathway) return ((Pathway)o).id;
		if(o instanceof Interaction) return ((Interaction)o).id;
		if(o instanceof Entity) return ((Entity)o).id;
		if(o instanceof LocalEntity) return ((LocalEntity)o).id;
		if(o instanceof Organism) return ((Organism)o).id;
		return null;
	}

	@Override
	protected String getName(Object o) {
		if(o instanceof Pathway) return ((Pathway)o).name;
		if(o instanceof Interaction) return ((Interaction)o).name;
		if(o instanceof Entity) return ((Entity)o).name;
		if(o instanceof LocalEntity) return ((LocalEntity)o).name;
		if(o instanceof Organism) return ((Organism)o).name;
		return null;
	}

	@Override
	protected int getNumPathwayParticipants(Object pwy) {
		return ((Pathway)pwy).getLocalEntities().size();
	}

	@Override
	protected Iterable getOrganisms() {
		Organism[] orgs = Organism.search();
		
		Iterable rst = Arrays.asList(orgs);
		return rst;
	}

	@Override
	protected Iterable getPathwayReactions(Object pwy) {
		return Arrays.asList(((Pathway)pwy).getInteractions().toArray());
	}

	@Override
	protected Iterable getPathwaysFromIds(ArrayList ids) {
		ArrayList<Pathway> rst = new ArrayList<Pathway>();
		for(Object o : ids)
		{
			Integer id = (Integer)o;
			rst.add(new Pathway(id));
		}
		return rst;
	}

	@Override
	protected Iterable getPathwaysFromOrganismId(Object orgId) {
		//System.out.println(orgId.getClass().getName());
		Organism org = new Organism((Integer)orgId);
		Pathway[] pwys = Pathway.search(org).toArray();
		return Arrays.asList(pwys);
	}

	@Override
	protected ArrayList<ModifierReference> getReactionModifiers(Object rxn) {
		ArrayList<ModifierReference> rst = new ArrayList<ModifierReference>();
		for(LocalEntity m : ((Interaction)rxn).getModifiers().toArray())
		{
			rst.add(new ModifierReference(m));
		}
		return rst;
	}

	@Override
	protected Iterable getReactionProducts(Object rxn) {
		return Arrays.asList(((Interaction)rxn).getProducts().toArray());
	}

	@Override
	protected Iterable getReactionReactants(Object rxn) {
		return Arrays.asList(((Interaction)rxn).getReactants().toArray());
	}

	@Override
	protected ArrayList<String> getSynonyms(Object o) {
		if(o instanceof LocalEntity)
		{
			LocalEntity c = (LocalEntity)o;
			return new ArrayList<String>(Arrays.asList(c.getEntity().getSynonyms()));
		}
		else if(o instanceof Entity)
		{
			return new ArrayList<String>(Arrays.asList(((Entity)o).getSynonyms()));
		}
		else return new ArrayList<String>();
	}

	@Override
	protected String getType(Object o) {
		String metNetType = "";
		if(o instanceof LocalEntity)
		{
			metNetType = ((LocalEntity)o).getEntity().type;
		}
		else if(o instanceof Entity)
		{
			metNetType = ((Entity)o).type;
		}
		else if(o instanceof Interaction)
		{
			metNetType = ((Interaction)o).type;
		}
		
		return this.MN2CD(metNetType);
	}

	@Override
	protected Object initEntity(String comp, Object orgId, PluginSpeciesAlias spca) {
		return CD2MN(spca,new Organism((Integer)orgId).name,comp);
	}

	@Override
	protected Object initPathway(PluginModel model, Object orgId) {
		String pwName = model.getName();
		
		Pathway_E pw = new Pathway_E();
		pw.name = pwName;
		pw.organism = new Organism((Integer)orgId).name;
		
		return pw;
	}

	@Override
	protected Object initReaction(Object orgId, PluginReaction CDrxn) {
		String ixn_name = CDrxn.getName();
		Interaction_E ixn;
		HashSet<String> ids = this.getAnnotation(this.getClass().getName()+".ID", CDrxn);
		if(ids!=null && ids.size()>0)
		{
			int id = Integer.parseInt(ids.iterator().next());
			ixn = new Interaction_E(id);
			ixn.Delete();
		}
		ixn = new Interaction_E();
		ixn.name = ixn_name;
		ixn.organism = new Organism((Integer)orgId).name;
		ixn.creator = Auth.getCurrentUsername();
		HashSet<String> ecs = this.getAnnotation("ec", CDrxn);
		if(ecs!=null && ecs.size()>0)
			ixn.ec = ecs.iterator().next();
		HashSet<String> types = this.getAnnotation(this.getClass().getName()+".TYPE", CDrxn);
		if(types!=null && types.size()>0 && !types.iterator().next().contains("org."))
			ixn.type = types.iterator().next();
		else
			ixn.type = this.CD2MN(CDrxn.getReactionType());
		HashSet<String> comps = this.getAnnotation("COMPARTMENT", CDrxn);
		if(comps!=null && comps.size()>0)
		{
			ixn.cellLocation = comps.iterator().next();
		}
		else
		{
			ixn.cellLocation = "not assigned";
		}
		return ixn;
	}

	@Override
	protected boolean prevalidateCommit() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected String getLocation(Object o) {
		String rst = null;
		if(o instanceof LocalEntity) rst = ((LocalEntity)o).cellLocation;
		if(o instanceof Interaction) rst = ((Interaction)o).cellLocation;
		if(rst==null || rst.equals("not assigned"))
			rst = this.CYTOSOL;
		return rst;
	}

	@Override
	protected int getStoichiometry(Object rxn, Object substrate) {
//		LocalEntity s = (LocalEntity)substrate;
//		Interaction ixn = (Interaction)rxn;
		return 1;
	}



	
	
}



