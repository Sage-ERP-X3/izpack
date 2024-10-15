package com.sage.izpack;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.installer.validator.ConditionValidator;

public class PacksNewPanelValidator extends ConditionValidator {

	private static final Logger logger = Logger.getLogger(PacksNewPanelValidator.class.getName());

    @Override
    public Status validateData(InstallData idata)
    {
    	Status validateResult = super.validateData(idata);    	
		logger.log(Level.FINE, "PacksNewPanelValidator.validateData - validateData: " + validateResult);
    	return validateResult;
    }
	
    
}
