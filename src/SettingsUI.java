import jdk.jfr.SettingControl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

public class SettingsUI extends JDialog {
    JTextField usernameTextField;
    Settings settings;

    public SettingsUI(Settings settings, Frame parent) {
        super(parent, "ChatClient-Settings", true);
        this.settings = settings;
        JPanel settingsPanel = new JPanel();
        JButton saveButton = new JButton("Speichern");
        saveButton.addActionListener(new SaveButtonListener());
        JLabel usernameTextLabel = new JLabel("Username: ");
        usernameTextField = new JTextField(10);
        JPanel usernamePanel = new JPanel();
        usernamePanel.add(usernameTextLabel);
        usernamePanel.add(usernameTextField);
        usernamePanel.setBackground(Color.white);
        settingsPanel.add(usernamePanel);
        settingsPanel.add(saveButton);
        settingsPanel.setBackground(Color.white);

        fillUsername();

        getContentPane().add(BorderLayout.WEST, settingsPanel);
        getContentPane().setBackground(Color.white);
        setSize(350,100);
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
    }

    private void fillUsername() {
        if (settings.getUsername() == null || settings.getUsername().equals("")){
            usernameTextField.setText("");
        } else {
            usernameTextField.setText(settings.getUsername());
        }
    }

    public class SaveButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!usernameTextField.getText().equals("")) {
                WriteXMLFile writeXMLFile = new WriteXMLFile();
                writeXMLFile.writeSettingsToXML(usernameTextField.getText());
                settings.setUsername(usernameTextField.getText());

                UIManager UI=new UIManager();
                UI.put("OptionPane.background", new Color(255, 255, 255));
                UI.put("Panel.background", Color.white);
                JOptionPane.showMessageDialog(null,"Einstellungen gespeichert!","Settings saved", JOptionPane.PLAIN_MESSAGE);
                dispose();
            }
        }
    }
}
