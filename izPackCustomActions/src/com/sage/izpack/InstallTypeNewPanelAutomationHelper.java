package com.sage.izpack;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.installer.automation.PanelAutomationHelper;


public class InstallTypeNewPanelAutomationHelper extends PanelAutomationHelper 
{


    public void startAction(String name, int no_of_steps)
    {
        // Nothing to do
    }

    public void stopAction()
    {
        // Nothing to do

    }

    public void nextStep(String step_name, int step_no, int no_of_substeps)
    {
        // Nothing to do

    }

    public void setSubStepNo(int no_of_substeps)
    {
        // Nothing to do

    }

    public void progress(int substep_no, String message)
    {
        // Nothing to do

    }

    public void createInstallationRecord(AutomatedInstallData installData, IXMLElement panelRoot)
    {
        // part of MODIFY_INSTALLATION
        IXMLElement ipath = new XMLElementImpl(InstallData.MODIFY_INSTALLATION, panelRoot);
        // check this writes even if value is the default,
        // because without the constructor, default does not get set.
        if (installData.getVariable(InstallData.MODIFY_INSTALLATION) != null) {
        	ipath.setContent(installData.getVariable(InstallData.MODIFY_INSTALLATION));
        } else {
        	ipath.setContent(Boolean.FALSE.toString());
        }

        IXMLElement prev = panelRoot.getFirstChildNamed(InstallData.MODIFY_INSTALLATION);
        if (prev != null) {
        	panelRoot.removeChild(prev);
        }
        panelRoot.addChild(ipath);

        // part of target path
        IXMLElement ipath2 = new XMLElementImpl("installpath",panelRoot);
        ipath2.setContent(installData.getInstallPath());

        IXMLElement prev2 = panelRoot.getFirstChildNamed("installpath");
        if (prev2 != null)
        {
            panelRoot.removeChild(prev2);
        }
        panelRoot.addChild(ipath2);
        
        

    }

    public void runAutomated(AutomatedInstallData installData, IXMLElement panelRoot)
            throws InstallerException
    {

        // part of MODIFY_INSTALLATION
    	IXMLElement ipath = panelRoot.getFirstChildNamed(InstallData.MODIFY_INSTALLATION);

        String modify = null;
        
        try 
        {    
            modify=ipath.getContent().trim();
        }
        catch (Exception ex)
        {
            // assume a normal install 
            installData.setVariable(InstallData.MODIFY_INSTALLATION, "false");
        }
                 
        if (modify == null || "".equals(modify))
        {
            // assume a normal install 
            installData.setVariable(InstallData.MODIFY_INSTALLATION, "false");
        }
        else
        {
            if (Boolean.parseBoolean(modify))
            {
                installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");
            }
            else
            {
                installData.setVariable(InstallData.MODIFY_INSTALLATION, "false");
            }
        } 
        // part of target path
        IXMLElement ipath2 = panelRoot.getFirstChildNamed("installpath");

        String installpath = null;
        
        try 
        {    
            installpath=ipath2.getContent().trim();
            installData.setInstallPath(installpath);
            
            
            System.out.println();
            System.out.println(ResourcesHelper.getCustomPropString("TargetPanel.summaryCaption"));
            System.out.println(installpath);
            System.out.println();
            
            
        }
        catch (Exception ex)
        {
            // assume a normal install
            throw new InstallerException(ex.getLocalizedMessage());
        }
                 
    }

}
