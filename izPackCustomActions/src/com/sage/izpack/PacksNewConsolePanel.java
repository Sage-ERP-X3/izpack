package com.sage.izpack;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.panels.packs.PacksConsolePanel;
import com.izforge.izpack.util.Console;

public class PacksNewConsolePanel extends PacksConsolePanel {

	private static Logger logger = Logger.getLogger(PacksNewConsolePanel.class.getName());
	
	public PacksNewConsolePanel(PanelView<ConsolePanel> panelView, InstallData installData, Prompt prompt) {
		super(panelView, installData, prompt);
	}

    @Override
    public boolean run(InstallData installData, Properties properties)
    {
		logger.log(Level.FINE, "PacksNewConsolePanel.run  properties: "+ properties);
    	return super.run(installData, properties);
    }
    
    @Override
    public boolean run(InstallData installData, Console console)
    {
		logger.log(Level.FINE, "PacksNewConsolePanel.run  console: "+ console);
    
		for (Pack p : installData.getAvailablePacks()) {

			logger.info("PacksNewConsolePanel.createPacksTable - Pack " + p.getName() + " Required: " + p.isRequired()
					+ " Preselected: " + p.isPreselected());

			if (p.isRequired()) {
				p.setPreselected(true);
			}
		}
		return super.run(installData, console);
    }

}
