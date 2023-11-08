package com.sage.izpack;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.console.ConsoleInstaller;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.panels.finish.FinishConsolePanel;
import com.izforge.izpack.util.Console;
import com.izforge.izpack.util.PlatformModelMatcher;

/*
* @author Franck DEPOORTERE
*/
public class FinishNewConsolePanel extends FinishConsolePanel {

	private static final Logger logger = Logger.getLogger(FinishNewConsolePanel.class.getName());

	private UninstallDataWriter uninstallDataWriter;
	private UninstallData uninstallData;
	private Prompt prompt;
	private InstallData installData;
	private ResourcesHelper resourceHelper;

	public FinishNewConsolePanel(InstallData installData, ObjectFactory factory, ConsoleInstaller parent,
			PlatformModelMatcher matcher, UninstallDataWriter uninstallDataWriter, UninstallData uninstallData,
			Resources resources, Prompt prompt, PanelView<ConsolePanel> panel) {
		super(factory, parent, matcher, uninstallData, prompt, panel);

		this.uninstallDataWriter = uninstallDataWriter;
		this.prompt = prompt;
		this.uninstallData = uninstallData;
		this.resourceHelper = new ResourcesHelper(installData, resources);
		resourceHelper.mergeCustomMessages();

	}

	@Override
	public boolean run(InstallData installData, Console console) {

		boolean result = super.run(installData, console);
		// Save Data
		if (result)
			result = writeUninstallData();
		return result;
	}

	private boolean writeUninstallData() {

		boolean result = true;

		// X3-256055: Uninstaller (izpack 5.2)
		// installData.setVariable("force-generate-uninstaller", "true");
		// installData.getInfo().setUninstallerCondition("uninstaller.write");

		boolean uninstallRequired = this.uninstallDataWriter.isUninstallRequired();
		logger.log(Level.FINE, "FinishNewPanel writeUninstallData. uninstallRequired:" + uninstallRequired);

		// We force the Uninstaller to be generated
		if (!uninstallDataWriter.isUninstallRequired()) {
			FinishNewPanelAutomationHelper.initUninstallPath(this.installData);
			result = uninstallDataWriter.write();
			logger.log(Level.FINE,
					"FinishNewPanel force writeUninstallData. uninstallDataWriter.write() returns " + result);

			if (!result) {
				// Messages messages = locales.getMessages();
				// String title = this.resourceHelper.getCustomString("installer.error");
				 String message = this.resourceHelper.getCustomString("installer.uninstall.writefailed");
				logger.warning(message);
			}
		}
		return result;
	}

}
