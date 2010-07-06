/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.plugin.advancedconfig;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;

import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.swing.*;

import org.osgi.framework.*;

/**
 * The advanced configuration panel.
 *
 * @author Yana Stamcheva
 */
public class AdvancedConfigurationPanel
    extends TransparentPanel
    implements  ServiceListener,
                ListSelectionListener
{
    /**
     * The logger.
     */
    private final Logger logger
        = Logger.getLogger(AdvancedConfigurationPanel.class);

    /**
     * The configuration list.
     */
    private final JList configList = new JList();

    /**
     * The center panel.
     */
    private final JPanel centerPanel = new TransparentPanel(new BorderLayout());

    /**
     * Creates an instance of the <tt>AdvancedConfigurationPanel</tt>.
     */
    public AdvancedConfigurationPanel()
    {
        super(new BorderLayout(10, 0));

        AdvancedConfigActivator.bundleContext.addServiceListener(this);

        initList();

        centerPanel.setPreferredSize(new Dimension(500, 500));

        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Initializes the config list.
     */
    private void initList()
    {
        configList.setModel(new DefaultListModel());
        configList.setCellRenderer(new ConfigListCellRenderer());
        configList.addListSelectionListener(this);
        configList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane configScrollList = new JScrollPane();

        configScrollList.getVerticalScrollBar().setUnitIncrement(30);

        configScrollList.getViewport().add(configList);

        add(configScrollList, BorderLayout.WEST);

        String osgiFilter = "("
            + ConfigurationForm.FORM_TYPE
            + "="+ConfigurationForm.ADVANCED_TYPE+")";
        ServiceReference[] confFormsRefs = null;
        try
        {
            confFormsRefs = AdvancedConfigActivator.bundleContext
                .getServiceReferences(  ConfigurationForm.class.getName(),
                                        osgiFilter);
        }
        catch (InvalidSyntaxException ex)
        {}

        if(confFormsRefs != null)
        {
            for (int i = 0; i < confFormsRefs.length; i++)
            {
                ConfigurationForm form
                    = (ConfigurationForm) AdvancedConfigActivator.bundleContext
                        .getService(confFormsRefs[i]);

                if (form.isAdvanced())
                    this.addConfigForm(form);
            }
        }
    }

    /**
     * Shows on the right the configuration form given by the given
     * <tt>ConfigFormDescriptor</tt>.
     *
     * @param configForm the configuration form to show
     */
    private void showFormContent(ConfigurationForm configForm)
    {
        this.centerPanel.removeAll();

        JComponent configFormPanel
            = (JComponent) configForm.getForm();

        configFormPanel.setOpaque(false);

        this.centerPanel.add(configFormPanel, BorderLayout.CENTER);

        this.centerPanel.revalidate();
        this.centerPanel.repaint();
    }

    /**
     * Handles registration of a new configuration form.
     * @param event the <tt>ServiceEvent</tt> that notified us
     */
    public void serviceChanged(ServiceEvent event)
    {
        Object sService
            = AdvancedConfigActivator.bundleContext
                .getService(event.getServiceReference());

        // we don't care if the source service is not a configuration form
        if (!(sService instanceof ConfigurationForm))
            return;

        ConfigurationForm configForm = (ConfigurationForm) sService;

        if (!configForm.isAdvanced())
            return;

        switch (event.getType())
        {
        case ServiceEvent.REGISTERED:
            if (logger.isInfoEnabled())
                logger.info("Handling registration of a new Configuration Form.");

            this.addConfigForm(configForm);
            break;

        case ServiceEvent.UNREGISTERING:
            this.removeConfigForm(configForm);
            break;
        }
    }

    /**
     * Adds a new <tt>ConfigurationForm</tt> to this list. 
     * @param configForm The <tt>ConfigurationForm</tt> to add.
     */
    public void addConfigForm(ConfigurationForm configForm)
    {
        if (configForm == null)
            throw new IllegalArgumentException("configForm");

        DefaultListModel listModel = (DefaultListModel) configList.getModel();

        int i = 0;
        int count = listModel.getSize();
        int configFormIndex = configForm.getIndex();
        for (; i < count; i++)
        {
            ConfigurationForm form = (ConfigurationForm) listModel.get(i);

            if (configFormIndex < form.getIndex())
                break;
        }
        listModel.add(i, configForm);

        configList.setSelectedIndex(0);
    }

    /**
     * Removes a <tt>ConfigurationForm</tt> from this list. 
     * @param configForm The <tt>ConfigurationForm</tt> to remove.
     */
    public void removeConfigForm(ConfigurationForm configForm)
    {
        DefaultListModel listModel = (DefaultListModel) configList.getModel();

        for(int count = listModel.getSize(), i = count - 1; i >= 0; i--)
        {
            ConfigurationForm form
                = (ConfigurationForm) listModel.get(i);

            if(form.equals(configForm))
            {
                listModel.remove(i);
                /*
                 * TODO We may just consider not allowing duplicates on addition
                 * and then break here.
                 */
            }
        }
    }

    /**
     * A custom cell renderer that represents a <tt>ConfigurationForm</tt>.
     */
    private class ConfigListCellRenderer extends DefaultListCellRenderer
    {
        private boolean isSelected = false;

        private final Color selectedColor
            = new Color(AdvancedConfigActivator.getResources().
                getColor("service.gui.LIST_SELECTION_COLOR"));

        /**
         * Creates an instance of <tt>ConfigListCellRenderer</tt> and specifies
         * that this renderer is transparent.
         */
        public ConfigListCellRenderer()
        {
            this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            this.setOpaque(false);
        }

        /**
         * Returns the component representing the cell given by parameters.
         * @param list the parent list
         * @param value the value of the cell
         * @param index the index of the cell
         * @param isSelected indicates if the cell is selected
         * @param cellHasFocus indicates if the cell has the focus
         * @return the component representing the cell
         */
        public Component getListCellRendererComponent(  JList list,
                                                        Object value,
                                                        int index,
                                                        boolean isSelected,
                                                        boolean cellHasFocus)
        {
            ConfigurationForm configForm = (ConfigurationForm) value;

            this.isSelected = isSelected;
            this.setText(configForm.getTitle());

            return this;
        }

        /**
         * Paint a background for all groups and a round blue border and
         * background when a cell is selected.
         * @param g the <tt>Graphics</tt> object
         */
        public void paintComponent(Graphics g)
        {
            Graphics g2 = g.create();
            try
            {
                internalPaintComponent(g2);
            }
            finally
            {
                g2.dispose();
            }
            super.paintComponent(g);
        }

        /**
         * Paint a background for all groups and a round blue border and
         * background when a cell is selected.
         * @param g the <tt>Graphics</tt> object
         */
        private void internalPaintComponent(Graphics g)
        {
            AntialiasingManager.activateAntialiasing(g);

            Graphics2D g2 = (Graphics2D) g;

            if (isSelected)
            {
                g2.setColor(selectedColor);
                g2.fillRect(0, 0, this.getWidth(), this.getHeight());
            }
        }
    }

    /**
     * Called when user selects a component in the list of configuration forms.
     * @param e the <tt>ListSelectionEvent</tt>
     */
    public void valueChanged(ListSelectionEvent e)
    {
        if(!e.getValueIsAdjusting())
        {
            ConfigurationForm configForm
                = (ConfigurationForm) configList.getSelectedValue();

            if(configForm != null)
                showFormContent(configForm);
        }
    }
}
