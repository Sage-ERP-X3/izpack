package com.sage.izpack;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.panels.checkedhello.CheckedHelloConsolePanel;
import com.izforge.izpack.panels.checkedhello.RegistryHelper;
import com.izforge.izpack.util.Console;


public class CheckedHelloNewConsolePanel extends CheckedHelloConsolePanel {

	private static Logger logger = Logger.getLogger(CheckedHelloNewConsolePanel.class.getName());
    private final RegistryHelper registryHelper;
    /**
     * The prompt.
     */
    private final Prompt prompt;
    /**
     * Determines if the application is already installed.
     */
    private boolean registered;
    private InstallData installData;

    
	public CheckedHelloNewConsolePanel(RegistryDefaultHandler handler, InstallData installData, Prompt prompt,
			PanelView<ConsolePanel> panel) throws NativeLibException {
		super(handler, installData, prompt, panel);
        registryHelper = new RegistryHelper(handler, installData);
        this.prompt = prompt;
        registered = registryHelper.isRegistered();
        this.installData = installData;

	}
	
	 /**
     * Runs the panel using the specified console.
     *
     * @param installData the installation data
     * @param console     the console
     * @return <tt>true</tt> if the panel ran successfully, otherwise <tt>false</tt>
     */
    @Override
    public boolean run(InstallData installData, Console console)
    {
        printHeadLine(installData, console);

        boolean result = true;
        if (registered)
        {
            result = multipleInstall(installData);
            if (result)
            {
                try
                {
                    registryHelper.updateUninstallName();
                    registered = false;
                }
                catch (NativeLibException exception)
                {
                    result = false;
                    logger.log(Level.SEVERE, exception.getMessage(), exception);
                }
            }
        }
		// We had to override this method to remove APP_VER
		// return variables.get("APP_NAME") + " " + variables.get("APP_VER");
    	Variables variables = this.installData.getVariables();		
        installData.setVariable("UNINSTALL_NAME", variables.get("APP_NAME"));        
        // installData.setVariable("UNINSTALL_NAME", registryHelper.getUninstallName());
        if (result)
        {
            display(installData, console);
            result = promptEndPanel(installData, console);
        }

        return result;
    }
}
