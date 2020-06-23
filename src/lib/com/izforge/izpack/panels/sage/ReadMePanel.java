
package com.izforge.izpack.panels;

import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.installer.ResourceManager;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.awt.Desktop;

public class ReadMePanel extends IzPanel implements HyperlinkListener, ActionListener
{

    /**
     *
     */
    private static final long serialVersionUID = 3256745385458746416L;

    /**
     * The text area.
     */
    private JEditorPane textArea;

    /**
     * The checkbox.
     */
    private JCheckBox readCheckBox;
	
    /**
     * The constructor.
     *
     * @param idata  The installation data.
     * @param parent Description of the Parameter
     */
    public ReadMePanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata, new IzPanelLayout());
        // We load the licence
        loadReadMe();

        // We put our components

        add(LabelFactory.create(parent.langpack.getString("ReadMePanel.info"),
                parent.icons.getImageIcon("history"), LEADING), NEXT_LINE);
        try
        {
            textArea = new JEditorPane();
            textArea.setEditable(false);
            textArea.addHyperlinkListener(this);
            JScrollPane scroller = new JScrollPane(textArea);
            textArea.setPage(loadReadMe());
            add(scroller, NEXT_LINE);
        }
        catch (Exception err)
        {
            err.printStackTrace();
        }

        ButtonGroup group = new ButtonGroup();

        readCheckBox = new JCheckBox(parent.langpack.getString("ReadMePanel.agree"), false);
        group.add(readCheckBox);
        add(readCheckBox, NEXT_LINE);
        readCheckBox.addActionListener(this);

        setInitialFocus(textArea);
        getLayoutHelper().completeLayout();
    }

    /**
     * Loads the license text.
     *
     * @return The license text URL.
     */
    private URL loadReadMe()
    {
        String resNamePrifix = "ReadMePanel.readme";
        try
        {
            return ResourceManager.getInstance().getURL(resNamePrifix);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Actions-handling method (here it launches the installation).
     *
     * @param e The event.
     */
    public void actionPerformed(ActionEvent e)
    {
        if (readCheckBox.isSelected())
        {
            parent.unlockNextButton();
        }
        else
        {
            parent.lockNextButton();
        }
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return true if the user agrees with the license, false otherwise.
     */
    public boolean isValidated()
    {
        if (! readCheckBox.isSelected())
        {
            parent.exit();
            return false;
        }
        return (readCheckBox.isSelected());
    }

    /**
     * Hyperlink events handler.
     *
     * @param e The event.
     */
    public void hyperlinkUpdate(HyperlinkEvent e)
    {
        try
        {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
            {            
				try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (Exception err) {
                    textArea.setPage(e.getURL()); 
                }
            }
        }
        catch (Exception err)
        {
            // TODO: Extend exception handling.
        }
    }

    /**
     * Called when the panel becomes active.
     */
    public void panelActivate()
    {
        if (!readCheckBox.isSelected())
        {
            parent.lockNextButton();
        }
    }
}
