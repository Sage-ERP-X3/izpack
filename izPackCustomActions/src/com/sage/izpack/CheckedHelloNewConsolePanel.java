package com.sage.izpack;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.izforge.izpack.api.handler.Prompt.Option.YES;
import static com.izforge.izpack.api.handler.Prompt.Options.YES_NO;
import static com.izforge.izpack.api.handler.Prompt.Type.ERROR;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
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
	private final Resources resources;
	private static final String logPrefix = "CheckedHelloNewConsolePanel - ";

	/**
	 * The prompt.
	 */
	private final Prompt prompt;
	/**
	 * Determines if the application is already installed.
	 */
	private InstallData installData;
	private RegistryHandler registryHandler;
	private RegistryHelper registryHelper;
	private RegistryHandlerX3 x3Handler;
	private ResourcesHelper resourceHelper;
	private String installPath;

	public CheckedHelloNewConsolePanel(RegistryDefaultHandler handler, InstallData installData, Resources resources,
			Prompt prompt, PanelView<ConsolePanel> panel) throws NativeLibException {
		super(handler, installData, prompt, panel);
		this.registryHelper = new RegistryHelper(handler, installData);
		this.prompt = prompt;
		this.installData = installData;
		this.resources = resources;
		this.registryHandler = handler != null ? handler.getInstance() : null;
		this.x3Handler = new RegistryHandlerX3(registryHandler, installData);

		logger.log(Level.FINE, logPrefix);

		this.resourceHelper = new ResourcesHelper(installData, resources);
		resourceHelper.mergeCustomMessages();

		this.registryHelper = new RegistryHelper(handler, installData);

		this.installPath = CheckedHelloNewPanel.initPath(installData, resources, registryHelper, x3Handler);
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

		logger.log(Level.FINE, logPrefix + "run()  installPath: " + this.installPath);

		printHeadLine(installData, console);

		if (this.x3Handler.needAdxAdmin()) {
			try {
				// Check presence of adxadmin
				String adxAdminPath = this.x3Handler.getAdxAdminDirPath();
				boolean adxAdminInstalled = (adxAdminPath != null);

				// No Adxadmin
				if (!adxAdminInstalled) {
					// No Adxadmin
					logger.log(Level.FINE, this.resourceHelper.getCustomString("adxadminNotRegistered"));
					console.println(this.resourceHelper.getCustomString("adxadminNotRegistered"));
					return false;
				}
			} catch (Exception e) { // Will only be happen if registry handler is good, but an
									// exception at performing was thrown. This is an error...
				logger.log(Level.WARNING,
						this.resourceHelper.getCustomString("installer.error") + ":" + e.getMessage());
				console.println(this.resourceHelper.getCustomString("installer.error"));
				// parent.lockNextButton();
				return false;
			}
		}

		boolean result = true;
		// Update mode
		if (this.installPath != null) {
			ModifyInstallationUtil.set(installData, Boolean.TRUE);
			String allowMultipleInstall = installData.getVariable("allow-multiple-instance");
			// multiple install is allowed
			// <variable name="CheckedHelloNewPanel.allowMultipleInstance" value="true"/>
			if (Boolean.TRUE.toString().equalsIgnoreCase(allowMultipleInstall)) {
				try {
					CheckedHelloNewPanelAutomationHelper.setUniqueUninstallKey(this.registryHandler,
							this.registryHelper, this.resourceHelper);
				} catch (Exception e) {
					logger.log(Level.WARNING, e.getMessage(), e);
					e.printStackTrace();
				}

				// We had to override this method to remove APP_VER
				// return variables.get("APP_NAME") + " " + variables.get("APP_VER");
				Variables variables = this.installData.getVariables();
				installData.setVariable("UNINSTALL_NAME", variables.get("APP_NAME"));
				logger.log(Level.FINE, logPrefix + "Set UNINSTALL_NAME: " + variables.get("APP_NAME"));
				// installData.setVariable("UNINSTALL_NAME", registryHelper.getUninstallName());
				if (result) {
					display(installData, console);
					result = promptEndPanel(installData, console);
				}

				// Default behavior: update mode
				// or <variable name="CheckedHelloNewPanel.allowMultipleInstance"
				// value="false"/>
			} else {
				logger.log(Level.FINE, logPrefix + "allow-multiple-instance=false (updatemode)");

				result = multipleInstall(installData);
				if (result) {
					installData.getInfo().setUninstallerPath(null);
					installData.getInfo().setUninstallerName(null);
				}

				// Set variable "modify.izpack.install"
				// installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");
				// logger.log(Level.FINE, "CheckedHelloNewConsolePanel Set " +
				// InstallData.MODIFY_INSTALLATION + ": true");

				// We had to override this method to remove APP_VER
				// return variables.get("APP_NAME") + " " + variables.get("APP_VER");
				Variables variables = this.installData.getVariables();
				// installData.setVariable("UNINSTALL_NAME", registryHelper.getUninstallName());
				installData.setVariable("UNINSTALL_NAME", variables.get("APP_NAME"));
				logger.log(Level.FINE, logPrefix + "Set UNINSTALL_NAME: " + variables.get("APP_NAME"));
				if (result) {
					display(installData, console);
					result = promptEndPanel(installData, console);
				}

			}
		}
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
	protected boolean multipleInstall(InstallData installData) {

		logger.log(Level.FINE, logPrefix + "multipleInstall()");

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
			String noLuck = this.resourceHelper.getCustomString("CheckedHelloNewPanel.productAlreadyExist0") + path
					+ ". " + this.resourceHelper.getCustomString("CheckedHelloNewPanel.productAlreadyExist1");

			result = prompt.confirm(ERROR, noLuck, YES_NO) == YES;

		} catch (NativeLibException e) {

			result = false;
			e.printStackTrace();
		}

		return result;
	}


}
