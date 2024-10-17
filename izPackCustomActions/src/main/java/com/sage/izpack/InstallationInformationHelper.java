package com.sage.izpack;

import static com.izforge.izpack.api.data.InstallData.MODIFY_INSTALLATION;
import static com.sage.izpack.InstallTypeNewPanelAutomation.NEED_SERVICE_CONFIGURATION_FIX;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;

public final class InstallationInformationHelper {

    private static final Logger logger = Logger.getLogger(InstallationInformationHelper.class.getName());
    private static final String logPrefix = "InstallationInformationHelper - ";

    private static final String ALREADY_LOADED = "installationinformation-already-loaded";
    /**
     * variables which not to load from previous installation, mostly to allow changes in the installer
     */
    private static final List<String> VARIABLES_EXCEPTIONS = Arrays.asList(
        "app-version", "APP_VER", "ISO2_LANG", "ISO3_LANG", "COMPONENT.VERSION", "component.version", "CLASS_PATH",
        MODIFY_INSTALLATION, "component.status", "component.updatemode", ALREADY_LOADED, "APP_NAME", "IP_ADDRESS",
        "OS.VERSION", "os.version", "sageos.version", "sageos.details", "izpack.version", "allow-multiple-instance",
        "APP_URL", "UNPACKER_CLASS", "force-generate-uninstaller", "USER_HOME", "izpack.file", "need-adxadmin",
        "JAVA_HOME", "ESTIMATED_SIZE", "TEMP_DIRECTORY", "HOST_NAME", "Publisher", NEED_SERVICE_CONFIGURATION_FIX
    );

    private static boolean isLegacyIzpack = false;

    public static boolean hasAlreadyReadInformation(InstallData installData) {
        boolean hasRead = Boolean.parseBoolean(installData.getVariable(ALREADY_LOADED));
        logger.log(Level.FINE, logPrefix + "hasAlreadyReadInformation : " + hasRead);
        return hasRead;
    }

    public static boolean isLegacyIzpackInfo() {
        return isLegacyIzpack;
    }

    public static void readInformation(InstallData installData) {
        boolean perform = ModifyInstallationUtil.get(installData);
        if (!perform && (installData instanceof InstallDataSage)) {
            perform = Boolean.TRUE;
        }
        if (!perform) {
            logger.log(Level.FINE, logPrefix + "not restoring variables because it is not an update or uninstallation.");
            return;
        }
        if (installData.getInstallPath() == null || installData.getInstallPath().isBlank()) {
            logger.log(Level.FINE, logPrefix + "INSTALL_PATH is not set, exiting.");
            return;
        }
        String osVersion = OsVersionHelper.OS_VERSION;
        String osVersionDetails = OsVersionHelper.getOsDetails();
        installData.setVariable("OS.VERSION", osVersion);
        installData.setVariable("os.version", osVersion);
        installData.setVariable("sageos.version", osVersion);
        installData.setVariable("sageos.details", osVersionDetails);

        logger.log(Level.FINE, logPrefix + "set variables 'os.version', 'sageos.version': " + osVersion
            + "  'sageos.details':" + osVersionDetails);

        logger.log(Level.FINE, logPrefix + "Reading file " + InstallData.INSTALLATION_INFORMATION);

        boolean informationloaded;
        try {
            informationloaded = loadInstallationInformation(installData);
            logger.log(Level.FINE, logPrefix + "Installation information loaded");
        } catch (Exception e) {
            logger.log(Level.FINE, logPrefix + "Installation information loading failed: " + e.getMessage());
            informationloaded = false;
        }
        installData.setVariable(ALREADY_LOADED, Boolean.toString(informationloaded));
    }

    public static boolean isIncompatibleInstallation(String installPath, boolean isReadInstallationInformation) {
        String logHeader = "InstallationInformationHelper.isIncompatibleInstallation - ";
        if (isReadInstallationInformation) {
            File installInfo = new File(installPath, InstallData.INSTALLATION_INFORMATION);
            if (installInfo.exists()) {
                try (ObjectInputStream oin = new ObjectInputStream(new FileInputStream(installInfo))) {
                    // noinspection rawtypes
                    List<?> objects = (List<?>) oin.readObject();
                    for (Object pack : objects) {
                        if (pack instanceof com.izforge.izpack.Pack) {
                            logger.log(Level.FINE, logHeader + "found legacy pack: " + ((com.izforge.izpack.Pack) pack).name);
                        } else if (pack instanceof com.izforge.izpack.api.data.Pack) {
                            logger.log(Level.FINE, logHeader + "found pack: "+ ((com.izforge.izpack.api.data.Pack)pack).getName());
                        } else {
                            return Boolean.TRUE;
                        }
                    }
                    Properties props = (Properties) oin.readObject();
                    for (String key : props.stringPropertyNames()) {
                        logger.log(Level.FINE, logHeader + "found variable: " + key + " = " + props.getProperty(key));
                    }
                } catch (Exception ignored) {
                    logger.log(Level.SEVERE, logHeader + "problem reading "+installInfo.getAbsolutePath());
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }

    private static boolean loadInstallationInformation(InstallData installData) {
        File installInfo = new File(installData.getInstallPath(), InstallData.INSTALLATION_INFORMATION);
        if (!installInfo.exists()) {
            logger.log(Level.FINE, logPrefix + installInfo.getAbsolutePath() + " doesnt exist, exiting.");
            return Boolean.FALSE;
        }
        try (ObjectInputStream oin = new ObjectInputStream(new FileInputStream(installInfo))) {
            // noinspection rawtypes
            List<?> packsinstalled = (List<?>) oin.readObject();
            logger.log(Level.FINE, logPrefix + "Found " + packsinstalled.size() + " installed packs");

            final List<Pack> selectedPacks = new ArrayList<>(installData.getSelectedPacks());
            for (Object obj : packsinstalled) {
                if (obj instanceof com.izforge.izpack.Pack) {
                    isLegacyIzpack = Boolean.TRUE;
                    final com.izforge.izpack.Pack pack = (com.izforge.izpack.Pack) obj;
                    installData.getAvailablePacks().stream()
                        .filter(p -> p.getName().equals(pack.name))
                        .filter(p -> notContainsPack(pack.name, selectedPacks))
                        .findFirst()
                        .ifPresent(selectedPacks::add);
                } else if (obj instanceof com.izforge.izpack.api.data.Pack) {
                    final com.izforge.izpack.api.data.Pack pack = (com.izforge.izpack.api.data.Pack) obj;
                    installData.getAvailablePacks().stream()
                        .filter(p -> p.getName().equals(pack.getName()))
                        .filter(p -> notContainsPack(pack.getName(), selectedPacks))
                        .findFirst()
                        .ifPresent(selectedPacks::add);
                } else {
                    return Boolean.FALSE;
                }
            }
            installData.setSelectedPacks(selectedPacks);

            Properties variables = (Properties) oin.readObject();
            for (Object key : variables.keySet()) {
                if (!VARIABLES_EXCEPTIONS.contains(key)) {
                    installData.setVariable((String) key, (String) variables.get(key));
                }
            }
            return Boolean.TRUE;
        } catch (Exception e) {
            logger.warning(logPrefix + "Could not read Pack installation information in current izPack version: " + e.getMessage());
            return Boolean.FALSE;
        }
    }

    private static boolean notContainsPack(String name, List<Pack> packs) {
        Optional<Pack> found = packs.stream()
            .filter(p -> name.equals(p.getName()))
            .findFirst();
        return found.isEmpty();
    }
}
