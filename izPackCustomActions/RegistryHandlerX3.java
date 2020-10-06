/*
 * $Id$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2005 Klaus Bartz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package src.com.sage.izpack;

import com.izforge.izpack.core.os.RegistryHandler;
// import com.izforge.izpack.util.OSClassHelper;
import com.izforge.izpack.api.exception.NativeLibException;

/**
 * This class represents a registry handler in a operating system independent way. OS specific
 * subclasses are used to implement the necessary mapping from this generic API to the classes that
 * reflect the system dependent AIP.
 *
 * @author Klaus Bartz
 */
public class RegistryHandlerX3 extends RegistryHandler//  implements MSWinConstants
{
 
    // public boolean adxadminProductRegistered() throws NativeLibException
    public boolean adxadminProductRegistered() throws NativeLibException
    {
        String keyName = "SOFTWARE\\Adonix\\X3RUNTIME\\ADXADMIN";
        int oldVal = getRoot();
        setRoot(HKEY_LOCAL_MACHINE);
        boolean retval = keyExist(keyName);
        keyName = "SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN";
        retval = retval || keyExist(keyName);
        setRoot(oldVal);
        return (retval);        
    }


}
