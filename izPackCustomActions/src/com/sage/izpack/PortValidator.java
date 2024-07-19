package com.sage.izpack;

import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.panels.userinput.processorclient.ProcessingClient;

public class PortValidator implements DataValidator, com.izforge.izpack.panels.userinput.validator.Validator {

	private static final Logger logger = Logger.getLogger(PortValidator.class.getName());
	private static final String PARAM_EXCLUDED_PORTS = "excluded";
	
	private InstallData installData;
	
	public PortValidator(InstallData installData) {
		super();
		this.installData = installData;
	}
	
	@Override
	public boolean validate(ProcessingClient client) {

		boolean result = false;
		String moduleName = client.getText();

		logger.log(Level.FINE, "PortValidator.validate  ModuleName: " + moduleName);
		
		InetAddress inet = null;
        String host = "localhost";
        int numfields = client.getNumFields();
        List<String> exludedPorts = new ArrayList<String>();
        
		boolean modifyinstallation = ModifyInstallationUtil.get(installData);

		// VariableSubstitutor substitutor = new VariableSubstitutorImpl(this.installData.getVariables());

		/*
		if (client.hasParams())
        {
            String param = client.getValidatorParams().get(PARAM_EXCLUDED_PORTS);
            
            if (param!=null && !"".equals(param)) 
            {
                VariableSubstitutor vs = new VariableSubstitutor(adata.getVariables());
                param = vs.substitute(param, null);
                exludedPorts.addAll(Arrays.asList( param.split(";")));
            }
        }
*/
		
        for (int i = 0; i < numfields; i++)
        {
            String value = client.getFieldContents(i);
            if ((value == null) || (value.length() == 0))
            {
        		logger.log(Level.FINE, "PortValidator.validate  Port value is null");
                return false;
            }
            else if (modifyinstallation && exludedPorts.contains(value.trim())) { 
            	continue;
            }
            
            try
            {
                Socket socket = new Socket("localhost",Integer.parseInt(value));
                socket.close();
                // Someone responding on port - seems not open
                logger.log(Level.FINE, "Someone responding on port - seems not open");
                result = false;
            }
            catch (Exception ex)
            {
                logger.log(Level.FINE, "No answer on port " + value+ " - seems available");
            	logger.log(Level.FINE, ex.getMessage());
            	// ex.printStackTrace();
                result=true;
            }
        }
		
		return result;
	}


	@Override
	public boolean getDefaultAnswer() {
		// can we validate in automated mode ?
		// say yes for now
		return false;
	}

	@Override
	public String getErrorMessageId() {
		return "portinuse";	
	}

	@Override
	public String getWarningMessageId() {
		return "portinuse";	
		}

	@Override
	public Status validateData(InstallData arg0) {
		return DataValidator.Status.OK;
	}

}

/*
 
    private static final String PARAM_EXCLUDED_PORTS = "excluded";

    public boolean validate(ProcessingClient client, AutomatedInstallData adata)
    {
        InetAddress inet = null;
        String host = "localhost";
        boolean retValue = true;
        int numfields = client.getNumFields();
        List<String> exludedPorts = new ArrayList<String>();
        Boolean modifyinstallation = Boolean.valueOf(adata.getVariable(InstallData.MODIFY_INSTALLATION));

        if (client.hasParams())
        {
            String param = client.getValidatorParams().get(PARAM_EXCLUDED_PORTS);
            
            if (param!=null && !"".equals(param)) 
            {
                VariableSubstitutor vs = new VariableSubstitutor(adata.getVariables());
                param = vs.substitute(param, null);
                exludedPorts.addAll(Arrays.asList( param.split(";")));
            }
        }

        for (int i = 0; i < numfields; i++)
        {
            String value = client.getFieldContents(i);

            if ((value == null) || (value.length() == 0))
            {
                Debug.log("Port value is null");
                return false;
            }
            else if (modifyinstallation && exludedPorts.contains(value.trim())) continue;

            try
            {
                Socket socket = new Socket("localhost",Integer.parseInt(value));
                socket.close();
                Debug.log("Someone responding on port - seems not open");
                // Someone responding on port - seems not open
                retValue = false;
            }
            catch (Exception ex)
            {
                Debug.log(ex);
                retValue=true;
            }
        }
        return retValue;
    }
 
 */ 
