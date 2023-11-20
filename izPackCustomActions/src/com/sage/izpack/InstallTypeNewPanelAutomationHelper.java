package com.sage.izpack;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.AutomatedInstallData;
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

    public void makeXMLData(AutomatedInstallData installData, IXMLElement panelRoot)
    {
        // part of MODIFY_INSTALLATION
    	InstallTypeNewPanelAutomationHelper automationHelper = new InstallTypeNewPanelAutomationHelper();
        automationHelper.makeXMLData(installData, panelRoot);
        
        // part of target path
        
        IXMLElement ipath = new XMLElementImpl("installpath",panelRoot);
        ipath.setContent(installData.getInstallPath());

        IXMLElement prev = panelRoot.getFirstChildNamed("installpath");
        if (prev != null)
        {
            panelRoot.removeChild(prev);
        }
        panelRoot.addChild(ipath);
        
        

    }

    public void runAutomated(AutomatedInstallData installData, IXMLElement panelRoot)
            throws InstallerException
    {

        // part of MODIFY_INSTALLATION
    	InstallTypeNewPanelAutomationHelper automationHelper = new InstallTypeNewPanelAutomationHelper();
        automationHelper.runAutomated(installData, panelRoot);
        
        // part of target path
        IXMLElement ipath = panelRoot.getFirstChildNamed("installpath");

        String installpath = null;
        
        try 
        {    
            installpath=ipath.getContent().trim();
            installData.setInstallPath(installpath);
            
            
            System.out.println();
            // System.out.println(installData.langpack.getString("TargetPanel.summaryCaption"));
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
