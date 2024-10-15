package com.sage.izpack;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;

public class ReadMeNewConsolePanel extends com.izforge.izpack.panels.info.InfoConsolePanel {

	private static Logger logger = Logger.getLogger(ReadMeNewConsolePanel.class.getName());
	private final Resources resources;
	private final InstallData installData;
	private static final long serialVersionUID = 8430577231809871722L;

	public ReadMeNewConsolePanel(InstallData installData, Resources resources, PanelView<ConsolePanel> panel) {
		super(resources, panel);

		this.installData = installData;
		this.resources = resources;

		logger.log(Level.FINE, "ReadMeNewConsolePanel instance.");
	}



}
