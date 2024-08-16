package com.sage.izpack;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.panels.packs.PacksConsolePanel;
import com.izforge.izpack.util.Console;

public class PacksNewConsolePanel extends PacksConsolePanel {

	private static Logger logger = Logger.getLogger(PacksNewConsolePanel.class.getName());
    private final InstallData installData;

    public PacksNewConsolePanel(PanelView<ConsolePanel> panelView, InstallData installData, Prompt prompt) {
		super(panelView, installData, prompt);
        this.installData = installData;
	}

    @Override
    public boolean run(InstallData installData, Properties properties)
    {
		logger.log(Level.FINE, "PacksNewConsolePanel.run  properties: "+ properties);
		PacksNewPanelAutomationHelper.preselectRequired(this.installData);
        PacksNewPanelAutomationHelper.readInstallationInformation(installData);
    	return super.run(this.installData, properties);
    }
    
    @Override
    public boolean run(InstallData installData, Console console)
    {
		logger.log(Level.FINE, "PacksNewConsolePanel.run  console: "+ console);
    
		PacksNewPanelAutomationHelper.preselectRequired(this.installData);
        PacksNewPanelAutomationHelper.readInstallationInformation(installData);
		return super.run(this.installData, console);
    }
}
