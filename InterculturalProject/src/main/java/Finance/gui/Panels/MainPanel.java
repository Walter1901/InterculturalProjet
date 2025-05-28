package Finance.gui.Panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;
import Finance.gui.components.UIComponents;

public class MainPanel {
    // This manages which screen is visible, like switching between pages
    private final CardLayout cardLayout;
    // This panel holds all the different screens we can switch between
    private final JPanel cardPanel;

    // Constructor to set up the panel with the layout, container
    public MainPanel(CardLayout cardLayout, JPanel cardPanel) {
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
    }

    // This builds the main panel UI with greeting, logo, menu, and logout button
    public JPanel createMainPanel() {
        // Using BorderLayout so we can put stuff on top, center, bottom, etc.
        JPanel mainPanel = new JPanel(new BorderLayout());
        // Background color
        mainPanel.setBackground(new Color(245, 245, 250));

        // Now load the logo image and put it in the middle of the panel
        try {
            // Look for the image file in resources folder
            URL imageUrl = getClass().getResource("/Finance/logo.png");
            if (imageUrl != null) {
                ImageIcon logoIcon = new ImageIcon(imageUrl);
                // Scale the logo nicely to 350x200 pixels
                Image image = logoIcon.getImage().getScaledInstance(350, 200, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(image));
                logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
                logoLabel.setBorder(new EmptyBorder(10, 0, 10, 0)); // padding top and bottom
                mainPanel.add(logoLabel, BorderLayout.CENTER); // add it in the center
            } else {
                // If the image is missing, throw an error so we know
                throw new Exception("Resource not found: /finance/logo.png");
            }
        } catch (Exception e) {
            // If loading the logo fails, just show a text saying "Logo not found"
            JLabel errorLabel = new JLabel("Logo not found", SwingConstants.CENTER);
            mainPanel.add(errorLabel, BorderLayout.CENTER);
            e.printStackTrace(); // print the error for debugging
        }

        // Create the menu panel that goes at the bottom with buttons/links
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS)); // stack items vertically
        menuPanel.setBackground(new Color(245, 245, 250)); // match main background color
        menuPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // add some padding around

        // Add menu items using our UIComponents helper class, each switches view on click
        menuPanel.add(UIComponents.createMenuItem("My expenses", " $", () -> cardLayout.show(cardPanel, "expenses")));
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10))); // space between items
        menuPanel.add(UIComponents.createMenuItem("Balance overview", "📈", () -> cardLayout.show(cardPanel, "balance")));
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(UIComponents.createMenuItem("Saving goals", "🎯", () -> cardLayout.show(cardPanel, "goals")));
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(UIComponents.createMenuItem("Investment", "\uD83D\uDCB0", () -> cardLayout.show(cardPanel, "investment")));
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Put the menu panel at the bottom of the main panel
        mainPanel.add(menuPanel, BorderLayout.SOUTH);

        return mainPanel; // return the finished panel
    }

    // This is a callback function that will run when the user logs out
    private Runnable onLogoutCallback;

    // Allow other classes to set what happens when logout is clicked
    public void setOnLogout(Runnable callback) {
        this.onLogoutCallback = callback;
    }

    // Called internally when the logout button is clicked
    private void onLogout() {
        if (onLogoutCallback != null) {
            onLogoutCallback.run(); // run whatever logout logic was set from outside
        }
    }
}