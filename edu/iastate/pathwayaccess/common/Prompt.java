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
package edu.iastate.pathwayaccess.common;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 * This is a handy class for prompting the user for a string or having him select from an ArrayList of strings.
 * @author John Van Hemert
 *
 */
public class Prompt {

	public Prompt() {
		// TODO Auto-generated constructor stub
	}
    
	/**
	Pop up a window to ask the user to select from a list of things.
	@param title the title of the window
	@param message the message to show the user
	@param list a list of things to select from
	@return the String representation of the thing the user selected
	 */
	public static String userSelect(String title,String message,ArrayList list)
    {
		return list.size()>0 ? (String)JOptionPane.showInputDialog(
                null,
                message,
                title,
                JOptionPane.QUESTION_MESSAGE,
                null,
                list.toArray(),
                list.get(0)) : null;
    }
    
	/**
	Pop up a window to show the user a long string.  Returns the text content.
	@param title the title of the window
	@param message the message to show the user
	@param text the text to show the user
	 */
	public static void showText(String title,String message,String text)
    {
		
		JTextArea area = new JTextArea(20, 40);
		area.setText(text);
		JScrollPane pane = new JScrollPane(area);
		int result = JOptionPane.showOptionDialog(
		                 null,
		                 new Object[] {message, pane},
		                 title,
		                 JOptionPane.DEFAULT_OPTION,
		                 JOptionPane.INFORMATION_MESSAGE,
		                 null, null, null);

    }
	
	/**
	Pop up a window to ask the user to enter a string.
	@param title the title of the window
	@param message the message to show the user
	@return the String that the user entered
	 */
	public static String userEnter(String title,String message)
    {
		return (String)JOptionPane.showInputDialog(
                null,
                message,
                title,
                JOptionPane.QUESTION_MESSAGE);
    }
	
	/**
	Pop up a window to ask the user to enter a string that is hidden from view.
	@param title the title of the window
	@return the String that the user entered
	 */
	public static String userEnterPassword(String title)
    {
		JPasswordField tPasswordField = new JPasswordField();
	    tPasswordField.setEchoChar('*');
	    JOptionPane.showMessageDialog ( null, tPasswordField, title, JOptionPane.OK_OPTION );
	    char[] chars = tPasswordField.getPassword();
	    return new String(chars);
    }
	
	/**
	Pop up a window to notify the user of an error.
	@param title the title of the window
	@param message the message to show the user
	 */
	public static void errorMessage(String title,String message)
    {
		JOptionPane.showMessageDialog(
                null,
                message,
                title,
                JOptionPane.ERROR_MESSAGE);
    }
//	public static void main(String[] args)
//	{
//		closeableNote("HELLO");
//		//errorMessage("HI","WORLD");
//	}

	/**
	Pop up a window to notify the user of information.
	Returns the resulting frame so that it can be closed.
	@param title the title of the window
	 */
	public static JFrame closeableNote(String title)
	{
		JFrame f = new JFrame(title);
		f.setPreferredSize(new Dimension(300,150));
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JLabel l = new JLabel(title,SwingConstants.CENTER);
		l.setPreferredSize(f.getPreferredSize());
		f.getContentPane().add(l,BorderLayout.CENTER);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
		f.pack();
		f.paint(f.getGraphics());
		return f;
	}
	
//	public static JFrame taskControllerPopup(String title,ActionListener al)
//	{
//		JFrame f = new JFrame(title);
//		f.setPreferredSize(new Dimension(300,150));
//		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//		JLabel l = new JLabel(title,SwingConstants.CENTER);
//		l.setPreferredSize(f.getPreferredSize());
//		f.getContentPane().add(l,BorderLayout.NORTH);
//		JButton b = new JButton("Cancel");
//		b.addActionListener(al);
//		f.getContentPane().add(b,BorderLayout.SOUTH);
//		f.setLocationRelativeTo(null);
//		f.setVisible(true);
//		f.pack();
//		f.paint(f.getGraphics());
//		return f;
//	}
	

	
	/**
	Pop up a window to notify the user of something.
	@param title the title of the window
	@param message the message to show the user
	 */
	public static void infoMessage(String title,String message)
    {
		JOptionPane.showMessageDialog(
                null,
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE);
    }


	/**
	Pop up a window to confirm a yes/no question.
	@param title the title of the window
	@param message the message to show the user
	@return true if the user selects Yes and false if the user selects No.
	 */
	public static boolean confirm(String title,String message)
    {
		return JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
	
	/**
	Pop up a window to confirm a yes/no question, with a cancel option.
	@param title the title of the window
	@param message the message to show the user
	@return one of JOptionPane.CANCEL_OPTION, JOptionPane.YES_OPTION, or JOptionPane.NO_OPTION
	 */
	public static int confirmOrCancel(String title,String message)
    {
		return JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_CANCEL_OPTION);
    }
	

}
