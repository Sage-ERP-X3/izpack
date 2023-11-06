package com.sage.izpack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.config.Options;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.handler.PromptUIHandler;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;

public class InstallTypeNewConsolePanel  implements ConsolePanel {

	private static Logger logger = Logger.getLogger(InstallTypeNewConsolePanel.class.getName());
	// private static final long serialVersionUID = 8430577231809871725L;
	private Resources resources;
	private PromptUIHandler handler;


	public InstallTypeNewConsolePanel(Resources resources, Prompt prompt, PanelView<ConsolePanel> panel) {
		// super(resources, panel);
		this.resources = resources;
		this.handler = new PromptUIHandler(prompt);
		logger.log(Level.FINE, "InstallTypeNewConsolePanel instance.");
	}

	@Override
	public boolean run(InstallData installData, Properties p) {

		  String strType = p.getProperty(InstallData.MODIFY_INSTALLATION).trim ();
	        if (strType == null || "".equals(strType))
	        {
	            // assume a normal install 
	            installData.setVariable(InstallData.MODIFY_INSTALLATION, "false");
	        }
	        else
	        {
	            if (Boolean.parseBoolean(strType))
	            {
	                // is a modify type install 
	                installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");	                
	                String strInstallpath = p.getProperty("installpath").trim ();
	                installData.setInstallPath(strInstallpath);
	                
	            }
	            else
	            {
	                installData.setVariable(InstallData.MODIFY_INSTALLATION, "false");
	            }
	        }
	return true;        
	}
    
    @Override
    public boolean run(InstallData installData, com.izforge.izpack.util.Console console) {
    	 
		ResourcesHelper resourcesHelper = new ResourcesHelper(installData, this.resources);
    	String str = resourcesHelper.getCustomString("InstallTypeNewPanel.info");         
         System.out.println("");
         System.out.println(str);

         int i = 0;
         while (i<1 || i>3)
         {
        	 i = this.handler.askQuestion("", resourcesHelper.getCustomString("InstallTypeNewPanel.asktype"), 3);
         }
         
         if (i==1)
         {
             installData.setVariable(InstallData.MODIFY_INSTALLATION, "false");
         }
         else if (i==2)
         {
             installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");             
             return chooseComponent (installData);
         }
         else
         {
             // want to exit
             return false;
         }
         
         return true;
    }


    private boolean chooseComponent(InstallData installData)
    {
		try {
		ResourcesHelper resourcesHelper = new ResourcesHelper(installData, this.resources);
        String strQuestion = resourcesHelper.getCustomString("InstallTypeNewPanel.askUpdatePath");        
		RegistryHandlerX3 helper = new RegistryHandlerX3(null, installData);
    	DefaultListModel<String> listItems = new DefaultListModel<String>();
        HashMap<String, String[]> installedProducts = helper.loadComponentsList(listItems);
        
        System.out.println();
        
		for (int i = 0; i < installedProducts.size(); i++) {
			String[] product = (String[]) installedProducts.get(i);
			System.out.println(i + " - " + product[0]);
			// System.out.println(i + " [" + (input.iSelectedChoice == i ? "x" : " ") + "] "
			// + (choice.strText != null ? choice.strText : ""));
		}
        
        System.out.println();
        
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true)
            {
                System.out.println(strQuestion);
                String strIn = br.readLine().trim();
                int j = -1;
                try
                {
                    j = Integer.valueOf(strIn).intValue();
                }
                catch (Exception ex)
                {}
                if (j>-1 && j<installedProducts.size())
                {
                    String[] product = (String[]) installedProducts.get(j);

                    installData.setInstallPath((String) product[2]);
                    return true;
                }
                else
                {
                    System.out.println(resourcesHelper.getCustomString("UserInputPanel.search.wrongselection.caption"));
                }
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }        
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}			

        return false;
    }

	@Override
	public void createInstallationRecord(IXMLElement arg0) {

		logger.log(Level.FINE, "InstallTypeNewConsolePanel createInstallationRecord.");

	}

	@Override
	public boolean generateOptions(InstallData installData, Options arg1) {

		logger.log(Level.FINE, "InstallTypeNewConsolePanel generateOptions.");
		return false;
	}

	@Override
	public boolean handlePanelValidationResult(boolean arg0) {


		logger.log(Level.FINE, "InstallTypeNewConsolePanel handlePanelValidationResult.");

		return false;
	}


	
}
