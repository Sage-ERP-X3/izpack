package com.sage.izpack;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.installer.validator.ConditionValidator;


public class ReadMeNewPanelValidator  extends ConditionValidator{

	private static final Logger logger = Logger.getLogger(ReadMeNewPanelValidator.class.getName());

	@Override
    public Status validateData(InstallData idata)
    {
    	Status validateResult = super.validateData(idata);    	
		logger.log(Level.FINE, "ReadMeNewPanelValidator.validateData - validateData: " + validateResult);
    	return validateResult;
    }
	
}
