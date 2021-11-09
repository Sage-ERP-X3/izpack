package com.sage.izpack;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.installer.validator.ConditionValidator;

public class FinishNewPanelValidator  extends ConditionValidator{

	private static final Logger logger = Logger.getLogger(FinishNewPanelValidator.class.getName());

	@Override
    public Status validateData(InstallData idata)
    {
    	Status validateResult = super.validateData(idata);    	
		logger.log(Level.FINE, "FinishNewPanelValidator.validateData - validateData: " + validateResult);
    	return validateResult;
    }
	
}
