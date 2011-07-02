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
package edu.iastate.pathwayaccess.celldesigner;

import edu.iastate.pathwayaccess.common.Relation;
import edu.iastate.pathwayaccess.common.Prompt;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ProgressMonitor;

import org.sbml.libsbml.SBase;
import org.sbml.libsbml.XMLAttributes;
import org.sbml.libsbml.XMLNode;
import org.sbml.libsbml.XMLToken;
import org.sbml.libsbml.XMLTriple;

import jp.sbi.celldesigner.plugin.CellDesignerPlugin;
import jp.sbi.celldesigner.plugin.PluginCompartment;
import jp.sbi.celldesigner.plugin.PluginKineticLaw;
import jp.sbi.celldesigner.plugin.PluginListOf;
import jp.sbi.celldesigner.plugin.PluginMenu;
import jp.sbi.celldesigner.plugin.PluginMenuItem;
import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginModifierSpeciesReference;
import jp.sbi.celldesigner.plugin.PluginParameter;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSBase;
import jp.sbi.celldesigner.plugin.PluginSimpleSpeciesReference;
import jp.sbi.celldesigner.plugin.PluginSpecies;
import jp.sbi.celldesigner.plugin.PluginSpeciesAlias;
import jp.sbi.celldesigner.plugin.PluginSpeciesReference;
import jp.sbi.celldesigner.plugin.util.PluginCompartmentSymbolType;
import jp.sbi.celldesigner.plugin.util.PluginReactionSymbolType;
import jp.sbi.celldesigner.plugin.util.PluginSpeciesSymbolType;

/**
 * Extend this class to create a plugin that accesses a pathway datasource.  Much of the work is encapsulated here.
 * Must implement CellDesignerPlugin methods as well.
 * "generic pathway database" refers to the pathway datasource being accessed, along with the objects that make up its API.
 * @see <a href='http://celldesigner.org/help/plugin/jp/sbi/celldesigner/plugin/CellDesignerPlugin.html'>CellDesignerPlugin</a>
 * @see <a href='http://vrac.iastate.edu/~jlv/pathwayaccess/plugin/metnetaccess.jar'>MetNetAccess jar</a>, which includes source, for examples. 
 * @see <a href='http://vrac.iastate.edu/~jlv/pathwayaccess/plugin/biocycaccess.jar'>BioCycAccess jar</a>, which includes source, for examples.
 * @author John Van Hemert
 *
 */
public abstract class PathwayAccessPlugin extends CellDesignerPlugin {
	
	
	
	public static final String CYTOSOL = "cytosol";
	public static final String NUCLEUS = "nucleus";
	public static final String EXTRACELLULAR = "extracellular";
	public static final String UNKNOWN = "unknown";
	
	/**
	 * A map between reaction ids and their respective locations in the model.
	 * This is necessary because CellDesigner reaction objects do not have 
	 * location fields.
	 */
	//protected HashMap<String,String> reactionLocations;
	
	/**
	The drop down option text for connecting to the datasource.
	 */
	public static final String CONNECT = "Connect to...";
	
	/**
	The drop down option text for logging into the datasource.
	 */
	public static final String LOGIN = "Login...";
	
	/**
	The drop down option text for downloading (a) pathway(s) to CellDesigner.
	 */
	public static final String DOWNLOAD = "Download pathway(s)...";
	
	/**
	The drop down option text for exporting or publishing the current CellDesigner model to the datasource.
	 */
	public static final String COMMIT = "Commit model...";
	
	/**
	The drop down option text for highlighting all objects (species and reactions) that were downloaded by this plugin.
	 */
	public static final String HILITE = "Highlight";
	
	/**
	The drop down option text for customizing a plugin's highlight color.
	 */
	public static final String SETCOLOR = "Edit highlight color...";
	
	/**
	The width of each compartment as it is downloaded from a datasource.
	 */
	protected double compartmentW = 200.0;
	
	/**
	The height of each compartment as it is downloaded from a datasource.
	 */
	protected double compartmentH = 200.0;
	
	/**
	The x position of the next compartment to be drawn as they are downloaded from the datasource. 
	Initialized to 100.0, but can be changed.
	Must be incremented (usually by the value of compartmentW) each time a new compartment is downloaded and drawn.
	 */
	protected double nextCompartmentX = 100.0;
	
	/**
	The y position of the next compartment to be drawn as they are downloaded from the datasource. 
	Initialized to 100.0, but can be changed.
	Must be incremented (usually by the value of compartmentH) each time a new compartment is downloaded and drawn.
	 */
	protected double nextCompartmentY = 100.0;
	
	/**
	The number of pixels to move an entity to the right and down from the top left corner of its compartment as it is downloaded from the datasource.
	 */
	protected double offsetInCompartment = 20.0;
	
	/**
	A map to keep track of the names of the compartments as they are downloaded.  Use it this way:
	When a new compartment is encountered and added to the CellDesigner model, put a pair consisting of 
	the compartment name as it appears in the datasource and the result of the new PluginCompartment's getName() method.
	 */
	protected HashMap<String,String> compartmentsNames;
	
	/**
	This is a Relation between the CellDesigner type strings and the type strings of your datasource.
	@see Relation
	 */
	protected Relation typeMap;
	
	/**
	A flag to keep track of whether or not the plugin is connected to a datasource.  Update it as appropriate.
	 */
	protected boolean connected = false;
	
	/**
	A flag to keep track of whether or not the user is logged in to a datasource.  Update it as appropriate.
	 */
	protected boolean loggedIn = false;
	
	/**
	Before each pathway download, synonyms are loaded into this database for fast lookup.
	 */
	protected HashMap<String,String> synonymsDatabase;
	
	/**
	Before each pathway download, all pre-existing reactions are hashed for fast lookup of redundant reactions.
	 */
	protected HashMap<String,String> reactionHashes;
	
	/**
	Each plugin must define its own color for highlighting- make sure your new plugin doesn't have the same color as another one.
	 */
	public Color myColor;
	
	/**
	The constructor creates a Plugin dropdown menu with all four (CONNECT,LOGIN,DOWNLOAD,COMMIT) of the options available 
	by calling the constructor PathwayAccessPlugin(String[] opts) with the array {CONNECT,LOGIN,DOWNLOAD,COMMIT}.
	 */
	public PathwayAccessPlugin() 
	{
		this(new String[] {CONNECT,LOGIN,DOWNLOAD,COMMIT});
	}
	
	/**
	Each plugin must define its own default color for highlighting- make sure your new plugin doesn't have the same default color as another one.
	Set the myColor Color to the color you choose.  Users can customize this at runtime.
	 */
	abstract protected void setMyColor();
	
	/**
	Extra datasource-specific annotation is stored as XML in the Notes field for all objects.  PathwayAccess only needs a simple, heirarchical
	list-item(s) structure where items can be nested lists of items and items are simple XMLNodes.  This is so simple, a formal namespace has 
	not been defined because the XML is used primarily by PathwayAccess and is readable in the saved SBML models.
	This is the list node whose children can be either other lists or ANNOTATION_XML_ITEM's.
	Should always have a name attribute.  For example, "<ANNOTATION_XML_LIST name="COMMENTS"/>
	 */
	public static final String ANNOTATION_XML_LIST = "List";
	
	/**
	This is the item node which should never have any children.
	Should always have a value attribute.  For example, "<ANNOTATION_XML_ITEM value="Discovered by Watson and Crick"/>
	 */
	public static final String ANNOTATION_XML_ITEM = "Item";
	
//	static
//	{
//		System.out.println("before "+PathwayAccessAction.class.getClassLoader());
//        Method addURL = null;
//		try {
//			addURL = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] {URL.class});
//		} catch (SecurityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		addURL.setAccessible(true);//you're telling the JVM to override the default visibility
//		try {
//			addURL.invoke(ClassLoader.getSystemClassLoader(), new Object[] { new File("lib/pathwayaccess.jar").toURL() });
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("after "+PathwayAccessAction.class.getClassLoader());
//	}
	
	/**
	Pass any combination of the four options.  For example, if only the connect and download options are supported by 
	your datasource, call PathwayAccessPlugin({CONNECT,DOWNLOAD}) 
	 */
	public PathwayAccessPlugin(String[] opts) 
	{
		setMyColor();
		makeDropDown(opts);
        this.compartmentsNames = new HashMap<String,String>();
        
        synonymsDatabase = new HashMap<String,String>();
        reactionHashes = new HashMap<String,String>();
	}
	
	private void makeDropDown(String[] options)
	{
		PluginMenu menu = new PluginMenu(this.getClass().getName());
        PathwayAccessAction action = new PathwayAccessAction(this);
        for(String opt : options)
        {
        	menu.add(new PluginMenuItem(opt, action));
        }
        menu.addSeparator();
        menu.add(new PluginMenuItem(SETCOLOR, action));
        menu.add(new PluginMenuItem(HILITE, action));
        addCellDesignerPluginMenu(menu);
	}
	
	/**
	The connect method.  It is called when the CONNECT option is selected from the Plugin menu in CellDesigner.
	 */
	public abstract void connect();
	
	/**
	The login method.  It is called when the LOGIN option is selected from the Plugin menu in CellDesigner.
	 */
	public abstract void login();
	
//	/**
//	Store unique hash signatures of each reaction in the selected model under its notes.
//	This is used to prevent future redundant reactions.
//	 */
//	protected void hashReactions()
//	{
//		HashMap<String,String> hashes = new HashMap<String,String>();
//		for(PluginSBase sb : this.getSelectedModel().getListOfReactions().toArray())
//		{
//			PluginReaction CDrxn = (PluginReaction)sb;
//			String hash = this.getHashCode(CDrxn,CDrxn.getProduct(0).getSpeciesInstance().getCompartment())+"";
//			this.addAnnotation(hash, this.getSelectedModel(), CDrxn.getId());
//		}
//	}
	
	public void hilite()
	{
		//System.out.println("HILITING...");
		for(PluginSBase sb : this.getSelectedModel().getListOfAllSpeciesAlias().toArray())
		{
			PluginSpeciesAlias spca = (PluginSpeciesAlias)sb;
			//System.out.println(spca.getName()+": "+spca.getSpecies().getNotesString());
			if(spca.getSpecies().getNotesString().contains(this.getClass().getName()))
			{
				spca.setColor(mixMyColor(spca.getColor()));
				this.notifySBaseChanged(spca);
			}
		}
		for(PluginSBase sb : this.getSelectedModel().getListOfReactions().toArray())
		{
			PluginReaction CDrxn = (PluginReaction)sb;
			if(CDrxn.getNotesString().contains(this.getClass().getName()))
			{
				CDrxn.setLineColor(mixMyColor(CDrxn.getLineColor()));
				//Always null, so this is pointless.
//				if(CDrxn.getAllMyPostionInfomations()!=null && CDrxn.getAllMyPostionInfomations().getReactionLinkMembers()!=null)
//				{
//					for(Object linko : CDrxn.getAllMyPostionInfomations().getReactionLinkMembers())
//					{
//						System.out.println(linko.getClass().getName());
//					}
//				}
				this.notifySBaseChanged(CDrxn);
			}
		}
	}
	
	public Color mixMyColor(Color oldColor)
	{
		int r,g,b,a;
		if(oldColor.getAlpha()==255)
		{
			r = myColor.getRed();
			g = myColor.getGreen();
			b = myColor.getBlue();
		}
		else
		{
			r = Math.max(75,Math.max(oldColor.getRed(),myColor.getRed())-Math.min(oldColor.getRed(),myColor.getRed()));
			g = Math.max(75,Math.max(oldColor.getGreen(),myColor.getGreen())-Math.min(oldColor.getGreen(),myColor.getGreen()));
			b = Math.max(75,Math.max(oldColor.getBlue(),myColor.getBlue())-Math.min(oldColor.getBlue(),myColor.getBlue()));
		}
		a = 254;
		Color newColor = new Color(r,g,b,a);
		//System.out.println(oldColor.getRed()+","+oldColor.getGreen()+","+oldColor.getBlue()+","+oldColor.getAlpha()+ "-->"+r+","+g+","+b+","+a);
		return newColor;
	}
	
	/**
	The download method.  It is called when the DOWNLOAD option is selected from the Plugin menu in CellDesigner.
	Must create a new instance of your datasource's SelectPathwaysFrame, which will listen for the Import button to be pressed, at 
	which time it will probably call a method in the Plugin you define like the following:
	public void importPathways(ArrayList<Integer> pathwayIds, mydatasourceapi.Organism selectedOrg, JProgressBar progressBar)
	{...}
	Pass the progress bar to update the user on progress as some pathways may take several moments to download.
	 */
	public synchronized void download(ArrayList pwIds,SelectPathwaysFrame frame)
	{
		frame.updateStatus("Waiting for another plugin to finish", 0);
		
		//this.putAnnotation("downloading",this.getSelectedModel(),"1");
		synchronized(System.out)
		{
			try
			{
				//won't actually work until all plugins use same classloader.
				PathwayAccessAction.download.acquire();
				
				frame.updateStatus("Calculating reaction hashes", 0);
				reactionHashes.clear();
				for(PluginSBase sb : this.getSelectedModel().getListOfReactions().toArray())
				{
					HashSet<String> hashes = this.getAnnotation("HASH", sb);
					String hash;
					if(hashes.size()==0)
					{
						hash = this.getHashCode((PluginReaction)sb)+"";
						this.addAnnotation("HASH",sb,hash);
					}
					else
					{
						hash = hashes.iterator().next();
					}
					reactionHashes.put(hash,((PluginReaction)sb).getId());
				}
				
				frame.updateStatus("Building synonyms database", 0);
				synonymsDatabase.clear();
				for(PluginSBase sb : this.getSelectedModel().getListOfAllSpeciesAlias().toArray())
				{
					String spcaId = ((PluginSpeciesAlias)sb).getAliasID();
					if(frame.getUseSynonyms())
					{
						HashSet<String> names = this.getAnnotation("NAMES",sb);
						for(String name : names)
						{
							synonymsDatabase.put(this.getCompartmentName((PluginSpeciesAlias)sb)+((PluginSpeciesAlias)sb).getType()+name.toUpperCase(),spcaId);
						}
					}
					HashSet<String> ids = this.getAnnotation(this.getClass().getName()+".ID",sb);
					for(String id : ids)
					{
						synonymsDatabase.put(this.getClass().getName()+id,spcaId);
					}
				}
				
				Iterable pwys = getPathwaysFromIds(pwIds);
				String name = "";
				int participants = 0;
				for(Object pwy : pwys)
				{
					String n = getName(pwy);
					name += "+"+n;
					//frame.updateStatus("Counting participants in "+n, 0);
					//participants += getNumPathwayParticipants(pwy);
				}
				name = name.substring(1);
				
				if(getSelectedModel()==null)
				{
					Prompt.errorMessage("No model", "Please create or open a model");
					return;
				}
				getSelectedModel().setName(name);
				this.notifySBaseChanged(getSelectedModel());
				
				resetCompartments(getSelectedModel());
				
				for(Object pwy : pwys)
				{
					frame.updateStatus("Loading reactions in "+getName(pwy), 0);
					for(Object rxn : getPathwayReactions(pwy))
					{
						//System.out.println("Importing reaction "+getName(rxn));
						Iterable reactants = getReactionReactants(rxn);
						if(!reactants.iterator().hasNext())
							continue;
						Iterable products = getReactionProducts(rxn);
						if(!products.iterator().hasNext())
							continue;
						
						frame.updateStatus("Loading reaction "+getName(rxn), 0);
						PluginReaction CDrxn = new PluginReaction();
						//getSelectedModel().addReaction(CDrxn);
						//System.out.println("setting name");
						CDrxn.setName(getName(rxn));
						//System.out.println("setting type");
						String type = getType(rxn);
						CDrxn.setReactionType(type);
						
						
						
						if(type.equals(PluginReactionSymbolType.TRANSCRIPTION) || type.equals(PluginReactionSymbolType.TRANSLATION))
						{
							//System.out.println("setting irreversible");
							CDrxn.setReversible(false);
						}
						//System.out.println("done setup");
						for(Object participant : reactants)
						{
							//System.out.println("Importing reactant "+getName(participant));
							PluginSpeciesReference ref = this.attachReactant(CDrxn, participant, this.getStoichiometry(rxn, participant), frame);
							this.enrichSpeciesAlias(ref.getAlias(), participant, frame);
						}
						
						for(Object participant : products)
						{
							PluginSpeciesReference ref = this.attachProduct(CDrxn, participant, this.getStoichiometry(rxn, participant), frame);
							this.enrichSpeciesAlias(ref.getAlias(), participant, frame);
						}
						
						ArrayList<ModifierReference> modifiers = getReactionModifiers(rxn);
						for(ModifierReference mod : modifiers)
						{
							PluginModifierSpeciesReference ref = this.attachModifier(CDrxn, mod.modifier,mod.type, frame);
							this.enrichSpeciesAlias(ref.getAlias(), mod.modifier, frame);
						}
						
						if(CDrxn.getReactionType().equals(PluginReactionSymbolType.TRANSCRIPTION) || CDrxn.getReactionType().equals(PluginReactionSymbolType.TRANSLATION))
							CDrxn.setReversible(false);
	
						CDrxn = this.addPluginReactionToModel(CDrxn, getLocation(rxn));
						//System.out.println("enriching");
						enrichReaction(CDrxn,rxn);
						//System.out.println("adding annots for reaction");
						this.addAnnotation(this.getClass().getName()+".ID", CDrxn, getId(rxn).toString());
						this.addAnnotation("NAMES", CDrxn, getName(rxn));
						this.addAnnotation(this.getClass().getName()+".NAMES", CDrxn, getName(rxn));
						this.addAnnotation(this.getClass().getName()+".TYPE", CDrxn, getOriginalType(rxn));
						//System.out.println("done adding annots for reaction");
						
						//System.out.println(CDrxn.getReactionType()+" "+CDrxn.getNumReactants()+" -> "+CDrxn.getNumProducts());
						
					}
					if(this.compartmentsNames.containsKey(CYTOSOL))
					{
						PluginCompartment cytosol = getPluginCompartment(CYTOSOL);
						cytosol.setX(0.0);
						cytosol.setWidth(this.nextCompartmentX+this.compartmentW);
						cytosol.setHeight(this.nextCompartmentY+this.compartmentH);
						cytosol.setLineColor(Color.GREEN);
						this.notifySBaseChanged(cytosol);
					}
					if(this.compartmentsNames.containsKey(UNKNOWN))
					{
						PluginCompartment unk = getPluginCompartment(UNKNOWN);
						unk.setLineColor(Color.RED);
						this.notifySBaseChanged((PluginSBase)unk);
					}
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			finally
			{
				PathwayAccessAction.download.release();
				//System.out.println(synonymsDatabase);
			}
		}
	}
	
	protected PluginReaction addPluginReactionToModel(PluginReaction CDrxn,String loc)
	{
		//System.out.println(CDrxn.getName()+ "("+CDrxn.getReactionType()+"): "+CDrxn.getReactant(0).getSpeciesInstance().getName()+"("+CDrxn.getReactant(0).getAlias().getType()+")/"+CDrxn.getNumReactants()+" -> "+CDrxn.getProduct(0).getSpeciesInstance().getName()+"("+CDrxn.getProduct(0).getAlias().getType()+")/"+CDrxn.getNumProducts());
		int rxnHash = getHashCode(CDrxn);
		
		if(reactionHashes.containsKey(rxnHash+""))
		{
			String id = reactionHashes.get(rxnHash);
			//System.out.println("FOUND REACTION "+id+" (not adding)");
			PluginReaction rst = getSelectedModel().getReaction(id);
			this.addAnnotation(this.getClass().getName()+".COMPARTMENT", rst, loc);
			//System.out.println("done retrieving existing reaction");
			return rst;
		}
		else
		{
			//System.out.println("adding reaction "+CDrxn.getName());
			getSelectedModel().addReaction(CDrxn);
			//System.out.println("notifying reaction");
			this.notifySBaseAdded(CDrxn);
			//System.out.println("adding annot for reaction");
			this.addAnnotation(this.getClass().getName()+".COMPARTMENT", CDrxn, loc);
			this.addAnnotation("HASH", CDrxn, rxnHash+"");
			reactionHashes.put(rxnHash+"",CDrxn.getId());
			//System.out.println("done adding reaction");
			return CDrxn;
		}
	}
	
	protected int getHashCode(PluginReaction CDrxn)
	{
		
		ArrayList<String> hashStrings = new ArrayList<String>();
		hashStrings.add(CDrxn.getReactionType());
		//hashStrings.add(loc);
		hashStrings.add(CDrxn.getReversible() ? "R" : "I");
		for(PluginSBase sb : CDrxn.getListOfReactants().toArray()) 
			hashStrings.add(((PluginSpeciesReference)sb).getSpeciesInstance().getId());
		for(PluginSBase sb : CDrxn.getListOfProducts().toArray()) 
			hashStrings.add(((PluginSpeciesReference)sb).getSpeciesInstance().getId());
		for(PluginSBase sb : CDrxn.getListOfModifiers().toArray()) 
			hashStrings.add(((PluginModifierSpeciesReference)sb).getSpeciesInstance().getId());
		Collections.sort(hashStrings);
		String hashString = "";
		for(String s : hashStrings)
			hashString += s;
		
		return hashString.hashCode();
	}
	
	/**
	 * Add a kinetic constant to a CellDesigner reaction
	 * @param CDrxn the reaction to add the constant to
	 * @param km the km value
	 */
	protected void addKm(PluginReaction CDrxn,Double km)
	{
		if(km!=null && km!=0.0)
		{
			PluginKineticLaw kl = new PluginKineticLaw(CDrxn);
			PluginParameter param = new PluginParameter(kl);
			param.setValue(km);
		}
	}
	
	private PluginSpeciesReference attachSubstrate(PluginReaction CDrxn,Object substrate,int stoich,String refType,SelectPathwaysFrame frame)
	{
		PluginSpeciesAlias spc = makePluginSpeciesAlias(substrate,frame);

		PluginSpeciesReference ref = new PluginSpeciesReference(CDrxn,spc);
		ref.setReferenceType(refType);
		ref.setStoichiometry(stoich);
		if(refType.equals(PluginSimpleSpeciesReference.REACTANT))
			CDrxn.addReactant(ref);
		else if(refType.equals(PluginSimpleSpeciesReference.PRODUCT))
			CDrxn.addProduct(ref);
		return ref;
	}
	
	/**
	 * Attach a generic pathway database reactant to a CellDesigner reaction by first 
	 * converting the generic reactant to a CellDesigner species.
	 * @param CDrxn the reaction to add the reactant to
	 * @param reactant the generic pathway database reactant object
	 * @param stoich the stoichiometry of the reactant in the reaction
	 * @param frame the SelectPathwaysFrame providing a GUI
	 * @return a reference to the converted CellDesigner species
	 */
	protected PluginSpeciesReference attachReactant(PluginReaction CDrxn,Object reactant,int stoich,SelectPathwaysFrame frame)
	{
		return attachSubstrate(CDrxn,reactant,stoich,PluginSimpleSpeciesReference.REACTANT,frame);
	}
	
	/**
	 * Attach a generic pathway database product to a CellDesigner reaction by first 
	 * converting the generic product to a CellDesigner species.
	 * @param CDrxn the reaction to add the product to
	 * @param product the generic pathway database product object
	 * @param stoich the stoichiometry of the product in the reaction
	 * @param frame the SelectPathwaysFrame providing a GUI
	 * @return a reference to the converted CellDesigner species
	 */
	protected PluginSpeciesReference attachProduct(PluginReaction CDrxn,Object product,int stoich,SelectPathwaysFrame frame)
	{
		return attachSubstrate(CDrxn,product,stoich,PluginSimpleSpeciesReference.PRODUCT,frame);
	}
	
	/**
	 * Attach a generic pathway database modifier (enzyme, cofactor, etc)
	 * to a CellDesigner reaction by first 
	 * converting the generic modifier to a CellDesigner species.
	 * @param CDrxn CDrxn the reaction to add the modifier to
	 * @param mod the generic pathway database modifier object
	 * @param modType the CellDesigner modifcation type.  Must be a constant modification type from jp.sbi.celldesigner.plugin.PluginModifierSpeciesReference
	 * @param frame the SelectPathwaysFrame providing a GUI
	 * @return a reference to the converted CellDesigner species
	 * @see jp.sbi.celldesigner.plugin.PluginModifierSpeciesReference
	 */
	protected PluginModifierSpeciesReference attachModifier(PluginReaction CDrxn,Object mod,String modType,SelectPathwaysFrame frame)
	{
		PluginSpeciesAlias spc = makePluginSpeciesAlias(mod,frame);
		PluginModifierSpeciesReference mref = new PluginModifierSpeciesReference(CDrxn,spc);
		mref.setModificationType(modType);
		CDrxn.addModifier(mref);
		return mref;
	}
	
//	/**
//	 * Add an annotation field to a CellDesigner object.  If the field exists, 
//	 * the value is added to a growing list with the same name/label
//	 * @param label the name or label of the annotation field
//	 * @param sb the CellDesigner object to add annotation to
//	 * @param value the value of the annotation field
//	 */
//	protected void addAnnotation(String label,PluginSBase sb,String value)
//	{
//		addAnnotation(label,sb,value,false);
//	}
//	
//	/**
//	 * Add an annotation field to a CellDesigner object.  If the field exists, it is cleared before
//	 * the value is added to the list with the same name/label
//	 * @param label the name or label of the annotation field
//	 * @param sb the CellDesigner object to add annotation to
//	 * @param value the value of the annotation field
//	 */
//	protected void putAnnotation(String label,PluginSBase sb,String value)
//	{
//		addAnnotation(label,sb,value,true);
//	}
//	
//	
//	private void addAnnotation(String label,PluginSBase sb,String value,boolean replace)
//	{
//		if(value==null)return;
//		addAnnotation(label,sb,Arrays.asList(value),replace);
//	}
//	
//	/**
//	 * Add an annotation field to a CellDesigner object.  If the field exists, 
//	 * the value is added to a growing list with the same name/label
//	 * @param label the name or label of the annotation field
//	 * @param sb the CellDesigner object to add annotation to
//	 * @param values the list of values of the annotation field
//	 */
//	protected void addAnnotation(String label,PluginSBase sb,Iterable<String> values)
//	{
//		addAnnotation(label,sb,values,false);
//	}
//	
//	/**
//	 * Add an annotation field to a CellDesigner object.  If the field exists, it is cleared before
//	 * the values are added to the list with the same name/label
//	 * @param label the name or label of the annotation field
//	 * @param sb the CellDesigner object to add annotation to
//	 * @param values the list of values of the annotation field
//	 */
//	protected void putAnnotation(String label,PluginSBase sb,Iterable<String> values)
//	{
//		addAnnotation(label,sb,values,true);
//	}
	
	/**
	 * @see PathwayAccessPlugin#addAnnotation(String, PluginSBase, Iterable)
	 * @param label the name or label of the annotation field.  Annotations are stored heirarchically in XML.  Labels refer to 
	 * @param sb the CellDesigner object to add annotation to
	 * @param value the list of value of the annotation field to add
	 */
	protected void addAnnotation(String label,PluginSBase sb,String value)
	{
		if(value==null)return;
		addAnnotation(label,sb,Arrays.asList(value));
	}
	
	/**
	 * Add an annotation field to a CellDesigner object.  If it exists, the value is added to a growing set of annotation values for the label.  
	 * Annotations are stored as XML in CellDesigner objects' Notes field.
	 * @param label the name or label of the annotation field.  Annotations are stored heirarchically in XML.  Labels refer to 
	 * XML nodes using a '.' delimited tree path, with the first element in the '.' delimited label being the top of the XML 
	 * tree.  PathwayAccess automatically adds the following annotations to all Species and Reactions:
	 * 1) {Plugin name}.ID  (This is the unique identifier in the datasource.
	 * 2) {Plugin name}.NAMES (This is the name of the object and any synonyms the datasource provides using your getSynonyms(Object o) method.
	 * 3) {Plugin name}.COMPARTMENT (For reactions only- species have a compartment field already)
	 * 4) HASH (For reactions only. A hash of the reaction's sorted input and output species alias ids used to prevent redundant reactions on downloads)
	 * Prefix your annotations with the class name of your plugin.
	 * @param sb the CellDesigner object to add annotation to
	 * @param values the list of values of the annotation field to add
	 */
	protected void addAnnotation(String label,PluginSBase sb,Iterable<String> values)
	{
		//System.out.println(label);
		String notesString = null;
		if(sb instanceof PluginSpeciesAlias) notesString = ((PluginSpeciesAlias)sb).getSpecies().getNotesString();
		else if(sb instanceof PluginReaction) notesString = ((PluginReaction)sb).getNotesString();
		else if(sb instanceof PluginModel) notesString = ((PluginModel)sb).getNotesString();
		else return;
		
		XMLNode root = XMLNode.convertStringToXMLNode(notesString);
		//if(notesString.length()>0 && root==null) System.out.println("string: "+notesString+", root: "+root);
		if(root==null)
			root = new XMLNode();
		
		addAnnotation(label,root,values);
		notesString = XMLNode.convertXMLNodeToString(root);
		if(sb instanceof PluginSpeciesAlias) ((PluginSpeciesAlias)sb).getSpecies().setNotes(notesString);
		else if(sb instanceof PluginReaction) ((PluginReaction)sb).setNotes(notesString);
		else if(sb instanceof PluginModel) ((PluginModel)sb).setNotes(notesString);
		if(sb instanceof PluginSpeciesAlias)
			this.notifySBaseChanged(((PluginSpeciesAlias)sb).getSpecies());
		else
			this.notifySBaseChanged(sb);
	}
	
	/**
	 * Retrieve the value(s) of an annotation field for a CellDesigner object
	 * @param label the name or label of the annotation field
	 * @param sb the CellDesigner object to add annotation to
	 * @return the value(s) of the annotation field.  A HashSet prevents duplicates.
	 */
	protected HashSet<String> getAnnotation(String label,PluginSBase sb)
	{
		HashSet<String> rst = new HashSet<String>();
		String notesString = null;
		if(sb instanceof PluginSpeciesAlias) notesString = ((PluginSpeciesAlias)sb).getSpecies().getNotesString();
		else if(sb instanceof PluginReaction) notesString = ((PluginReaction)sb).getNotesString();
		else if(sb instanceof PluginModel) notesString = ((PluginModel)sb).getNotesString();
		else return rst;

		XMLNode root = XMLNode.convertStringToXMLNode(notesString);
		if(root==null) return rst;
		getAnnotation(label,root,rst);
		return rst;
	}
	
	private void getAnnotation(String label,XMLNode root,HashSet<String> rst)
	{
		String[] labelParts = label.split("\\.",2);
		String searchName = labelParts[0];
		for(int i=0; i<root.getNumChildren(); i++)
		{
			XMLNode node = root.getChild(i);
			String nodeName = node.getAttributes().getValue("name");
			if(nodeName != null && nodeName.equals(searchName))
			{
				if(labelParts.length==1) getLeafValues(node,rst);
				else getAnnotation(labelParts[1],node,rst);
			}
			else
			{
				getAnnotation(label,node,rst);
			}
		}
	}
	
	private void getLeafValues(XMLNode node,HashSet<String> rst)
	{
		for(int i=0; i<node.getNumChildren(); i++)
		{
			XMLNode leaf = node.getChild(i);
			if(leaf.getNumChildren()>0)
				getLeafValues(leaf,rst);
			else
			{
				String val = leaf.getAttributes().getValue("value");
				if(val!=null)
					rst.add(val);
			}
		}
	}
	
	private void addAnnotation(String label,XMLNode root,Iterable<String> values)
	{
		//System.out.println(root);
		String[] labelParts = label.split("\\.",2);
		String searchName = labelParts[0];
		for(int i=0; i<root.getNumChildren(); i++)
		{
			XMLNode node = root.getChild(i);
			String nodeName = node.getAttributes().getValue("name");
			if(nodeName != null && nodeName.equals(searchName))
			{
				if(labelParts.length==1) addLeaves(node,values);
				else addAnnotation(labelParts[1],node,values);
				return;
			}
		}
		buildNewXMLBranch(root,label,values);
		
	}
	
	private void buildNewXMLBranch(XMLNode root,String label,Iterable<String> values)
	{
		String[] labelParts = label.split("\\.",2);
		//System.out.println("creating "+labelParts[0]+" of "+label);
		XMLAttributes atts = new XMLAttributes();
		atts.add("name",labelParts[0]);
		XMLNode node = new XMLNode(new XMLToken(new XMLTriple(ANNOTATION_XML_LIST),atts));
		if(labelParts.length==1)
			addLeaves(node,values);
		else
			buildNewXMLBranch(node,labelParts[1],values);
		root.addChild(node);
	}
	
	private void addLeaves(XMLNode node, Iterable<String> values)
	{
		for(String value : values)
		{
			XMLAttributes atts = new XMLAttributes();
			//System.out.println("adding "+value+" to "+node.getAttributes().getValue("name"));
			atts.add("value",value);
			XMLNode leaf = new XMLNode(new XMLToken(new XMLTriple(ANNOTATION_XML_ITEM),atts));
			node.addChild(leaf);
		}
	}
	
	
//	private void addAnnotation(String label,PluginSBase sb,Iterable<String> values,boolean replace)
//	{
//		HashMap<String,HashSet<String>> notes = parseNotes(sb);
//		if(!notes.containsKey(label)) notes.put(label,new HashSet<String>());
//		if(replace) notes.get(label).clear();
//		for(String value : values) notes.get(label).add(value);//.replace("\"", ""));
//		String noteString = makeNotes(notes);
//		
//		if(sb instanceof PluginSpeciesAlias)
//		{
//			//System.out.println(((PluginSpeciesAlias)sb).getName()+" adding "+label+":"+value+" to '"+((PluginSpeciesAlias)sb).getSpecies().getNotesString()+"' -> "+noteString);
//			((PluginSpeciesAlias)sb).getSpecies().setNotes(noteString);
//			//System.out.println(((PluginSpeciesAlias) sb).getName());
//			this.notifySBaseChanged(((PluginSpeciesAlias)sb).getSpecies());
//		}
//		else if(sb instanceof PluginReaction)
//		{
//			((PluginReaction)sb).setNotes(noteString);
//			this.notifySBaseChanged(sb);
//		}
//		else if(sb instanceof PluginModel)
//		{
//			((PluginModel)sb).setNotes(noteString);
//			this.notifySBaseChanged(sb);
//		}
//	}
//	
//	/**
//	 * Retrieve the value(s) of an annotation field for a CellDesigner object
//	 * @param label the name or label of the annotation field
//	 * @param sb the CellDesigner object to add annotation to
//	 * @return the value(s) of the annotation field
//	 */
//	protected HashSet<String> getAnnotation(String label,PluginSBase sb)
//	{
//		HashMap<String,HashSet<String>> annots = parseNotes(sb);
//		return annots.containsKey(label) ? annots.get(label) : new HashSet<String>();
//	}
	
//	private HashMap<String,HashSet<String>> parseNotes(PluginSBase sb)
//	{
//		HashMap<String,HashSet<String>> notes = new HashMap<String,HashSet<String>>();
//		String noteString = "";
//		if(sb instanceof PluginSpeciesAlias) noteString = ((PluginSpeciesAlias)sb).getSpecies().getNotesString();
//		else if(sb instanceof PluginReaction) noteString = ((PluginReaction)sb).getNotesString();
//		if(noteString.contains(":"))
//			for(String note : noteString.split("\n"))
//			{
//				String[] pair = note.trim().split(":",2);
//				String key = pair[0];
//				notes.put(key, new HashSet<String>(Arrays.asList(pair[1].split(","))));
//			}
//		return notes;
//	}
//
//	private String makeNotes(HashMap<String,HashSet<String>> notes)
//	{
//		String rst = "";
//		for(String key : notes.keySet())
//		{
//			HashSet<String> values = notes.get(key);
//			String valueString = "";
//			for(String value : values)
//			{
//				valueString += ","+value;
//			}
//			valueString = valueString.substring(1);
//			rst += "\n"+key+":"+valueString;
//		}
//		return rst.substring(1);
//	}
	

	
	private HashMap<String,HashSet<String>> parseNotes(PluginSBase sb)
	{
		HashMap<String,HashSet<String>> notes = new HashMap<String,HashSet<String>>();
		String noteString = "";
		if(sb instanceof PluginSpeciesAlias) noteString = ((PluginSpeciesAlias)sb).getSpecies().getNotesString();
		else if(sb instanceof PluginReaction) noteString = ((PluginReaction)sb).getNotesString();
		XMLNode root = XMLNode.convertStringToXMLNode(noteString);
		for(int i=0; root!=null && i<root.getNumChildren(); i++)
		{
			XMLNode node = root.getChild(i);
			HashSet<String> vals = new HashSet<String>();
			for(int j=0; j<node.getNumChildren(); j++)
			{
				vals.add(node.getChild(j).getAttributes().getValue("value"));
			}
			notes.put(node.getAttributes().getValue("name"),vals);
		}
		return notes;
	}
	
	private String makeNotes(HashMap<String,HashSet<String>> notes)
	{
		XMLNode root = new XMLNode();
		for(String key : notes.keySet())
		{
			HashSet<String> values = notes.get(key);
			XMLAttributes atts = new XMLAttributes();
			atts.add("name", key);
			XMLNode node = new XMLNode(new XMLToken(new XMLTriple(ANNOTATION_XML_LIST),atts));
			for(String value : values)
			{
				XMLAttributes atts2 = new XMLAttributes();
				atts2.add("value",value);
				XMLNode node2 = new XMLNode(new XMLToken(new XMLTriple(ANNOTATION_XML_ITEM),atts2));
				node.addChild(node2);
			}
			root.addChild(node);
		}
		return XMLNode.convertXMLNodeToString(root);
	}
	
	/**
	 * Given a list of pathway ID objects (usually integers or Strings), 
	 * get the corresponding generic pathway database pathway objects
	 * @param ids a list of the generic pathway database pathway IDs.
	 * @return an Iterable of the corresponding generic pathway database pathway objects
	 */
	protected abstract Iterable getPathwaysFromIds(ArrayList ids);
	
	/**
	 * Get the type of object as represented in the datasource.
	 * Some datasources use custom class names and others use some sort of
	 * type attribute.  Your plugin must return whatever is applicable to your 
	 * datasource.
	 * @param o the object whose type from the datasource is to be returned.
	 * @return a String representation of the type as represented in the specific datasource.
	 */
	protected abstract String getOriginalType(Object o);
	
	/**
	 * Get the name of a generic pathway database object (pathway, molecule, gene, etc).
	 * These are used to identify nodes in an imported CellDesigner model, so they should be unique.
	 * @param o the generic pathway database object
	 * @return the generic pathway database object's name
	 */
	protected abstract String getName(Object o);
	
	/**
	 * Get the ID of a generic pathway database object (pathway, molecule, gene, etc).
	 * These are stored as annotation fields in an imported CellDesigner model.
	 * @param o the generic pathway database object
	 * @return the generic pathway database object's ID
	 */
	protected abstract Object getId(Object o);
	
	/**
	 * Count the sum of all genes, enzymes, and chemical compounds in a pathway.
	 * @param pwy the generic pathway database pathway object to count
	 * @return the sum of all genes, enzymes, and chemical compounds in the pathway.
	 */
	protected abstract int getNumPathwayParticipants(Object pwy);
	
	/**
	 * Get the generic pathway database reaction objects associated 
	 * with a given generic pathway database pathway.
	 * @param pwy the generic pathway database pathway
	 * @return the generic pathway database reaction objects associated with pwy
	 */
	protected abstract Iterable getPathwayReactions(Object pwy);
	
	/**
	 * Get the generic pathway database objects associated as reactants/inputs to a 
	 * generic pathway database reaction.
	 * @param rxn the generic pathway database reaction
	 * @return the generic pathway database reactants
	 */
	protected abstract Iterable getReactionReactants(Object rxn);
	
	/**
	 * Get the generic pathway database objects associated as products/outputs to a 
	 * generic pathway database reaction.
	 * @param rxn the generic pathway database reaction
	 * @return the generic pathway database products
	 */
	protected abstract Iterable getReactionProducts(Object rxn);
	
	/**
	 * Get the generic pathway database objects associated as modifiers/enzymes/cofactors to a 
	 * generic pathway database reaction.
	 * @param rxn the generic pathway database reaction
	 * @return the generic pathway database modifiers
	 */
	protected abstract ArrayList<ModifierReference> getReactionModifiers(Object rxn);
	
	/**
	 * Use available methods for a generic pathway database object to learn 
	 * its type in the pathway database and then convert that to a CellDesigner species or reaction type.
	 * @param o the generic pathway database object
	 * @return a constant from either of PluginCompartmentSymbolType, PluginReactionSymbolType, 
	 * or PluginSpeciesSymbolType constants that best represents the generic pathway database object's 
	 * type as represented in the database.
	 * @see jp.sbi.celldesigner.plugin.util.PluginCompartmentSymbolType
	 * @see jp.sbi.celldesigner.plugin.util.PluginReactionSymbolType
	 * @see jp.sbi.celldesigner.plugin.util.PluginSpeciesSymbolType
	 */
	protected abstract String getType(Object o);
	
	/**
	 * Get any synonyms provided by a generic pathway database for one of its objects.
	 * @param o the generic pathway database object
	 * @return the generic pathway database object's synonyms.  
	 * Return an empty ArrayList<String> if there are no synonyms.
	 */
	protected abstract ArrayList<String> getSynonyms(Object o);
	
	/**
	 * Do any extra processing of a generic pathway database reaction during import to CellDesigner.
	 * Now is the chance to add special annotations or features.
	 * @param CDrxn the CellDesigner reaction that has been so far created to represent the 
	 * generic pathway database reaction.  The is the CellDesigner object to enrich with annotation.
	 * @param rxn the generic pathway database reaction that is being imported to CellDesigner.
	 */
	protected abstract void enrichReaction(PluginReaction CDrxn,Object rxn);
	
	/**
	 * Do any extra processing of a generic pathway database species object during import to CellDesigner.
	 * Now is the chance to add special annotations or features.
	 * @param spca the CellDesigner species that has been so far created to represent the 
	 * generic pathway database reaction.  The is the CellDesigner object to enrich with annotation.
	 * @param o the generic pathway database object that is being imported to CellDesigner.
	 * @param frame the SelectPathwaysFrame providing a GUI
	 * For example, some pathway databases do not represent transcription or translation events as reactions, 
	 * rather, they are special transcription or translation events, which must be traversed here.
	 */
	protected abstract void enrichSpeciesAlias(PluginSpeciesAlias spca,Object o,SelectPathwaysFrame frame);
	
	/**
	 * Get the organisms represented in the pathway database.
	 * @return an Iterable of the organism objects represented in the pathway database
	 */
	protected abstract Iterable getOrganisms();
	
	/**
	 * Get the generic pathway database pathway objects associated with an organism ID (usually an integer or String).
	 * @param orgId the organism ID
	 * @return an Iterable of generic pathway database pathway objects associated with the organism.
	 */
	protected abstract Iterable getPathwaysFromOrganismId(Object orgId);
	
	/**
	 * Get the name of the subcellular location of a generic pathway database object.
	 * @param o the generic pathway database object
	 * @return the name of the subcellular location of the generic pathway database object
	 */
	protected abstract String getLocation(Object o);
	
	/**
	 * Get the stoichiometry of a generic pathway database object as it participates (reactant or product) 
	 * in a generic pathway database reaction.
	 * @param rxn the generic pathway database reaction
	 * @param substrate the generic pathway database reaction participant
	 * @return the stoichiometry of substrate as it participates in rxn
	 */
	protected abstract int getStoichiometry(Object rxn,Object substrate);
	
	/**
	 * Do any necessary checks for CellDesigner model validity or pathway database connectivity 
	 * before committing a CellDesigner model to it.
	 * @return true if the commit can commence, else false.
	 */
	protected abstract boolean prevalidateCommit();
	
	/**
	 * Initialize a pathway in the database based on the current CellDesigner model before a pathway commit to the database.
	 * @param model the CellDesigner model to be committed.
	 * @param orgId the ID of the organism to associate the new pathway with in the pathway database.
	 * @return the generic pathway database pathway object created during initialization.  All CellDesigner reactions 
	 * and species in the model will be added to this pathway in the database during a commit.
	 */
	protected abstract Object initPathway(PluginModel model,Object orgId);
	
	/**
	 * Initialize a reaction in the database based on a CellDesigner reaction before it is committed to the database.
	 * @param orgId the ID of the organism to associate this reaction with.
	 * @param CDrxn the CellDesigner reaction to model the pathway database reaction after
	 * @return the generic pathway database reaction object created during initialization.  All participants in 
	 * in the CDrxn will be committed to the database.
	 */
	protected abstract Object initReaction(Object orgId,PluginReaction CDrxn);
	
	/**
	 * Initialize an entity or species in the database based on a CellDesigner species before it is committed to the database.
	 * @param comp the name of the subcellular location/compartment of the entity
	 * @param orgId the ID of the organism
	 * @param spca the CellDesigner species alias to base the entity on
	 * @return the generic pathway database entity/species object created during initialization.
	 */
	protected abstract Object initEntity(String comp,Object orgId,PluginSpeciesAlias spca);
	
	/**
	 * Commit/upload/save a generic pathway database object to the database.  Must be able to commit all parts of a 
	 * pathway, including pathway, reaction, protein, RNA, gene, or any other objects.  If the database does not support 
	 * commits, do nothing.
	 * @param o the object to commit to the database.  Always the object returned from the init methods.
	 * @see PathwayAccessPlugin#initPathway
	 * @see PathwayAccessPlugin#initReaction
	 * @see PathwayAccessPlugin#initEntity
	 * @return the committed object.
	 */
	protected abstract Object commit(Object o);
	
	/**
	 * During a commit, add a reactant to a reaction in the database.
	 * @param rxn the generic pathway database reaction object
	 * @param participant the generic pathway database reactant object
	 * @param stoich the stoichiometric coefficient for the participant in the reaction
	 */
	protected abstract void addReactant(Object rxn,Object participant,int stoich);
	
	/**
	 * During a commit, add a product to a reaction in the database.
	 * @param rxn the generic pathway database reaction object
	 * @param participant the generic pathway database product object
	 * @param stoich the stoichiometric coefficient for the participant in the reaction
	 */
	protected abstract void addProduct(Object rxn,Object participant,int stoich);
	
	/**
	 * During a commit, add a modifier (enzyme, cofactor, etc) to a reaction in the database.
	 * @param rxn the generic pathway database reaction object
	 * @param participant the generic pathway database modifier object
	 */
	protected abstract void addModifier(Object rxn,Object participant);
	
	/**
	 * During a commit, associate a reaction with a pathway in the generic pathway database
	 * @param rxn the generic pathway database reaction
	 * @param pwy the generic pathway database pathway
	 */
	protected abstract void addReactionToPathway(Object rxn,Object pwy);
	
	/**
	 * Create a CellDesigner species (alias) based on a generic pathway database object (compound, gene, protein, etc) 
	 * using its name, id, type, and other information for implemented get methods.  To prevent infinite recursion, 
	 * enrichSpeciesAlias is not called- you must to that yourself in your own plugin.
	 * Uses synonyms if frame's "Use Synonyms" checkbox is checked.
	 * @param o the generic pathway database object
	 * @param frame the SelectPathwaysFrame that requested the SpeciesAlias creation. (used for checking option values and status updates)
	 * @return the existing CellDesigner species alias if one of the generic pathway database object's name already 
	 * exists in the current CellDesigner model, else a newly created one based on o.
	 */
	protected PluginSpeciesAlias makePluginSpeciesAlias(Object o, SelectPathwaysFrame frame)
	{
		try
		{
			String name = getName(o);
			frame.updateStatus("Loading "+name, 0);
			String type = getType(o);
			frame.updateStatus("Loading "+name+" of type "+type, 0);
			String locationName = getLocation(o);
			if(locationName==null || locationName.toLowerCase().equals("cytoplasm")) locationName = this.CYTOSOL;
			else locationName = locationName.toLowerCase();
			frame.updateStatus("Loading "+name+" of type "+type+" in the "+locationName, 0);
			ArrayList<String> names = getSynonyms(o);
			names.add(0,name);
			
			PluginSpecies spc;
			PluginSpeciesAlias spca = this.findSpeciesAlias(type, names,getId(o).toString(), locationName, frame);
			if(spca!=null)
				return spca;
			
			//System.out.println(name+" species not in model yet");
			spc = new PluginSpecies(type,name);
			try {spc.setSpeciesType(type);} catch(NoSuchMethodError e) {}
			this.getSelectedModel().addSpecies(spc);
			spca = spc.getSpeciesAlias(0);
			

			
			PluginCompartment pcomp = getPluginCompartment(locationName);
			spca.setFramePosition(pcomp.getX()+offsetInCompartment, pcomp.getY()+offsetInCompartment);
			spc.setCompartment(pcomp.getId());
			
			if(spca.getType().equals(PluginSpeciesSymbolType.COMPLEX))
			{
				spca.setFrameSize(50.0,50.0);
				spca.setFramePosition(spca.getX(),spca.getY()+this.compartmentH/2.0);
			}
			
			//System.out.println(name+" ("+type+") notifying");
			this.notifySBaseAdded(spca);
			//System.out.println(name+" done notifying "+spca.getAliasID()+" "+spc.getId());
			//System.out.println(name+" adding annots");
			this.addAnnotation(this.getClass().getName()+".ID", spca, getId(o).toString());
			//this.addAnnotation("NAMES", spca, names);
			this.addAnnotation(this.getClass().getName()+".NAMES", spca, names);
			this.addAnnotation(this.getClass().getName()+".TYPE", spca, getOriginalType(o));
			//System.out.println(name+" annots done");
			//System.out.println(name+" "+spca.getSpecies().getNumSpeciesAlias());
			
			if(frame.getUseSynonyms())
			{
				//HashSet<String> names = this.getAnnotation("NAMES",spca);
				for(String n : names)
				{
					synonymsDatabase.put(this.getCompartmentName(spca)+spca.getType()+n.toUpperCase(),spca.getAliasID());
				}
			}
			synonymsDatabase.put(this.getClass().getName()+getId(o).toString(),spca.getAliasID());
			return spca;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
		
		
	}
	
	public class GenericComplex
	{
		public HashMap<String,Integer> counts;
		public PluginSpeciesAlias complex;
		public String name;
		private PathwayAccessPlugin plugin;
		public GenericComplex(String name,PluginSpeciesAlias complex,PathwayAccessPlugin plug)
		{
			counts = new HashMap<String,Integer>();
			this.name = name;
			plugin=plug;
			this.complex = complex;
			if(complex!=null) this.name = complex.getName();
		}
		public int getCount(String id)
		{
			if(counts.containsKey(id)) return counts.get(id);
			else return 0;
		}
		public int add(PluginSpeciesAlias spca)
		{
			if(!counts.containsKey(spca.getAliasID())) counts.put(spca.getAliasID(),1);
			else counts.put(spca.getAliasID(),counts.get(spca.getAliasID()) + 1);
			return counts.get(spca.getAliasID());
		}
		public int getSum()
		{
			int sum = 0;
			for(Integer i : counts.values()) sum += i;
			return sum;
		}
		public PluginSpeciesAlias constructSpeciesAlias()
		{
			if(counts.size()==1)
			{
				if(complex==null)
				{
					PluginSpecies spc = new PluginSpecies(PluginSpeciesSymbolType.PROTEIN,name);
					try {spc.setSpeciesType(PluginSpeciesSymbolType.PROTEIN);} catch(NoSuchMethodError e) {}
					plugin.getSelectedModel().addSpecies(spc);
					complex = spc.getSpeciesAlias(0);
				}
				if(counts.containsValue(1))
				{
					//not really a complex- just return the single component
					return complex;
				}
				else
				{
					//it is a homomultimer
					complex.setHomodimer(counts.values().iterator().next());
					PluginSpeciesAlias src = plugin.getSelectedModel().getPluginSpeciesAlias(counts.keySet().iterator().next());
					PluginReaction CDrxn = new PluginReaction();
					CDrxn.setName(name+"-formation");
					CDrxn.setReversible(true);
					CDrxn.setReactionType(PluginReactionSymbolType.STATE_TRANSITION);
					PluginSpeciesReference srcRef = new PluginSpeciesReference(CDrxn,src);
					srcRef.setReferenceType(PluginSimpleSpeciesReference.REACTANT);
					srcRef.setStoichiometry(counts.get(src.getAliasID())*1.0);
					CDrxn.addReactant(srcRef);
					PluginSpeciesReference cRef = new PluginSpeciesReference(CDrxn,complex);
					cRef.setReferenceType(PluginSimpleSpeciesReference.PRODUCT);
					CDrxn.addProduct(cRef);
					plugin.addPluginReactionToModel(CDrxn, getCompartmentName(complex));
				}
			}
			else
			{
				//it is a heteromultimer
				if(complex==null)
				{
					PluginSpecies spc = new PluginSpecies(PluginSpeciesSymbolType.COMPLEX,name);
					try {spc.setSpeciesType(PluginSpeciesSymbolType.COMPLEX);} catch(NoSuchMethodError e) {}
					plugin.getSelectedModel().addSpecies(spc);
					complex = spc.getSpeciesAlias(0);
				}
				PluginReaction CDrxn = new PluginReaction();
				CDrxn.setName(name+"-formation");
				CDrxn.setReversible(true);
				CDrxn.setReactionType(PluginReactionSymbolType.STATE_TRANSITION);
				PluginSpeciesReference cRef = new PluginSpeciesReference(CDrxn,complex);
				cRef.setReferenceType(PluginSimpleSpeciesReference.PRODUCT);
				CDrxn.addProduct(cRef);
				for(String id : counts.keySet())
				{
					PluginSpeciesAlias src = plugin.getSelectedModel().getPluginSpeciesAlias(id);
					PluginSpeciesReference srcRef = new PluginSpeciesReference(CDrxn,src);
					srcRef.setReferenceType(PluginSimpleSpeciesReference.REACTANT);
					srcRef.setStoichiometry(counts.get(id)*1.0);
					CDrxn.addReactant(srcRef);
					plugin.addPluginReactionToModel(CDrxn, plugin.getSelectedModel().getCompartment(src.getSpecies().getCompartment()).getName());
				}
			}
			return complex;
		}
	}

    
    class CommitProgressMonitor extends JPanel
    implements PropertyChangeListener {

    	private ProgressMonitor progressMonitor;
    	private JTextArea taskOutput;

    	public CommitProgressMonitor(PathwayAccessPlugin p) {
    		super(new BorderLayout());

    		taskOutput = new JTextArea(5, 20);
    		taskOutput.setMargin(new Insets(5,5,5,5));
    		taskOutput.setEditable(false);

    		add(new JScrollPane(taskOutput), BorderLayout.CENTER);
    		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    		
    		progressMonitor = new ProgressMonitor(CommitProgressMonitor.this,
    				"Commiting pathway...",
    				"Initializing", 0, 100);
    		progressMonitor.setProgress(0);

    	}
    	
        public void propertyChange(PropertyChangeEvent evt) {
            if ("progress" == evt.getPropertyName()) {
                int progress = (Integer) evt.getNewValue();
                progressMonitor.setProgress(progress);
            } 

        }

    }
    
   	/**
	 * To avoid locking the screen when downloading, the commit process is threaded.
	 */
    class CommitThread extends Thread
    {
    	PathwayAccessPlugin plug;
    	ProgressMonitor monitor;
    	JFrame frame;
    	
    	CommitThread(PathwayAccessPlugin p,ProgressMonitor m)
    	{
    		plug = p;
    		monitor = m;
        }

        public void run() 
        {
        	plug.commit(monitor);
        }
    }
	
	/**
	The commit method.  It is called when the COMMIT option is selected from the Plugin menu in CellDesigner.
	 */
    protected void commit()
    {
        ProgressMonitor monitor = new ProgressMonitor(null,
				"Commiting pathway...",
				"Initializing", 0, 100);
        monitor.setMillisToPopup(0);
        monitor.setMillisToDecideToPopup(0);
        monitor.setProgress(0);
        CommitThread t = new CommitThread(this,monitor);
		t.start();
    }
    
	protected void commit(ProgressMonitor monitor)
	{
		PluginModel model = this.getSelectedModel();
		
		monitor.setMaximum(model.getNumReactions()+model.getNumSpecies());
		monitor.setMinimum(0);
		monitor.setNote("Initializing "+model.getName()+"("+monitor.getMaximum()+" items)");
		monitor.setProgress(0);
		HashSet<String> commitDone = new HashSet<String>();
		
		PrintStream stdout = System.out;
		PrintStream stderr = System.err;
		PrintStream outputStream = new StringStream(new ByteArrayOutputStream());
		System.setOut(outputStream);
		System.setErr(outputStream);
		
		String name = model.getName();
		String id = model.getId();
		if(id!=null && id.length()>0 && !id.equals("untitled"))
			name = id;
		if(name==null || name.equals("untitled") || name.length()==0)
			name = Prompt.userEnter("Pathway Name","Please name the model/pathway");
		model.setName(name);
		System.out.println("Model ID: "+model.getId());
		System.out.println("Model Name: "+model.getName());
		
		monitor.setNote("Loading organisms");
		System.out.println("Loading organisms...");
		TreeMap<String,Object> orgMap = new TreeMap<String,Object>(String.CASE_INSENSITIVE_ORDER);
		for(Object o : this.getOrganisms())
		{
			orgMap.put(getName(o), getId(o));
		}
		System.out.println("Waiting for organism selection...");
		monitor.setNote("Waiting for organism selection...");
		String orgName = Prompt.userSelect("Organism","Select organism", new ArrayList(Arrays.asList(orgMap.keySet().toArray())));
		if(orgName!=null)
		{
			Object orgId = orgMap.get(orgName);
			try
			{
	
				monitor.setNote("Validating data");
				if(!prevalidateCommit())
				{
					Prompt.errorMessage("Prevalidation Failed", "Commit failed.\nAre you properly connected and have a proper model loaded?");
					return;
				}
	
				System.out.println("Using organism: "+orgName);
				//if(true) return;
				Object pwy = commit(initPathway(model,orgId));
				if(pwy==null) return;
			
				PluginListOf rxnList = model.getListOfReactions();
				for(PluginSBase sb : rxnList.toArray())
				{
					PluginReaction CDrxn = (PluginReaction)(sb);
					monitor.setNote("Initializing reaction: "+CDrxn.getName());
					Object rxn = initReaction(orgId,CDrxn);
					if(rxn==null) continue;
	    			//reactants
	    			for(int j=0; j<CDrxn.getNumReactants(); j++)
	    			{
	    				PluginSpeciesAlias spca = CDrxn.getReactant(j).getAlias();
	    				monitor.setNote("Committing species: "+spca.getName());
	    				Object participant = commit(initEntity(this.getContainingCompartment(spca, model),orgId,spca));
	    				addReactant(rxn,participant,(int)Math.round(CDrxn.getReactant(j).getStoichiometry()));
	    				if(!commitDone.contains(spca.getAliasID()))
	    				{
	    					commitDone.add(spca.getAliasID());
	    					monitor.setProgress(commitDone.size());
	    				}
	    			}
	    			//modifiers
	    			for(int j=0; j<CDrxn.getNumModifiers(); j++)
	    			{
	    				PluginSpeciesAlias spca = CDrxn.getModifier(j).getAlias();
	    				monitor.setNote("Committing species: "+spca.getName());
	    				Object participant = commit(initEntity(this.getContainingCompartment(spca, model),orgId,spca));
	    				addModifier(rxn,participant);
	    				if(!commitDone.contains(spca.getAliasID()))
	    				{
	    					commitDone.add(spca.getAliasID());
	    					monitor.setProgress(commitDone.size());
	    				}
	    			}
	    			    			
	    			//products
	    			for(int j=0; j<CDrxn.getNumProducts(); j++)
	    			{
	    				PluginSpeciesAlias spca = CDrxn.getProduct(j).getAlias();
	    				monitor.setNote("Committing species: "+spca.getName());
	    				Object participant = commit(initEntity(this.getContainingCompartment(spca, model),orgId,spca));
	    				addProduct(rxn,participant,(int)Math.round(CDrxn.getProduct(j).getStoichiometry()));
	    				if(!commitDone.contains(spca.getAliasID()))
	    				{
	    					commitDone.add(spca.getAliasID());
	    					monitor.setProgress(commitDone.size());
	    				}
	    			}
	    			monitor.setNote("Committing reaction: "+CDrxn.getName());
	    			commit(rxn);
	    			addReactionToPathway(rxn,pwy);
					if(!commitDone.contains(CDrxn.getId()))
					{
						commitDone.add(CDrxn.getId());
						monitor.setProgress(commitDone.size());
					}
				}
				
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		else
		{
			System.out.println("No organisms!");
		}
		String output = outputStream.toString();
		System.setOut(stdout);
		System.setErr(stderr);
		Prompt.showText("Pathway commit complete.","Attempt to upload "+model.getName()+" complete.\n See the API output below.",output);
	}
	
	/**
	Get an ArrayList of the names of all the SpeciesAlias' in a list of PluginSBase's.
	@param list a PluginListOf of PluginSBases to extract SpeciesAlias names from.
	@return the names of all the SpeciesAlias' in a list of PluginSBase's
	 */
	protected ArrayList<String> getPluginSpeciesAliasNames(PluginListOf list)
	{
		ArrayList<String> rst = new ArrayList<String>(0);
		PluginSpeciesAlias s;
		for(int i=0; i<list.size(); i++)
		{
			s = (PluginSpeciesAlias)list.get(i);
			rst.add(s.getName());
		}
		return rst;
	}
	
	/**
	Find a PluginSpeciesAlias by type and name in the model if it exists.
	@param type the type of the species alias to lookup
	@param name the name of the species alias to lookup
	@return if a match is found, returns the match. else returns null.
	 */
	protected PluginSpeciesAlias findSpeciesAlias(String type, String name, String id, String loc, SelectPathwaysFrame f)
	{
		ArrayList<String> names = new ArrayList<String>();
		names.add(name);
		return findSpeciesAlias(type,names, id, loc, f);
	}
	
	/**
	Find a PluginSpeciesAlias by type and  a set of names in the model if it exists. 
	Use this when trying to lookup a species alias from a new entity with synonyms and alternate names.
	@param type the type of the species alias to lookup
	@param names all the possible names the species alias may use.
	@return if a match is found, returns the match. else returns null.
	 */
	protected PluginSpeciesAlias findSpeciesAlias(String type, List<String> names, String id, String loc, SelectPathwaysFrame f)
	{
		if(synonymsDatabase.containsKey(this.getClass().getName()+id)) 
		{
			//System.out.println("FOUND SPCA BY ID: "+id+" "+type+" "+names.get(0));
			return this.getSelectedModel().getPluginSpeciesAlias(synonymsDatabase.get(this.getClass().getName()+id));
		}
		else 
		{
			if(f.getUseSynonyms())
				for(String name : names)
					if(synonymsDatabase.containsKey(loc+type+name.toUpperCase()))
					{
						//System.out.println("FOUND SPCA BY NAME ("+name+"): "+id+" "+type+" "+names.get(0));
						PluginSpeciesAlias spca = this.getSelectedModel().getPluginSpeciesAlias(synonymsDatabase.get(loc+type+name.toUpperCase()));
						this.addAnnotation(this.getClass().getName()+".ID", spca, id);
						this.addAnnotation(this.getClass().getName()+".NAMES", spca, names);
						synonymsDatabase.put(this.getClass().getName()+id, spca.getAliasID());
						for(String n : names)
						{
							synonymsDatabase.put(loc+type+n.toUpperCase(), spca.getAliasID());
						}
						return spca;
					}
		}
		//System.out.println("NOT FOUND: "+id+" "+type+" "+names.get(0));
		return null;
		
		
//		//System.out.println("Looking for "+names.get(0)+" (of "+names.size()+") ("+type+", "+loc+") "+list.size()+"...");
//		for(PluginSBase sb : this.getSelectedModel().getListOfAllSpeciesAlias().toArray())
//		{
//			PluginSpeciesAlias s = (PluginSpeciesAlias)sb;
//			String sCompName = getCompartmentName(s);//s.getSpecies().getCompartment();//s.getType().equals(PluginSpeciesSymbolType.COMPLEX) ? this.getSelectedModel().getCompartment(s.getSpecies().getCompartment()).getName() : s.getSpecies().getCompartment();
//			if(s.getType()!=null && s.getType().equals(type) && sCompName.equals(loc))
//			{
//				//System.out.println("\t"+s.getName());
//				HashMap<String,HashSet<String>> notes = this.parseNotes(s);
//				String idKey = this.getClass().getName()+".ID";
//				if(notes.containsKey(idKey) && notes.get(idKey).equals(id))
//				{
//					return s;
//				}
//				else if(s.getName().toUpperCase().equals(names.get(0).toUpperCase()))
//				{
//					return s;
//				}
//				else if(!f.getUseSynonyms())
//				{
//					continue;
//				}
//				for(String name : names)
//				{
//					name = name.toUpperCase();
//					HashSet<String> sNames = notes.get("NAMES");
//					for(String n : sNames)
//					{
//						//System.out.println(name+" ?=? "+n.toUpperCase());
//						if(n.toUpperCase().equals(name))
//						{
//							//System.out.println("HIT "+s.getName());
//							return s;
//						}
//					}
//					
//				}
//			}
//			//System.out.println("\t\t"+names.get(0)+" != "+s.getName()+" "+s.getType()+" "+sCompName);
//		}
//		//System.out.println("Can't find "+names.get(0));
//		return null;
	}
	
//	protected PluginSpecies findSpecies(String type, String name)
//	{
//		ArrayList<String> names = new ArrayList<String>();
//		names.add(name);
//		return findSpecies(type,names);
//	}
//	
//	/**
//	Find a PluginSpecies by type and  a set of names in the model if it exists. 
//	Use this when trying to lookup a species alias from a new entity with synonyms and alternate names.
//	@param type the type of the species alias to lookup
//	@param names all the possible names the species alias may use.
//	@return if a match is found, returns the match. else returns null.
//	 */
//	protected PluginSpecies findSpecies(String type, List<String> names)
//	{
//		//System.out.println(this.getSelectedModel());
//		PluginListOf list = this.getSelectedModel().getListOfSpecies();
//		//System.out.println("Looking for "+names.get(0)+" (of "+names.size()+") ("+type+")...");
//		PluginSpeciesAlias s;
//		for(int i=0; i<list.size(); i++)
//		{
//			s = ((PluginSpecies)list.get(i)).getSpeciesAlias(0);
//			if(s.getType()!=null && s.getType().equals(type))
//			{
//				//System.out.println("\t"+s.getName());
//				for(String name : names)
//				{
//					name = name.toUpperCase();
//					//System.out.println("checking "+name);
//					if(s.getName().toUpperCase().equals(name))
//					{
//						//System.out.println("HIT "+s.getName());
//						return s.getSpecies();
//					}
//					else
//					{
//						for(String n : this.getAnnotation("NAMES", s))
//						{
//							//System.out.println("checking "+name);
//							if(n.toUpperCase().equals(name))
//							{
//								//System.out.println("HIT "+s.getName());
//								return s.getSpecies();
//							}
//						}
//					}
//				}
//			}
//		}
//		//System.out.println("Can't find "+names.get(0));
//		return null;
//	}
	
	protected String getCompartmentName(PluginSpeciesAlias spca)
	{
		String cn = spca.getSpecies().getCompartment();
		//System.out.println(spca.getName()+" in "+cn);
		PluginCompartment comp = this.getSelectedModel().getCompartment(spca.getSpecies().getCompartment());
		if(comp != null) return comp.getName();
		else return cn;
	}
	
	/**
	Find a compartment by name in the model. 
	Use this when checking if an encountered compartment is already in the model.
	@param name the name of the compartment.
	@return if a match is found, returns the match. else creates a new compartment, places it in the model according to 
	nextCompartmentX and nextCompartmentY, adds compartmentW to nextCompartmentX for the next compartment, adds 
	the newly created compartment's names to the compartmentsNames map, and returns 
	the newly created compartment.
	 */
	protected PluginCompartment getPluginCompartment(String name)
	{
		PluginCompartment pcomp;
		if(this.compartmentsNames.containsKey(name))
			pcomp = this.getSelectedModel().getCompartment(compartmentsNames.get(name));
		else
		{
			pcomp = new PluginCompartment(PluginCompartmentSymbolType.SQUARE);
			pcomp.setName(name);
			pcomp.setX(this.nextCompartmentX);
			pcomp.setY(this.nextCompartmentY);
			pcomp.setWidth(this.compartmentW);
			pcomp.setHeight(this.compartmentH);
			this.nextCompartmentX+=this.compartmentW;
			this.getSelectedModel().addCompartment(pcomp);
			this.notifySBaseAdded(pcomp);
			//System.out.println(pcomp.getName()+" ("+model.getListOfCompartments().size()+")");
			compartmentsNames.put(name,pcomp.getId());
		}
		return pcomp;
	}
	
	/**
	Clears compartmentsNames, sets nextCompartmentY to 100.0, and finds the rightmost compartment and sets nextCompartmentX to be 
	just to the right if it.
	@param model the model to search in.
	 */
	protected void resetCompartments(PluginModel model)
	{
		compartmentsNames.clear();
		//nextCompartmentY = 100.0;
		double maxX = 100.0;
		double maxY = 100.0;
		for(PluginSBase b : model.getListOfCompartments().toArray())
		{
			PluginCompartment comp = (PluginCompartment)b;
			if(comp.getName().length()>0)
				compartmentsNames.put(comp.getName(),comp.getId());
			double rightSide = comp.getX()+comp.getWidth();
			double bottomSide = comp.getY()+comp.getHeight();
			if(rightSide > maxX)
				maxX = rightSide;
			if(bottomSide > maxY)
				maxY = bottomSide;
		}
		nextCompartmentX = maxX;
		nextCompartmentY = maxY;
	}
	
	/**
	Gets the tightest (geometrically smallest) containing compartment in the model of a species alias.
	Checks whether any of the four corners of the species alias is within the bounds of a compartment.
	@param spca the species alias to check for its container
	@param model the model to search in.
	@return the tightest (geometrically smallest) containing compartment in the model of a species alias if one exists. else returns null.
	 */
	protected String getContainingCompartment(PluginSpeciesAlias spca,PluginModel model)
	{
		double spcLeft = spca.getX();
		double spcTop = spca.getY();
		double spcRight = spcLeft+spca.getWidth();
		double spcBottom = spcTop+spca.getHeight();
		
		PluginCompartment rst = null;
		for(PluginSBase b : model.getListOfCompartments().toArray())
		{
			PluginCompartment comp = (PluginCompartment)b;
			//System.out.println("checking "+comp.getName());
			if(rst==null || (comp.getWidth()<rst.getWidth() && comp.getHeight()<rst.getHeight()))
				if(within(spcLeft,spcTop,comp) || within(spcLeft,spcBottom,comp) || within(spcRight,spcTop,comp) || within(spcRight,spcBottom,comp))
				{
					//System.out.println(spca.getName()+" in "+comp.getName());
					rst = comp;
				}
		}
		return rst==null ? UNKNOWN : validateCompartment(rst.getName());
	}
	
	/**
	 * Process or check a compartment name for database-specific rules.  Super class method does nothing.
	 * @param comp the name of the compartment
	 * @return the proper name of the compartment
	 */
	protected String validateCompartment(String comp)
	{
		return comp;
	}
	
	/**
	Check is a 2D point is within the bounds of a compartment
	@param x the horizontal coordinate of the 2D point
	@param y the vertical (top=0.0) coordinate of the 2D point
	@param comp the compartment to check.
	@return true if the 2D point is contained in the compartment. else returns false.
	 */
	protected boolean within(double x,double y,PluginCompartment comp)
	{
		return within(x,y,comp.getX(),comp.getY(),comp.getX()+comp.getWidth(),comp.getY()+comp.getHeight());
	}
	
	/**
	Check is a 2D point is within the bounds of a rectangle defined by its four edges.
	@param x the horizontal coordinate of the 2D point
	@param y the vertical (top=0.0) coordinate of the 2D point
	@param l the left side of the rectangle
	@param t the top side of the rectangle
	@param r the right side of the rectangle
	@param b the bottom side of the rectangle
	@return true if the 2D point is contained in the rectangle. else returns false.
	 */
	protected boolean within(double x,double y,double l, double t,double r, double b)
	{
		return x>l && x<r && y>t && y<b;
	}
	
	private class StringStream extends PrintStream {
		 
	    ByteArrayOutputStream out;
	 
	    public StringStream(ByteArrayOutputStream out) {
	        super(System.out);
	        this.out = out;
	    }
	 
	    public void write(byte buf[], int off, int len) {
	        out.write(buf, off, len);
	    }
	 
	    public void flush() {
	       super.flush();
	    }
	    
	    @Override
	    public String toString()
	    {
	    	return out.toString();
	    }
	} 
	
	/**
	 * A struct for associating a generic pathway database reaction modifier with 
	 * its CellDesigner type and km constant.
	 *
	 */
	protected class ModifierReference
	{
		/**
		 * a generic pathway database reaction modifier
		 */
		public Object modifier;
		
		/**
		 * the modifier's CellDesigner type (use PluginReactionSymbolType constants)
		 * @see PluginReactionSymbolType
		 */
		public String type = PluginReactionSymbolType.CATALYSIS;
		
		/**
		 * the modifier's km constant as it participates in the referencing reaction
		 */
		public Double km;
		public ModifierReference(Object mod,String t,Double k)
		{
			modifier=mod;
			type=t;
			km=k;
		}
		public ModifierReference(Object mod,Double k)
		{
			modifier=mod;
			km=k;
		}
		public ModifierReference(Object mod,String t)
		{
			modifier=mod;
			type=t;
		}
		public ModifierReference(Object mod)
		{
			modifier=mod;
		}
	}


}
