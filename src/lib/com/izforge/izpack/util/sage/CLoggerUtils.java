package com.izforge.izpack.util.sage;

import static com.izforge.izpack.util.sage.CTextLineUtils.generateLineBeginEnd;
import static com.izforge.izpack.util.sage.CTextLineUtils.generateLineFull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * X3-250275 Compile Prerequisite Control (on OL and RHEL) #367
 * 
 * @author ogattaz
 *
 */
public class CLoggerUtils {

	/**
	 * 
	 * <pre>
	 * DATE(1)                   LEVEL(4) THREAD(3)         SOURCE(2): INSTANCE + METHOD                            LINE (5) + (6)
	 * <------- 24 car ------->..<-7c-->..<---- 16c ----->..<--------------------- 54c -------------------------->..<------------------ N characters  -------...
	 *                                                      <--------- 27c------------>..<----------25c ---------->
	 * Logger File
	 * 2019/02/12; 10:58:05:630; FINE   ;    SSEMonitor(1); SSEMachineRequestsMaps_6830;                 sendIddle; begin
	 * 2019/02/12; 10:58:05:630; FINE   ;    SSEMonitor(1); se.CSSEMachineRequests_9877;                 sendIddle; key=[(01,105)(cb6c8485-258a-496c-93a8-40aff9f997b7)] ...
	 * 2019/02/12; 10:58:05:630; FINE   ;    SSEMonitor(1); SSEMachineRequestsMaps_6830;                 sendIddle; end. NbSentIddle=[0]
	 * Logger console
	 * 2019/02/12; 15:59:28:339;   Infos;             main; apps.impl.CTestLogging_0842;                    doTest; SimpleFormatter current format=[%1$tY/%1$tm/%1$td; %1$tH:%1$tM:%1$tS:%1$tL; %4$7.7s; %3$16.016s; %2$54.54s; %5$s%6$s%n]
	 * 2019/02/12; 15:59:28:344;   Infos;             main; apps.impl.CTestLogging_0842;                    doTest; SimpleFormatter jvm property  =[%1$tY/%1$tm/%1$td; %1$tH:%1$tM:%1$tS:%1$tL; %4$7.7s; %3$16.016s; %2$54.54s; %5$s%6$s%n]
	 * 2019/02/12; 15:59:28:345;   Infos;             main; apps.impl.CTestLogging_0842;                    doTest; IsSimpleFormatterFormatValid=[true] / JulLogger: Name=[] Level=[ALL] 
	 * 2019/02/12; 15:59:28:346;   Infos;             main; apps.impl.CTestLogging_0842;                    doTest; logInfo: Ligne log info
	 * </pre>
	 * 
	 * 
	 * <pre>
	 * SimpleFormat=[%1$tY/%1$tm/%1$td; %1$tH:%1$tM:%1$tS:%1$tL; %4$7.7s; %3$16.016s; %2$54.54s; %5$s%6$s%n]
	 * </pre>
	 */
	public static final String SIMPLE_FORMATTER_FORMAT = "%1$tY/%1$tm/%1$td; %1$tH:%1$tM:%1$tS:%1$tL; %4$7.7s; %3$16.016s; %2$54.54s; %5$s%6$s%n";

	private static final Logger sLoggerRoot = Logger.getLogger("");

	/**
	 * 
	 */
	static {
		try {
			String wLoggerInitReport = setFormatOfSimpleFormatter(SIMPLE_FORMATTER_FORMAT);

			logInfo(wLoggerInitReport);
		} catch (Exception e) {
			logSevere(dumpStackTrace(e));
		}
	}

	/**
	 * @param e
	 * @return
	 */
	public static String dumpStackTrace(Throwable e) {
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		return errors.toString();
	}

	/**
	 * @param aLevel
	 * @param aText
	 * @return
	 */
	public static String log(final Level aLevel, final String aText) {

		sLoggerRoot.log(aLevel, aText);

		return aText;
	}

	/**
	 * @param aLevel
	 * @param aText
	 * @return
	 */
	public static String logBanner(final Level aLevel, final String aText) {
		log(aLevel, generateLineFull('#', 80));
		log(aLevel, generateLineBeginEnd('#', 80));
		log(aLevel, generateLineBeginEnd('#', 80, aText));
		log(aLevel, generateLineBeginEnd('#', 80));
		log(aLevel, generateLineFull('#', 80));
		return aText;
	}

	/**
	 * @param aLevel
	 * @param aFormat
	 * @param aArgs
	 * @return
	 */
	public static String logBanner(final Level aLevel, final String aFormat,
			final Object... aArgs) {
		return logBanner(aLevel, String.format(aFormat, aArgs));
	}

	/**
	 * @param aText
	 * @return
	 */
	public static String logInfo(final String aText) {

		return log(Level.INFO, aText);
	}

	/**
	 * @param aFormat
	 * @param aArgs
	 * @return
	 */
	public static String logInfo(final String aFormat, final Object... aArgs) {

		return log(Level.INFO, String.format(aFormat, aArgs));
	}

	/**
	 * @param aText
	 * @return
	 */
	public static String logSevere(final String aText) {

		return log(Level.SEVERE, aText);
	}

	/**
	 * @param aFormat
	 * @param aArgs
	 * @return
	 */
	public static String logSevere(final String aFormat,
			final Object... aArgs) {

		return log(Level.SEVERE, String.format(aFormat, aArgs));
	}

	/**
	 * @param e
	 * @return
	 */
	public static String logSevere(final Throwable e) {

		return log(Level.SEVERE, dumpStackTrace(e));
	}

	/**
	 * 
	 * <pre>
	 * 61    // format string for printing the log record
	 * 62    private static final String format = LoggingSupport.getSimpleFormat();
	 * </pre>
	 * 
	 * @param aFormat: the format to replace the format given by the method "LoggingSupport.getSimpleFormat() "
	 * @return the report of the setting
	 * @throws Exception
	 */
	private static String setFormatOfSimpleFormatter(final String aFormat) throws Exception {

		return setPrivateStaticFinalString(SimpleFormatter.class, "format", aFormat);
				
	}

	/**
	 * 
	 * @param aClass
	 * @param aFieldName
	 * @param aValue
	 * @return the report of the setting
	 * @throws Exception
	 */
	private static String setPrivateStaticFinalString(Class<?> aClass,
			final String aFieldName, final String aValue) throws Exception {

		try {

			java.lang.reflect.Field wTargetField = aClass.getDeclaredField(aFieldName);

			// Java 8
			// boolean wHasToRemovePrivate = !wTargetField.isAccessible();
			// remove "private"
			// if (wHasToRemovePrivate) {
			//	wTargetField.setAccessible(true);
			//}

			// Java 11
            boolean wHasToRemovePrivate = !wTargetField.canAccess(null);
            // remove "private"
            if (wHasToRemovePrivate) {
                wTargetField.setAccessible(true); // still works in Java 11
            }
			
			
			// remove "final"
			// @see https://stackoverflow.com/questions/3301635/change-private-static-final-field-using-java-reflection

			int wOriginalModifiers = wTargetField.getModifiers();
			boolean wHasToRemoveFinal = (wOriginalModifiers & ~Modifier.FINAL) != wOriginalModifiers;
			Field wModifiersField = null;

			if (wHasToRemoveFinal) {
				wModifiersField = Field.class.getDeclaredField("modifiers");
				wModifiersField.setAccessible(true);
				wModifiersField.setInt(wTargetField, wTargetField.getModifiers() & ~Modifier.FINAL);
			}

			// verif
			String wOldValue = String.valueOf(wTargetField.get(null));

			// set static field
			wTargetField.set(null, aValue);

			// verif
			String wNewValue = String.valueOf(wTargetField.get(null));

			// verif
			boolean wModified = aValue.equals(wNewValue);

			return String.format(
					"Modified static fied: [%s.%s]\n - Modified=[%b]\n - NewValue=[%s]\n - OldValue=[%s]", aClass.getSimpleName(),
					wTargetField.getName(), wModified, wNewValue, wOldValue);

		} catch (Exception e) {
			throw new Exception(String.format("ERROR: Unable to set the final field [%s.%s]",  aClass.getSimpleName(), aFieldName), e);
		}
	}

	/**
	 * never instanciate a Helper
	 */
	private CLoggerUtils() {
		super();
	}
}
