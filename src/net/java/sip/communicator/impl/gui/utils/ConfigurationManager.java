/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.utils;

import java.util.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.service.configuration.*;
import net.java.sip.communicator.service.protocol.*;

public class ConfigurationManager
{
    public static final String ENTER_COMMAND = "Enter";
    
    public static final String CTRL_ENTER_COMMAND = "Ctrl-Enter";
    
    /**
     * Indicates whether the message automatic popup is enabled.
     */
    private static boolean autoPopupNewMessage = false;
    
    private static String sendMessageCommand;

    private static boolean isCallPanelShown = true;
    
    private static boolean isShowOffline = true;
    
    private static boolean isApplicationVisible = true;
    
    private static boolean isSendTypingNotifications = true;
    
    private static ConfigurationService configService
        = GuiActivator.getConfigurationService();

    public static void loadGuiConfigurations()
    {
        // Load the "auPopupNewMessage" property.
        String autoPopup = configService.getString(
            "net.java.sip.communicator.impl.gui.autoPopupNewMessage");

        if(autoPopup != null && autoPopup.equalsIgnoreCase("yes"))
            autoPopupNewMessage = true;

        // Load the "sendMessageCommand" property.
        String messageCommand = configService.getString(
            "net.java.sip.communicator.impl.gui.sendMessageCommand");

        if(messageCommand == null || messageCommand != "")
            sendMessageCommand = messageCommand;

        // Load the showCallPanel property.
        String callPanelShown = configService.getString(
            "net.java.sip.communicator.impl.gui.showCallPanel");

        if(callPanelShown != null && callPanelShown != "")
        {
            isCallPanelShown = new Boolean(callPanelShown).booleanValue();
        }

        // Load the "showOffline" property.
        String showOffline = configService.getString(
            "net.java.sip.communicator.impl.gui.showOffline");
        
        if(showOffline != null && showOffline != "")
        {
            isShowOffline = new Boolean(showOffline).booleanValue();
        }

        // Load the "showApplication" property.
        String isVisible = configService.getString(
            "net.java.sip.communicator.impl.systray.showApplication");

        if(isVisible != null && isVisible != "")
        {
            isApplicationVisible = new Boolean(isVisible).booleanValue();
        }

        // Load the "sendTypingNotifications" property.
        String isSendTypingNotif = configService.getString(
            "net.java.sip.communicator.impl.gui.sendTypingNotifications");
        
        if(isSendTypingNotif != null && isSendTypingNotif != "")
        {
            isSendTypingNotifications
                = new Boolean(isSendTypingNotif).booleanValue();
        }
    }

    /**
     * Return TRUE if "autoPopupNewMessage" property is true, otherwise - return
     * FALSE. Indicates to the user interface whether new messages should be
     * opened and bring to front.
     * @return TRUE if "autoPopupNewMessage" property is true, otherwise - return
     * FALSE.
     */
    public static boolean isAutoPopupNewMessage()
    {
        return autoPopupNewMessage;
    }

    /**
     * Return TRUE if "showCallPanel" property is true, otherwise - return
     * FALSE. Indicates to the user interface whether the panel containing the
     * call and hangup buttons should be shown.
     * @return TRUE if "showCallPanel" property is true, otherwise - return
     * FALSE.
     */
    public static boolean isCallPanelShown()
    {
        return isCallPanelShown;
    }

    /**
     * Return TRUE if "showOffline" property is true, otherwise - return
     * FALSE. Indicates to the user interface whether offline user should be
     * shown in the contact list or not.
     * @return TRUE if "showOffline" property is true, otherwise - return
     * FALSE.
     */
    public static boolean isShowOffline()
    {
        return isShowOffline;
    }

    /**
     * Return TRUE if "showApplication" property is true, otherwise - return
     * FALSE. Indicates to the user interface whether the main application
     * window should shown or hidden on startup.
     * @return TRUE if "showApplication" property is true, otherwise - return
     * FALSE.
     */
    public static boolean isApplicationVisible()
    {
        return isApplicationVisible;
    }

    /**
     * Return TRUE if "sendTypingNotifications" property is true, otherwise -
     * return FALSE. Indicates to the user interface whether typing
     * notifications are enabled or disabled.
     * @return TRUE if "sendTypingNotifications" property is true, otherwise -
     * return FALSE.
     */
    public static boolean isSendTypingNotifications()
    {
        return isSendTypingNotifications;
    }

    /**
     * Return the "sendMessageCommand" property that was saved previously through
     * the <tt>ConfigurationService</tt>. Indicates to the user interface whether
     * the default send message command is Enter or Ctrl-Enter.
     * @return "Enter" or "Ctrl-Enter" message commands.
     */
    public static String getSendMessageCommand()
    {
        return sendMessageCommand;
    }

    /**
     * Updates the "autoPopupNewMessage" property.
     * 
     * @param autoPopupNewMessage indicates to the user interface whether new
     * messages should be opened and bring to front. 
     **/
    public static void setAutoPopupNewMessage(boolean autoPopupNewMessage)
    {
        ConfigurationManager.autoPopupNewMessage = autoPopupNewMessage;
          
        if(autoPopupNewMessage)
            configService.setProperty(
                    "net.java.sip.communicator.impl.gui.autoPopupNewMessage",
                    "yes");
        else
            configService.setProperty(
                    "net.java.sip.communicator.impl.gui.autoPopupNewMessage",
                    "no");
    }

    public static void setShowOffline(boolean isShowOffline)
    {
        ConfigurationManager.isShowOffline = isShowOffline;

        configService.setProperty(
                "net.java.sip.communicator.impl.gui.showOffline",
                new Boolean(isShowOffline));
    }

    public static void setShowCallPanel(boolean isCallPanelShown)
    {
        ConfigurationManager.isCallPanelShown = isCallPanelShown;

        configService.setProperty(
                "net.java.sip.communicator.impl.gui.showCallPanel",
                new Boolean(isCallPanelShown));
    }

    public static void setApplicationVisible(boolean isVisible)
    {
        isApplicationVisible = isVisible;
            
        configService.setProperty(
                "net.java.sip.communicator.impl.systray.showApplication",
                new Boolean(isVisible));
    }

    public static void setSendTypingNotifications(boolean isSendTypingNotif)
    {
        isSendTypingNotifications = isSendTypingNotif;
            
        configService.setProperty(
                "net.java.sip.communicator.impl.gui.sendTypingNotifications",
                new Boolean(isSendTypingNotif));
    }

    public static void setSendMessageCommand(String newMessageCommand)
    {
        sendMessageCommand = newMessageCommand;

        configService.setProperty(
                "net.java.sip.communicator.impl.gui.sendMessageCommand",
                newMessageCommand);
    }

    public static void saveChatRoom(ProtocolProviderService protocolProvider,
        String oldChatRoomId, String newChatRoomId, String newChatRoomName)
    {   
        String prefix = "net.java.sip.communicator.impl.gui.accounts";
        
        List accounts = configService
                .getPropertyNamesByPrefix(prefix, true);

        Iterator accountsIter = accounts.iterator();

        while(accountsIter.hasNext()) {
            String accountRootPropName
                = (String) accountsIter.next();

            String accountUID
                = configService.getString(accountRootPropName);

            if(accountUID.equals(protocolProvider
                    .getAccountID().getAccountUniqueID()))
            {
                List chatRooms = configService
                    .getPropertyNamesByPrefix(
                        accountRootPropName + ".chatRooms", true);

                Iterator chatRoomsIter = chatRooms.iterator();

                boolean isExistingChatRoom = false;

                while(chatRoomsIter.hasNext())
                {
                    String chatRoomPropName
                        = (String) chatRoomsIter.next();

                    String chatRoomID
                        = configService.getString(chatRoomPropName);

                    if(!oldChatRoomId.equals(chatRoomID))
                        continue;

                    isExistingChatRoom = true;

                    configService.setProperty(chatRoomPropName,
                        newChatRoomId);

                    configService.setProperty(chatRoomPropName + ".chatRoomName",
                        newChatRoomName);
                }

                if(!isExistingChatRoom)
                {
                    String chatRoomNodeName
                        = "chatRoom" + Long.toString(System.currentTimeMillis());

                    String chatRoomPackage = accountRootPropName
                        + ".chatRooms." + chatRoomNodeName;

                    configService.setProperty(chatRoomPackage,
                        newChatRoomId);

                    configService.setProperty(chatRoomPackage + ".chatRoomName",
                        newChatRoomName);
                }
            }
        }
    }
}
