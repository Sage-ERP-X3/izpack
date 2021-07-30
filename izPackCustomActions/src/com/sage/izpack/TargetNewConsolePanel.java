package com.sage.izpack;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.panels.target.TargetConsolePanel;
import com.izforge.izpack.util.Console;

public class TargetNewConsolePanel extends TargetConsolePanel {

	private static Logger logger = Logger.getLogger(TargetNewConsolePanel.class.getName());

	
	public TargetNewConsolePanel(PanelView<ConsolePanel> panel, InstallData installData, Prompt prompt) {
		super(panel, installData, prompt);
	}


    @Override
    public boolean run(InstallData installData, Properties properties)
    {
    	logger.log(Level.FINE, "TargetNewConsolePanel.run  properties: " + properties);
    	return super.run(installData, properties);
    }
    
    @Override
    public boolean run(InstallData installData, Console console)
    {
    	logger.log(Level.FINE, "TargetNewConsolePanel.run  console: " + console);
    	return super.run(installData, console);
    }
}
