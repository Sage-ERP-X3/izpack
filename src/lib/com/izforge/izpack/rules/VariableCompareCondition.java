package com.izforge.izpack.rules;

import com.izforge.izpack.util.Debug;

public class VariableCompareCondition extends VariableCondition
{
    /**
     * 
     */
    private static final long serialVersionUID = 9133334869593548118L;

    public boolean isTrue()
    {
        if (this.installdata != null)
        {
            String val = this.installdata.getVariable(variablename);
            String val2 = this.installdata.getVariable(value);

            Debug.trace("Compare condition variable '"+variablename+"' = '"+val+"' and  '"+value+"' = '"+val2+"' Result: '"+ (val == null ? false : val.equals(val2)) + "'.");
            
            if (val == null)
            {
                return false;
            }
            else
            {
                return val.equals(val2);
            }
        }
        else
        {
            return false;
        }
    }

}
