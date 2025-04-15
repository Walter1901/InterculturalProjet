import javax.swing.*;
import java.awt.*;


public class Homescreen extends phoneUtils{

    public static CardLayout cardLayout = new CardLayout();
    public static JPanel mainPanel = new JPanel(cardLayout);

    public static void main(String[] args) {
        JFrame phoneFrame = phoneUtils.createPhoneFrame("The Phone");

        // Add home screen
        JPanel homeScreen = createAppIconsPanel(phoneFrame);
        mainPanel.add(homeScreen, "Home");

        // Add Investify screen
        JPanel investifyScreen = InvestifyApp.createInvestify();
        mainPanel.add(investifyScreen, "Investify");

        // Add Address Book screen
        JPanel addressBookScreen = AddressBook.createAddressBook();
        mainPanel.add(addressBookScreen, "Address Book");

        // Add Picture Gallery screen
        JPanel pictureGalleryScreen = PictureGallery.createPictureGallery();
        mainPanel.add(pictureGalleryScreen, "Picture Gallery");

        // Add Finance Tracker screen
        JPanel financeTrackerScreen = FinanceTracker.createFinanceTracker();
        mainPanel.add(financeTrackerScreen, "Finance Tracker");


        phoneFrame.add(mainPanel, BorderLayout.CENTER);
        phoneFrame.add(phoneUtils.createTopBar(), BorderLayout.NORTH);
        phoneFrame.add(phoneUtils.createBottomBar(phoneFrame), BorderLayout.SOUTH);
        phoneFrame.setVisible(true);
    }

    // Method to create the app icons panel
    public static JPanel createAppIconsPanel(JFrame parentFrame) {
        JPanel appIconsPanel = new JPanel(new GridLayout(2, 2, 40, 40));
        appIconsPanel.setBackground(backgroundColor);
        appIconsPanel.setBorder(BorderFactory.createEmptyBorder(90, 30, 150, 30));

        appIconsPanel.add(createAppButton("Address Book", "src/main/resources/addressBookIcon.png", parentFrame));
        appIconsPanel.add(createAppButton("Picture Gallery", "src/main/resources/galleryIcon.png", parentFrame));
        appIconsPanel.add(createAppButton("Finance Tracker", "src/main/resources/financeTrackerIcon.png", parentFrame));
        appIconsPanel.add(createAppButton("Investify", "src/main/resources/investifyIcon.png", parentFrame));

        return appIconsPanel;
    }

    // Method to create app buttons (used by the method "createAppIconsPanel")
    public static JButton createAppButton(String appName, String iconPath, JFrame parentFrame) {
        JButton appButton = new JButton(appName);
        appButton.setFont(new Font("Inter", Font.PLAIN, 12));

        try {
            ImageIcon icon = new ImageIcon(iconPath);
            Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            appButton.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            System.err.println("Error loading icon from path: " + iconPath);
            ImageIcon icon = (ImageIcon) UIManager.getIcon("FileView.computerIcon");
            if (icon != null) {
                Image img = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                appButton.setIcon(new ImageIcon(img));
            }
        }

        appButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        appButton.setHorizontalTextPosition(SwingConstants.CENTER);
        appButton.setForeground(textColor);
        appButton.setBackground(backgroundColor);
        appButton.setFocusPainted(false);
        appButton.setBorderPainted(false);

        appButton.addActionListener(e -> {
            switch (appName) {
                case "Investify":
                    Homescreen.cardLayout.show(Homescreen.mainPanel, "Investify");
                    break;
                case "Address Book":
                    Homescreen.cardLayout.show(Homescreen.mainPanel, "Address Book");
                    break;
                case "Picture Gallery":
                    Homescreen.cardLayout.show(Homescreen.mainPanel, "Picture Gallery");
                    break;
                case "Finance Tracker":
                    Homescreen.cardLayout.show(Homescreen.mainPanel, "Finance Tracker");
                    break;
                default:
                    JOptionPane.showMessageDialog(parentFrame, "Opening " + appName + "...");
                    break;
            }
        });

        return appButton;
    }

}
