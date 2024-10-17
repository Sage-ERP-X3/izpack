package com.sage.izpack;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.panels.packs.PacksPanelAutomationHelper;

public class PacksNewPanelAutomationHelper extends PacksPanelAutomationHelper {

    private static final Logger logger = Logger.getLogger(PacksNewPanelAutomationHelper.class.getName());
    private final Resources resources;

    public PacksNewPanelAutomationHelper(Resources resources) {
        super();
        this.resources = resources;
    }

    @Override
    public void runAutomated(InstallData installData, IXMLElement panelRoot) {
        preselectRequired(installData);
        readInstallationInformation(installData);
        super.runAutomated(installData, panelRoot);
    }

    public static void preselectRequired(InstallData installData) {
        for (Pack p : installData.getAvailablePacks()) {

            if (p.isRequired()) {
                // selectedPacks.add(p);
                p.setHidden(false);
                p.setPreselected(true);
            }

            logger.log(Level.FINE, logger.getName() + "createPacksTable - Pack " + p.getName() + " Required: "
                + p.isRequired() + " Preselected: " + p.isPreselected());
        }
    }

    public static void readInstallationInformation(InstallData installData) {
        if (installData.getInfo().isReadInstallationInformation() && !InstallationInformationHelper.hasAlreadyReadInformation(installData)) {
            InstallationInformationHelper.readInformation(installData);
            logger.log(Level.FINE, PacksNewPanelAutomationHelper.class.getName() + " - ReadInstallationInformation: has been read.");
        }
    }
}
