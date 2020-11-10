package com.sage.izpack;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.panels.checkedhello.CheckedHelloConsolePanel;

public class CheckedHelloNewConsolePanel extends CheckedHelloConsolePanel {

	private static Logger logger = Logger.getLogger(CheckedHelloNewConsolePanel.class.getName());

	public CheckedHelloNewConsolePanel(RegistryDefaultHandler handler, InstallData installData, Prompt prompt,
			PanelView<ConsolePanel> panel) throws NativeLibException {
		super(handler, installData, prompt, panel);
		// TODO Auto-generated constructor stub
	}
	

}
