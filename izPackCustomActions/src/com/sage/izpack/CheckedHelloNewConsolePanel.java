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
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.core.resource.DefaultLocales;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.panels.checkedhello.CheckedHelloConsolePanel;
import com.izforge.izpack.panels.checkedhello.RegistryHelper;
import com.izforge.izpack.util.Console;
import com.izforge.izpack.util.OsVersion;

/*
 * @author Franck DEPOORTERE
 */
public class CheckedHelloNewConsolePanel extends CheckedHelloConsolePanel {

	private static Logger logger = Logger.getLogger(CheckedHelloNewConsolePanel.class.getName());
	private Resources resources;

	/**
	 * The prompt.
	 */
	private final Prompt prompt;
	/**
	 * Determines if the application is already installed.
	 */
	private boolean registered;
	private InstallData installData;
	private RegistryHandler registryHandler;
	private RegistryHelper registryHelper;
	
	public CheckedHelloNewConsolePanel(RegistryDefaultHandler handler, InstallData installData, Resources resources,
			Prompt prompt, PanelView<ConsolePanel> panel) throws NativeLibException {
		super(handler, installData, prompt, panel);
		this.registryHelper = new RegistryHelper(handler, installData);
		this.prompt = prompt;
		this.installData = installData;
		this.resources = resources;
		this.registryHandler = handler != null ?  handler.getInstance(): null;

		logger.log(Level.FINE, "CheckedHelloNewConsolePanel");

		ResourcesHelper resourceHelper = new ResourcesHelper(installData, resources);
		resourceHelper.mergeCustomMessages();
		
		this.registryHelper = new RegistryHelper(handler, installData);
		
		String path = installData.getInstallPath();
		if (path == null && OsVersion.IS_WINDOWS)
			path = this.registryHelper.getInstallationPath();
		
		if (path == null) {
			RegistryHandlerX3 x3Handler = new RegistryHandlerX3(this.registryHandler, installData);
			if (x3Handler.isAdminSetup()) {
				path = x3Handler.getAdxAdminDirPath();
			}
			logger.log(Level.FINE,
					"Warning CheckedHelloNewConsolePanel Could not get RegistryHandler.getInstallationPath() return NULL. path: "+ path);
		}

		// Update case :
		if (path != null) {
			registered = true;
			
			String targetPanelDir = "TargetPanel.dir.windows";
			if (OsVersion.IS_LINUX) {
				targetPanelDir = "TargetPanel.dir.unix";
				installData.setVariable(targetPanelDir, path);				
			} else {
				installData.setVariable(targetPanelDir, path);				
			}
			logger.log(Level.FINE, "CheckedHelloNewConsolePanel Set "+targetPanelDir+": " + path);

			installData.setVariable(InstallData.INSTALL_PATH, path);
			logger.log(Level.FINE, "CheckedHelloNewConsolePanel  Set INSTALL_PATH: " + path);

			Variables variables = this.installData.getVariables();
			installData.setVariable("UNINSTALL_NAME", variables.get("APP_NAME"));

			// Set variable "modify.izpack.install"
			installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");
			logger.log(Level.FINE, "CheckedHelloNewConsolePanel  Registered: " + registered);
		}

		// Update case : read .installationinformation
		if (path != null && installData.getInfo().isReadInstallationInformation()) {

			if (!InstallationInformationHelper.hasAlreadyReadInformation(this.installData)) {
				InstallationInformationHelper.readInformation(installData, resources);
			} else {
				logger.log(Level.FINE,
						"CheckedHelloNewConsolePanel ReadInstallationInformation: "
								+ this.installData.getInfo().isReadInstallationInformation() + " AlreadyRead: "
								+ InstallationInformationHelper.hasAlreadyReadInformation(this.installData));
			}
		}
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
	protected boolean multipleInstall(InstallData installData) {

		logger.log(Level.FINE, "CheckedHelloNewConsolePanel.multipleInstall()");

		boolean result;
		String path;
		try {
			path = installData.getInstallPath();
			if (path == null && OsVersion.IS_WINDOWS)
				path = this.registryHelper.getInstallationPath();
			
			if (path == null) {
				path = "<not found>";
			}
			// X3-240420 : Wrong message when updating the console This method should only
			ResourcesHelper resourcesHelper = new ResourcesHelper(this.installData, this.resources);
			String noLuck = resourcesHelper.getCustomString("CheckedHelloNewPanel.productAlreadyExist0") + path
					+ ". " + resourcesHelper.getCustomString("CheckedHelloNewPanel.productAlreadyExist1");

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
	public boolean run(InstallData installData, Console console) {

		logger.log(Level.FINE, "CheckedHelloNewConsolePanel.run()");

		printHeadLine(installData, console);

		boolean result = true;
		if (registered) {
			result = multipleInstall(installData);
			if (result) {
				// try {
				// _registryHelper.updateUninstallName();
				// registered = false;
				// } catch (NativeLibException exception) {
				// result = false;
				// logger.log(Level.SEVERE, "CheckedHelloNewConsolePanel " +
				// exception.getMessage(), exception);
				// }
			}

			// Set variable "modify.izpack.install"
			installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");
			logger.log(Level.FINE, "CheckedHelloNewConsolePanel Set " + InstallData.MODIFY_INSTALLATION + ": true");

			// We had to override this method to remove APP_VER
			// return variables.get("APP_NAME") + " " + variables.get("APP_VER");
			Variables variables = this.installData.getVariables();
			installData.setVariable("UNINSTALL_NAME", variables.get("APP_NAME"));
			logger.log(Level.FINE, "CheckedHelloNewConsolePanel Set UNINSTALL_NAME: " + variables.get("APP_NAME"));
			// installData.setVariable("UNINSTALL_NAME", registryHelper.getUninstallName());
			if (result) {
				display(installData, console);
				result = promptEndPanel(installData, console);
			}

		}
		return result;
	}
	
	/**
	 * Returns the text to display.
	 * @return the text
	 */
	/*
	@Override
	protected String getText() {
		ResourcesHelper resourcesHelper = new ResourcesHelper(this.installData, this.resources);
        String str = resourcesHelper.getCustomString("HelloPanel.welcome1") + this.installData.getInfo().getAppName() + " "
                + this.installData.getInfo().getAppVersion() + resourcesHelper.getCustomString("HelloPanel.welcome2");
        return str;
		// return resources.getString(panelResourceName, "Error : could not load the info text!");
	}
	*/
}
