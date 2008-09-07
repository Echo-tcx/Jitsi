/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.plugin.updatechecker;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.text.MessageFormat;
import java.util.*;

import javax.swing.*;

import javax.swing.text.*;
import javax.swing.text.html.*;
import net.java.sip.communicator.service.browserlauncher.*;
import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.resources.*;
import net.java.sip.communicator.service.version.VersionService;
import net.java.sip.communicator.util.*;

import org.osgi.framework.*;

/**
 * Activates the UpdateCheck plugin
 * @author Damian Minkov
**/
public class UpdateCheckActivator
    implements BundleActivator
{

    private static Logger logger = Logger.getLogger(UpdateCheckActivator.class);

    private static BundleContext bundleContext = null;

    private static BrowserLauncherService browserLauncherService;

    private static ResourceManagementService resourcesService;

    private String downloadLink = null;
    private String lastVersion = null;

    /**
     * Starts this bundle
     *
     * @param bundleContext BundleContext
     * @throws Exception
     */
    public void start(BundleContext bundleContext) throws Exception
    {
        try
        {
            logger.logEntry();
            this.bundleContext = bundleContext;
        }
        finally
        {
            logger.logExit();
        }

        ServiceReference serviceReference = bundleContext
            .getServiceReference( net.java.sip.communicator.service.version.
                VersionService.class.getName());

        VersionService verService = (VersionService) bundleContext
                .getService(serviceReference);

        net.java.sip.communicator.service.version.Version
            ver = verService.getCurrentVersion();

        if(isNewestVersion(ver.toString()))
            return;

        final JDialog dialog = new JDialog();
        dialog.setTitle(
            getResources().getI18NString("dialogTitle"));

        JEditorPane contentMessage = new JEditorPane();
        contentMessage.setContentType("text/html");
        contentMessage.setOpaque(false);
        contentMessage.setEditable(false);

        String dialogMsg =
            getResources().getI18NString("dialogMessage1",
            new String[]{getResources().getSettingsString("applicationName")});

        if(lastVersion != null)
            dialogMsg +=
                getResources().getI18NString(
                "dialogMessage2",
                new String[]{
                    getResources().getSettingsString("applicationName"),
                    lastVersion});

        contentMessage.setText(dialogMsg);

        JPanel contentPane = new JPanel(new BorderLayout(5,5));
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPane.add(contentMessage, BorderLayout.CENTER);

        JPanel buttonPanel
            = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton closeButton
            = new JButton( getResources().getI18NString("buttonClose"));

        closeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                dialog.setVisible(false);
            }
        });

        if(downloadLink != null)
        {
            JButton downloadButton =
                new JButton(getResources().getI18NString("buttonDownload"));

            downloadButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e)
                {
                    getBrowserLauncher().openURL(downloadLink);
                    dialog.dispose();
                }
            });

            buttonPanel.add(downloadButton);
        }

        buttonPanel.add(closeButton);

        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(contentPane);

        dialog.pack();

        dialog.setLocation(
            Toolkit.getDefaultToolkit().getScreenSize().width/2
                - dialog.getWidth()/2,
            Toolkit.getDefaultToolkit().getScreenSize().height/2
                - dialog.getHeight()/2
        );

        dialog.setVisible(true);
    }

    /**
    * stop the bundle
    */
    public void stop(BundleContext bundleContext) throws Exception
    {
    }

    /**
     * Returns the <tt>BrowserLauncherService</tt> obtained from the bundle
     * context.
     * @return the <tt>BrowserLauncherService</tt> obtained from the bundle
     * context
     */
    public static BrowserLauncherService getBrowserLauncher()
    {
        if (browserLauncherService == null)
        {
            ServiceReference serviceReference = bundleContext
                .getServiceReference(BrowserLauncherService.class.getName());

            browserLauncherService = (BrowserLauncherService) bundleContext
                .getService(serviceReference);
        }

        return browserLauncherService;
    }

    public static ResourceManagementService getResources()
    {
        if (resourcesService == null)
        {
            ServiceReference serviceReference = bundleContext
                .getServiceReference(ResourceManagementService.class.getName());

            if(serviceReference == null)
                return null;

            resourcesService = (ResourceManagementService) bundleContext
                .getService(serviceReference);
        }

        return resourcesService;
    }

    /**
     * Check the first link as files on the web are sorted by date
     * @param currentVersionStr
     * @return
     */
    private boolean isNewestVersion(String currentVersionStr)
    {
        try
        {
            String configString = Resources.getConfigString("update_link");

            if(configString == null)
            {
                logger.debug("Updates are disabled. Faking latest version.");
                return true;
            }

            URL url = new URL(configString);

            Properties props = new Properties();
            props.load(url.openStream());

            lastVersion = props.getProperty("last_version");
            downloadLink = props.getProperty("download_link");

            return lastVersion.compareTo(currentVersionStr) <= 0;
        }
        catch (Exception e)
        {
            logger.warn("Cannot get and compare versions!");
            logger.debug("Error was: ", e);
            // if we get an exception this mean we were unable to compare versions
            // will retrun that current is newest to prevent opening info dialog
            // about new version
            return true;
        }
    }
}
