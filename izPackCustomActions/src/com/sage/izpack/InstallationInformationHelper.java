package com.sage.izpack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;

import static com.izforge.izpack.api.data.InstallData.MODIFY_INSTALLATION;
import static com.sage.izpack.InstallTypeNewPanelAutomation.NEED_SERVICE_CONFIGURATION_FIX;

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

    public static boolean hasAlreadyReadInformation(com.izforge.izpack.api.data.InstallData installData) {
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
        // If old version 4.3.8, ...
        if (!informationloaded) {
            logger.log(Level.FINE, logPrefix + "Loading legacy installation information");
            try {
                informationloaded = loadLegacyInstallationInformation(installData);
                logger.log(Level.FINE, logPrefix + "Legacy installation information loaded");
            } catch (Exception e) {
                logger.log(Level.FINE, logPrefix + "Installation legacy information loading failed: " + e.getMessage());
                informationloaded = false;
            }
        }
        installData.setVariable(ALREADY_LOADED, Boolean.toString(informationloaded));
    }

    public static boolean isIncompatibleInstallation(String installPath, boolean isReadInstallationInformation) {

        boolean result = false;
        String logHeader = "InstallationInformationHelper.isIncompatibleInstallation - ";
        if (isReadInstallationInformation) {

            ObjectInputStream oin = null;
            File installInfo = new File(installPath, InstallData.INSTALLATION_INFORMATION);
            if (installInfo.exists()) {
                FileInputStream fin;
                List<Pack> packsinstalled;
                try {
                    fin = new FileInputStream(installInfo);
                    oin = new ObjectInputStream(fin);
                    // noinspection unchecked
                    packsinstalled = (List<Pack>) oin.readObject();

                    for (Pack installedpack : packsinstalled) {
                        logger.log(Level.FINE, logHeader + "pack " + installedpack.getName());
                    }
                    logger.log(Level.FINE, logHeader + "Found " + packsinstalled.size() + " installed packs");
                } catch (Exception e) {
                    result = true;
                    logger.warning(logHeader + "Could not read Pack installation information: " + e.getMessage());
                }

                // Previous version of izPack 4.3
                if (result) {

                    try {
                        fin = new FileInputStream(installInfo);
                        oin = new ObjectInputStream(fin);
                        // noinspection unchecked
                        List<com.izforge.izpack.Pack> packsinstalled2 = (List<com.izforge.izpack.Pack>) oin.readObject();

                        for (com.izforge.izpack.Pack installedpack : packsinstalled2) {
                            logger.log(Level.FINE, logHeader + "installedpack: " + installedpack.name);
                        }
                        logger.log(Level.FINE, logHeader + "Found Legacy " + packsinstalled2.size() + " installed packs");
                        result = false;
                    } catch (Exception e) {
                        logger.warning(logHeader + "Could not read Legacy 'Pack' installation information: " + e.getMessage());
                    }
                }
            }

            if (oin != null) {
                try {
                    oin.close();
                } catch (IOException ignored) {
                }
            }
        }
        logger.log(Level.FINE, logPrefix + "isIncompatibleInstallation returns " + result);

        return result;
    }

    private static boolean loadInstallationInformation(com.izforge.izpack.api.data.InstallData installData) {

        File installInfo = new File(installData.getInstallPath(), InstallData.INSTALLATION_INFORMATION);
        if (!installInfo.exists()) {
            logger.log(Level.FINE, logPrefix + installInfo.getAbsolutePath() + " doesnt exist, exiting.");
            return Boolean.FALSE;
        }
        try (ObjectInputStream oin = new ObjectInputStream(new FileInputStream(installInfo))) {
            // noinspection unchecked
            List<com.izforge.izpack.api.data.Pack> packsinstalled = (List<com.izforge.izpack.api.data.Pack>) oin.readObject();
            logger.log(Level.FINE, logPrefix + "Found " + packsinstalled.size() + " installed packs");

            List<Pack> selectedPacks = new ArrayList<>(installData.getSelectedPacks());
            selectedPacks.addAll(installData.getAvailablePacks().stream()
                        .filter(p->containsPack(p, packsinstalled))
                        .filter(p->!containsPack(p, installData.getSelectedPacks()))
                        .collect(Collectors.toList()));
            installData.setSelectedPacks(selectedPacks);

            Properties variables = (Properties) oin.readObject();
            for (Object key : variables.keySet()) {
                if (!VARIABLES_EXCEPTIONS.contains((String) key)) {
                    installData.setVariable((String) key, (String) variables.get(key));
                }
            }
            return Boolean.TRUE;
        } catch (Exception e) {
            logger.warning(logPrefix + "Could not read Pack installation information in current izPack version: " + e.getMessage());
            return Boolean.FALSE;
        }
    }

    private static boolean containsPack(Pack p, List<Pack> packs) {
        Optional<Pack> found = packs.stream()
            .filter(a -> p.getName().equals(a.getName()))
            .findFirst();
        return found.isPresent();
    }

    private static boolean loadLegacyInstallationInformation(com.izforge.izpack.api.data.InstallData installData) {

        File installInfo = new File(installData.getInstallPath(), InstallData.INSTALLATION_INFORMATION);
        if (!installInfo.exists()) {
            logger.log(Level.FINE, logPrefix + installInfo.getAbsolutePath() + " doesnt exist, exiting.");
            return Boolean.FALSE;
        }
        String logHeader = logPrefix + "loadLegacyInstallationInformation - ";
        try (ObjectInputStream oin = new ObjectInputStream(new FileInputStream(installInfo))) {

            Object variablesObject = oin.readObject();
            while (variablesObject != null) {
                logger.log(Level.FINE, logHeader + "readObject legacy type:" + variablesObject.getClass().getName());

                if (variablesObject instanceof Properties) {
                    Properties variables = (Properties) variablesObject;
                    for (Object key : variables.keySet()) {
                        if (!VARIABLES_EXCEPTIONS.contains((String) key)) {
                            installData.setVariable((String) key, (String) variables.get(key));
                        }
                    }
                }
                if (variablesObject instanceof ArrayList) {
                    //noinspection rawtypes
                    readLegacyPackages(installData, (ArrayList) variablesObject);
                }
                variablesObject = oin.readObject();
            }

            logger.log(Level.FINE, logHeader + "writeInstallationInformation to fix legacy issue");

            // X3-256055: Uninstaller (izpack 5.2)
            installData.setVariable("force-generate-uninstaller", "true");
            logger.log(Level.FINE, logHeader + "force-generate-uninstaller set to true");

            isLegacyIzpack = true;
            return Boolean.TRUE;
        } catch (Exception e) {
            logger.warning(logHeader + "Could not read Legacy 'Properties' installation information: " + e.getMessage());
            return Boolean.FALSE;
        }
    }

    private static void readLegacyPackages(com.izforge.izpack.api.data.InstallData installData, @SuppressWarnings("rawtypes") ArrayList variables) {
        logger.log(Level.FINE, "readVariables ArrayList objects: " + variables + " : " + variables.getClass().getName());
        List<com.izforge.izpack.api.data.Pack> packLists = new ArrayList<>(installData.getSelectedPacks());
        for (Object key : variables) {
            logger.log(Level.FINE, "keyType:" + key.getClass().getName());
            if (key instanceof com.izforge.izpack.Pack) {
                com.izforge.izpack.Pack theFormerPack = (com.izforge.izpack.Pack) key;
                installData.getAvailablePacks().stream()
                    .filter(p -> p.getName().equals(theFormerPack.name))
                    .filter(p -> !containsPack(p, installData.getSelectedPacks()))
                    .findFirst()
                    .ifPresent(packLists::add);
            }
        }
        if (!packLists.isEmpty())
            installData.setSelectedPacks(new ArrayList<>(packLists));
    }

}
