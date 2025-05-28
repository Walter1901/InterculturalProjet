package Finance.gui.Panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import Finance.data.BalanceStorage;

// Panel for displaying and managing financial balance information
public class BalancePanel {
    // Data structures for storing monthly financial information
    private final Map<String, Map<String, String>> monthlyNetSavings;
    private final Map<String, Map<String, String>> monthlyDebts;
    private final BalanceStorage balanceStorage; // Storage handler for persisting data

    // UI components
    private JPanel balanceArea;
    private JComboBox<String> balanceMonthCombo;
    private JComboBox<String> balanceYearCombo;
    private static final Font TITLE_FONT = new Font("Inter", Font.BOLD, 14);

    // Constructor initializes data storage and loads saved data
    public BalancePanel(Map<String, Map<String, String>> monthlyNetSavings,
                        Map<String, Map<String, String>> monthlyDebts) {
        this.balanceStorage = new BalanceStorage();

        // Load saved data from storage
        Map<String, Map<String, String>> loadedSavings = balanceStorage.loadNetSavings();
        Map<String, Map<String, String>> loadedDebts = balanceStorage.loadDebts();

        // Initialize with passed-in maps and merge with loaded data
        this.monthlyNetSavings = monthlyNetSavings;
        this.monthlyNetSavings.putAll(loadedSavings);

        this.monthlyDebts = monthlyDebts;
        this.monthlyDebts.putAll(loadedDebts);
    }

    // Creates and returns the main balance panel
    public JPanel createBalancePanel() {
        JPanel balancePanel = new JPanel(new BorderLayout());
        balancePanel.setBackground(new Color(245, 245, 250));
        balancePanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Create header panel with title and date selection
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 245, 250));

        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        datePanel.setBackground(new Color(245, 245, 250));

        // Month and year selection dropdowns
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        String[] years = {"2024", "2025"};

        balanceMonthCombo = new JComboBox<>(months);
        balanceYearCombo = new JComboBox<>(years);
        balanceMonthCombo.setFont(new Font("Inter", Font.PLAIN, 14));
        balanceYearCombo.setFont(new Font("Inter", Font.PLAIN, 14));

        // Set default to current month/year
        LocalDate now = LocalDate.now();
        balanceMonthCombo.setSelectedIndex(now.getMonthValue() - 1);
        balanceYearCombo.setSelectedItem(String.valueOf(now.getYear()));

        JLabel titleLabel = new JLabel("Balance overview");
        titleLabel.setFont(TITLE_FONT);

        datePanel.add(balanceMonthCombo);
        datePanel.add(balanceYearCombo);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(datePanel, BorderLayout.EAST);

        balancePanel.add(headerPanel, BorderLayout.NORTH);

        // Main content area with scroll support
        balanceArea = new JPanel(new BorderLayout());
        balanceArea.setBackground(new Color(245, 245, 250));

        JScrollPane scrollPane = new JScrollPane(balanceArea);
        scrollPane.setBorder(null);
        balancePanel.add(scrollPane, BorderLayout.CENTER);

        // Date change listener to update display when month/year changes
        ItemListener dateChangeListener = e -> {
            titleLabel.setText("Balance overview");
            updateBalanceArea();
        };
        balanceMonthCombo.addItemListener(dateChangeListener);
        balanceYearCombo.addItemListener(dateChangeListener);

        // Bottom panel with action buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(245, 245, 250));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(245, 245, 250));

        // Buttons for entering financial data
        JButton netSavingsButton = new JButton("Enter Net Savings");
        netSavingsButton.setBackground(new Color(0, 122, 255));
        netSavingsButton.setForeground(Color.WHITE);
        netSavingsButton.addActionListener(e -> enterFinancialData("Net Savings"));

        JButton debtsButton = new JButton("Enter Debts");
        debtsButton.setBackground(new Color(0, 122, 255));
        debtsButton.setForeground(Color.WHITE);
        debtsButton.addActionListener(e -> enterFinancialData("Debts"));

        buttonPanel.add(netSavingsButton);
        buttonPanel.add(debtsButton);
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);

        balancePanel.add(bottomPanel, BorderLayout.SOUTH);

        // Initial update of the display
        updateBalanceArea();
        return balancePanel;
    }

    // Shows dialog for entering financial data (either net savings or debts)
    private void enterFinancialData(String type) {
        String month = (String) balanceMonthCombo.getSelectedItem();
        String year = (String) balanceYearCombo.getSelectedItem();
        String key = month + " " + year;

        JPanel panel = new JPanel(new GridLayout(0, 1));

        if (type.equals("Net Savings")) {
            // Net savings input fields
            JLabel currentLabel = new JLabel("Current month (CHF):");
            JTextField currentField = new JTextField();
            JLabel yearLabel = new JLabel("Full year (CHF):");
            JTextField yearField = new JTextField();

            panel.add(currentLabel);
            panel.add(currentField);
            panel.add(yearLabel);
            panel.add(yearField);

            // Add fields for previous 3 months
            LocalDate now = LocalDate.now();
            for (int i = 3; i >= 1; i--) {
                LocalDate prevMonth = now.minusMonths(i);
                String monthName = prevMonth.getMonth().name().substring(0, 1) +
                        prevMonth.getMonth().name().substring(1).toLowerCase();
                String monthKey = prevMonth.getMonth().name().substring(0, 3).toLowerCase(); // "mar", "apr", etc.

                JLabel label = new JLabel(monthName + " (CHF):");
                JTextField field = new JTextField();
                field.setName(monthKey); // Store as 3-letter key (e.g., "mar")
                panel.add(label);
                panel.add(field);
            }

            int result = JOptionPane.showConfirmDialog(null, panel,
                    "Enter Net Savings Data", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    Map<String, String> netSavingsData = new HashMap<>();
                    netSavingsData.put("current", currentField.getText());
                    netSavingsData.put("year", yearField.getText());

                    // Get values from previous month fields
                    Component[] components = panel.getComponents();
                    for (Component c : components) {
                        if (c instanceof JTextField && c.getName() != null) {
                            netSavingsData.put(c.getName(), ((JTextField) c).getText());
                        }
                    }

                    monthlyNetSavings.put(key, netSavingsData);
                    balanceStorage.saveNetSavings(monthlyNetSavings);

                    updateBalanceArea();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Invalid input", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (type.equals("Debts")) {
            // Debts input fields
            JLabel creditLabel = new JLabel("Credit Card (CHF):");
            JTextField creditField = new JTextField();

            JLabel loanLabel = new JLabel("Student Loan (CHF):");
            JTextField loanField = new JTextField();

            panel.add(creditLabel);
            panel.add(creditField);
            panel.add(loanLabel);
            panel.add(loanField);

            int result = JOptionPane.showConfirmDialog(null, panel,
                    "Enter Debts Data", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    Map<String, String> debtsData = new HashMap<>();
                    debtsData.put("credit", creditField.getText());
                    debtsData.put("loan", loanField.getText());

                    monthlyDebts.put(key, debtsData);
                    balanceStorage.saveDebts(monthlyDebts);
                    updateBalanceArea();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Invalid input", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // Updates the display area with current financial data
    private void updateBalanceArea() {
        String month = (String) balanceMonthCombo.getSelectedItem();
        String year = (String) balanceYearCombo.getSelectedItem();
        String key = month + " " + year;

        balanceArea.removeAll();
        balanceArea.setLayout(new BorderLayout());
        balanceArea.setBackground(new Color(245, 245, 250));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(245, 245, 250));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        if (monthlyNetSavings.containsKey(key) || monthlyDebts.containsKey(key)) {
            // Create net savings card
            JPanel savingsCard = createCardPanel("Net Savings");
            Map<String, String> netSavings = monthlyNetSavings.getOrDefault(key, new HashMap<>());

            // Current month and full year values
            JPanel currentMonthPanel = createValuePanel("Current Month",
                    "CHF " + netSavings.getOrDefault("current", "0"),
                    new Color(50, 205, 50));

            JPanel fullYearPanel = createValuePanel("Full Year",
                    "CHF " + netSavings.getOrDefault("year", "0"),
                    new Color(50, 205, 50));

            savingsCard.add(currentMonthPanel);
            savingsCard.add(Box.createRigidArea(new Dimension(0, 5)));
            savingsCard.add(fullYearPanel);

            // Monthly trend section
            JLabel trendLabel = new JLabel("Monthly Trend");
            trendLabel.setFont(new Font("Inter", Font.BOLD, 12));
            trendLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel trendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            trendPanel.setBackground(Color.WHITE);
            trendPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            savingsCard.add(Box.createRigidArea(new Dimension(0, 10)));
            savingsCard.add(trendLabel);
            savingsCard.add(Box.createRigidArea(new Dimension(0, 5)));
            savingsCard.add(trendPanel);

            // Calculate current date from selection
            LocalDate currentDate = LocalDate.of(
                    Integer.parseInt(year),
                    balanceMonthCombo.getSelectedIndex() + 1, // Months are 0-based in combo
                    1
            );

            // Get previous 3 months for trend analysis
            String[] prevMonths = new String[3];
            for (int i = 0; i < 3; i++) {
                LocalDate prevMonth = currentDate.minusMonths(3 - i);
                prevMonths[i] = prevMonth.getMonth().name().substring(0, 3).toLowerCase(); // "mar", "apr", etc.
            }

            // Display trend for these months
            trendPanel.add(createTrendItem(
                    prevMonths[0].substring(0, 1).toUpperCase() + prevMonths[0].substring(1), // "Mar"
                    netSavings.getOrDefault(prevMonths[0], "0"),
                    "-",
                    new Color(100, 100, 100)
            ));

            // Calculate and display trends
            for (int i = 1; i < 3; i++) {
                double currentVal = parseDoubleSafe(netSavings.getOrDefault(prevMonths[i], "0"));
                double prevVal = parseDoubleSafe(netSavings.getOrDefault(prevMonths[i-1], "0"));

                String trend = currentVal > prevVal ? "▲" : (currentVal < prevVal ? "▼" : "-");
                Color color = currentVal > prevVal ? new Color(50, 205, 60) :
                        (currentVal < prevVal ? new Color(220, 20, 60) : new Color(100, 100, 100));

                trendPanel.add(createTrendItem(
                        prevMonths[i].substring(0, 1).toUpperCase() + prevMonths[i].substring(1), // "Apr"
                        netSavings.getOrDefault(prevMonths[i], "0"),
                        trend,
                        color
                ));
            }

            savingsCard.add(Box.createRigidArea(new Dimension(0, 5)));
            savingsCard.add(trendPanel);

            mainPanel.add(savingsCard);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

            // Create debts card
            JPanel debtsCard = createCardPanel("Debts");
            Map<String, String> debts = monthlyDebts.getOrDefault(key, new HashMap<>());

            JPanel creditCardPanel = createValuePanel("Credit Card",
                    "CHF " + debts.getOrDefault("credit", "0"),
                    new Color(220, 20, 60));

            JPanel studentLoanPanel = createValuePanel("Student Loan",
                    "CHF " + debts.getOrDefault("loan", "0"),
                    new Color(220, 20, 60));

            debtsCard.add(creditCardPanel);
            debtsCard.add(Box.createRigidArea(new Dimension(0, 5)));
            debtsCard.add(studentLoanPanel);

            mainPanel.add(debtsCard);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

            // Calculate and display net worth
            try {
                double assets = Double.parseDouble(netSavings.getOrDefault("year", "0"));
                double totalDebts = Double.parseDouble(debts.getOrDefault("credit", "0")) +
                        Double.parseDouble(debts.getOrDefault("loan", "0"));
                double netWorth = assets - totalDebts;

                Color netWorthColor = netWorth >= 0 ? new Color(50, 205, 50) : new Color(220, 20, 60);
                JPanel netWorthPanel = createValuePanel("Net Worth",
                        String.format("CHF %.2f", netWorth),
                        netWorthColor);

                mainPanel.add(netWorthPanel);
            } catch (NumberFormatException e) {
                // Ignore invalid numbers
            }
        } else {
            // Show empty state if no data available
            JPanel emptyPanel = new JPanel();
            emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
            emptyPanel.setBackground(new Color(245, 245, 250));

            JLabel emptyLabel = new JLabel("No data available. Please enter financial data.");
            emptyLabel.setFont(new Font("Inter", Font.PLAIN, 14));
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            emptyPanel.add(Box.createVerticalGlue());
            emptyPanel.add(emptyLabel);
            emptyPanel.add(Box.createVerticalGlue());

            mainPanel.add(emptyPanel);
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        balanceArea.add(scrollPane, BorderLayout.CENTER);

        balanceArea.revalidate();
        balanceArea.repaint();
    }

    // Helper method to safely parse double values
    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // Creates a styled card panel for financial information
    private JPanel createCardPanel(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(10, 10, 10, 10)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));

        return card;
    }

    // Creates a panel displaying a labeled value with specific formatting
    private JPanel createValuePanel(String label, String value, Color valueColor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        labelLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Inter", Font.BOLD, 14));
        valueLabel.setForeground(valueColor);
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        panel.add(labelLabel, BorderLayout.WEST);
        panel.add(valueLabel, BorderLayout.EAST);

        return panel;
    }

    // Creates a trend indicator item showing month, value, and trend arrow
    private JPanel createTrendItem(String month, String value, String trendIcon, Color color) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel monthLabel = new JLabel(month);
        monthLabel.setFont(new Font("Inter", Font.PLAIN, 10));

        JLabel valueLabel = new JLabel("CHF " + value);
        valueLabel.setFont(new Font("Inter", Font.BOLD, 12));
        valueLabel.setForeground(color);

        JLabel trendLabel = new JLabel(trendIcon);
        trendLabel.setFont(new Font("Inter", Font.BOLD, 12));
        trendLabel.setForeground(color);

        panel.add(monthLabel);
        panel.add(valueLabel);
        panel.add(trendLabel);

        return panel;
    }
}