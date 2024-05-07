package com.izforge.izpack.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class WinRegistry {

	public static final int HKEY_CLASSES_ROOT = 0x80000000;
	public static final int HKEY_CURRENT_USER = 0x80000001;
	public static final int HKEY_LOCAL_MACHINE = 0x80000002;
	private static final String CLASSES_ROOT = "HKEY_CLASSES_ROOT";
	private static final String CURRENT_USER = "HKEY_CURRENT_USER";
	private static final String LOCAL_MACHINE = "HKEY_LOCAL_MACHINE";
	private static Preferences userRoot = Preferences.userRoot();
	private static Preferences systemRoot = Preferences.systemRoot();

	/**
	 * Reads value for the key from given path
	 * 
	 * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	 * @param path
	 * @param key
	 * @return the value
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws IOException
	 */
	public static String valueForKey(int hkey, String path, String key)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException {
		
		// if (Java11API) {
			if (hkey == HKEY_LOCAL_MACHINE)
				return valueForKeyJava11(systemRoot, path, key);
			else if (hkey == HKEY_CURRENT_USER)
				return valueForKeyJava11(userRoot, path, key);
			else
				return valueForKeyJava11(null, path, key);			
		//}
//		else {
//		if (hkey == HKEY_LOCAL_MACHINE)
//			return valueForKey(systemRoot, hkey, path, key);
//		else if (hkey == HKEY_CURRENT_USER)
//			return valueForKey(userRoot, hkey, path, key);
//		else
//			return valueForKey(null, hkey, path, key);
//		}
	}

	/**
	 * Reads all key(s) and value(s) from given path
	 * 
	 * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	 * @param path
	 * @return the map of key(s) and corresponding value(s)
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws BackingStoreException 
	 */
	public static Map<String, String> valuesForPath(int hkey, String path)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException, BackingStoreException {
		if (hkey == HKEY_LOCAL_MACHINE)
			return valuesForPath(systemRoot, path);
		else if (hkey == HKEY_CURRENT_USER)
			return valuesForPath(userRoot,  path);
		else
			return valuesForPath(null, path);
	}

	/**
	 * Read all the subkey(s) from a given path
	 * 
	 * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	 * @param path
	 * @return the subkey(s) list
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws BackingStoreException 
	 */
	public static List<String> subKeysForPath(int hkey, String path)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, BackingStoreException {
		if (hkey == HKEY_LOCAL_MACHINE)
			return subKeysForPath(systemRoot, path);
		else if (hkey == HKEY_CURRENT_USER)
			return subKeysForPath(userRoot, path);
		else
			return subKeysForPath(null, path);
	}

	/**
	 * Create a key
	 * 
	 * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	 * @param key
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void createKey(int hkey, String key)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		// int[] ret;
		if (hkey == HKEY_LOCAL_MACHINE) {
			// Create the node corresponding to the specified path
			createKey(systemRoot, key);
			// ret = createKey(systemRoot, hkey, key);
			// regCloseKey.invoke(systemRoot, new Object[] { new Integer(ret[0]) });
		} else if (hkey == HKEY_CURRENT_USER) {
			createKey(userRoot, key);
			// ret = createKey(userRoot, hkey, key);
			// regCloseKey.invoke(userRoot, new Object[] { new Integer(ret[0]) });
		} else
			throw new IllegalArgumentException("hkey=" + hkey);
		// if (ret[1] != REG_SUCCESS)
		// 	throw new IllegalArgumentException("rc=" + ret[1] + "  key=" + key);
	}

	/**
	 * Write a value in a given key/value name
	 * 
	 * @param hkey
	 * @param key
	 * @param valueName
	 * @param value
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws BackingStoreException 
	 */
	public static void writeStringValue(int hkey, String key, String valueName, String value)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, BackingStoreException {
		if (hkey == HKEY_LOCAL_MACHINE)
			writeStringValue(systemRoot, key, valueName, value);
		else if (hkey == HKEY_CURRENT_USER)
			writeStringValue(userRoot, key, valueName, value);
		else
			throw new IllegalArgumentException("hkey=" + hkey);
	}

	/**
	 * Delete a given key
	 * 
	 * @param hkey
	 * @param key
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws BackingStoreException 
	 */
	public static void deleteKey(int hkey, String key) throws BackingStoreException {
		if (hkey == HKEY_LOCAL_MACHINE)
			deleteKey(systemRoot, key);
		else if (hkey == HKEY_CURRENT_USER)
			deleteKey(userRoot,  key);
	}

	/**
	 * delete a value from a given key/value name
	 * 
	 * @param hkey
	 * @param key
	 * @param value
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void deleteValue(int hkey, String key, String value)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		// int rc = -1;
		if (hkey == HKEY_LOCAL_MACHINE)
			deleteValue(systemRoot, key, value);
		else if (hkey == HKEY_CURRENT_USER)
			deleteValue(userRoot,key, value);
		// if (rc != REG_SUCCESS)
		//	throw new IllegalArgumentException("rc=" + rc + "  key=" + key + "  value=" + value);
	}

	// =====================

	private static void deleteValue(Preferences root, String key, String value)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		// int[] handles = (int[]) regOpenKey.invoke(root, new Object[] { new Integer(hkey), toCstr(key), new Integer(KEY_ALL_ACCESS) });
		//if (handles[1] != REG_SUCCESS)
		// 	return handles[1]; // can be REG_NOTFOUND, REG_ACCESSDENIED
		//int rc = ((Integer) regDeleteValue.invoke(root, new Object[] { new Integer(handles[0]), toCstr(value) }))
		//		.intValue();
		//regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) });
		// return rc;
        Preferences node = root.node(key);
        node.remove(value);
		
	}

	private static void deleteKey(Preferences root, String key) throws BackingStoreException {
		// Java 8
		// int rc = ((Integer) regDeleteKey.invoke(root, new Object[] { new Integer(hkey), toCstr(key) })).intValue();
		// return rc; // can REG_NOTFOUND, REG_ACCESSDENIED, REG_SUCCESS
		// Open the node corresponding to the specified path
        Preferences node = root.node(key);
        node.removeNode();
	}

//	private static String valueForKey(Preferences root, int hkey, String path, String key)
//			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException {
//		int[] handles = (int[]) regOpenKey.invoke(root,
//				new Object[] { new Integer(hkey), toCstr(path), new Integer(KEY_READ) });
//		if (handles[1] != REG_SUCCESS)
//			throw new IllegalArgumentException(
//					"The system can not find the specified path: '" + getParentKey(hkey) + "\\" + path + "'");
//		byte[] valb = (byte[]) regQueryValueEx.invoke(root, new Object[] { new Integer(handles[0]), toCstr(key) });
//		regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) });
//		return (valb != null ? parseValue(valb) : queryValueForKey(hkey, path, key));
//	}

	private static String valueForKeyJava11(Preferences root, String path, String key)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException {
		
		Preferences node = root.node(path);
		String value = node.get(key, "");
		return value;
		// long[] handles = (long[]) regOpenKey.invoke(root, new Object[] { new Integer(hkey), toCstr(path), new Integer(KEY_READ) });
		//if (handles[1] != REG_SUCCESS)
		//	throw new IllegalArgumentException( "The system can not find the specified path: '" + getParentKey(hkey) + "\\" + path + "'");
		// byte[] valb = (byte[]) regQueryValueEx.invoke(root, new Object[] { new Long(handles[0]), toCstr(key) });
		// regCloseKey.invoke(root, new Object[] { new Long(handles[0]) });
		// return (valb != null ? parseValue(valb) : queryValueForKey(hkey, path, key));
	}

	
	
	private static String queryValueForKey(int hkey, String path, String key) throws IOException {
		return queryValuesForPath(hkey, path).get(key);
	}

	private static Map<String, String> valuesForPath(Preferences root, String path)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException, BackingStoreException {

		HashMap<String, String> results = new HashMap<String, String>();
		// Java 8
//		int[] handles = (int[]) regOpenKey.invoke(root, new Object[] { new Integer(hkey), toCstr(path), new Integer(KEY_READ) });
//		if (handles[1] != REG_SUCCESS)
//			throw new IllegalArgumentException( "The system can not find the specified path: '" + getParentKey(hkey) + "\\" + path + "'");
//		int[] info = (int[]) regQueryInfoKey.invoke(root, new Object[] { new Integer(handles[0]) });
//		int count = info[2]; // Fixed: info[0] was being used here
//		int maxlen = info[4]; // while info[3] was being used here, causing wrong results
//		for (int index = 0; index < count; index++) {
//			byte[] valb = (byte[]) regEnumValue.invoke(root,
//					new Object[] { new Integer(handles[0]), new Integer(index), new Integer(maxlen + 1) });
//			String vald = parseValue(valb);
//			if (valb == null || vald.isEmpty())
//				return queryValuesForPath(hkey, path);
//			results.put(vald, valueForKey(root, hkey, path, vald));
//		}
//		regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) });
		
		// Java 11
		Preferences node = root.node(path);
		// Retrieve all keys (value names) under the specified node
        String[] keys = node.keys();
     // Iterate over the keys and print their names and values
        if (keys.length > 0) {
            // System.out.println("Values under registry path " + path + ":");
            for (String key : keys) {
                String value = node.get(key, null);
                if (value != null) {
                    // System.out.println(key + ": " + value);
                	results.put(key, value);
                }
            }
        } else {
            // System.out.println("No values found under registry path " + path);
        }
		return results;
	}

	/**
	 * Searches recursively into the path to find the value for key. This method
	 * gives only first occurrence value of the key. If required to get all values
	 * in the path recursively for this key, then
	 * {@link #valuesForKeyPath(int hkey, String path, String key)} should be used.
	 * 
	 * @param hkey
	 * @param path
	 * @param key
	 * @param list
	 * @return the value of given key obtained recursively
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws BackingStoreException 
	 */
	public static String valueForKeyPath(int hkey, String path, String key)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException, BackingStoreException {
		String val;
		try {
			val = valuesForKeyPath(hkey, path, key).get(0);
		} catch (IndexOutOfBoundsException e) {
			throw new IllegalArgumentException("The system can not find the key: '" + key + "' after "
					+ "searching the specified path: '" + getParentKey(hkey) + "\\" + path + "'");
		}
		return val;
	}

	/**
	 * Searches recursively into given path for particular key and stores obtained
	 * value in list
	 * 
	 * @param hkey
	 * @param path
	 * @param key
	 * @param list
	 * @return list containing values for given key obtained recursively
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws BackingStoreException 
	 */
	public static List<String> valuesForKeyPath(int hkey, String path, String key)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException, BackingStoreException {
		List<String> list = new ArrayList<String>();
		if (hkey == HKEY_LOCAL_MACHINE)
			return valuesForKeyPath(systemRoot, hkey, path, key, list);
		else if (hkey == HKEY_CURRENT_USER)
			return valuesForKeyPath(userRoot, hkey, path, key, list);
		else
			return valuesForKeyPath(null, hkey, path, key, list);
	}

	private static List<String> valuesForKeyPath(Preferences root, int hkey, String path, String key, List<String> list)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException, BackingStoreException {
		if (!isDirectory(root, path)) {
			takeValueInListForKey(hkey, path, key, list);
		} else {
			List<String> subKeys = subKeysForPath(root, path);
			for (String subkey : subKeys) {
				String newPath = path + "\\" + subkey;
				if (isDirectory(root, newPath))
					valuesForKeyPath(root, hkey, newPath, key, list);
				takeValueInListForKey(hkey, newPath, key, list);
			}
		}
		return list;
	}

	/**
	 * Takes value for key in list
	 * 
	 * @param hkey
	 * @param path
	 * @param key
	 * @param list
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws IOException
	 */
	private static void takeValueInListForKey(int hkey, String path, String key, List<String> list)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException {
		String value = valueForKey(hkey, path, key);
		if (value != null)
			list.add(value);
	}

	/**
	 * Checks if the path has more subkeys or not
	 * 
	 * @param root
	 * @param hkey
	 * @param path
	 * @return true if path has subkeys otherwise false
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws BackingStoreException 
	 */
	private static boolean isDirectory(Preferences root, String path)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, BackingStoreException {
		return !subKeysForPath(root,  path).isEmpty();
	}

	private static List<String> subKeysForPath(Preferences root, String path)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, BackingStoreException {
		List<String> results = new ArrayList<String>();

//		int[] handles = (int[]) regOpenKey.invoke(root,
//				new Object[] { new Integer(hkey), toCstr(path), new Integer(KEY_READ) });
//		if (handles[1] != REG_SUCCESS)
//			throw new IllegalArgumentException(
//					"The system can not find the specified path: '" + getParentKey(hkey) + "\\" + path + "'");
//		int[] info = (int[]) regQueryInfoKey.invoke(root, new Object[] { new Integer(handles[0]) });
//		int count = info[0]; // Fix: info[2] was being used here with wrong results. Suggested by davenpcj,
//								// confirmed by Petrucio
//		int maxlen = info[3]; // value length max
//		for (int index = 0; index < count; index++) {
//			byte[] valb = (byte[]) regEnumKeyEx.invoke(root,
//					new Object[] { new Integer(handles[0]), new Integer(index), new Integer(maxlen + 1) });
//			results.add(parseValue(valb));
//		}
//		regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) });

		Preferences node = root.node(path);
		// Retrieve all subkeys (child nodes) under the specified node
		String[] subKeys = node.childrenNames();
		if (subKeys.length > 0) {
			// System.out.println("Subkeys under registry path " + keyPath +
			// ":");
			for (String subKey : subKeys) {
				results.add(subKey);
				// System.out.println(subKey);
			}
		} else {
			// System.out.println("No subkeys found under registry path " +
			// keyPath);
		}

		return results;
	}

	private static void createKey(Preferences root, String key)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		root.node(key);
	}

	private static void writeStringValue(Preferences root, String key, String valueName, String value)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, BackingStoreException {
		// Java 8 
//		int[] handles = (int[]) regOpenKey.invoke(root, new Object[] { new Integer(hkey), toCstr(key), new Integer(KEY_ALL_ACCESS) });
//		regSetValueEx.invoke(root, new Object[] { new Integer(handles[0]), toCstr(valueName), toCstr(value) });
//		regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) });
		// Java 11
		Preferences node = root.node(key);
		node.put(valueName, value);
		node.flush();
	}

	/**
	 * Makes cmd query for the given hkey and path then executes the query
	 * 
	 * @param hkey
	 * @param path
	 * @return the map containing all results in form of key(s) and value(s)
	 *         obtained by executing query
	 * @throws IOException
	 */
	private static Map<String, String> queryValuesForPath(int hkey, String path) throws IOException {
		String line;
		StringBuilder builder = new StringBuilder();
		Map<String, String> map = new HashMap<String, String>();
		Process process = Runtime.getRuntime().exec("reg query \"" + getParentKey(hkey) + "\\" + path + "\"");
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		while ((line = reader.readLine()) != null) {
			if (!line.contains("REG_"))
				continue;
			StringTokenizer tokenizer = new StringTokenizer(line, " \t");
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if (token.startsWith("REG_"))
					builder.append("\t ");
				else
					builder.append(token).append(" ");
			}
			String[] arr = builder.toString().split("\t");
			map.put(arr[0].trim(), arr[1].trim());
			builder.setLength(0);
		}
		return map;
	}

	/**
	 * Determines the string equivalent of hkey
	 * 
	 * @param hkey
	 * @return string equivalent of hkey
	 */
	private static String getParentKey(int hkey) {
		if (hkey == HKEY_CLASSES_ROOT)
			return CLASSES_ROOT;
		else if (hkey == HKEY_CURRENT_USER)
			return CURRENT_USER;
		else if (hkey == HKEY_LOCAL_MACHINE)
			return LOCAL_MACHINE;
		return null;
	}
}