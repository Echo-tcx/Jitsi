/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.plugin.globalproxyconfig;

import java.net.*;
import java.util.*;

import net.java.sip.communicator.service.configuration.*;
import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;

import org.osgi.framework.*;

/**
 * Register the configuration form.
 *
 * @author  Atul Aggarwal
 * @author Damian Minkov
 */
public class GlobalProxyPluginActivator implements BundleActivator
{
    /**
     * Our logger.
     */
    private Logger logger = Logger.getLogger(GlobalProxyPluginActivator.class);

    /**
     * The Configuration service.
     */
    private static ConfigurationService configService;

    /**
     * The context of this bundle.
     */
    protected static BundleContext bundleContext;

    /**
     * Starts the bundle.
     * @param bc the context
     * @throws Exception
     */
    public void start(BundleContext bc) throws Exception
    {
        bundleContext = bc;

        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put( ConfigurationForm.FORM_TYPE,
                        ConfigurationForm.ADVANCED_TYPE);
        bundleContext.registerService(
            ConfigurationForm.class.getName(),
            new LazyConfigurationForm(
                GlobalProxyConfigForm.class.getName(),
                getClass().getClassLoader(),
                "plugin.globalproxy.PLUGIN_ICON",
                "plugin.globalproxy.GLOBAL_PROXY_CONFIG",
                51, true),
            properties);

        initProperties();

        if (logger.isInfoEnabled())
            logger.info("GLOBAL PROXY CONFIGURATION PLUGIN... [REGISTERED]");
    }

    /**
     * Stops it.
     * @param bc the context.
     * @throws Exception
     */
    public void stop(BundleContext bc) throws Exception
    {
    }
    
    /**
     * Returns the <tt>ConfigurationService</tt> obtained from the bundle
     * context.
     * @return the <tt>ConfigurationService</tt> obtained from the bundle
     * context
     */
    public static ConfigurationService getConfigurationService()
    {
        if(configService == null)
        {
            ServiceReference configReference = bundleContext
                .getServiceReference(ConfigurationService.class.getName());

            configService = (ConfigurationService) bundleContext
                .getService(configReference);
        }

        return configService;
    }

    /**
     * Init system properties that corresponds to proxy settings.
     */
    static void initProperties()
    {
                // Activate proxy settings
            String globalProxyType = getConfigurationService()
                .getString(ProxyInfo.CONNECTON_PROXY_TYPE_PROPERTY_NAME);
            if(globalProxyType != null &&
               !globalProxyType.equals(ProxyInfo.ProxyType.NONE.name()))
            {
                String globalProxyAddress =
                    getConfigurationService().getString(
                    ProxyInfo.CONNECTON_PROXY_ADDRESS_PROPERTY_NAME);
                String globalProxyPortStr =
                    getConfigurationService().getString(
                    ProxyInfo.CONNECTON_PROXY_PORT_PROPERTY_NAME);
                int globalProxyPort = -1;
                try
                {
                    globalProxyPort = Integer.parseInt(
                        globalProxyPortStr);
                }
                catch(NumberFormatException ex)
                {
                    // problem parsing port, will not set it
                }
                String globalProxyUsername =
                    getConfigurationService().getString(
                    ProxyInfo.CONNECTON_PROXY_USERNAME_PROPERTY_NAME);
                String globalProxyPassword =
                    getConfigurationService().getString(
                    ProxyInfo.CONNECTON_PROXY_PASSWORD_PROPERTY_NAME);
                if(globalProxyAddress == null ||
                    globalProxyAddress.length() <= 0)
                {
                    // no address
                    return;
                }

                String type = null;
                if(globalProxyType.equals(
                    ProxyInfo.ProxyType.HTTP.name()))
                {
                    type = "HTTP";

                    // java network properties
                    System.setProperty(
                        "http.proxyHost", globalProxyAddress);

                    if(globalProxyPortStr != null)
                    {
                        System.setProperty(
                            "http.proxyPort", globalProxyPortStr);
                    }

                    // used by some protocols like yahoo
                    System.setProperty(
                        "proxySet", "true");
                }
                else if(globalProxyType.equals(
                    ProxyInfo.ProxyType.SOCKS4.name()) ||
                    globalProxyType.equals(
                    ProxyInfo.ProxyType.SOCKS5.name()))
                {
                    type = "SOCKS";

                    // java network properties
                    System.setProperty(
                        "socksProxyHost", globalProxyAddress);

                    if(globalProxyPortStr != null)
                    {
                        System.setProperty(
                            "socksProxyPort", globalProxyPortStr);
                    }

                    // used by some protocols like yahoo
                    System.setProperty(
                        "socksProxySet", "true");
                }

                Authenticator.setDefault(new AuthenticatorImpl(
                    globalProxyAddress, globalProxyPort,
                    type,
                    globalProxyUsername, globalProxyPassword));
            }
            else
                Authenticator.setDefault(null);
    }

    /**
     * Gets care of the proxy configurations which require authentication.
     */
    private static class AuthenticatorImpl
        extends Authenticator
    {

        /**
         * The proxy port we are serving.
         */
        String host;
        /**
         * The port that is used.
         */
        int port;
        /**
         * Type of the proxy config.
         */
        String type;
        /**
         * The username to supply.
         */
        String username;
        /**
         * The password to supply.
         */
        String password;

        /**
         * Creates it.
         * @param host The proxy port we are serving.
         * @param port The port that is used.
         * @param type Type of the proxy config.
         * @param username The username to supply.
         * @param password The password to supply.
         */
        public AuthenticatorImpl(String host, int port,
            String type, String username, String password)
        {
            this.host = host;
            this.port = port;
            this.type = type;
            this.username = username;
            this.password = password;
        }

        /**
         * Called when password authorization is needed.  Subclasses should
         * override the default implementation, which returns null.
         * @return The PasswordAuthentication collected from the
         *		user, or null if none is provided.
         */
        protected PasswordAuthentication getPasswordAuthentication()
        {
            if(getRequestingProtocol().startsWith(type)
                && getRequestingHost().equals(host)
                && port == getRequestingPort()
                && getRequestorType().equals(Authenticator.RequestorType.SERVER))
            {
                return new PasswordAuthentication(
                    username, password.toCharArray());
            }
            else
                return super.getPasswordAuthentication();
        }
    }
}
