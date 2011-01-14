package edu.iastate.pathwayaccess.celldesigner.reactome

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

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JProgressBar;
import org.reactome.cabig.domain.*;
import org.sbml.libsbml.XMLAttributes;
import org.sbml.libsbml.XMLNode;
import org.sbml.libsbml.XMLTriple;

import jp.sbi.celldesigner.plugin.*;
import jp.sbi.celldesigner.plugin.util.*;

import edu.iastate.pathwayaccess.*;


public class ReactomePlugin extends PathwayAccessPlugin {

	ReactomeConnection reactome;
	
	public ReactomePlugin() {
		super(new String[]{DOWNLOAD});
		this.loggedIn=true;
	}
	
	protected void setMyColor()
	{
		myColor = Color.cyan;
	}

	public ReactomePlugin(String[] opts) {
		super(opts);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void commit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void connect() {
		try {
			reactome = new ReactomeConnection();
			this.connected = true;
		} catch (RemoteException e) {
			Prompt.errorMessage("Connection Problem", "We're sorry, there was a problem connecting to Reactome. \nSomething about a "+e.getClass().getName()+": "+e.getMessage()+"...");
		}
	}

	@Override
	public void login() {
		this.loggedIn = true;
		

//		System.out.println("making reaction");
//		PluginReaction r = new PluginReaction();
//		this.getSelectedModel().addReaction(r);
//		r.setName("reaction");
//		r.setReactionType(PluginReactionSymbolType.STATE_TRANSITION);
//		
//		System.out.println("making a");
//		//PluginSpeciesAlias a = new PluginSpeciesAlias(new PluginSpecies(PluginSpeciesSymbolType.SIMPLE_MOLECULE,"a"),PluginSpeciesSymbolType.SIMPLE_MOLECULE);
//		PluginSpecies aspc = new PluginSpecies(PluginSpeciesSymbolType.SIMPLE_MOLECULE,"a");
//		aspc.setSpeciesType(PluginSpeciesSymbolType.SIMPLE_MOLECULE);
//		PluginSpeciesAlias a = aspc.getSpeciesAlias(0);
//		a.setType(PluginSpeciesSymbolType.SIMPLE_MOLECULE);
//		this.notifySBaseAdded(a);
//		PluginSpeciesReference refa = new PluginSpeciesReference(r,a);
//		refa.setReferenceType(PluginSimpleSpeciesReference.REACTANT);
////		XMLNode root = new XMLNode();
////		XMLAttributes atts = new XMLAttributes();
////		atts.add("hello","world");
////		atts.add("hi","there");
////		XMLNode node = new XMLNode(new XMLTriple("mynode"),atts);
////		root.addChild(node);
////		root.addChild(new XMLNode(new XMLTriple("mynode")));
////		a.getSpecies().setNotes(node.toXMLString());
////		System.out.println(a.getSpecies().toSBML());
////		this.notifySBaseChanged(a.getSpecies());
////		System.out.println("node: "+node.toXMLString());
////		System.out.println("in notes: "+a.getNotes().toXMLString());
//		r.addReactant(refa);
//		
//		System.out.println("making a2");
//		PluginSpeciesAlias a2 = new PluginSpeciesAlias(aspc,PluginSpeciesSymbolType.SIMPLE_MOLECULE);
//		//a.setType(PluginSpeciesSymbolType.SIMPLE_MOLECULE);
//		this.notifySBaseAdded(a2);
//		PluginSpeciesReference refa2 = new PluginSpeciesReference(r,a2);
//		refa.setReferenceType(PluginSimpleSpeciesReference.REACTANT);
//		r.addReactant(refa2);
//		
//		System.out.println("making b");
//		//PluginSpeciesAlias b = new PluginSpeciesAlias(new PluginSpecies(PluginSpeciesSymbolType.SIMPLE_MOLECULE,"b"),PluginSpeciesSymbolType.SIMPLE_MOLECULE);
//		PluginSpecies bspc = new PluginSpecies(PluginSpeciesSymbolType.SIMPLE_MOLECULE,"b");
//		bspc.setSpeciesType(PluginSpeciesSymbolType.SIMPLE_MOLECULE);
//		PluginSpeciesAlias b = bspc.getSpeciesAlias(0);
//		b.setType(PluginSpeciesSymbolType.SIMPLE_MOLECULE);
//		this.notifySBaseAdded(b);
//		PluginSpeciesReference refb = new PluginSpeciesReference(r,b);
//		refb.setReferenceType(PluginSimpleSpeciesReference.PRODUCT);
//		//refb.setStoichiometry(1);
//		//this.notifySBaseAdded(refb);
//		r.addProduct(refb);
//	
//		PluginReaction cf = new PluginReaction();
//		this.getSelectedModel().addReaction(cf);
//		cf.setName("complex formation");
//		cf.setReactionType(PluginReactionSymbolType.BOOLEAN_LOGIC_GATE_AND);
//		
//		PluginSpecies c1spc = new PluginSpecies(PluginSpeciesSymbolType.PROTEIN,"c1");
//		c1spc.setSpeciesType(PluginSpeciesSymbolType.PROTEIN);
//		PluginSpeciesAlias c1 = c1spc.getSpeciesAlias(0);
//		c1.setType(PluginSpeciesSymbolType.PROTEIN);
//		this.notifySBaseAdded(c1);
//		PluginSpeciesReference refc1 = new PluginSpeciesReference(cf,c1);
//		refc1.setStoichiometry(2.0);
//		refc1.setReferenceType(PluginSimpleSpeciesReference.REACTANT);
//		cf.addReactant(refc1);
//		
//		PluginSpecies cspc = new PluginSpecies(PluginSpeciesSymbolType.COMPLEX,"c");
//		cspc.setSpeciesType(PluginSpeciesSymbolType.COMPLEX);
//		PluginSpeciesAlias c = cspc.getSpeciesAlias(0);
//		c.setType(PluginSpeciesSymbolType.COMPLEX);
//		c.setFramePosition(100, 0);
//		this.notifySBaseAdded(c);
//		
//		PluginSpeciesReference refcf = new PluginSpeciesReference(cf,c);
//		refcf.setReferenceType(PluginSimpleSpeciesReference.PRODUCT);
//		cf.addProduct(refcf);
//		this.notifySBaseAdded(cf);
//		
//		PluginModifierSpeciesReference refc = new PluginModifierSpeciesReference(r,c);
//		refc.setModificationType(PluginReactionSymbolType.CATALYSIS);
//		r.addModifier(refc);
//
//		
//		PluginReaction t = new PluginReaction();
//		this.getSelectedModel().addReaction(t);
//		t.setName("translation");
//		t.setReactionType(PluginReactionSymbolType.TRANSLATION);
//		
//		PluginSpecies dspc = new PluginSpecies(PluginSpeciesSymbolType.RNA,"d");
//		dspc.setSpeciesType(PluginSpeciesSymbolType.RNA);
//		PluginSpeciesAlias d = dspc.getSpeciesAlias(0);
//		d.setType(PluginSpeciesSymbolType.RNA);
//		this.notifySBaseAdded(d);
//		PluginSpeciesReference refd = new PluginSpeciesReference(t,d);
//		refd.setReferenceType(PluginSpeciesReference.REACTANT);
//		PluginSpeciesReference refc1t = new PluginSpeciesReference(t,c1);
//		refc1t.setReferenceType(PluginSpeciesReference.PRODUCT);
//		
//		t.addReactant(refd);
//		t.addProduct(refc1t);
//		this.notifySBaseAdded(t);
//		
////		System.out.println("adding...");
////		r.addReactant(refa);
////		r.addReactant(refa2);
////		r.addProduct(refb);
////		r.addModifier(refc);
//		System.out.println("notifying...");
//		this.notifySBaseAdded(r);
////		System.out.println(this.getSelectedModel().getListOfAllSpeciesAlias(cspc.getId()).size());
//	
////		try
////		{
////			Object a = reactome.queryByIds(new Long[] {351164L});
////			this.attachReactant(r, a, 1, frame);
////			System.out.println(a.getClass().getName());
////			
////			Object b = reactome.queryByIds(new Long[] {29514L});
////			this.attachProduct(r, b, 1, frame);
////			
////			Object c = reactome.queryByIds(new Long[] {692699L});
////			this.attachModifier(r, c,PluginReactionSymbolType.CATALYSIS, frame);
////			
////			this.notifySBaseAdded(r);
////		}
////		catch(Exception e){e.printStackTrace();}
	}

	public void SBaseAdded(PluginSBase arg0) {
		// TODO Auto-generated method stub

	}

	public void SBaseChanged(PluginSBase arg0) {
		// TODO Auto-generated method stub

	}

	public void SBaseDeleted(PluginSBase arg0) {
		// TODO Auto-generated method stub

	}

	public void addPluginMenu() {
		// TODO Auto-generated method stub

	}

	public void modelClosed(PluginSBase arg0) {
		// TODO Auto-generated method stub

	}

	public void modelOpened(PluginSBase arg0) {
		// TODO Auto-generated method stub

	}

	public void modelSelectChanged(PluginSBase arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void addModifier(Object rxn, Object participant) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void addProduct(Object rxn, Object participant, int stoich) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void addReactant(Object rxn, Object participant, int stoich) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void addReactionToPathway(Object rxn, Object pwy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Object commit(Object o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void enrichReaction(PluginReaction CDrxn, Object rxn) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected String getOriginalType(Object o)
	{
		return o.getClass().getName();
	}

	@Override
	protected void enrichSpeciesAlias(PluginSpeciesAlias spca, Object o,SelectPathwaysFrame frame) {
		try {
			if(o instanceof Complex)
			{
	
					Complex c = (Complex)(reactome.load(o));
					PluginReaction CDrxn = new PluginReaction();
					String name = getName(c)+"-formation";
					CDrxn.setName(name);
					String type = PluginReactionSymbolType.STATE_TRANSITION;
					this.attachProduct(CDrxn, c, 1, frame);
					for(Object compo : c.getHasComponent())
					{
						Object comp = reactome.load(compo);
						PluginSpeciesReference ref = this.attachReactant(CDrxn, comp, this.getStoichiometry(c, compo), frame);
						this.enrichSpeciesAlias(ref.getAlias(), comp, frame);
					}
					CDrxn.setReactionType(type);
					//CDrxn.setReversible(false);
					//this.getSelectedModel().addReaction(CDrxn);
					//this.notifySBaseAdded(CDrxn);
					CDrxn = this.addPluginReactionToModel(CDrxn, getLocation(o));
					this.addAnnotation(this.getClass().getName()+".NAMES", CDrxn, name);
					
//					PluginSpeciesAlias ca = this.makePluginSpeciesAlias(c, frame);
//					GenericComplex gc = new GenericComplex(ca.getName(),ca,this);
//					for(Object componentObject : c.getHasComponent())
//					{
//						Object component = reactome.load(componentObject);
//						PluginSpeciesAlias componentAlias = this.makePluginSpeciesAlias(component, frame);
//						gc.add(componentAlias);
//						this.enrichSpeciesAlias(componentAlias, component, frame);
//					}
//					spca = gc.constructSpeciesAlias();
//					this.notifySBaseChanged(spca);
			}
			else if(o instanceof EventEntitySet)
			{
				EventEntitySet s = (EventEntitySet)reactome.load(o);
				HashMap<Long,Integer> stoichs = new HashMap<Long,Integer>();
				if(s.getHasMember() != null)
				{
					for(EventEntity e : s.getHasMember())
					{
						if(stoichs.containsKey(e.getId()))
							stoichs.put(e.getId(), stoichs.get(e.getId())+1);
						else
							stoichs.put(e.getId(), 1);
					}
					HashSet<Long> done = new HashSet<Long>();
					for(EventEntity e : s.getHasMember())
					{
						if(done.contains(e.getId())) continue;
						done.add(e.getId());
						e = (EventEntity)reactome.load(e);
						PluginReaction CDrxn = new PluginReaction();
						CDrxn.setName(spca.getName()+"-formation");
						CDrxn.setReactionType(PluginReactionSymbolType.STATE_TRANSITION);
						this.attachProduct(CDrxn, s, 1, frame);
						PluginSpeciesReference ref = this.attachReactant(CDrxn, e, stoichs.get(e.getId()), frame);
						if(ref.getAlias().getType().equals(PluginSpeciesSymbolType.RNA)) 
						{
							CDrxn.setName(spca.getName()+"-translation");
							CDrxn.setReactionType(PluginReactionSymbolType.TRANSLATION);
							CDrxn.setReversible(false);
						}
						CDrxn = this.addPluginReactionToModel(CDrxn, getLocation(e));
					}
				}
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected Object getId(Object o) {
		return reactome.getId(o);
	}

	@Override
	protected String getLocation(Object o) {
		GeneOntology rst = null;
		try {
			rst = (GeneOntology)o.getClass().getMethod("getCompartment", null).invoke(o, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(rst==null) return this.CYTOSOL;
		else return rst.getName();
	}

	@Override
	protected String getName(Object o) {
		String rst = null;
		try {
			
			if(o instanceof Taxon) rst = ((Taxon)(reactome.load(o))).getScientificName();
			else rst = (String)o.getClass().getMethod("getName", null).invoke(o, null);
			if(rst==null) rst = getName(reactome.load(o));
			rst = rst.split("\\[")[0].split("\\(")[0].replace("Homologues of ","").trim();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rst;
	}

	@Override
	protected int getNumPathwayParticipants(Object pwy) {
		try {
			return ((Object [])(reactome.calls.get("listPathwayParticipants").invoke(new Object[] {pwy}))).length;
		} catch (RemoteException e) {
			e.printStackTrace();
			return 100;
		}
	}

	@Override
	protected Iterable getOrganisms() {
		try {
			return Arrays.asList(reactome.listObjects(Taxon.class));
		} catch (RemoteException e) {
			e.printStackTrace();
			return new ArrayList();
		}
	}

	@Override
	protected Iterable getPathwayReactions(Object pwy) {
		ArrayList<Reaction> rst = new ArrayList<Reaction>();
		try {
			addReactionsToList((Pathway)pwy,rst);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rst;
	}
	
	private void addReactionsToList(Pathway p,ArrayList<Reaction> rst) throws RemoteException
	{
		for(Object o : p.getHasComponent())
		{
			if(o instanceof Pathway) addReactionsToList((Pathway)reactome.load(o),rst);
			else rst.add((Reaction)reactome.load(o));
		}
	}

	@Override
	protected Iterable getPathwaysFromIds(ArrayList ids) {
		try {
			return Arrays.asList(reactome.queryByIds(new Object[] {ids.toArray()}));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ArrayList();
		}
	}

	@Override
	protected Iterable getPathwaysFromOrganismId(Object orgId) {
		try {
			Taxon tax = new Taxon();
			tax.setId((Long)orgId);
			return Arrays.asList(reactome.listByQuery(Pathway.class, "species", tax));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ArrayList();
		}
	}

	@Override
	protected ArrayList<ModifierReference> getReactionModifiers(Object rxn) {
		ArrayList<ModifierReference> rst = new ArrayList<ModifierReference>();
		try {
			if(((Reaction)rxn).getCatalystActivity() != null)
				for(Object ca : ((Reaction)rxn).getCatalystActivity())
				{
					if(ca==null) continue;
					CatalystActivity catAct = (CatalystActivity)reactome.load(ca);
					Object m = reactome.load(catAct.getPhysicalEntity());
					if(m!=null) rst.add(new ModifierReference(m,PluginReactionSymbolType.CATALYSIS));
				}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rst;
	}

	@Override
	protected Iterable getReactionProducts(Object rxn) {
		try {
			return Arrays.asList(reactome.load(((Reaction)(reactome.load(rxn))).getOutput().toArray()));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ArrayList();
		}
	}

	@Override
	protected Iterable getReactionReactants(Object rxn) {
		try {
			return Arrays.asList(reactome.load(((Reaction)(reactome.load(rxn))).getInput().toArray()));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ArrayList();
		}
	}

	@Override
	protected int getStoichiometry(Object rxn, Object substrate) {
		String stoich = "";
		if(rxn instanceof Reaction)
		{
			stoich = ((Reaction)rxn).getInputStoichiometry()+","+((Reaction)rxn).getOutputStoichiometry();
		}
		else if(rxn instanceof Complex)
		{
			stoich = ((Complex)rxn).getStoichiometry();
		}
		HashMap<Long,String> pairs = new HashMap<Long,String>();
		for(String pair : stoich.split(","))
		{
			String[] p = pair.split(":");
			pairs.put(Long.parseLong(p[0]),p[1]);
		}
		Long id = ((EventEntity)substrate).getId();
		return Integer.parseInt(pairs.get(id));
	}

	@Override
	protected ArrayList<String> getSynonyms(Object o) {
		ArrayList<String> rst  = new ArrayList<String>();
		try
		{
			if(o instanceof EventEntity && ((EventEntity)o).getCrossReference()!=null)
			{
				for(DatabaseCrossReference ref : ((EventEntity)o).getCrossReference())
				{
					rst.add(((DatabaseCrossReference)reactome.load(ref)).getCrossReferenceId());
				}
			}
			else if(o instanceof EventEntitySet && ((EventEntitySet)o).getReferenceEntity()!=null)
			rst.add(((EventEntity)(reactome.load(((EventEntitySet)o).getReferenceEntity()))).getName());
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return rst;
	}

	@Override
	protected String getType(Object o) {
		//System.out.println(o.getClass().getName());
		if(o instanceof GenomeEncodedEntity) return PluginSpeciesSymbolType.RNA;
		if(o instanceof Polymer) return PluginSpeciesSymbolType.PROTEIN;
		if(o instanceof Complex) return PluginSpeciesSymbolType.COMPLEX;
		if(o instanceof CatalystActivity) return PluginReactionSymbolType.CATALYSIS;
		if(o instanceof Reaction) return PluginReactionSymbolType.STATE_TRANSITION;
		if(o instanceof SmallMoleculeEntity) return PluginSpeciesSymbolType.SIMPLE_MOLECULE;
		if(o instanceof EventEntitySet) 
		{
			EventEntitySet ees;
			try {
				ees = (EventEntitySet)reactome.load(o);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ees = (EventEntitySet)o;
			}
			if(ees.getHasMember()==null || ees.getHasMember().get(0) instanceof SmallMoleculeEntity)
			{
				return PluginSpeciesSymbolType.SIMPLE_MOLECULE;
			}
			else return PluginSpeciesSymbolType.PROTEIN;
		}
		System.out.println(getName(o)+"\t"+o.getClass().getName());
		return PluginSpeciesSymbolType.UNKNOWN;
	}

	@Override
	protected Object initEntity(String comp, Object orgId, PluginSpeciesAlias spca) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Object initPathway(PluginModel model, Object orgId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Object initReaction(Object orgId, PluginReaction CDrxn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean prevalidateCommit() {
		// TODO Auto-generated method stub
		return false;
	}

}
