package com.sage.izpack;

import java.util.logging.Level;
import java.util.logging.Logger;


import static com.izforge.izpack.api.handler.Prompt.Option.YES;
import static com.izforge.izpack.api.handler.Prompt.Options.YES_NO;
import static com.izforge.izpack.api.handler.Prompt.Type.ERROR;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.resource.DefaultLocales;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.panels.checkedhello.CheckedHelloConsolePanel;
import com.izforge.izpack.panels.checkedhello.RegistryHelper;
import com.izforge.izpack.util.Console;


public class CheckedHelloNewConsolePanel extends CheckedHelloConsolePanel {

	private static Logger logger = Logger.getLogger(CheckedHelloNewConsolePanel.class.getName());
	private LocaleDatabase customResources;
	private String customResourcesPath;

    /**
     * The prompt.
     */
    private final Prompt prompt;
    /**
     * Determines if the application is already installed.
     */
    private boolean registered;
    private InstallData installData;

	private RegistryHelper _registryHelper;

	public CheckedHelloNewConsolePanel(RegistryDefaultHandler handler, InstallData installData, Resources resources, Prompt prompt,
			PanelView<ConsolePanel> panel) throws NativeLibException {
		super(handler, installData, prompt, panel);
        _registryHelper = new RegistryHelper(handler, installData);
        this.prompt = prompt;
        registered = _registryHelper.isRegistered();
        this.installData = installData;
        
		customResourcesPath = "/com/sage/izpack/langpacks/" + installData.getLocaleISO3() + ".xml";
		Locales locales = new DefaultLocales(resources, installData.getLocale());
		customResources = new LocaleDatabase(getClass().getResourceAsStream(customResourcesPath), locales);

		
		RegistryHelper registryHelper = new RegistryHelper(handler, installData);
		_registryHelper = registryHelper;
		String path = registryHelper.getInstallationPath();
		// Update case :
		if (path != null) {
			installData.setVariable("TargetPanel.dir.windows", path);
			logger.log(Level.FINE, "CheckedHelloNewConsolePanel Set TargetPanel.dir.windows: " + path);

			installData.setVariable(InstallData.INSTALL_PATH, path);
			logger.log(Level.FINE, "CheckedHelloNewConsolePanel Set INSTALL_PATH: " + path);
		}
		
		// Update case : read .installationinformation
		if (path != null && installData.getInfo().isReadInstallationInformation()) {

			InstallationInformationHelper.readInformation(installData);

		}


	}
	
	

	/*
	 * X3-240420 : Wrong message when updating the console This method should only
	 */
	// @Override
	private String getString(String key) {
		String result = null;
		try {
			result = customResources.get(key);
		} catch (Exception ex) {
			logger.log(Level.FINE, "CheckedHelloNewPanel Cannot get resource " + key + " " + customResourcesPath);

		}
		/*
		if (result == null) {
			result = super.getString(key);
		}
		*/
		return result;
	}


	/**
	 * X3-240420 : Wrong message when updating the console This method should only
	 * be called if this product was already installed. It resolves the install path
	 * of the first already installed product and asks the user whether to install
	 * twice or not.
	 *
	 * @return whether a multiple Install should be performed or not.
	 * @throws NativeLibException for any native library error
	 */
	@Override
	protected boolean multipleInstall(InstallData installData)  {
		boolean result;
		String path;
		try {
			path = _registryHelper.getInstallationPath();
		if (path == null) {
			path = "<not found>";
		}

		String noLuck = getString("CheckedHelloPanel.productAlreadyExist0") + path + ". "
				+ getString("CheckedHelloPanel.productAlreadyExist1");

		result = prompt.confirm(ERROR, noLuck, YES_NO) == YES;

		} catch (NativeLibException e) {

			result = false;
			e.printStackTrace();
		}

		return result;
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
                    _registryHelper.updateUninstallName();
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
