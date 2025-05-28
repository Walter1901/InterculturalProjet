package Finance.gui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

// Utility class for reusable UI components used in the Finance GUI
public class UIComponents {

    /**
     * Creates a bottom navigation bar with buttons to switch between different views
     * using CardLayout.
     *
     * @param cardLayout The CardLayout managing the main content panels.
     * @param cardPanel The container JPanel that uses the CardLayout.
     * @return JPanel containing navigation buttons.
     */
    public static JPanel createBottomNavigation(CardLayout cardLayout, JPanel cardPanel) {
        // Panel with GridLayout: 1 row, 3 columns for buttons (can add more later)
        JPanel navPanel = new JPanel(new GridLayout(1, 3));

        // Light purple background color for the nav bar
        navPanel.setBackground(new Color(230, 230, 250));

        // Padding around the nav panel's edges
        navPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Add Home button that switches view to "main"
        navPanel.add(createNavButton("Home", "🏠", () -> cardLayout.show(cardPanel, "main")));

        // Note: The third slot is left empty for now; could add another button later

        return navPanel;
    }

    /**
     * Creates a navigation button with an icon and text stacked vertically.
     * The button reacts visually on hover and executes an action on click.
     *
     * @param text The text label below the icon.
     * @param icon A string representing an icon (e.g., emoji or Unicode symbol).
     * @param action The Runnable to execute when the button is clicked.
     * @return JPanel representing the navigation button.
     */
    public static JPanel createNavButton(String text, String icon, Runnable action) {
        JPanel navButton = new JPanel();

        // Vertical box layout to stack icon and text
        navButton.setLayout(new BoxLayout(navButton, BoxLayout.Y_AXIS));

        // Same background as navigation bar
        navButton.setBackground(new Color(230, 230, 250));

        // Padding inside the button
        navButton.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Change cursor to hand pointer on hover
        navButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // JLabel for the icon, centered
        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Inter", Font.PLAIN, 20)); // Larger font for icon
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center horizontally

        // JLabel for the text, centered below icon
        JLabel textLabel = new JLabel(text, SwingConstants.CENTER);
        textLabel.setFont(new Font("Inter", Font.PLAIN, 14)); // Smaller font for text
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components to nav button panel vertically with spacing
        navButton.add(iconLabel);
        navButton.add(Box.createRigidArea(new Dimension(0, 5))); // Spacer between icon and text
        navButton.add(textLabel);

        // Add mouse listener for click and hover effects
        navButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.run(); // Execute action on click
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // Slightly darker background on hover
                navButton.setBackground(new Color(210, 220, 240));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Reset background when mouse leaves
                navButton.setBackground(new Color(230, 230, 250));
            }
        });

        return navButton;
    }

    /**
     * Creates a menu item panel with an icon on the left, text next to it,
     * and a vertical ellipsis ("⋮") on the right.
     * Supports hover color change and click action.
     *
     * @param text The descriptive text for the menu item.
     * @param iconText The icon or symbol displayed on the left.
     * @param action The Runnable to execute when the menu item is clicked.
     * @return JPanel representing the menu item.
     */
    public static JPanel createMenuItem(String text, String iconText, Runnable action) {
        // Panel with BorderLayout to arrange left content and right icon
        JPanel itemPanel = new JPanel(new BorderLayout());

        // White background for menu items
        itemPanel.setBackground(Color.WHITE);

        // Padding inside the menu item
        itemPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Cursor changes to hand pointer on hover to indicate clickability
        itemPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Icon label on the left with blue color and larger font size
        JLabel iconLabel = new JLabel(iconText);
        iconLabel.setFont(new Font("Inter", Font.PLAIN, 24));
        iconLabel.setForeground(new Color(0, 122, 255)); // Blue color

        // Text label next to the icon
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Inter", Font.PLAIN, 16));
        textLabel.setForeground(new Color(30, 30, 30)); // Dark text color

        // Panel to hold icon and text horizontally with spacing
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(Color.WHITE); // Match background
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
        contentPanel.add(iconLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(10, 0))); // Spacer between icon and text
        contentPanel.add(textLabel);

        // Add the content panel to the west (left side) of itemPanel
        itemPanel.add(contentPanel, BorderLayout.WEST);

        // Add vertical ellipsis ("⋮") label to the east (right side)
        itemPanel.add(new JLabel("⋮"), BorderLayout.EAST);

        // Mouse listener to handle click and hover color changes
        itemPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.run(); // Execute action on click
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // Light blue background on hover
                itemPanel.setBackground(new Color(230, 240, 255));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Reset to white background when mouse leaves
                itemPanel.setBackground(Color.WHITE);
            }
        });
        return itemPanel;
    }
}