package Finance.gui.Panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Paths;
import shared.RecurringInvestment;

/**
 * Panel for managing recurring investment plans.
 * This component allows users to create, view, and export investment plans.
 * It handles the UI display, persistence, and export functionality of investment plans.
 */
public class InvestmentPanel {
    private final List<Investment> investments = new ArrayList<>(); // Stores all investment plans
    private JPanel mainPanel; // Main container for the UI components
    private JScrollPane scrollPane; // Scroll pane for investments list
    private final CardLayout cardLayout; // Layout manager for card-based navigation
    private final JPanel cardPanel; // Panel that uses the card layout

    /**
     * Constructor for InvestmentPanel.
     *
     * @param cardLayout The card layout manager for navigating between panels
     * @param cardPanel  The panel containing the different cards for navigation
     */
    public InvestmentPanel(CardLayout cardLayout, JPanel cardPanel) {
        this.cardLayout = cardLayout; // Store card layout reference
        this.cardPanel = cardPanel; // Store card panel reference
    }

    // Design constants for consistent UI styling
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 250); // Light background for main panel
    private static final Color CARD_COLOR = Color.WHITE; // White background for cards
    private static final Color PRIMARY_COLOR = new Color(0, 122, 255); // Blue accent color
    private static final Font TITLE_FONT = new Font("Inter", Font.BOLD, 14); // Bold font for titles
    private static final Font DETAIL_FONT = new Font("Inter", Font.PLAIN, 14); // Regular font for details
    private static final int CARD_PADDING = 15; // Padding inside cards
    private static final int CARD_HEIGHT = 120; // Fixed height for investment cards

    /**
     * Creates and returns the investment panel UI.
     *
     * @return The fully configured investment panel
     */
    public JPanel createInvestmentPanel() {
        initializeUI(); // Set up the UI components
        return mainPanel; // Return the configured panel
    }

    /**
     * Initializes the UI components of the panel.
     * Sets up the main panel, loads saved investments, and creates UI elements.
     */
    private void initializeUI() {
        mainPanel = new JPanel(); // Create main container panel
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS)); // Vertical layout
        mainPanel.setBackground(BACKGROUND_COLOR); // Set background color
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15)); // Add padding

        // Load saved investments before creating UI
        loadInvestments(); // Load previously saved investments from disk

        createHeader(); // Set up the header with buttons
        scrollPane = createInvestmentsList(); // Create scrollable list of investments
        mainPanel.add(scrollPane); // Add the scroll pane to main panel
    }

    /**
     * Creates the header section with action buttons.
     * Sets up "New Plan" and "Export to Investify" buttons.
     */
    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout()); // Create header container
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS)); // vertical order
        headerPanel.setBackground(BACKGROUND_COLOR); // Match main background
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0)); // Add bottom margin
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); // limit height

        JLabel titleLabel = new JLabel("Investment plans");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Center buttons panel with New Plan + Export buttons
        JPanel buttonsPanel = new JPanel(); // Panel to hold buttons
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0)); // Center alignment
        buttonsPanel.setBackground(BACKGROUND_COLOR); // Match main background

        JButton newPlanButton = createStyledButton("New Plan"); // Create "New Plan" button
        newPlanButton.addActionListener(e -> showNewInvestmentDialog()); // Add click handler

        JButton exportButton = createStyledButton("Export to Investify"); // Create "Export" button
        exportButton.addActionListener(e -> exportToInvestify()); // Add click handler

        // Add buttons to the panel
        buttonsPanel.add(newPlanButton); // Add new plan button
        buttonsPanel.add(exportButton); // Add export button

        // Place buttons panel in the center of header
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 8))); // Abstand zwischen Titel und Buttons

        headerPanel.add(buttonsPanel, BorderLayout.CENTER); // Center the buttons
        mainPanel.add(headerPanel); // Add header to main panel
    }

    /**
     * Creates a scrollable list showing all investment plans.
     * If no investments exist, displays a placeholder message.
     *
     * @return A scroll pane containing the investments list
     */
    private JScrollPane createInvestmentsList() {
        JPanel cardsPanel = new JPanel(new BorderLayout()); // Create main container panel with BorderLayout for proper centering capability
        cardsPanel.setBackground(BACKGROUND_COLOR);// background color to match the application theme

        // Check if there are no investments to display
        if (investments.isEmpty()) {
            JPanel centerPanel = new JPanel(new GridBagLayout()); // Create a panel specifically for centering the "empty" message
            centerPanel.setBackground(BACKGROUND_COLOR); // Set background to match parent

            JLabel emptyLabel = new JLabel("No investment plans yet.");  // Create the "no plans" label

            emptyLabel.setFont(DETAIL_FONT); // Set font style for the label
            emptyLabel.setForeground(Color.GRAY); // Set text color to gray for secondary text

            centerPanel.add(emptyLabel); // Add the label to the center panel
            cardsPanel.add(centerPanel, BorderLayout.CENTER);  // Add the center panel to the main container's center position

        } else {
            JPanel contentPanel = new JPanel(); // Create container for investment cards when plans exist
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS)); // Use BoxLayout for vertical stacking of cards
            contentPanel.setBackground(BACKGROUND_COLOR); // Match background color

            int cardWidth = Math.max(250, mainPanel.getWidth() - 2 * CARD_PADDING);// Calculate card width based on available space minus padding
            Dimension cardSize = new Dimension(cardWidth, CARD_HEIGHT); // Create dimension object for consistent card sizing

            for (Investment investment : investments) {  // Loop through all investments to create their cards
                JPanel card = createInvestmentCard(investment, cardSize); // Create a card UI component for this investment
                contentPanel.add(card); // Add the card to the vertical stack
                contentPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add spacing between cards
            }
            cardsPanel.add(contentPanel, BorderLayout.NORTH); // Add the cards container to the top of the main panel
        }

        JScrollPane scroll = new JScrollPane(cardsPanel); // Create scroll pane to contain the cards panel
        scroll.setBorder(null); // Remove default border
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); // Only show vertical scrollbar when needed
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // Never show horizontal scrollbar
        scroll.getVerticalScrollBar().setUnitIncrement(16);  // Set smooth scrolling increment
        return scroll; // Return the configured scroll pane
    }

    /**
     * Creates a UI card displaying an investment's details with a delete button.
     *
     * @param investment The investment to display
     * @param size       The dimension constraints for the card
     * @return A configured panel showing the investment details
     */
    private JPanel createInvestmentCard(Investment investment, Dimension size) {
        JPanel card = new JPanel(); // Create card container
        card.setLayout(new BorderLayout()); // Change to BorderLayout for better organization
        card.setBackground(CARD_COLOR); // White background
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)), // Light gray border
                new EmptyBorder(10, 15, 10, 15))); // Inner padding
        card.setPreferredSize(size); // Set card dimensions
        card.setMaximumSize(size); // Maintain fixed size

        // Panel for investment details (left side)
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(CARD_COLOR);

        // Investment Name
        JLabel nameLabel = new JLabel(investment.getName()); // Create investment name label
        nameLabel.setFont(TITLE_FONT); // Use title font
        nameLabel.setBorder(new EmptyBorder(0, 0, 5, 0)); // Add bottom margin
        detailsPanel.add(nameLabel); // Add name to details panel

        // Amount and Frequency
        JPanel amountFreqPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); // Create details container
        amountFreqPanel.setBackground(CARD_COLOR); // Match card background
        amountFreqPanel.setAlignmentX(Component.LEFT_ALIGNMENT); // Left-align content

        JLabel amountLabel = new JLabel(String.format("CHF %,.2f", investment.getAmount())); // Format amount with currency
        amountLabel.setFont(new Font("Inter", Font.BOLD, 14)); // Use bold font for amount
        amountLabel.setForeground(PRIMARY_COLOR); // Use primary color for amount

        JLabel frequencyLabel = new JLabel("• " + investment.getFrequency()); // Add bullet before frequency
        frequencyLabel.setFont(DETAIL_FONT); // Use detail font
        frequencyLabel.setForeground(Color.GRAY); // Gray text for secondary info

        amountFreqPanel.add(amountLabel); // Add amount to details
        amountFreqPanel.add(frequencyLabel); // Add frequency to details
        detailsPanel.add(amountFreqPanel); // Add details panel to card

        // Date
        JLabel dateLabel = new JLabel(
                investment.getDate().format(DateTimeFormatter.ofPattern("dd.MMM.yyyy"))); // Format date
        dateLabel.setFont(DETAIL_FONT); // Use detail font
        dateLabel.setForeground(Color.GRAY); // Gray text for secondary info
        dateLabel.setBorder(new EmptyBorder(10, 0, 0, 0)); // Add top margin
        detailsPanel.add(dateLabel); // Add date to details panel

        // Add details to the left side of the card
        card.add(detailsPanel, BorderLayout.CENTER);

        // Create delete button panel (right side)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_COLOR);

        // Create delete button
        JButton deleteButton = new JButton("Delete");
        deleteButton.setFont(new Font("Inter", Font.BOLD, 12));
        deleteButton.setBackground(new Color(220, 53, 69)); // Red color for delete action
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);
        deleteButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 53, 69).darker()),
                new EmptyBorder(4, 10, 4, 10)));

        // Add action listener to handle deletion
        deleteButton.addActionListener(e -> deleteInvestment(investment));

        buttonPanel.add(deleteButton);
        card.add(buttonPanel, BorderLayout.EAST);

        return card; // Return the configured card
    }

    /**
     * Deletes an investment from the investments list.
     * Removes the investment, updates the UI, and persists changes.
     *
     * @param investment The investment to delete
     */
    private void deleteInvestment(Investment investment) {
        // Confirm deletion with user
        int confirm = JOptionPane.showConfirmDialog(
                mainPanel,
                "Are you sure you want to delete the investment plan: " + investment.getName() + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            // Remove the investment from the list
            investments.remove(investment);

            // Save changes to disk
            saveInvestments();

            // Export updated investments to the shared file for Investify
            exportToInvestify();

            // Refresh the UI
            refreshInvestmentsList();

            // Show confirmation
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Investment plan deleted successfully.",
                    "Deletion Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    /**
     * Creates a styled button with consistent appearance.
     *
     * @param text The button text
     * @return A configured button with the application's style
     */
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text); // Create button with text
        button.setBackground(PRIMARY_COLOR); // Blue background
        button.setForeground(Color.WHITE); // White text
        button.setFont(new Font("Inter", Font.BOLD, 12)); // Bold font
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), // Light gray border
                new EmptyBorder(5, 15, 5, 15))); // Inner padding
        button.setFocusPainted(false); // Remove focus outline
        return button; // Return the styled button
    }

    /**
     * Displays a dialog to create a new investment plan.
     * Validates input and adds the new investment to the list if valid.
     */
    private void showNewInvestmentDialog() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5)); // Create form layout
        panel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Add padding

        JLabel nameLabel = new JLabel("Investment Name:"); // Name field label
        nameLabel.setFont(DETAIL_FONT); // Use detail font
        JTextField nameField = new JTextField(); // Field for investment name

        JLabel amountLabel = new JLabel("Amount (CHF):"); // Amount field label
        amountLabel.setFont(DETAIL_FONT); // Use detail font
        JTextField amountField = new JTextField(); // Field for amount

        JLabel dateLabel = new JLabel("Date:"); // Date field label
        dateLabel.setFont(DETAIL_FONT); // Use detail font
        JTextField dateField = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))); // Field for date with today as default

        JLabel frequencyLabel = new JLabel("Frequency:"); // Frequency field label
        frequencyLabel.setFont(DETAIL_FONT); // Use detail font
        String[] options = {"Daily", "Weekly", "Monthly", "Yearly"}; // Frequency options
        JComboBox<String> frequencyComboBox = new JComboBox<>(options); // Dropdown for frequency

        // Add form fields to panel
        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(amountLabel);
        panel.add(amountField);
        panel.add(dateLabel);
        panel.add(dateField);
        panel.add(frequencyLabel);
        panel.add(frequencyComboBox);

        int result = JOptionPane.showConfirmDialog(null, panel,
                "New Investment Plan", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE); // Show dialog and get result

        if (result == JOptionPane.OK_OPTION) {
            try {
                // Extract and parse form values
                String name = nameField.getText();
                double amount = Double.parseDouble(amountField.getText());
                LocalDate date = LocalDate.parse(dateField.getText(),
                        DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                String frequency = (String) frequencyComboBox.getSelectedItem();

                Investment investment = new Investment(name, amount, date, frequency); // Create investment object
                investments.add(investment); // Add to investments list
                refreshInvestmentsList(); // Update the UI

                // Save changes to disk
                saveInvestments();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                        "Please enter valid values. " + ex.getMessage(),
                        "Input Error", JOptionPane.ERROR_MESSAGE); // Show error for invalid input
            }
        }
    }

    /**
     * Refreshes the investments list in the UI.
     * Removes and recreates the scroll pane to reflect changes.
     */
    private void refreshInvestmentsList() {
        mainPanel.remove(scrollPane); // Remove current scroll pane
        scrollPane = createInvestmentsList(); // Create updated list
        mainPanel.add(scrollPane); // Add new scroll pane
        mainPanel.revalidate(); // Revalidate layout
        mainPanel.repaint(); // Repaint component
    }

    /**
     * Saves the investments list to a file on disk.
     * Stores data in the user's home directory.
     */
    private void saveInvestments() {
        // Create path in user's home directory
        String filePath = Paths.get(System.getProperty("user.home"),
                "financeTrackerInvestments.dat").toString(); // Get file path in user's home directory

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            // Save the entire investments list
            out.writeObject(new ArrayList<>(investments)); // Write investments to file
            System.out.println("Investments saved successfully"); // Log success
        } catch (Exception e) {
            System.err.println("Error saving investments: " + e.getMessage()); // Log error
        }
    }

    /**
     * Loads investments from disk.
     * Replaces current investments list with saved data if available.
     */
    @SuppressWarnings("unchecked")
    private void loadInvestments() {
        String filePath = Paths.get(System.getProperty("user.home"),
                "financeTrackerInvestments.dat").toString(); // Get file path in user's home directory

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            // Read the saved list and replace the current list
            List<Investment> loadedInvestments = (List<Investment>) in.readObject(); // Read from file
            investments.clear(); // Clear current list
            investments.addAll(loadedInvestments); // Add all loaded investments
            System.out.println("Loaded " + investments.size() + " investments"); // Log count
        } catch (FileNotFoundException e) {
            // First time use - no file exists yet
            System.out.println("No saved investments found - starting with empty list"); // Log first use
        } catch (Exception e) {
            System.err.println("Error loading investments: " + e.getMessage()); // Log other errors
        }
    }

    /**
     * Exports investments to Investify application.
     * Converts internal investments to shared model format and writes to shared file.
     */
    public void exportToInvestify() {
        try {
            String sharedFilePath = Paths.get(System.getProperty("user.home"),
                    "recurringInvestments.dat").toString(); // Path for shared file

            List<RecurringInvestment> exportList = new ArrayList<>(); // Create list for export

            // Convert internal investments to shared model
            for (Investment inv : investments) {
                // Default symbol to first word of name if not explicitly set
                String symbol = inv.getName().split(" ")[0]; // Simple heuristic for stock symbol

                RecurringInvestment recInv = new RecurringInvestment(
                        inv.getName(),
                        inv.getAmount(),
                        inv.getFrequency(),
                        inv.getDate(),
                        symbol
                ); // Create shared model instance
                exportList.add(recInv); // Add to export list
            }

            // Save to shared file location
            try (ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(sharedFilePath))) {
                out.writeObject(exportList); // Write shared model to file
            }

            JOptionPane.showMessageDialog(mainPanel,
                    "Investments exported to Investify successfully!",
                    "Export Success", JOptionPane.INFORMATION_MESSAGE); // Show success message

        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainPanel,
                    "Failed to export investments: " + e.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE); // Show error message
        }
    }

    /**
     * Internal model class for storing investment data.
     * Implements Serializable for persistence.
     */
    private static class Investment implements Serializable {
        private static final long serialVersionUID = 1L; // For serialization compatibility
        private final String name; // Name of the investment
        private final double amount; // Amount to invest each time
        private final LocalDate date; // Start date of the investment
        private final String frequency; // How often to invest (Daily, Weekly, etc)

        /**
         * Creates a new investment plan.
         *
         * @param name      The name of the investment
         * @param amount    The investment amount
         * @param date      The start date
         * @param frequency How often to invest
         */
        public Investment(String name, double amount, LocalDate date, String frequency) {
            this.name = name; // Set investment name
            this.amount = amount; // Set investment amount
            this.date = date; // Set start date
            this.frequency = frequency; // Set frequency
        }

        /**
         * Gets the investment name.
         *
         * @return The name of this investment plan
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the investment amount.
         *
         * @return The amount to invest each time
         */
        public double getAmount() {
            return amount;
        }

        /**
         * Gets the start date.
         *
         * @return The date this investment begins
         */
        public LocalDate getDate() {
            return date;
        }

        /**
         * Gets the investment frequency.
         *
         * @return How often this investment occurs
         */
        public String getFrequency() {
            return frequency;
        }
    }
}