
package com.izforge.izpack.panels.sage;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.installer.ResourceManager;

public class ReadMePanel extends IzPanel
		implements HyperlinkListener, ActionListener {

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
	 * @param idata
	 *            The installation data.
	 * @param parent
	 *            Description of the Parameter
	 */
	public ReadMePanel(InstallerFrame parent, InstallData idata) {
		super(parent, idata, new IzPanelLayout());
		// We load the licence
		loadReadMe();

		// We put our components

		add(LabelFactory.create(parent.langpack.getString("ReadMePanel.info"),
				parent.icons.getImageIcon("history"), LEADING), NEXT_LINE);
		try {
			textArea = new JEditorPane();
			textArea.setEditable(false);
			textArea.addHyperlinkListener(this);
			JScrollPane scroller = new JScrollPane(textArea);
			textArea.setPage(loadReadMe());
			add(scroller, NEXT_LINE);
		} catch (Exception err) {
			err.printStackTrace();
		}

		ButtonGroup group = new ButtonGroup();

		readCheckBox = new JCheckBox(
				parent.langpack.getString("ReadMePanel.agree"), false);
		group.add(readCheckBox);
		add(readCheckBox, NEXT_LINE);
		readCheckBox.addActionListener(this);

		setInitialFocus(textArea);
		getLayoutHelper().completeLayout();
	}

	/**
	 * Actions-handling method (here it launches the installation).
	 *
	 * @param e
	 *            The event.
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
	 * @param e
	 *            The event.
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
			// TODO: Extend exception handling.
		}
	}

	/**
	 * Indicates wether the panel has been validated or not.
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
		String resNamePrifix = "ReadMePanel.readme";
		try {
			return ResourceManager.getInstance().getURL(resNamePrifix);
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
