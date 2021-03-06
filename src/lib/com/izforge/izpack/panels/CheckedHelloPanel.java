/*
 * $Id$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/ http://izpack.codehaus.org/
 * 
 * Copyright 2005 Klaus Bartz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.izforge.izpack.panels;

import javax.swing.JOptionPane;

import com.coi.tools.os.win.MSWinConstants;
import com.coi.tools.os.win.RegDataContainer;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.os.RegistryDefaultHandler;
import com.izforge.izpack.util.os.RegistryHandler;
import com.izforge.izpack.util.Debug;

/**
 * An extended hello panel class which detects whether the product was already installed or not.
 * This class should be only used if the RegistryInstallerListener will be also used. Current the
 * check will be only performed on Windows operating system. This class can be used also as example
 * how to use the registry stuff to get informations from the current system.
 *
 * @author Klaus Bartz
 */
public class CheckedHelloPanel extends HelloPanel implements MSWinConstants
{

    /**
     * Required (serializable)
     */
    private static final long serialVersionUID = 1737042770727953387L;
    /**
     * Flag to break installation or not.
     */
    protected boolean abortInstallation;

    /**
     * The constructor.
     *
     * @param parent The parent.
     * @param idata  The installation data.
     */
    public CheckedHelloPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);
        abortInstallation = isRegistered();
    }

    /**
     * This method should only be called if this product was allready installed. It resolves the
     * install path of the first already installed product and asks the user whether to install
     * twice or not.
     *
     * @return whether a multiple Install should be performed or not.
     * @throws Exception
     */
    protected String getOldInstallPath() throws Exception
    {
        // Let us play a little bit with the regstry...
        // Just for fun we would resolve the path of the already
        // installed application.
        // First we need a handler. There is no overhead at a
        // secound call of getInstance, therefore we do not buffer
        // the handler in this class.
        RegistryHandler rh = RegistryDefaultHandler.getInstance();
        int oldVal = rh.getRoot(); // Only for security...
        // We know, that the product is already installed, else we
        // would not in this method. Now we search for the path...
        String uninstallName = rh.getUninstallName();
        String oldInstallPath = "<not found>";
        while (true) // My goto alternative :-)
        {

            if (uninstallName == null)
            {
                break; // Should never be...
            }
            // First we "create" the reg key.
            String keyName = RegistryHandler.UNINSTALL_ROOT + uninstallName;
            rh.setRoot(HKEY_LOCAL_MACHINE);
            if (!rh.valueExist(keyName, "UninstallString"))
            // We assume that the application was installed with
            // IzPack. Therefore there should be the value "UninstallString"
            // which contains the uninstaller call. If not we can do nothing.
            {
                break;
            }
            // Now we would get the value. A value can have different types.
            // Therefore we get an container which can handle all possible types.
            // There are different ways to handle. Use normally only one of the
            // ways; at this point more are used to demonstrate the different ways.

            // 1. If we are secure about the type, we can extract the value immediately.
            String valString = rh.getValue(keyName, "UninstallString").getStringData();

            // 2. If we are not so much interessted at the type, we can get the value
            // as Object. A DWORD is then a Long Object not a long primitive type.
            Object valObj = rh.getValue(keyName, "UninstallString").getDataAsObject();
            if (valObj instanceof String) // Only to inhibit warnings about local variable never read.
            {
                valString = (String) valObj;
            }

            // 3. If we are not secure about the type we should differ between possible
            // types.
            RegDataContainer val = rh.getValue(keyName, "UninstallString");
            int typeOfVal = val.getType();
            switch (typeOfVal)
            {
                case REG_EXPAND_SZ:
                case REG_SZ:
                    valString = val.getStringData();
                    break;
                case REG_BINARY:
                case REG_DWORD:
                case REG_LINK:
                case REG_MULTI_SZ:
                    throw new Exception("Bad data type of chosen registry value " + keyName);
                default:
                    throw new Exception("Unknown data type of chosen registry value " + keyName);
            }
            // That's all with registry this time... Following preparation of
            // the received value.
            // It is [java path] -jar [uninstaller path]
            int start = valString.lastIndexOf("-jar") + 5;
            if (start < 5 || start >= valString.length())
            // we do not know what todo with it.
            {
                break;
            }
            String uPath = valString.substring(start).trim();
            if (uPath.startsWith("\""))
            {
                uPath = uPath.substring(1).trim();
            }
            int end = uPath.indexOf("uninstaller");
            if (end < 0)
            // we do not know what todo with it.
            {
                break;
            }
            oldInstallPath = uPath.substring(0, end - 1);
            // Much work for such a peanuts...
            break; // That's the problem with the goto alternative. Forget this
            // break produces an endless loop.
        }

        rh.setRoot(oldVal); // Only for security...
        
        return oldInstallPath;
    }

    protected boolean multipleInstall() throws Exception
    {
        String oldInstallPath = getOldInstallPath();
        // The text will be to long for one line. Therefore we should use
        // the multi line label. Unfortunately it has no icon. Nothing is
        // perfect...
        String noLuck = parent.langpack.getString("CheckedHelloPanel.productAlreadyExist0")
                + oldInstallPath
                + parent.langpack.getString("CheckedHelloPanel.productAlreadyExist1");
        return (askQuestion(parent.langpack.getString("installer.error"), noLuck,
                AbstractUIHandler.CHOICES_YES_NO) == AbstractUIHandler.ANSWER_YES);
    }

    /**
     * Returns wether the handled application is already registered or not. The validation will be
     * made only on systems which contains a registry (Windows).
     *
     * @return wether the handled application is already registered or not
     */
    protected boolean isRegistered()
    {
        boolean retval = false;
        try
        {
            // Get the default registry handler.
            Debug.log("CheckedHelloPanel - Getting Registryhandler ...");
            RegistryHandler rh = RegistryDefaultHandler.getInstance();
            if (rh != null)
            {
                Debug.log("CheckedHelloPanel - Got " + rh.toString());

                rh.verify(idata);
                retval = rh.isProductRegistered();

                // test adxadmin déjà installé avec registry
                if (!retval && idata.info.isAdxAdmin())
                {
                    retval = rh.adxadminProductRegistered();
                }
            }
            else
            {
                Debug.log("CheckedHelloPanel - Could not get RegistryHandler !");

                // else we are on a os which has no registry or the
                // needed dll was not bound to this installation. In
                // both cases we forget the "already exist" check.
                
                // test adxadmin sous unix avec /adonix/adxadm ?
                if (!retval && idata.info.isAdxAdmin())
                {
                    if (OsVersion.IS_UNIX)
                    {
                        java.io.File adxadmFile = new java.io.File ("/sage/adxadm");
                        if (!adxadmFile.exists())
                        {
                            adxadmFile = new java.io.File ("/adonix/adxadm");
                            if (adxadmFile.exists())
                            {
                                retval = true;
                            }
                        }
                        else
                        {
                            retval = true;
                        }
                    }
                }
            }

        }
        catch (Exception e)
        { // Will only be happen if registry handler is good, but an
            // exception at performing was thrown. This is an error...
            e.printStackTrace();
        }
        return (retval);
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return true if the internal abort flag is not set, else false
     */
    public boolean isValidated()
    {
        return (!abortInstallation);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.installer.IzPanel#panelActivate()
     */
    public void panelActivate()
    {
        if (idata.info.needAdxAdmin())
        {
            try
            {
                // vérifier la présence d'un adxadmin
                RegistryHandler rh = RegistryDefaultHandler.getInstance();
                if (rh != null)
                {
    
                    rh.verify(idata);
    
                    // test adxadmin déjà installé avec registry
                    if (!rh.adxadminProductRegistered())
                    {
                        // pas d'adxadmin
                        JOptionPane.showMessageDialog(null, parent.langpack.getString( "adxadminNotRegistered"), parent.langpack.getString( "installer.error"), JOptionPane.ERROR_MESSAGE);
                        parent.lockNextButton();
                        return;
                    }
                }
                else
                {
    
                    // else we are on a os which has no registry or the
                    // needed dll was not bound to this installation. In
                    // both cases we forget the "already exist" check.
                    
                    // test adxadmin sous unix avec /adonix/adxadm ?
                        if (OsVersion.IS_UNIX)
                        {
                            java.io.File adxadmFile = new java.io.File ("/sage/adxadm");
                            if (!adxadmFile.exists())
                            {
                                adxadmFile = new java.io.File ("/adonix/adxadm");
                                if (!adxadmFile.exists())
                                {
                                    // pas d'adxadmin
                                    JOptionPane.showMessageDialog(null, parent.langpack.getString( "adxadminNotRegistered"), parent.langpack.getString( "installer.error"), JOptionPane.ERROR_MESSAGE);
                                    parent.lockNextButton();
                                    return;
                                }
                            }
                        }
                }
            }
            catch (Exception e)
            { // Will only be happen if registry handler is good, but an
                // exception at performing was thrown. This is an error...
                Debug.log(e);
                JOptionPane.showMessageDialog(null, e.getMessage(), parent.langpack.getString( "installer.error"), JOptionPane.ERROR_MESSAGE);
                parent.lockNextButton();
                return;
            }
        }
        
        
        
        if (abortInstallation)
        {
            
            // test whether multiple install is allowed
            String disallowMultipleInstall = idata.getVariable("CheckedHelloPanel.disallowMultipleInstance");
            
            if (!Boolean.TRUE.toString().equalsIgnoreCase(disallowMultipleInstall))
            {
                parent.lockNextButton();
                try
                {
                    if (multipleInstall())
                    {
                        setUniqueUninstallKey();
                        abortInstallation = false;
                        parent.unlockNextButton();
                    }
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            else
            {
                
                String allowUpdateMode = idata.getVariable("CheckedHelloPanel.allowUpdateMode");
                
                if (Boolean.TRUE.toString().equalsIgnoreCase(allowUpdateMode))
                {
                    try
                    {
                        String oldInstallPath = getOldInstallPath();
                        
                        if (askQuestion(parent.langpack.getString("updateMode"), parent.langpack.getString("compFoundAskUpdate"), AbstractUIHandler.CHOICES_YES_NO, AbstractUIHandler.ANSWER_YES) == AbstractUIHandler.ANSWER_YES )
                        {
                        
                            idata.setInstallPath(oldInstallPath);
                            // positionnement update
                            Debug.trace("modification installation");
                            idata.setVariable(InstallData.MODIFY_INSTALLATION, "true");
                            abortInstallation = false;
                            parent.unlockNextButton();
                        
                        }
                        else
                        {

                            parent.lockNextButton();
                            
                        }
                    }
                    catch (Exception e)
                    {
                        emitNotification(parent.langpack
                                .getString("FinishPanel.fail"));
                    }

                }
                else
                {
                    parent.lockNextButton();
                    emitNotification(parent.langpack
                            .getString("CheckedHelloPanel.infoMultipleInstallNotAllowed"));
                    
                }
                
            }

        }
        RegistryHandler rh = RegistryDefaultHandler.getInstance();
        if (rh != null)
        {
            idata.setVariable("UNINSTALL_NAME", rh.getUninstallName());
            // set service name for dos batch and call to procrun
            idata.setVariable("PROCRUN_SERVICE", rh.getUninstallName().replace(' ', '_'));
        }
    }

    /**
     * @throws Exception
     */
    private void setUniqueUninstallKey() throws Exception
    {
        // Let us play a little bit with the regstry again...
        // Now we search for an unique uninstall key.
        // First we need a handler. There is no overhead at a
        // secound call of getInstance, therefore we do not buffer
        // the handler in this class.
        RegistryHandler rh = RegistryDefaultHandler.getInstance();
        int oldVal = rh.getRoot(); // Only for security...
        // We know, that the product is already installed, else we
        // would not in this method. First we get the
        // "default" uninstall key.
        if (oldVal > 100) // Only to inhibit warnings about local variable never read.
        {
            return;
        }
        String uninstallName = rh.getUninstallName();
        int uninstallModifier = 1;
        while (true)
        {
            if (uninstallName == null)
            {
                break; // Should never be...
            }
            // Now we define a new uninstall name.
            String newUninstallName = uninstallName + "(" + Integer.toString(uninstallModifier)
                    + ")";
            // Then we "create" the reg key with it.
            String keyName = RegistryHandler.UNINSTALL_ROOT + newUninstallName;
            rh.setRoot(HKEY_LOCAL_MACHINE);
            if (!rh.keyExist(keyName))
            { // That's the name for which we searched.
                // Change the uninstall name in the reg helper.
                rh.setUninstallName(newUninstallName);
                // Now let us inform the user.
                emitNotification(parent.langpack
                        .getString("CheckedHelloPanel.infoOverUninstallKey")
                        + newUninstallName);
                // Now a little hack if the registry spec file contains
                // the pack "UninstallStuff".
                break;
            }
            uninstallModifier++;
        }
    }

}
