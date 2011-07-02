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
    
    This code was edited or generated using CloudGarden's Jigloo
	SWT/Swing GUI Builder, which is free for non-commercial
	use. If Jigloo is being used commercially (ie, by a corporation,
	company or business for any purpose whatever) then you
	should purchase a license for each developer using Jigloo.
	Please visit www.cloudgarden.com for details.
	Use of Jigloo implies acceptance of these licensing terms.
	A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
	THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
	LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
package edu.iastate.pathwayaccess.celldesigner;
import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.ProgressMonitor;

import javax.swing.WindowConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.*;

import edu.iastate.pathwayaccess.PathwayAccessPlugin.CommitProgressMonitor;

import jp.sbi.celldesigner.plugin.CellDesignerPlugin;


/**
 * This is how the user selects which Pathways to download. 
 * @author John Van Hemert
*/
public class SelectPathwaysFrame extends javax.swing.JFrame implements ListSelectionListener,PropertyChangeListener {
	protected JSplitPane jSplitPane1;
	protected JScrollPane orgScrollPane;
	protected JList pwList;
	protected JScrollPane pwScrollPane;
	protected JList orgList;
	
	/**
	 * A sorted map from what you display to the user in the selectable list, to some kind of ID or the pathway objects themselves.
	 */
	protected TreeMap pwMap;
	
	/**
	 * A sorted map from what you display to the user in the selectable list, to some kind of ID or the organism objects themselves.
	 */
	protected TreeMap orgMap;
	
	protected JButton importButton,cancelButton;
	protected JPanel upper;
	protected JPanel middle;
	protected JPanel lower;
	
	protected Checkbox synBox;
	
	private JProgressBar progressBar;
	
	/**
	 * The PathwayAccessPlugin associated with this SelectPathwaysFrame;
	 */
	protected PathwayAccessPlugin plugin;
	
	/**
	 * A reference to myself used in subclasses.
	 */
	protected SelectPathwaysFrame f;
	
	public SelectPathwaysFrame(PathwayAccessPlugin plug) {
		super();
		
		plugin = plug;
		f = this;
		
		this.initGUI();
		ListOrganismsThread t = new ListOrganismsThread(orgMap,progressBar);
		TimeoutChecker toc = new TimeoutChecker(t,this);
    	toc.start();
		
	}
	
//	public void dispose()
//	{
//		System.out.println("cancelling if necessary...");
//		if(cancelButton.isVisible()) cancelButton.doClick();
//		System.out.println("canceled");
//		super.dispose();
//	}
	
	/**
	 * Shows the window.  Should't need to be changed, but feel free.
	 */
	protected void initGUI()
	{
		try
		{			
			setVisible(false);
			this.setTitle(plugin.getClass().getName()+": Select Pathway(s)");
			
			setLocationRelativeTo(null);
			this.setAlwaysOnTop(true);
			
			BorderLayout thisLayout = new BorderLayout();
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			getContentPane().setLayout(thisLayout);
			{
				
			
				upper = new JPanel();
				upper.setLayout(new GridLayout(1,2));
				getContentPane().add(upper, BorderLayout.NORTH);
				{		
					
					
					//statusLabel = new JLabel("");
					//upperLeft.add(statusLabel,BorderLayout.WEST);
					

					
					JLabel orgLabel = new JLabel("Organisms:");
					orgLabel.setFont(orgLabel.getFont().deriveFont(orgLabel.getFont().getStyle() ^ Font.BOLD));
					upper.add(orgLabel);
					JLabel pwyLabel = new JLabel("Pathways:");
					pwyLabel.setFont(pwyLabel.getFont().deriveFont(pwyLabel.getFont().getStyle() ^ Font.BOLD));
					upper.add(pwyLabel);
					
				}
				
				
				middle = new JPanel();
				BorderLayout jPanel1Layout = new BorderLayout();
				middle.setLayout(jPanel1Layout);
				getContentPane().add(middle, BorderLayout.CENTER);
				middle.setPreferredSize(new java.awt.Dimension(600, 400));
				{
					jSplitPane1 = new JSplitPane();
					middle.add(jSplitPane1, BorderLayout.CENTER);
					{
						orgScrollPane = new JScrollPane();
						orgScrollPane.setPreferredSize(new java.awt.Dimension(300, 400));
						jSplitPane1.add(orgScrollPane, JSplitPane.LEFT);
						{
							//ListModel orgListModel = new DefaultComboBoxModel(new String[] {"Loading organism names..."});
							orgList = new JList();
							orgScrollPane.setViewportView(orgList);
							//orgList.setModel(orgListModel);
							orgList.addListSelectionListener(this);
							
						}
					}
					{
						pwScrollPane = new JScrollPane();
						jSplitPane1.add(pwScrollPane, JSplitPane.RIGHT);
						{
							ListModel pwListModel = new DefaultComboBoxModel();
							pwList = new JList();
							pwScrollPane.setViewportView(pwList);
							pwList.setModel(pwListModel);
						}
					}
				}
			}

			
			
				lower = new JPanel();
				lower.setLayout(new GridLayout(2,1));
				getContentPane().add(lower,BorderLayout.SOUTH);
				{
					JPanel buttonsPanel = new JPanel();
					buttonsPanel.setLayout(new GridLayout(1,2));
					lower.add(buttonsPanel);
					
					JPanel cancelPanel = new JPanel();
					buttonsPanel.add(cancelPanel);
					cancelButton = new JButton();
					cancelPanel.add(cancelButton);
					cancelButton.setText("Cancel");
					cancelButton.setEnabled(false);
					
					JPanel importPanel = new JPanel();
					buttonsPanel.add(importPanel);
					importButton = new JButton();
					importButton.setText("Import Selected Pathways");
					importPanel.add(importButton,BorderLayout.SOUTH);
					synBox = new Checkbox();
					synBox.setLabel("Use Synonyms");
					synBox.setFont(importButton.getFont());
					synBox.setState(true);
					importPanel.add(synBox,BorderLayout.NORTH);
					//importPanel.add(new JLabel("Use Synonyms"),BorderLayout.NORTH);
					
					JPanel progressPanel = new JPanel();
					progressPanel.setBackground(plugin.myColor);
					lower.add(progressPanel);
					progressBar = new JProgressBar(0, 100);
					Dimension d = progressBar.getPreferredSize();
					d.width = middle.getPreferredSize().width;
					progressBar.setPreferredSize(d);
					progressBar.setValue(0);
					progressBar.setStringPainted(true);
					progressBar.setBackground(plugin.myColor);
					//progressBar.setIndeterminate(true);
					progressBar.setString("");
					progressPanel.add(progressBar);
				}
			
			
			importButton.addActionListener(new ImportListener(this));
			setSize(400, 300);
			pack();
			setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	/**
	 * Check if the user wants to search synonyms when searching for existing species.
	 * @return true if the user wants to search synonyms when searching for existing species.
	 * This means before creating a new species, all plugins check if a new object's name or synonyms 
	 * match any of the stored names or synonyms for all species already in the current model (of the same 
	 * type and loction as well).  If there is a match, the (greedily found) matching species is referenced 
	 * instead of creating a new species.  false if the user wants to use only species and object names for the searches.
	 */
	public boolean getUseSynonyms()
	{
		return synBox.getState();
	}
	
	class ImportListener implements java.awt.event.ActionListener
	{
		SelectPathwaysFrame frame;
		public ImportListener(SelectPathwaysFrame f)
		{
			frame = f;
		}
		public void actionPerformed(java.awt.event.ActionEvent e)
		{
			ImportThread t = new ImportThread(pwMap);
			TimeoutChecker toc = new TimeoutChecker(t,frame);
			toc.start();
		}
	}
	
	/**
	 * Set the text and value of the progress bar.
	 * Use this to update the user as pathways members are downloaded.
	 * @param status a string to display in the progress bar.
	 * @param value the value to set the progress bar to.
	 */
	public void updateStatus(String status,int value)
	{
		//statusLabel.setText(status);
		progressBar.setString(status);
	}
	
	/**
	 * To avoid locking the screen when downloading, the download process is threaded.
	 */
    class ImportThread extends Thread
    {
    	TreeMap pwMap;
    	
    	ImportThread(TreeMap pm)
    	{
    		pwMap = pm;
        }

        public void run() 
        {
        	
        	importPathways();
        	//f.dispose();
        	f.setAlwaysOnTop(false);
        	//f.updateStatus("Download Complete. Model now has "+plugin.getSelectedModel().getNumReactions()+" reactions and "+plugin.getSelectedModel().getNumSpecies()+" species",0);
        	f.updateStatus("Download Complete. Apply an automatic layout using the CellDesigner Layout menu.",0);
        	//Prompt.infoMessage(plugin.getClass().getName()+" Download Complete","Model now has "+plugin.getSelectedModel().getNumReactions()+" reactions and "+plugin.getSelectedModel().getNumSpecies()+" species");
        	f.setAlwaysOnTop(true);
//        	try{
//				if(!PathwayAccessAction.download.hasQueuedThreads())
//	        	{
//
//					try
//					{
//						plugin.doCircularLayout();
//					}
//					catch(NoSuchMethodError e)
//					{
//						Robot rob = new Robot();
//						rob.keyPress(KeyEvent.VK_ALT);
//						rob.keyPress(KeyEvent.VK_SHIFT);
//						rob.keyPress(KeyEvent.VK_H);
//						rob.keyRelease(KeyEvent.VK_ALT);
//						rob.keyRelease(KeyEvent.VK_SHIFT);
//						rob.keyRelease(KeyEvent.VK_H);
//					}
//	        	}
//			}catch(Exception ex){ex.printStackTrace();}
        }
    }
    
	/**
	 * To avoid locking the screen when listing pathways, the process is threaded.
	 */
    class ListPathwaysThread extends Thread
    {
    	TreeMap pwMap;
    	
    	ListPathwaysThread(TreeMap pm)
    	{
    		pwMap = pm;
        }

        public void run() 
        {
        	pwMap = populatePwMap(orgMap.get(orgList.getSelectedValue()));
    		Set<String> keys = pwMap.keySet();
    		Object[] keysArray = keys.toArray();
    		pwList.setModel(new DefaultComboBoxModel(keysArray));
        }
    }
    
	/**
	 * To avoid locking the screen when listing pathways, the process is threaded.
	 */
    class ListOrganismsThread extends Thread
    {
    	TreeMap orgMap;
    	
    	ListOrganismsThread(TreeMap om,JProgressBar pb)
    	{
    		orgMap = om;
        }

        public void run() 
        {
        	orgMap = populateOrgMap(orgMap);
        }
    }
    
	/**
	 * This thread wraps any other thread used for connection operations 
	 * so that the user can cancel an operation if it is taking too long.
	 * Since the operations that would cause a delay or freeze-up are 
	 * deep within datasource api's and beyond the scope of this object,
	 * we must use the Thread.stop() method to cancel an operation.  This 
	 * is known as poor practice, but it is generally safe for this application, 
	 * but not guaranteed to prevent deadlock.
	 */
    class TimeoutChecker extends Thread implements ActionListener
    {
    	Thread subject;
    	SelectPathwaysFrame frame;
    	TimeoutChecker(Thread s,SelectPathwaysFrame f)
    	{
    		subject = s;
    		cancelButton.addActionListener(this);
    		cancelButton.setEnabled(true);
    		frame = f;
    	}
    	public void run()
    	{
    		importButton.setEnabled(false);
    		synBox.setEnabled(false);
    		subject.start();
    		try {
				subject.join();
				cancelButton.setEnabled(false);
			} catch (InterruptedException e) {}
			finally
			{
				importButton.setEnabled(true);
				synBox.setEnabled(true);
			}
    	}
		public synchronized void actionPerformed(ActionEvent e)
		{
			if(subject != null && subject.isAlive())
				subject.stop();
			cancelButton.setEnabled(false);
			importButton.setEnabled(true);
    		synBox.setEnabled(true);
			System.setProperty("DOWNLOADING","0");
			frame.updateStatus("Canceled", 0);
		}
    }

	
	public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting())
            return;

        if((JList)e.getSource() == orgList)
        {
	        if (orgList.isSelectionEmpty())
	        {
	            pwList.clearSelection();
	        }
	        else
	        {
	        	//String note[]={"Loading pathway names..."};
	        	//pwList.setModel(new DefaultComboBoxModel(note));
	        	//System.out.println(pwList.getModel().getElementAt(0));
	        	ListPathwaysThread t = new ListPathwaysThread(pwMap);
	        	TimeoutChecker toc = new TimeoutChecker(t,this);
	        	toc.start();
	        }
        }
    }
	
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            //progressBar.setValue(progress);
        } 
    }

	/**
	 * Internal list populating process.
	 */
	protected TreeMap populateOrgMap(TreeMap orgMap)
	{
		this.updateStatus("Loading organism names", 0);
		orgMap = new TreeMap<String,Object>(String.CASE_INSENSITIVE_ORDER);
		for(Object o : plugin.getOrganisms())
		{
			orgMap.put(plugin.getName(o),plugin.getId(o));
		}
		this.orgMap = orgMap;
		orgList.setModel(new DefaultComboBoxModel(orgMap.keySet().toArray()));
		this.updateStatus(orgMap.size()+" organisms", 0);
		return orgMap;
	}
	
	/**
	 * Internal list populating process.
	 */
	protected TreeMap populatePwMap(Object orgId)
	{
		this.updateStatus("Loading pathway names", 0);
		pwMap = new TreeMap<String,Object>(String.CASE_INSENSITIVE_ORDER);
		Iterable pwys = plugin.getPathwaysFromOrganismId(orgId);
//		int c = 0;
//		for(Object pwy : pwys) c++;
//		progressBar.setMaximum(c);
		for(Object pwy : pwys)
		{
			pwMap.put(plugin.getName(pwy), plugin.getId(pwy));
		}
		this.updateStatus(pwMap.size()+" pathways", 0);
		return pwMap;
	}
	
	/**
	 * Internal download process.
	 */
	protected void importPathways()
	{
		ArrayList selectedPwIds = new ArrayList();
		for(int i=0; i<pwList.getSelectedIndices().length; i++)
		{
			Object id = pwMap.get(pwList.getSelectedValues()[i]);
			selectedPwIds.add(id);
		}
		
		plugin.download(selectedPwIds,this);
	}

}
