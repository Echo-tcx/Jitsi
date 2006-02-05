/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package net.java.sip.communicator.impl.gui.main.message.menu;

import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import net.java.sip.communicator.impl.gui.main.utils.Constants;
import net.java.sip.communicator.impl.gui.main.utils.ImageLoader;
import net.java.sip.communicator.impl.gui.main.customcontrols.AntialiasedMenuItem;
import net.java.sip.communicator.impl.gui.main.i18n.Messages;
import net.java.sip.communicator.impl.gui.main.utils.AntialiasingManager;

public class EditMenu extends JMenu {
	
	private AntialiasedMenuItem cutMenuItem 
							= new AntialiasedMenuItem(Messages.getString("cut")
									, new ImageIcon(ImageLoader.getImage(ImageLoader.CUT_ICON)));

	private AntialiasedMenuItem copyMenuItem 
							= new AntialiasedMenuItem(Messages.getString("copy")
									, new ImageIcon(ImageLoader.getImage(ImageLoader.COPY_ICON)));
	
	private AntialiasedMenuItem pasteMenuItem 
							= new AntialiasedMenuItem(Messages.getString("paste")
									, new ImageIcon(ImageLoader.getImage(ImageLoader.PASTE_ICON)));

	public EditMenu(){
		
		super(Messages.getString("edit"));
		
		this.add(cutMenuItem);
		this.add(copyMenuItem);
		this.add(pasteMenuItem);
	}
	
	public void paint(Graphics g){
		
		AntialiasingManager.activateAntialiasing(g);
	
		super.paint(g);		
	}
}
