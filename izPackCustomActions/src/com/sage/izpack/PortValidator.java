package com.sage.izpack;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.panels.userinput.processorclient.ProcessingClient;

public class PortValidator implements DataValidator, com.izforge.izpack.panels.userinput.validator.Validator {

	private static final Logger logger = Logger.getLogger(PortValidator.class.getName());
	private static final String PARAM_EXCLUDED_PORTS = "excluded";
	 
	@Override
	public boolean validate(ProcessingClient client) {

		boolean result = false;
		String moduleName = client.getText();

		logger.log(Level.FINE, "PortValidator.validate  ModuleName: " + moduleName);
		
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWarningMessageId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Status validateData(InstallData arg0) {
		// TODO Auto-generated method stub
		return null;
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
