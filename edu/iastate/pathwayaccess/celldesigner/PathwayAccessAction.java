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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import jp.sbi.celldesigner.plugin.PluginAction;
import jp.sbi.celldesigner.plugin.PluginListOf;
import jp.sbi.celldesigner.plugin.PluginModel;

/**
 * The PluginAction implementation for PathwayAccess plugins.
 * @author jlv
 *
 */
public class PathwayAccessAction extends PluginAction {

	/**
	 * The PathwayAccessPlugin associated with this PluginAction
	 */
	private PathwayAccessPlugin plugin;
	
	/**
	 * A semaphore used to force multiple PathwayAccessPlugins to queue 
	 * for the opportunity to download (a) pathway(s).  If multiple 
	 * plugins were allowed to download simultaneously, very bad
	 * things would happen.
	 */
	public static Semaphore download = new Semaphore(1);
	
	/**
	To store unique hash signatures of each reaction in a model and store them in a static 
	data structure associated with PathwayAccessAction.  PathwayAccessAction must be used 
	to communicate between PathwayAccessPlugins because it is the only class that is 
	visible to all PathwayAccessPlugins and uses the same class loader (static variables 
	are independent between different class loaders).  This is used to prevent future redundant reactions.
	 */
	//public static HashMap<Integer,HashMap<Integer,String>> reactions = new HashMap<Integer,HashMap<Integer,String>>();
	
	public PathwayAccessAction(PathwayAccessPlugin p) {
		plugin = p;
	}
	
//	class TaskController extends Thread implements ActionListener
//	{
//		Thread subject;
//		public TaskController(Thread s)
//		{
//			subject = s;
//		}
//		public void run()
//		{
//			subject.start();
//			try {
//				subject.join();
//			} catch (InterruptedException e) {}
//		}
//		public void actionPerformed(ActionEvent e) 
//		{
//			subject.stop();
//		}
//	}
//	
//	class ConnectThread extends Thread
//	{
//		PathwayAccessPlugin plugin;
//		public ConnectThread(PathwayAccessPlugin p)
//		{
//			plugin = p;
//		}
//		public void run()
//		{
//			plugin.connect();
//		}
//	}
//	class LoginThread extends Thread
//	{
//		PathwayAccessPlugin plugin;
//		public LoginThread(PathwayAccessPlugin p)
//		{
//			plugin = p;
//		}
//		public void run()
//		{
//			plugin.login();
//		}
//	}

	public void myActionPerformed(ActionEvent e) {
		
		if(e.getActionCommand().equals(plugin.HILITE))
		{
			plugin.hilite();
			return;
		}
		if(e.getActionCommand().equals(plugin.SETCOLOR))
		{
			Color newColor = JColorChooser.showDialog(null,"Set highlight color for "+plugin.getClass().getName(),plugin.myColor);
			if(newColor!=null)
				plugin.myColor = newColor;
			return;
		}
		if(!plugin.connected || e.getActionCommand().equals(plugin.CONNECT))
		{
//			TaskController tc = new TaskController(new ConnectThread(plugin));
//			JFrame dia = Prompt.taskControllerPopup("Connecting...",tc);
//			tc.start();
//			try {
//				tc.join();
//			} catch (InterruptedException e1) {}
//			dia.dispose();
			plugin.connect();
			if(plugin.connected) Prompt.infoMessage("Connection Successful", plugin.getClass().getName()+" connected.");
    		else Prompt.errorMessage("Connection failed", plugin.getClass().getName()+" not connected.");
		}
		if(!plugin.loggedIn || e.getActionCommand().equals(plugin.LOGIN))
    	{
//			TaskController tc = new TaskController(new LoginThread(plugin));
//			JFrame dia = Prompt.taskControllerPopup("Logging in...",tc);
//			tc.start();
//			try {
//				tc.join();
//			} catch (InterruptedException e1) {}
//    		dia.dispose();
			plugin.login();
    		if(plugin.loggedIn) Prompt.infoMessage("Login Successful", plugin.getClass().getName()+" logged in.");
    		else Prompt.errorMessage("Login failed", plugin.getClass().getName()+" not logged in.");
    	}
       	if(e.getActionCommand().equals(plugin.DOWNLOAD))
    	{
       		try
       		{
       			if(plugin.loggedIn)
       			{
       				new SelectPathwaysFrame(plugin);
       			}
       		}
       		catch(Exception ex)
       		{
       			ex.printStackTrace();
       			Prompt.errorMessage("Download Problem", "There was a problem downloading pathways. \n"+ex.getClass().getName()+": "+ex.getMessage());
       		}
    	}
		if(e.getActionCommand().equals(plugin.COMMIT))
    	{
			if(plugin.loggedIn)
				plugin.commit();
    	}


	}

}
