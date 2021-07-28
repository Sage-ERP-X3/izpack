package com.sage.izpack;

import com.izforge.izpack.installer.validator.ConditionValidator;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;

public class TargetNewPanelValidator extends ConditionValidator {

	
	private static final Logger logger = Logger.getLogger(TargetNewPanelValidator.class.getName());

    @Override
    public Status validateData(InstallData idata)
    {
    	Status validateResult = super.validateData(idata);    	
		logger.log(Level.FINE, "TargetNewPanelValidator.validateData - validateData: " + validateResult);
    	return validateResult;
    }
	
}
