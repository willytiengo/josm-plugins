package geochat;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.JosmUserIdentityManager;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 *
 * @author zverik
 */
public class GeoChatPanel extends ToggleDialog implements ChatServerConnectionListener {

    private JTextPane chatPane;
    private JTextField input;
    private JTabbedPane tabs;
    private JPanel chatPanel;
    private JComponent noData;
    private JPanel loginPanel;
    private JPanel gcPanel;
    private ChatServerConnection connection;
    
    public GeoChatPanel() {
        super(tr("GeoChat"), "geochat", tr("Open GeoChat panel"), null, 200, true);

        chatPane = new JTextPane();
        chatPane.setEditable(false);

        noData = new JLabel(tr("Zoom in to see messages"), SwingConstants.CENTER);

        tabs = new JTabbedPane();
        tabs.addTab(tr("Public"), noData);

        input = new JTextField();

        gcPanel = new JPanel(new BorderLayout());
        gcPanel.add(tabs, BorderLayout.CENTER);
        gcPanel.add(input, BorderLayout.SOUTH);

        loginPanel = new JPanel(new BorderLayout());
        final JTextField nameField = new JTextField();
        String userName = JosmUserIdentityManager.getInstance().getUserName();
        if( userName == null )
            userName = "";
        if( userName.contains("@") )
            userName = userName.substring(0, userName.indexOf('@'));
        nameField.setText(name);

        JButton loginButton = new JButton(tr("Login"));
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                connection.login(nameField.getText());
            }
        });

        loginPanel.add(nameField, BorderLayout.CENTER);
        loginPanel.add(loginButton, BorderLayout.EAST);

        gcPanel = new JPanel(new BorderLayout());
        gcPanel.add(loginPanel, BorderLayout.CENTER);
        createLayout(gcPanel, false, null);

        // Start threads
        connection = ChatServerConnection.getInstance();
    }

    public void loggedIn( String userName ) {
        gcPanel.remove(0);
        gcPanel.add(tabs, 0);
    }

    public void notLoggedIn( String reason ) {
        JOptionPane.showMessageDialog(Main.parent, reason);
    }

    public void messageSendFailed( String reason ) {
    }

    public void statusChanged( boolean active ) {
        tabs.setComponentAt(0, active ? chatPane : noData);
    }

    public void updateUsers( Map<String, LatLon> users ) {
    }

    private final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
    public void receivedMessages( boolean replace, List<ChatMessage> messages ) {
        if( replace ) {
            chatPane.setText("");
        }
        StringBuffer sb = new StringBuffer();
        for( ChatMessage msg : messages ) {
            sb.append('\n');
            sb.append('[').append(TIME_FORMAT.format(msg.getTime())).append("] ");
            sb.append(msg.getAuthor()).append(": ").append(msg.getMessage());
        }

        Document doc = chatPane.getDocument();
        try {
            doc.insertString(doc.getLength(), sb.toString(), null);
        } catch( BadLocationException ex ) {
            // whatever
        }
    }

    public void receivedPrivateMessages( boolean replace, List<ChatMessage> messages ) {
    }
}