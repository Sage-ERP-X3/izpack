package com.sage.izpack;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.panels.packs.PacksModel;
import com.izforge.izpack.panels.packs.PacksPanel;

/*
* @author Franck DEPOORTERE
*/
public class PacksNewPanel extends PacksPanel {

	private static final long serialVersionUID = 2809544763635023846L;

	private static final Logger logger = Logger.getLogger(PacksNewPanel.class.getName());

	/**
	 * The packs messages.
	 */

	public PacksNewPanel(Panel panel, InstallerFrame frame, GUIInstallData installData, Resources resources,
			ObjectFactory factory, RulesEngine rules) {
		super(panel, frame, installData, resources, factory);
	}

	/**
	 * Creates the table for the packs. All parameters are required. The table will
	 * be returned.
	 *
	 * @param width       of the table
	 * @param scroller    the scroller to be used
	 * @param layout      layout to be used
	 * @param constraints constraints to be used
	 * @return the created table
	 */
	@Override
	protected JTable createPacksTable(int width, JScrollPane scroller, GridBagLayout layout,
			GridBagConstraints constraints) {
		logger.log(Level.FINE, "PacksNewPanel.createPacksTable");

		// List<Pack> selectedPacks = new LinkedList<Pack>();
		for (Pack p : this.installData.getAvailablePacks()) {

			if (p.isRequired()) {
				// selectedPacks.add(p);
				p.setHidden(false);
				p.setPreselected(true);
			}

			logger.log(Level.FINE, "PacksNewPanel.createPacksTable - Pack " + p.getName() + " Required: "
					+ p.isRequired() + " Preselected: " + p.isPreselected());
		}

//		this.installData.setSelectedPacks(selectedPacks);

		JTable table = super.createPacksTable(width, scroller, layout, constraints);

		logger.log(Level.FINE, "PacksNewPanel.createPacksTable constraints: " + constraints + " ");
		/*
		 * Pack pack = this.packsModel.getPackAtRow(0); if (pack != null &&
		 * !pack.isPreselected()) pack.setPreselected(true);
		 * 
		 * this.packsModel.updateTable();
		 */
		return table;

	}

	/**
	 * Called when the panel becomes active. If a derived class implements this
	 * method also, it is recommended to call this method with the super operator
	 * first.
	 */

	@Override
	public void panelActivate() {
		logger.log(Level.FINE, "PacksNewPanel.panelActivate");
		// this.packsModel.updateTable();

		if (installData.getSelectedPacks().isEmpty()) {
			logger.log(Level.FINE, "PacksNewPanel.panelActivate : getSelectedPacks().isEmpty()");

			List<Pack> selectedPacks = new LinkedList<Pack>();
			for (Pack p : installData.getAvailablePacks()) {
				if (p.isRequired()) {
					selectedPacks.add(p);
					logger.log(Level.FINE, "PacksNewPanel.panelActivate : add selectedPacks:" + p.getName());
				}
			}

			logger.log(Level.FINE, "PacksNewPanel.panelActivate : selectedPacks:" + selectedPacks);
			installData.setSelectedPacks(selectedPacks);

			// parent.lockNextButton();
		} else {
			logger.log(Level.FINE,
					"PacksNewPanel.panelActivate : getSelectedPacks() : " + installData.getSelectedPacks());
			parent.unlockNextButton();
		}

		super.panelActivate();

		if (installData.getSelectedPacks().isEmpty()) {
			logger.log(Level.FINE, "PacksNewPanel.panelActivate2 : getSelectedPacks().isEmpty()");

			List<Pack> selectedPacks = new LinkedList<Pack>();
			for (Pack p : installData.getAvailablePacks()) {
				if (p.isRequired()) {
					selectedPacks.add(p);
					logger.log(Level.FINE, "PacksNewPanel.panelActivate2 : add selectedPacks:" + p.getName());
				}
			}

			logger.log(Level.FINE, "PacksNewPanel.panelActivate2 : selectedPacks:" + selectedPacks);
			installData.setSelectedPacks(selectedPacks);

			if (!installData.getSelectedPacks().isEmpty()) {
				parent.unlockNextButton();
			}
			
		} else {
			logger.log(Level.FINE,
					"PacksNewPanel.panelActivate2 : getSelectedPacks() : " + installData.getSelectedPacks());
			parent.unlockNextButton();
		}

		Map<String, Pack> installedpacks = packsModel.getInstalledPacks();
		CheckBoxNewRenderer packSelectedRenderer = new CheckBoxNewRenderer();
		packsTable.getColumnModel().getColumn(0).setCellRenderer(packSelectedRenderer);

		logger.log(Level.FINE, "PacksNewPanel.panelActivate2 : installedpacks : " + installedpacks);

	}

	/**
	 * Indicates whether the panel has been validated or not.
	 *
	 * @return true if the needed space is less than the free space, else false
	 */
	@Override
	public boolean isValidated() {
		logger.log(Level.FINE, "PacksNewPanel.isValidated : ");

		boolean isValidated = super.isValidated();
		logger.log(Level.FINE, "PacksNewPanel.isValidated : " + isValidated);

		return isValidated;
	}

	static class CheckBoxNewRenderer implements TableCellRenderer {
		JCheckBox checkbox = new JCheckBox();

		CheckBoxNewRenderer() {
			if (com.izforge.izpack.util.OsVersion.IS_UNIX && !com.izforge.izpack.util.OsVersion.IS_OSX) {
				checkbox.setIcon(new LFIndependentIcon());
				checkbox.setDisabledIcon(new LFIndependentIcon());
				checkbox.setSelectedIcon(new LFIndependentIcon());
				checkbox.setDisabledSelectedIcon(new LFIndependentIcon());
			}
			checkbox.setHorizontalAlignment(CENTER);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			logger.log(Level.FINE, "PacksNewPanel.CheckBoxNewRenderer : isSelected: " + isSelected);

			if (isSelected) {
				checkbox.setForeground(table.getSelectionForeground());
				checkbox.setBackground(table.getSelectionBackground());
			} else {
				checkbox.setForeground(table.getForeground());
				checkbox.setBackground(table.getBackground());
			}

			PacksModel.CbSelectionState state = (PacksModel.CbSelectionState) value;

			logger.log(Level.FINE,
					"PacksNewPanel.CheckBoxNewRenderer : state: " + state + " state.isSelectedOrRequiredSelected() : "
							+ state.isSelectedOrRequiredSelected() + "  state.isChecked() : " + state.isChecked()
							+ "  value: " + value);

			if (state == PacksModel.CbSelectionState.DEPENDENT_DESELECTED) {
				// condition not fulfilled
				checkbox.setForeground(Color.GRAY);
				logger.log(Level.FINE, "PacksNewPanel.CheckBoxNewRenderer :  condition not fulfilled");
			}
			if (state == PacksModel.CbSelectionState.REQUIRED_PARTIAL_SELECTED) {
				logger.log(Level.FINE, "PacksNewPanel.CheckBoxNewRenderer :  setSelected");
				checkbox.setForeground(Color.RED);
				checkbox.setSelected(true);
			}

			if (state != null) {
				logger.log(Level.FINE,
						"PacksNewPanel.CheckBoxNewRenderer :  setSelected? : " + (value != null && state.isChecked())); // isSelectedOrRequiredSelected()));

				checkbox.setEnabled(state.isSelectable());
				checkbox.setSelected((value != null && state.isChecked())); // isSelectedOrRequiredSelected()));
			}
			return checkbox;
		}
	}

}
