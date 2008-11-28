/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.login;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.customcontrols.*;
import net.java.sip.communicator.impl.gui.i18n.*;
import net.java.sip.communicator.impl.gui.main.*;
import net.java.sip.communicator.impl.gui.main.authorization.*;
import net.java.sip.communicator.impl.gui.utils.Constants;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;

import org.osgi.framework.*;

/**
 * The <tt>LoginManager</tt> manages the login operation. Here we obtain the
 * <tt>ProtocolProviderFactory</tt>, we make the account installation and we
 * handle all events related to the registration state.
 * <p>
 * The <tt>LoginManager</tt> is the one that opens one or more
 * <tt>LoginWindow</tt>s for each <tt>ProtocolProviderFactory</tt>. The
 * <tt>LoginWindow</tt> is where user could enter an identifier and password.
 * <p>
 * Note that the behavior of this class will be changed when the Configuration
 * Service is ready.
 *
 * @author Yana Stamcheva
 */
public class LoginManager
    implements  ServiceListener,
                RegistrationStateChangeListener
{
    private Logger logger = Logger.getLogger(LoginManager.class.getName());

    private MainFrame mainFrame;

    private boolean manuallyDisconnected = false;

    /**
     * Creates an instance of the <tt>LoginManager</tt>, by specifying the main
     * application window.
     * 
     * @param mainFrame the main application window
     */
    public LoginManager(MainFrame mainFrame)
    {
        this.mainFrame = mainFrame;

        GuiActivator.bundleContext.addServiceListener(this);
    }

    /**
     * Registers the given protocol provider.
     * 
     * @param protocolProvider the ProtocolProviderService to register.
     */
    public void login(ProtocolProviderService protocolProvider)
    {
        SecurityAuthorityImpl secAuth = new SecurityAuthorityImpl(mainFrame,
            protocolProvider);

        new RegisterProvider(protocolProvider, secAuth).start();
    }

    /**
     * Unregisters the given protocol provider.
     * 
     * @param protocolProvider the ProtocolProviderService to unregister
     */
    public void logoff(ProtocolProviderService protocolProvider)
    {
        new UnregisterProvider(protocolProvider).start();
    }

    /**
     * Shows login window for each registered account.
     * 
     * @param parent The parent MainFrame window.
     */
    public void runLogin(MainFrame parent)
    {
        for (ProtocolProviderFactory providerFactory : GuiActivator
            .getProtocolProviderFactories().values())
        {
            ServiceReference serRef;
            ProtocolProviderService protocolProvider;

            for (AccountID accountID : providerFactory.getRegisteredAccounts())
            {
                serRef = providerFactory.getProviderForAccount(accountID);

                protocolProvider =
                    (ProtocolProviderService) GuiActivator.bundleContext
                        .getService(serRef);

                protocolProvider.addRegistrationStateChangeListener(this);

                this.mainFrame.addProtocolProvider(protocolProvider);

                Object status =
                    this.mainFrame
                        .getProtocolProviderLastStatus(protocolProvider);

                if (status == null
                    || status.equals(Constants.ONLINE_STATUS)
                    || ((status instanceof PresenceStatus) && (((PresenceStatus) status)
                        .getStatus() >= PresenceStatus.ONLINE_THRESHOLD)))
                {
                    this.login(protocolProvider);
                }
            }
        }
    }

    /**
     * The method is called by a ProtocolProvider implementation whenever a
     * change in the registration state of the corresponding provider had
     * occurred.
     * 
     * @param evt ProviderStatusChangeEvent the event describing the status
     *            change.
     */
    public void registrationStateChanged(RegistrationStateChangeEvent evt)
    {
        ProtocolProviderService protocolProvider = evt.getProvider();

        AccountID accountID = protocolProvider.getAccountID();

        logger.trace("Protocol provider: " + protocolProvider
            + " changed its state to: " + evt.getNewState().getStateName());

        OperationSetPresence presence = mainFrame
            .getProtocolPresenceOpSet(protocolProvider);

        OperationSetMultiUserChat multiUserChat = mainFrame
            .getMultiUserChatOpSet(protocolProvider);

        if (evt.getNewState().equals(RegistrationState.REGISTERED))
        {
            if (presence != null)
            {
                presence.setAuthorizationHandler(new AuthorizationHandlerImpl(
                    mainFrame));
            }

            if(multiUserChat != null)
            {
                GuiActivator.getUIService().getConferenceChatManager()
                    .getChatRoomList().synchronizeOpSetWithLocalContactList(
                        protocolProvider, multiUserChat); 
            }
        }
        else if (evt.getNewState().equals(
            RegistrationState.AUTHENTICATION_FAILED))
        {
            if (evt.getReasonCode() == RegistrationStateChangeEvent
                    .REASON_RECONNECTION_RATE_LIMIT_EXCEEDED)
            {

                String msgText = Messages.getI18NString(
                    "reconnectionLimitExceeded", new String[]
                    { accountID.getUserID(), accountID.getService() })
                    .getText();

                new ErrorDialog(null,
                                Messages.getI18NString("error").getText(),
                                msgText).showDialog();
            }
            else if (evt.getReasonCode() == RegistrationStateChangeEvent
                                                .REASON_NON_EXISTING_USER_ID)
            {
                String msgText = Messages.getI18NString("nonExistingUserId",
                    new String[]
                    { protocolProvider.getProtocolDisplayName() }).getText();

                new ErrorDialog(null,
                    Messages.getI18NString("error").getText(),
                    msgText).showDialog();
            }

            logger.error(evt.getReason());
        }
        else if (evt.getNewState().equals(RegistrationState.CONNECTION_FAILED))
        {
            String msgText = Messages.getI18NString("connectionFailedMessage",
                new String[]
                { accountID.getUserID(), accountID.getService() }).getText();

            int result = new MessageDialog(null,
                Messages.getI18NString("error").getText(),
                msgText,
                Messages.getI18NString("retry").getText(),
                false).showDialog();

            if (result == MessageDialog.OK_RETURN_CODE)
            {
                this.login(protocolProvider);
            }

            logger.error(evt.getReason());
        }
        else if (evt.getNewState().equals(RegistrationState.EXPIRED))
        {
            String msgText = Messages.getI18NString("connectionExpiredMessage",
                new String[]
                { protocolProvider.getProtocolDisplayName() }).getText();

            new ErrorDialog(null,
                Messages.getI18NString("error").getText(),
                msgText).showDialog();

            logger.error(evt.getReason());
        }
        else if (evt.getNewState().equals(RegistrationState.UNREGISTERED))
        {
            if (!manuallyDisconnected)
            {
                if (evt.getReasonCode() == RegistrationStateChangeEvent
                                            .REASON_MULTIPLE_LOGINS)
                {
                    String msgText = Messages.getI18NString("multipleLogins",
                        new String[]
                        { accountID.getUserID(), accountID.getService() })
                        .getText();

                    new ErrorDialog(null,
                        Messages.getI18NString("error").getText(),
                        msgText).showDialog();
                }
                else if (evt.getReasonCode() == RegistrationStateChangeEvent
                                            .REASON_CLIENT_LIMIT_REACHED_FOR_IP)
                {
                    String msgText = Messages.getI18NString(
                        "limitReachedForIp", new String[]
                        { protocolProvider.getProtocolDisplayName() }).getText();

                    new ErrorDialog(null,
                        Messages.getI18NString("error").getText(),
                        msgText).showDialog();
                }
                else if (evt.getReasonCode() == RegistrationStateChangeEvent
                                            .REASON_USER_REQUEST)
                {
                    // do nothing
                }
                else
                {
                    String msgText = Messages.getI18NString(
                        "unregisteredMessage", new String[]
                        { accountID.getUserID(), accountID.getService() })
                        .getText();

                    new ErrorDialog(null,
                        Messages.getI18NString("error").getText(),
                        msgText).showDialog();
                }
                logger.error(evt.getReason());
            }
        }
    }

    /**
     * Returns the MainFrame.
     * 
     * @return The MainFrame.
     */
    public MainFrame getMainFrame()
    {
        return mainFrame;
    }

    /**
     * Sets the MainFrame.
     * 
     * @param mainFrame The main frame.
     */
    public void setMainFrame(MainFrame mainFrame)
    {
        this.mainFrame = mainFrame;
    }

    /**
     * Implements the <tt>ServiceListener</tt> method. Verifies whether the
     * passed event concerns a <tt>ProtocolProviderService</tt> and adds the
     * corresponding UI controls.
     * 
     * @param event The <tt>ServiceEvent</tt> object.
     */
    public void serviceChanged(ServiceEvent event)
    {
        // if the event is caused by a bundle being stopped, we don't want to
        // know
        if (event.getServiceReference().getBundle().getState()
                == Bundle.STOPPING)
        {
            return;
        }

        Object service = GuiActivator.bundleContext.getService(event
            .getServiceReference());

        // we don't care if the source service is not a protocol provider
        if (!(service instanceof ProtocolProviderService))
        {
            return;
        }

        if (event.getType() == ServiceEvent.REGISTERED)
        {
            this.handleProviderAdded((ProtocolProviderService) service);
        }
        else if (event.getType() == ServiceEvent.UNREGISTERING)
        {
            this.handleProviderRemoved((ProtocolProviderService) service);
        }
    }

    /**
     * Adds all UI components (status selector box, etc) related to the given
     * protocol provider.
     * 
     * @param protocolProvider the <tt>ProtocolProviderService</tt>
     */
    private void handleProviderAdded(ProtocolProviderService protocolProvider)
    {
        logger.trace("The following protocol provider was just added: "
            + protocolProvider.getAccountID().getAccountAddress());

        protocolProvider.addRegistrationStateChangeListener(this);
        this.mainFrame.addProtocolProvider(protocolProvider);

        Object status = this.mainFrame
            .getProtocolProviderLastStatus(protocolProvider);

        if (status == null
            || status.equals(Constants.ONLINE_STATUS)
            || ((status instanceof PresenceStatus) && (((PresenceStatus) status)
                .getStatus() >= PresenceStatus.ONLINE_THRESHOLD)))
        {
            this.login(protocolProvider);
        }
    }

    /**
     * Removes all UI components related to the given protocol provider.
     * 
     * @param protocolProvider the <tt>ProtocolProviderService</tt>
     */
    private void handleProviderRemoved(ProtocolProviderService protocolProvider)
    {
        this.mainFrame.removeProtocolProvider(protocolProvider);
    }

    public boolean isManuallyDisconnected()
    {
        return manuallyDisconnected;
    }

    public void setManuallyDisconnected(boolean manuallyDisconnected)
    {
        this.manuallyDisconnected = manuallyDisconnected;
    }

    /**
     * Registers a protocol provider in a separate thread.
     */
    private class RegisterProvider
        extends Thread
    {
        private final ProtocolProviderService protocolProvider;

        private final SecurityAuthority secAuth;

        RegisterProvider(ProtocolProviderService protocolProvider,
            SecurityAuthority secAuth)
        {
            this.protocolProvider = protocolProvider;
            this.secAuth = secAuth;
        }

        /**
         * Registers the contained protocol provider and process all possible
         * errors that may occur during the registration process.
         */
        public void run()
        {
            try
            {
                protocolProvider.register(secAuth);
            }
            catch (OperationFailedException ex)
            {
                handleOperationFailedException(ex);
            }
            catch (Throwable ex)
            {
                logger.error("Failed to register protocol provider. ", ex);

                AccountID accountID = protocolProvider.getAccountID();
                new ErrorDialog(mainFrame, Messages.getI18NString("error")
                    .getText(), Messages.getI18NString("loginGeneralError",
                    new String[]
                    { accountID.getUserID(), accountID.getService() })
                    .getText()).showDialog();
            }
        }

        private void handleOperationFailedException(OperationFailedException ex)
        {
            String errorMessage = "";

            switch (ex.getErrorCode())
            {
            case OperationFailedException.GENERAL_ERROR:
            {
                logger.error("Provider could not be registered"
                    + " due to the following general error: ", ex);

                AccountID accountID = protocolProvider.getAccountID();
                errorMessage =
                    Messages.getI18NString("loginGeneralError", new String[]
                    { accountID.getUserID(), accountID.getService() })
                        .getText();

                new ErrorDialog(mainFrame, Messages.getI18NString("error")
                    .getText(), errorMessage, ex).showDialog();
            }
                break;
            case OperationFailedException.INTERNAL_ERROR:
            {
                logger.error("Provider could not be registered"
                    + " due to the following internal error: ", ex);

                AccountID accountID = protocolProvider.getAccountID();
                errorMessage =
                    Messages.getI18NString("loginInternalError", new String[]
                    { accountID.getUserID(), accountID.getService() })
                        .getText();

                new ErrorDialog(mainFrame, Messages.getI18NString("error")
                    .getText(), errorMessage, ex).showDialog();
            }
                break;
            case OperationFailedException.NETWORK_FAILURE:
            {
                logger.error("Provider could not be registered"
                    + " due to a network failure: " + ex);

                AccountID accountID = protocolProvider.getAccountID();
                errorMessage =
                    Messages.getI18NString("loginNetworkError", new String[]
                    { accountID.getUserID(), accountID.getService() })
                        .getText();

                int result =
                    new MessageDialog(null, Messages.getI18NString("error")
                        .getText(), errorMessage, Messages.getI18NString(
                        "retry").getText(), false).showDialog();

                if (result == MessageDialog.OK_RETURN_CODE)
                {
                    login(protocolProvider);
                }
            }
                break;
            case OperationFailedException.INVALID_ACCOUNT_PROPERTIES:
            {
                logger.error("Provider could not be registered"
                    + " due to an invalid account property: ", ex);

                AccountID accountID = protocolProvider.getAccountID();
                errorMessage =
                    Messages.getI18NString("loginInvalidPropsError",
                        new String[]
                        { accountID.getUserID(), accountID.getService() })
                        .getText();

                new ErrorDialog(mainFrame, Messages.getI18NString("error")
                    .getText(), errorMessage, ex).showDialog();
            }
                break;
            default:
                logger.error("Provider could not be registered.", ex);
            }
        }
    }

    /**
     * Unregisters a protocol provider in a separate thread.
     */
    private class UnregisterProvider
        extends Thread
    {
        ProtocolProviderService protocolProvider;

        UnregisterProvider(ProtocolProviderService protocolProvider)
        {
            this.protocolProvider = protocolProvider;
        }

        /**
         * Unregisters the contained protocol provider and process all possible
         * errors that may occur during the un-registration process.
         */
        public void run()
        {
            try
            {
                protocolProvider.unregister();
            }
            catch (OperationFailedException ex)
            {
                int errorCode = ex.getErrorCode();

                if (errorCode == OperationFailedException.GENERAL_ERROR)
                {
                    logger.error("Provider could not be unregistered"
                        + " due to the following general error: " + ex);
                }
                else if (errorCode == OperationFailedException.INTERNAL_ERROR)
                {
                    logger.error("Provider could not be unregistered"
                        + " due to the following internal error: " + ex);
                }
                else if (errorCode == OperationFailedException.NETWORK_FAILURE)
                {
                    logger.error("Provider could not be unregistered"
                        + " due to a network failure: " + ex);
                }

                new ErrorDialog(mainFrame,
                    Messages.getI18NString("error").getText(),
                    Messages.getI18NString("logoffNotSucceeded",
                        new String[]
                        { protocolProvider.getAccountID().getUserID(),
                            protocolProvider.getAccountID().getService() })
                        .getText())
                    .showDialog();
            }
        }
    }
}
