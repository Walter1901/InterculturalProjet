import javax.swing.*;
import java.awt.*;

public class AddressBook {

    public static JPanel createAddressBook() {

        JPanel addressBookApp = new JPanel();
        addressBookApp.setBackground(phoneUtils.backgroundColor);
        JLabel label = new JLabel("Address Book App", SwingConstants.CENTER);
        label.setFont(new Font("Inter", Font.BOLD, 20));
        label.setForeground(phoneUtils.textColor);
        addressBookApp.add(label, BorderLayout.CENTER);

        return addressBookApp;
    }


}
