import javax.swing.*;
import java.awt.*;

public class Homescreen extends phoneUtils {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    private final JFrame phoneFrame;

    public Homescreen() {
        this.phoneFrame = createPhoneFrame("The Phone");

        // Add the screens
        mainPanel.add(createAppIconsPanel(), "Home");
        mainPanel.add(InvestifyApp.createInvestify(), "Investify");
        mainPanel.add(AddressBook.createAddressBook(), "Address Book");
        mainPanel.add(PictureGallery.createPictureGallery(), "Picture Gallery");
        mainPanel.add(FinanceTracker.createFinanceTracker(), "Finance Tracker");

        // Add components to the frame
        phoneFrame.add(mainPanel, BorderLayout.CENTER);
        phoneFrame.add(createTopBar(), BorderLayout.NORTH);
        phoneFrame.add(createBottomBar(() -> cardLayout.show(mainPanel, "Home")), BorderLayout.SOUTH);

        phoneFrame.setVisible(true);
    }

    private JPanel createAppIconsPanel() {
        JPanel appIconsPanel = new JPanel(new GridLayout(2, 2, 40, 40));
        appIconsPanel.setBackground(backgroundColor);
        appIconsPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 150, 30));

        appIconsPanel.add(createAppButton("Address Book", "src/main/resources/homescreenIcons/addressBookIcon.png"));
        appIconsPanel.add(createAppButton("Picture Gallery", "src/main/resources/homescreenIcons/galleryIcon.png"));
        appIconsPanel.add(createAppButton("Finance Tracker", "src/main/resources/homescreenIcons/financeTrackerIcon.png"));
        appIconsPanel.add(createAppButton("Investify", "src/main/resources/homescreenIcons/investifyIcon.png"));

        return appIconsPanel;
    }

    private JButton createAppButton(String appName, String iconPath) {
        JButton appButton = new JButton(appName);
        appButton.setFont(new Font("Inter", Font.PLAIN, 12));

        try {
            ImageIcon icon = new ImageIcon(iconPath);
            Image img = icon.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
            appButton.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            System.err.println("Error loading icon from path: " + iconPath);
        }

        appButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        appButton.setHorizontalTextPosition(SwingConstants.CENTER);
        appButton.setForeground(textColor);
        appButton.setBackground(backgroundColor);
        appButton.setFocusPainted(false);
        appButton.setBorderPainted(false);

        appButton.addActionListener(e -> cardLayout.show(mainPanel, appName));

        return appButton;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Homescreen::new);
    }
}
