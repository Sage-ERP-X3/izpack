/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2006 Vladimir Ralev
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

package com.izforge.izpack.uninstaller;

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.IXMLParser;
import com.izforge.izpack.adaptator.impl.XMLParser;
import com.izforge.izpack.compiler.CompilerException;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.OsVersion;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class UninstallerConsole
{

    /**
     * The installation path.
     */
    protected String installPath;

    /**
     * The language pack.
     */
    protected static LocaleDatabase langpack;

    public UninstallerConsole() throws Exception
    {
        // Initializations
        langpack = new LocaleDatabase(UninstallerConsole.class.getResourceAsStream("/langpack.xml"));
        getInstallPath();
    }

    /**
     * Gets the installation path from the log file.
     *
     * @throws Exception Description of the Exception
     */
    private void getInstallPath() throws Exception
    {
        InputStream in = UninstallerFrame.class.getResourceAsStream("/install.log");
        InputStreamReader inReader = new InputStreamReader(in);
        BufferedReader reader = new BufferedReader(inReader);
        installPath = reader.readLine();
        reader.close();
    }

    /**
     * Runs the cmd line uninstaller.
     *
     * @param destroy same as the destroy option in the GUI.
     */
    public void runUninstall(boolean destroy)
    {
        try
        {
        
            // we must test here if we are an adxadmin setup
            // thus allowing to test that there are no components installed anymore in the file adxinstall.xml
            if (UninstallerConsole.class.getResource("/is-adxadmin")!=null)
            {
                File adxinstall = new File (installPath+"/inst/adxinstalls.xml");
                if (adxinstall.exists())
                {
                    try
                    {
                        IXMLParser parser = new XMLParser();
                        IXMLElement data = null;
                        data = parser.parse(new FileInputStream(adxinstall),adxinstall.getAbsolutePath());
                        
                        if (data.hasChildren() && data.getFirstChildNamed("module")!=null)
                        {
                            // remaining modules children
                            // cancel installation !
                            
                            System.out.println (langpack.getString("uninstaller.adxadmin.remainingmodules"));
                            return;
                        }
                        
                    }
                    catch (Exception ex)
                    {
                        System.out.println (langpack.getString("uninstaller.adxadmin.errparseadxinstall"));
                        return;
                    }
                }
                
                // delete adxadm file
                String stradxadmfile = "c:\\sage\\adxadm";
                if (OsVersion.IS_UNIX) stradxadmfile = "/sage/adxadm";
                File adxadmfile = new File(stradxadmfile);
                Files.deleteIfExists(adxadmfile.toPath());
            }
            
            Destroyer destroyer = new Destroyer(installPath,
                    destroy, true, new DestroyerHandler());
            destroyer.start();
        
        
        // on windows platform
        // self delete must be delayed
        
            destroyer.join();
        
            if (OsVersion.IS_WINDOWS)
            {
                java.io.File selfjar = SelfModifier.findJarFile(this.getClass());
                
                // get temporary location
                File deleteScript = File.createTempFile("izpack", ".js");
                Writer blout = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new FileOutputStream(deleteScript), "8859_1"));
                blout.write("var fso = new ActiveXObject(\"Scripting.FileSystemObject\");\r\n");
                blout.write("WScript.Sleep(6000);\r\n");
                blout.write("fso.DeleteFile(\""+selfjar.getAbsolutePath().replace("\\", "\\\\")+"\");\r\n");
                if (destroy) blout.write("fso.DeleteFolder(\""+installPath.replace("\\", "\\\\")+"\", true);");
                blout.flush();
                blout.close();
                
                List<String> command = new ArrayList<String>();
                command.add("cmd.exe");
                command.add("/c");
                command.add("start");
                command.add("wscript");
                command.add("/B");
                command.add("\""+deleteScript.getAbsolutePath()+"\"");
                
                //String strCommand = "cmd.exe /c \"start \"\" /B wscript /B \""+deleteScript.getAbsolutePath()+"\" > c:\\temp\\log.txt \" ";
                //Runtime.getRuntime().exec (command.toArray(new String[command.size()]),null,null);
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.start();
            }
            
        }
        catch (Exception ex)
        {
            // we don't care
            System.out.println (ex.getLocalizedMessage());
        }
        
        
    }

    /**
     * The destroyer handler.
     * <p/>
     * This class also implements the InstallListener because the FileExecutor needs it. TODO: get
     * rid of the InstallListener - implement generic Listener
     */
    private final class DestroyerHandler implements
            com.izforge.izpack.util.AbstractUIProgressHandler
    {
        private int AUTO_ANSWER_MODE = -2;

        private void out(String str)
        {
            System.out.println(str);
        }

        private boolean askOKCancel(String question, int defaultchoice)
        {
            if (defaultchoice == AUTO_ANSWER_MODE)
            {
                return true;
            }
            boolean defaultanswer = defaultchoice == 1;
            try
            {
                System.out.print(question + " (Ok/Cancel) [" + (defaultanswer ? "O" : "C") + "]:");
                String rline = readln();
                if (rline.toLowerCase().startsWith("o"))
                {
                    return true;
                }
                if (rline.toLowerCase().startsWith("c"))
                {
                    return false;
                }
            }
            catch (Exception e)
            {
            }
            if (defaultchoice == -1)
            {
                return askOKCancel(question, defaultchoice);
            }
            return defaultanswer;
        }

        private int askYesNoCancel(String question, int defaultchoice)
        {
            if (defaultchoice == AUTO_ANSWER_MODE)
            {
                return AbstractUIHandler.ANSWER_YES;
            }
            boolean defaultanswer = defaultchoice == 1;
            try
            {
                System.out.print(question + " (Yes/No/Cancel) [" + (defaultanswer ? "Y" : "N") + "]:");
                String rline = readln();
                if (rline.toLowerCase().equals("y"))
                {
                    return AbstractUIHandler.ANSWER_YES;
                }
                if (rline.toLowerCase().equals("n"))
                {
                    return AbstractUIHandler.ANSWER_NO;
                }
                if (rline.toLowerCase().equals("c"))
                {
                    return AbstractUIHandler.ANSWER_CANCEL;
                }
            }
            catch (Exception e)
            {
            }
            if (defaultchoice == -1)
            {
                return askYesNoCancel(question, defaultchoice);
            }
            return defaultchoice;
        }

        private int askYesNo(String question, int defaultchoice)
        {
            if (defaultchoice == AUTO_ANSWER_MODE)
            {
                return AbstractUIHandler.ANSWER_YES;
            }
            boolean defaultanswer = defaultchoice == 1;
            try
            {
                System.out.print(question + " (Yes/No) [" + (defaultanswer ? "Y" : "N") + "]:");
                String rline = readln();
                if (rline.toLowerCase().equals("y"))
                {
                    return AbstractUIHandler.ANSWER_YES;
                }
                if (rline.toLowerCase().equals("n"))
                {
                    return AbstractUIHandler.ANSWER_NO;
                }
            }
            catch (Exception e)
            {
            }
            if (defaultchoice == -1)
            {
                return askYesNoCancel(question, defaultchoice);
            }
            return defaultchoice;
        }

        private String read() throws Exception
        {
            byte[] byteArray = {(byte) System.in.read()};
            return new String(byteArray);
        }

        private String readln() throws Exception
        {
            String input = read();
            int available = System.in.available();
            if (available > 0)
            {
                byte[] byteArray = new byte[available];
                System.in.read(byteArray);
                input += new String(byteArray);
            }
            return input.trim();
        }

        /**
         * The destroyer starts.
         *
         * @param name The name of the overall action. Not used here.
         * @param max  The maximum value of the progress.
         */
        public void startAction(final String name, final int max)
        {
            out("Processing " + name);
        }

        /**
         * The destroyer stops.
         */
        public void stopAction()
        {
            out(langpack.getString("InstallPanel.finished"));
        }

        /**
         * The destroyer progresses.
         *
         * @param pos     The actual position.
         * @param message The message.
         */
        public void progress(final int pos, final String message)
        {
            out(message);
        }

        public void nextStep(String step_name, int step_no, int no_of_substeps)
        {
            // not used
        }

        public void setSubStepNo(int no_of_substeps)
        {
            // not used
        }

        /**
         * Output a notification.
         * <p/>
         * Does nothing here.
         *
         * @param text
         */
        public void emitNotification(String text)
        {
            out (text);
        }

        /**
         * Output a warning.
         *
         * @param text
         */
        public boolean emitWarning(String title, String text)
        {
            return askOKCancel(title + ": " + text, AUTO_ANSWER_MODE);
        }

        /**
         * The destroyer encountered an error.
         *
         * @param error The error message.
         */
        public void emitError(String title, String error)
        {
            out(title + ": " + error);
        }
        
        /**
         * The destroyer encountered an error.
         * 
         * @param error The error message.
         */
        public void emitErrorAndBlockNext(String title, String error)
        {
            emitError(title, error);
        }

        /**
         * Ask the user a question.
         *
         * @param title    Message title.
         * @param question The question.
         * @param choices  The set of choices to present.
         * @return The user's choice.
         * @see AbstractUIHandler#askQuestion(String, String, int)
         */
        public int askQuestion(String title, String question, int choices)
        {
            return askQuestion(title, question, choices, AUTO_ANSWER_MODE);
        }

        /**
         * Ask the user a question.
         *
         * @param title          Message title.
         * @param question       The question.
         * @param choices        The set of choices to present.
         * @param default_choice The default choice. (-1 = no default choice)
         * @return The user's choice.
         * @see AbstractUIHandler#askQuestion(String, String, int, int)
         */
        public int askQuestion(String title, String question, int choices, int default_choice)
        {
            int choice = 0;

            if (choices == AbstractUIHandler.CHOICES_YES_NO)
            {
                choice = askYesNo(title + ": " + question, default_choice);
            }
            else if (choices == AbstractUIHandler.CHOICES_YES_NO_CANCEL)
            {
                choice = askYesNoCancel(title + ": " + question, default_choice);
            }

            return choice;


        }


    }
}
