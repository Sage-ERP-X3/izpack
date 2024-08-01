package com.sage.izpack;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.core.os.RegistryDefaultHandler;

public class InstallTypeNewPanel extends IzPanel implements ActionListener, ListSelectionListener {

	private static final long serialVersionUID = -7778360562175088671L;

	private static Logger logger = Logger.getLogger(InstallTypeNewPanel.class.getName());

	private JRadioButton normalinstall;
	private JRadioButton modifyinstall;
	private DefaultListModel<String> listItems;
	private HashMap<String, String[]> lstCompProps;
	private JList<String> installedComponents;
	private String selectedKey;
	private RegistryHandler registryHandler;

	public InstallTypeNewPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
			RegistryDefaultHandler handler, Log log) {
		super(panel, parent, installData, new IzPanelLayout(log), resources);
		buildGUI();
		this.registryHandler = handler.getInstance();
	}

	private void buildGUI() {

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));

		// We put our components

//        add(LabelFactory.create(parent.langpack.getString("InstallationTypePanel.info"),
//                parent.icons.getImageIcon("history"), LEADING), NEXT_LINE);

//		topPanel.add(LabelFactory.create(ResourcesHelper.getCustomPropString("InstallationTypePanel.info"),
//				ResourcesHelper.getCustomPropString("history"), LEADING));
		// topPanel.add(LabelFactory.create(ResourcesHelper.getCustomPropString("InstallTypeNewPanel.info"),
		// LEADING));
		topPanel.add(LabelFactory.create(super.getString("InstallTypeNewPanel.info"), LEADING));

		topPanel.add(Box.createRigidArea(new Dimension(0, 5)));

		ButtonGroup group = new ButtonGroup();

		// boolean modifyinstallation =
		// Boolean.valueOf(idata.getVariable(InstallData.MODIFY_INSTALLATION));
		boolean modifyinstallation = ModifyInstallationUtil.get(installData);

		// normalinstall = new
		// JRadioButton(parent.langpack.getString("InstallationTypePanel.normal"),
		// !modifyinstallation);
		normalinstall = new JRadioButton(super.getString("InstallTypeNewPanel.normal"), !modifyinstallation);
		normalinstall.addActionListener(this);
		group.add(normalinstall);
		// add(normalinstall, NEXT_LINE);
		topPanel.add(normalinstall);

		// modifyinstall = new
		// JRadioButton(parent.langpack.getString("InstallationTypePanel.modify"),
		// modifyinstallation);
		modifyinstall = new JRadioButton(super.getString("InstallTypeNewPanel.modify"), modifyinstallation);
		modifyinstall.addActionListener(this);
		group.add(modifyinstall);
		// add(modifyinstall, NEXT_LINE);
		topPanel.add(modifyinstall);

		lstCompProps = new HashMap<String, String[]>();

		listItems = new DefaultListModel<String>();
		installedComponents = new JList<String>(listItems);
		installedComponents.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		installedComponents.setLayoutOrientation(JList.VERTICAL);
		installedComponents.setVisibleRowCount(5);
		installedComponents.setEnabled(false);

		JScrollPane listScroller = new JScrollPane(installedComponents);
		listScroller.setPreferredSize(new Dimension(600, 100));
		listScroller.setAlignmentX(LEFT_ALIGNMENT);

		topPanel.add(listScroller);

		add(topPanel, NEXT_LINE);

		setInitialFocus(normalinstall);
		getLayoutHelper().completeLayout();
	}

	/**
	 *
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.izforge.izpack.installer.IzPanel#panelActivate()
	 */
	public void panelActivate() {
		listItems.clear();
		lstCompProps.clear();

		RegistryHandlerX3 helper = new RegistryHandlerX3(this.registryHandler, installData);
		try {
			this.lstCompProps = helper.loadComponentsList();
			for (Map.Entry<String, String[]> pair : this.lstCompProps.entrySet()) {
				listItems.addElement(pair.getKey());
			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error in InstallTypeNewPanel panelActivate: " + e.getMessage());
			e.printStackTrace();
		}

		boolean modifyinstallation = ModifyInstallationUtil.get(installData);

		if (modifyinstallation) {
			modifyinstall.setSelected(true);
			installedComponents.setEnabled(true);

			if (selectedKey != null) {
				if (listItems.contains(selectedKey)) {
					installedComponents.setSelectedValue(selectedKey, true);
				} else {
					if (listItems.size() > 0) {
						installedComponents.setSelectedIndex(0);
					}
				}
			} else {
				if (listItems.size() > 0) {
					installedComponents.setSelectedIndex(0);
				}
			}

		} else {
			normalinstall.setSelected(true);
			installedComponents.setEnabled(false);
		}

	}

	public void actionPerformed(ActionEvent e) {
		logger.log(Level.FINE, "InstallTypeNewPanel installation type changed");
		// Debug.trace("installation type changed");
		if (e.getSource() == normalinstall) {
			logger.log(Level.FINE, "InstallTypeNewPanel normal installation");
			// Debug.trace("normal installation");
			installedComponents.clearSelection();

			installedComponents.setEnabled(false);
			ModifyInstallationUtil.set(installData, Boolean.FALSE);
		} else if (e.getSource() == modifyinstall) {
			logger.log(Level.FINE, "InstallTypeNewPanel modification installation");
			// Debug.trace("modification installation");
			installedComponents.setEnabled(true);
			ModifyInstallationUtil.set(installData, Boolean.TRUE);

			if (selectedKey != null) {
				if (listItems.contains(selectedKey)) {
					installedComponents.setSelectedValue(selectedKey, true);
				} else {
					if (listItems.size() > 0) {
						installedComponents.setSelectedIndex(0);
					}
				}
			} else {
				if (listItems.size() > 0) {
					installedComponents.setSelectedIndex(0);
				}
			}

		} else if (e.getSource() == installedComponents) {

		}

	}

	public String getPathFromSelected() {
		String compPath = null;

		if (installedComponents.getSelectedValue() != null) {
			String key = (String) installedComponents.getSelectedValue();
			String[] compProps = (String[]) lstCompProps.get(key);
			compPath = compProps[1];
		}

		return compPath;

	}

	public boolean isValidated() {
		// we must ensure .installinformation is present if in modification mode
		// then set install_path
		Boolean modifyinstallation = ModifyInstallationUtil.get(installData);

		if (modifyinstallation) {

			String compPath = getPathFromSelected();
			if (compPath == null)
				return false;

			File installationinformation = new File(compPath + File.separator + InstallData.INSTALLATION_INFORMATION);
			if (!installationinformation.exists()) {
				emitError(ResourcesHelper.getCustomPropString("installer.error"),
						ResourcesHelper.getCustomPropString("PathInputPanel.required.forModificationInstallation"));

				return false;
			}

			// TargetPanel..reset();
			// idata.setInstallPath(compPath);
			installData.setInstallPath(compPath);
			selectedKey = (String) installedComponents.getSelectedValue();
		}

		return super.isValidated();
	}

	/**
	 * Asks to make the XML panel data.
	 *
	 * @param panelRoot The tree to put the data in.
	 */
	@Override
	public void createInstallationRecord(IXMLElement panelRoot) {
		new InstallTypeNewPanelAutomation().createInstallationRecord(installData, panelRoot);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.izforge.izpack.installer.IzPanel#getSummaryBody()
	 */
	public String getSummaryBody() {

		// if (Boolean.parseBoolean(idata.getVariable(InstallData.MODIFY_INSTALLATION)))
		if (ModifyInstallationUtil.get(installData)) {
			// return ResourcesHelper.getCustomPropString("InstallTypeNewPanel.modify");
			return super.getString("InstallTypeNewPanel.modify");
			// return parent.langpack.getString("InstallationTypePanel.modify");
		} else {
			return super.getString("InstallTypeNewPanel.normal");
			// return ResourcesHelper.getCustomPropString("InstallTypeNewPanel.normal");
			// return parent.langpack.getString("InstallationTypePanel.normal");
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		selectedKey = (String) installedComponents.getSelectedValue();

	}

}
