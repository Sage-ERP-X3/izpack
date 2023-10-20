package com.sage.izpack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.handler.AbstractUIHandler;

public class PreValidatePacksPanelAction implements com.izforge.izpack.data.PanelAction  {

	private static final Logger logger = Logger.getLogger(PreValidatePacksPanelAction.class.getName());
    private Map installedpacks = null;

	@Override
	public void executeAction(InstallData installData, AbstractUIHandler arg1) {
        // well if in modify mode
        // then we must select already installed packs
		
		boolean modifyinstallation = Boolean.valueOf(installData.getVariable(InstallData.MODIFY_INSTALLATION));
		this.installedpacks = new HashMap();
		if (modifyinstallation)
        {
            // installation shall be modified
            // load installation information
			logger.log(Level.FINE, "PreValidatePacksPanelAction   Update mode, loading installed packs ...");
			
            try
            {
            	File file= new File(installData.getInstallPath() + File.separator + AutomatedInstallData.INSTALLATION_INFORMATION);
                FileInputStream fin = new FileInputStream(file);
                ObjectInputStream oin = new ObjectInputStream(fin);
                List packsinstalled = (List) oin.readObject();
                for (Object aPacksinstalled : packsinstalled)
                {
                    Pack installedpack = (Pack) aPacksinstalled;
                    /*
                    if ((installedpack.getImageId() != null) && (installedpack.getImageId().length() > 0))
                    {
                        installedpacks.put(installedpack.getImageId(), installedpack);
                        logger.log(Level.FINE, "PreValidatePacksPanelAction  Found " +installedpack.getImageId());
                    }
                    else
                    {
                    */
                        installedpacks.put(installedpack.getName(), installedpack);
                        logger.log(Level.FINE, "PreValidatePacksPanelAction  Found " +installedpack.getName());
                    //}
                }
                
                this.removeAlreadyInstalledPacks(installData.getSelectedPacks());
                // TODO: FRDEPO
                //   adata.installedPacks = packsinstalled;

                
                List<com.izforge.izpack.api.data.Pack> packages =  new ArrayList<com.izforge.izpack.api.data.Pack>();
                for (Object aPack : installData.getAvailablePacks())
                { // installedpacks.containsKey( ((Pack)aPack).getImageId() ) || 
                    if (installedpacks.containsKey( ((Pack)aPack).getName()) )
                    {
                    	((Pack)aPack).setPreselected(true);
                        // ((Pack)aPack).set.required = true;
                    	packages.add((com.izforge.izpack.api.data.Pack)aPack);                
                    }
                }
            	installData.setSelectedPacks(packages);

            	logger.log(Level.FINE,"PreValidatePacksPanelAction  Found " + packsinstalled.size() + " installed packs");
            	logger.log(Level.FINE,"PreValidatePacksPanelAction  Loading properties ...");
                
                Properties variables = (Properties) oin.readObject();
                
                Iterator iter = variables.keySet().iterator();
                while (iter.hasNext())
                {
                    String key = (String) iter.next();
                    if (Character.isLowerCase(key.charAt(0)) || "UNINSTALL_NAME".equals(key))
                    {
                    	installData.setVariable( key, (String) variables.get(key));
                    	logger.log(Level.FINE,(String) key+"="+ (String) variables.get(key));
                    }
                    
                    // TODO : verrue !
                    // if (key.equals("syracuse.winservice.username"))
                    //{
                    //	installData.setVariable( "syracuse.winservice.username.oldvalue", (String) variables.get(key));
                    //}
                }                              
                
                fin.close();                
            	
            } catch (FileNotFoundException e)
            {
            	logger.log(Level.WARNING,e.getMessage());
                e.printStackTrace();
            }
            catch (java.io.IOException e)
            {
            	logger.log(Level.WARNING,e.getMessage());
                e.printStackTrace();
            }
            catch (ClassNotFoundException e)
            {
            	logger.log(Level.WARNING,e.getMessage());
                e.printStackTrace();
            }
        }
		
	}

	@Override
	public void initialize(PanelActionConfiguration arg0) {
		// nothing to do really
	}

	
	private void removeAlreadyInstalledPacks(List<com.izforge.izpack.api.data.Pack> selectedpacks)
    {
        List<Pack> removepacks = new ArrayList<Pack>();

        for (Object selectedpack1 : selectedpacks)
        {
            Pack selectedpack = (Pack) selectedpack1;
            String key = selectedpack.getName();
            /*
            if ((selectedpack.getImageId() != null) && (selectedpack.getImageId().length() > 0))
            {
                key = selectedpack.getImageId();
            }
            else
            {
                key = selectedpack.getName();
            }
            */
            if (installedpacks.containsKey(key))
            {
                // pack is already installed, remove it
                removepacks.add(selectedpack);
            }
        }
        for (Pack removepack : removepacks)
        {
            selectedpacks.remove(removepack);
        }
    }
    

	
	/*
	 FRDEPO : TODO
	 SOURCE from SAGE izpack 4.3 : 

    private Map installedpacks = null;

    public void executeAction(AutomatedInstallData adata, AbstractUIHandler handler)
    {
        // well if in modify mode
        // then we must select already installed packs
        
        Boolean modifyinstallation = Boolean.valueOf(adata.getVariable(InstallData.MODIFY_INSTALLATION));
        installedpacks = new HashMap();

        if (modifyinstallation)
        {
            // installation shall be modified
            // load installation information
            Debug.trace("Update mode, loading installed packs ...");

            try
            {
                FileInputStream fin = new FileInputStream(new File(adata.getInstallPath() + File.separator + AutomatedInstallData.INSTALLATION_INFORMATION));
                ObjectInputStream oin = new ObjectInputStream(fin);
                List packsinstalled = (List) oin.readObject();
                for (Object aPacksinstalled : packsinstalled)
                {
                    Pack installedpack = (Pack) aPacksinstalled;
                    if ((installedpack.id != null) && (installedpack.id.length() > 0))
                    {
                        installedpacks.put(installedpack.id, installedpack);
                        Debug.trace("Found " +installedpack.id);
                    }
                    else
                    {
                        installedpacks.put(installedpack.name, installedpack);
                        Debug.trace("Found " +installedpack.name);
                    }
                }
                
                this.removeAlreadyInstalledPacks(adata.selectedPacks);
                adata.installedPacks = packsinstalled;
                
                for (Object aPack : adata.availablePacks)
                {
                    if (installedpacks.containsKey( ((Pack)aPack).id ) || installedpacks.containsKey( ((Pack)aPack).name ) )
                    {
                        adata.selectedPacks.add((Pack)aPack);
                        ((Pack)aPack).preselected = true;
                        ((Pack)aPack).required = true;
                
                    }
                }
                
                //adata.availablePacks.
                
                Debug.trace("Found " + packsinstalled.size() + " installed packs");
                Debug.trace("Loading properties ...");

                Properties variables = (Properties) oin.readObject();
                
                Iterator iter = variables.keySet().iterator();
                while (iter.hasNext())
                {
                    String key = (String) iter.next();
                    if (Character.isLowerCase(key.charAt(0)) || "UNINSTALL_NAME".equals(key))
                    {
                        adata.setVariable( key, (String) variables.get(key));
                        Debug.trace((String) key+"="+ (String) variables.get(key));
                    }
                    
                    // TODO : verrue !
                    if (key.equals("syracuse.winservice.username"))
                    {
                        adata.setVariable( "syracuse.winservice.username.oldvalue", (String) variables.get(key));
                    }
                }
                
                
                
                fin.close();
                
                
            }
            catch (FileNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (ClassNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        

    }

    public void initialize(PanelActionConfiguration configuration)
    {
        // nothing to do really

    }
    
    private void removeAlreadyInstalledPacks(List selectedpacks)
    {
        List<Pack> removepacks = new ArrayList<Pack>();

        for (Object selectedpack1 : selectedpacks)
        {
            Pack selectedpack = (Pack) selectedpack1;
            String key = "";
            if ((selectedpack.id != null) && (selectedpack.id.length() > 0))
            {
                key = selectedpack.id;
            }
            else
            {
                key = selectedpack.name;
            }
            if (installedpacks.containsKey(key))
            {
                // pack is already installed, remove it
                removepacks.add(selectedpack);
            }
        }
        for (Pack removepack : removepacks)
        {
            selectedpacks.remove(removepack);
        }
    }
    

 
	 */
	
}
