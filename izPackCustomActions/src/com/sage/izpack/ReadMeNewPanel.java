package com.sage.izpack;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;

/**
 * Readme Panel: Html page + Checkbox to check
 *
 * @author Franck DEPOORTERE
 *
 */
public class ReadMeNewPanel extends IzPanel implements HyperlinkListener, ActionListener {

	private static final Logger logger = Logger.getLogger(ReadMeNewPanel.class.getName());

	/**
	 *
	 */
	private static final long serialVersionUID = 3256745385458746416L;

	/**
	 * The checkbox.
	 */
	private JCheckBox readCheckBox;

	/**
	 * The text area.
	 */
	private JEditorPane textArea;

	/**
	 * The constructor.
	 *
	 * @param idata  The installation data.
	 * @param parent Description of the Parameter
	 */
	public ReadMeNewPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
			Log log) {
		super(panel, parent, installData, new IzPanelLayout(log), resources);
		// We load the licence
		loadReadMe();

		// We put our components

		add(LabelFactory.create(super.getString("ReadMeNewPanel.info"), LEADING));

		// add(LabelFactory.create(parent.langpack.getString("ReadMeNewPanel.info"),
		// parent.icons.getImageIcon("history"), LEADING), NEXT_LINE);
		try {
			textArea = new JEditorPane();
			textArea.setEditable(false);
			textArea.addHyperlinkListener(this);
			JScrollPane scroller = new JScrollPane(textArea);
			textArea.setPage(loadReadMe());
			add(scroller, NEXT_LINE);

			ButtonGroup group = new ButtonGroup();
			readCheckBox = new JCheckBox();
			String agree = getString("ReadMeNewPanel.agree");
			readCheckBox.setText(agree);
			group.add(readCheckBox);
			add(readCheckBox, NEXT_LINE);
			readCheckBox.addActionListener(this);

		} catch (Exception err) {
			logger.log(Level.WARNING, "ReadMeNewPanel :" + err.getMessage());
			err.printStackTrace();
		}
		setInitialFocus(textArea);
		getLayoutHelper().completeLayout();
	}

	/**
	 * Actions-handling method (here it launches the installation).
	 *
	 * @param e The event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (readCheckBox.isSelected()) {
			parent.unlockNextButton();
		} else {
			parent.lockNextButton();
		}
	}

	/**
	 * Hyperlink events handler.
	 *
	 * @param e The event.
	 */
	@Override
	public void hyperlinkUpdate(HyperlinkEvent e) {
		try {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				try {
					Desktop.getDesktop().browse(e.getURL().toURI());
				} catch (Exception err) {
					textArea.setPage(e.getURL());
				}
			}
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	/**
	 * Indicates whether the panel has been validated or not.
	 *
	 * @return true if the user agrees with the license, false otherwise.
	 */
	@Override
	public boolean isValidated() {
		if (!readCheckBox.isSelected()) {
			parent.exit();
			return false;
		}
		return (readCheckBox.isSelected());
	}

	/**
	 * Loads the license text.
	 *
	 * @return The license text URL.
	 */
	private URL loadReadMe() {
		String resNamePrifix = "ReadMeNewPanel.readme";
		try {
			logger.log(Level.FINE, "ReadMeNewPanel loadReadme: " + resNamePrifix);
			URL url = getResources().getURL(resNamePrifix);
			String debug = "NULL";
			if (url != null) {
				debug = url.getPath();
			}
			logger.log(Level.FINE, "ReadMeNewPanel loadReadme return " + debug);
			return url;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * Called when the panel becomes active.
	 */
	@Override
	public void panelActivate() {
		if (!readCheckBox.isSelected()) {
			parent.lockNextButton();
		}
	}
}
