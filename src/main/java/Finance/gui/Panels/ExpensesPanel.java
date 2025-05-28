package Finance.gui.Panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import Finance.api.CurrencyConverter;
import Finance.data.ExpenseStorage;

/**
 * Panel for displaying and managing expense tracking functionality.
 * Shows expenses by month with ability to add/delete entries.
 */
public class ExpensesPanel {
    // Data structure to store expenses by month-year key
    private final Map<String, List<String>> monthlyExpenses;

    // UI components
    private JPanel mainPanel;
    private JScrollPane scrollPane;
    private JComboBox<String> monthCombo;
    private JComboBox<String> yearCombo;

    // Currency settings
    private final String selectedCurrency = "CHF";

    // Storage handler for persisting expenses
    private final ExpenseStorage storage;

    // UI design constants
    private final Color BACKGROUND_COLOR = new Color(245, 245, 250);
    private final Color CARD_COLOR = Color.WHITE;
    private final Color EXPENSE_COLOR = new Color(220, 20, 60);
    private final Font TITLE_FONT = new Font("Inter", Font.BOLD, 14);
    private final Font CARD_TITLE_FONT = new Font("Inter", Font.BOLD, 16);
    private final Font DETAIL_FONT = new Font("Inter", Font.PLAIN, 14);
    private final Dimension HEADER_SIZE = new Dimension(400, 40);
    private final int MIN_CARD_WIDTH = 250;
    private final int MAX_CARD_WIDTH = 350;
    private final int CARD_HEIGHT = 100;

    /**
     * Constructor initializes expense data and UI
     * @param monthlyExpenses Map to store expenses data
     */
    public ExpensesPanel(Map<String, List<String>> monthlyExpenses) {
        this.monthlyExpenses = monthlyExpenses;
        this.storage = new ExpenseStorage();
        // Load any existing expenses from storage
        this.monthlyExpenses.putAll(storage.loadExpenses());

        initializeUI();
    }

    /**
     * Returns the main panel containing all expense UI components
     */
    public JPanel createExpensesPanel() {
        return mainPanel;
    }

    /**
     * Initializes the main UI components
     */
    private void initializeUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        createHeader();
        scrollPane = createExpensesList();
        mainPanel.add(scrollPane);
    }

    /**
     * Creates the header panel with title, date selection and add button
     */
    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        headerPanel.setPreferredSize(HEADER_SIZE);
        headerPanel.setMaximumSize(HEADER_SIZE);

        JLabel titleLabel = new JLabel("My Expenses");
        titleLabel.setFont(TITLE_FONT);

        JPanel datePanel = createDateSelectionPanel();
        datePanel.setBackground(BACKGROUND_COLOR);

        // Add expense button
        JButton addButton = createStyledButton();
        addButton.setFont(new Font("Inter", Font.BOLD, 11));
        addButton.setText("➕");
        addButton.setPreferredSize(new Dimension(35, 28));
        addButton.addActionListener(e -> showAddExpenseDialog());

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightPanel.setBackground(BACKGROUND_COLOR);
        rightPanel.add(datePanel);
        rightPanel.add(addButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        mainPanel.add(headerPanel);
    }

    /**
     * Creates the date selection panel with month/year dropdowns
     */
    private JPanel createDateSelectionPanel() {
        JPanel balancePanel = new JPanel(new BorderLayout());
        balancePanel.setBackground(new Color(245, 245, 250));
        balancePanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 245, 250));

        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        datePanel.setBackground(new Color(245, 245, 250));

        // Month/year dropdown options
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        String[] years = {"2024", "2025"};

        // Set to current month/year
        LocalDate now = LocalDate.now();

        monthCombo = new JComboBox<>(months);
        yearCombo = new JComboBox<>(years);
        monthCombo.setFont(new Font("Inter", Font.PLAIN, 14));
        yearCombo.setFont(new Font("Inter", Font.PLAIN, 14));

        monthCombo.setSelectedIndex(now.getMonthValue() - 1);
        yearCombo.setSelectedItem(String.valueOf(now.getYear()));

        // Refresh list when date changes
        ActionListener dateChangeListener = e -> refreshExpensesList();
        monthCombo.addActionListener(dateChangeListener);
        yearCombo.addActionListener(dateChangeListener);

        datePanel.add(monthCombo);
        datePanel.add(yearCombo);

        headerPanel.add(datePanel, BorderLayout.EAST);

        balancePanel.add(headerPanel, BorderLayout.NORTH);

        return datePanel;
    }

    /**
     * Creates the scrollable list of expense cards
     */
    private JScrollPane createExpensesList() {
        JPanel cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        cardsPanel.setBackground(BACKGROUND_COLOR);

        int CARD_PADDING = 20;
        int MAX_CARD_WIDTH = 300;
        Dimension cardSize = new Dimension(MAX_CARD_WIDTH, CARD_HEIGHT);

        // Get expenses for selected month/year
        String month = (String) monthCombo.getSelectedItem();
        String year = (String) yearCombo.getSelectedItem();
        String key = month + " " + year;
        List<String> currentExpenses = monthlyExpenses.getOrDefault(key, new java.util.ArrayList<>());

        if (currentExpenses.isEmpty()) {
            // Show empty state if no expenses
            JLabel emptyLabel = new JLabel("No expenses for " + month + " " + year);
            emptyLabel.setFont(DETAIL_FONT);
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            cardsPanel.add(Box.createVerticalGlue());
            cardsPanel.add(emptyLabel);
            cardsPanel.add(Box.createVerticalGlue());
        } else {
            double totalAmount = 0;
            double conversionRate = 1.0;

            // Get currency conversion rate if needed
            if (!"CHF".equals(selectedCurrency)) {
                try {
                    conversionRate = CurrencyConverter.getExchangeRate("CHF", selectedCurrency);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,
                            "Currency conversion failed: " + e.getMessage(),
                            "Conversion Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            // Create card for each expense
            for (String expense : currentExpenses) {
                String[] parts = expense.split("\t");
                String dateStr = parts.length > 0 ? parts[0] : "";
                String description = parts.length > 1 ? parts[1] : "";
                String amountStr = parts.length > 2 ? parts[2] : "0.00";

                double amountInCHF = 0.0;
                try {
                    amountInCHF = Double.parseDouble(amountStr.replace(",", "."));
                    totalAmount += amountInCHF;
                } catch (NumberFormatException e) {
                    // Skip invalid amounts
                }

                JPanel card = createExpenseCard(dateStr, description, amountInCHF * conversionRate, cardSize);
                cardsPanel.add(card);
                cardsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }

            // Add total expenses panel
            JPanel totalPanel = createTotalPanel(totalAmount * conversionRate, cardSize);
            cardsPanel.add(totalPanel);
        }

        JScrollPane scroll = new JScrollPane(cardsPanel);
        scroll.setBorder(null);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setPreferredSize(new Dimension(MAX_CARD_WIDTH + 30, 450));

        return scroll;
    }

    /**
     * Creates an individual expense card
     */
    private JPanel createExpenseCard(String date, String description, double amount, Dimension size) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(10, 15, 10, 15)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setPreferredSize(size);
        card.setMaximumSize(size);
        card.setMinimumSize(new Dimension(MIN_CARD_WIDTH, CARD_HEIGHT));

        // Top section with date and delete button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(CARD_COLOR);

        JLabel dateLabel = new JLabel(date);
        dateLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(100, 100, 100));
        topPanel.add(dateLabel, BorderLayout.WEST);

        // Delete button
        JButton deleteButton = new JButton("Delete");
        deleteButton.setFont(new Font("Inter", Font.BOLD, 12));
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);
        deleteButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 53, 69).darker()),
                new EmptyBorder(4, 10, 4, 10)));
        deleteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        deleteButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(mainPanel,
                    "Delete this expense?\n" + date + " - " + description + " - " + String.format("%s %.2f", selectedCurrency, amount),
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                deleteExpense(date, description, amount);
            }
        });

        topPanel.add(deleteButton, BorderLayout.EAST);
        card.add(topPanel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));

        // Description section
        JPanel descWrapper = new JPanel();
        descWrapper.setLayout(new BoxLayout(descWrapper, BoxLayout.X_AXIS));
        descWrapper.setBackground(CARD_COLOR);

        JTextArea descArea = new JTextArea(description);
        descArea.setFont(new Font("Inter", Font.BOLD, 14));
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBackground(CARD_COLOR);
        descArea.setBorder(new EmptyBorder(0, 0, 0, 0));

        descWrapper.add(descArea);
        card.add(descWrapper);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        // Amount section
        JPanel amountPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        amountPanel.setBackground(CARD_COLOR);
        amountPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel amountLabel = new JLabel(String.format("%s %.2f", selectedCurrency, amount));
        amountLabel.setFont(new Font("Inter", Font.BOLD, 16));
        amountLabel.setForeground(EXPENSE_COLOR);
        amountPanel.add(amountLabel);

        card.add(amountPanel);

        return card;
    }

    /**
     * Creates the total expenses summary panel
     */
    private JPanel createTotalPanel(double totalAmount, Dimension size) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(10, 15, 10, 15)));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setPreferredSize(new Dimension(size.width, 60));
        panel.setMaximumSize(new Dimension(size.width, 60));

        JLabel totalLabel = new JLabel("Total Expenses:");
        totalLabel.setFont(CARD_TITLE_FONT);

        JLabel amountLabel = new JLabel(String.format("%s %.2f", selectedCurrency, totalAmount));
        amountLabel.setFont(CARD_TITLE_FONT);
        amountLabel.setForeground(EXPENSE_COLOR);

        panel.add(totalLabel, BorderLayout.WEST);
        panel.add(amountLabel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Creates a styled button with default appearance
     */
    private JButton createStyledButton() {
        JButton button = new JButton();
        button.setBackground(new Color(0, 122, 255));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(8, 8, 8, 8)));
        button.setFocusPainted(false);
        return button;
    }

    /**
     * Shows dialog for adding a new expense
     */
    private void showAddExpenseDialog() {
        JPanel panel = new JPanel(new GridLayout(4, 2));

        // Default to current date
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        JLabel dateLabel = new JLabel("Date:");
        JTextField dateField = new JTextField(currentDate);

        JLabel descLabel = new JLabel("Description:");
        JTextField descField = new JTextField();

        JLabel amountLabel = new JLabel("Amount:");
        JTextField amountField = new JTextField();

        // Currency selection
        String[] currencies = {"CHF", "EUR", "USD"};
        JComboBox<String> currencyBox = new JComboBox<>(currencies);
        currencyBox.setSelectedItem("CHF");

        panel.add(dateLabel);
        panel.add(dateField);
        panel.add(descLabel);
        panel.add(descField);
        panel.add(amountLabel);
        panel.add(amountField);
        panel.add(new JLabel("Currency:"));
        panel.add(currencyBox);

        int result = JOptionPane.showConfirmDialog(null, panel,
                "Add New Expense", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                // Parse and validate input
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                LocalDate expenseDate = LocalDate.parse(dateField.getText(), formatter);

                // Get month/year for storage key
                String month = expenseDate.getMonth().toString();
                month = month.charAt(0) + month.substring(1).toLowerCase();
                String year = String.valueOf(expenseDate.getYear());
                String key = month + " " + year;

                // Handle currency conversion if needed
                double originalAmount = Double.parseDouble(amountField.getText());
                String selectedInputCurrency = (String) currencyBox.getSelectedItem();
                if (selectedInputCurrency == null) selectedInputCurrency = "CHF";

                double amountInCHF = originalAmount;
                if (!"CHF".equals(selectedInputCurrency)) {
                    amountInCHF = CurrencyConverter.getExchangeRate(selectedInputCurrency, "CHF") * originalAmount;
                }

                // Create expense entry string
                String newEntry = String.format("%s\t%s\t%.2f", dateField.getText(), descField.getText(), amountInCHF);

                // Add to data structure and save
                if (!monthlyExpenses.containsKey(key)) {
                    monthlyExpenses.put(key, new java.util.ArrayList<>());
                }
                monthlyExpenses.get(key).add(newEntry);
                storage.saveExpenses(monthlyExpenses);
                refreshExpensesList();

                // Update UI to show new expense
                monthCombo.setSelectedItem(month);
                yearCombo.setSelectedItem(year);
                refreshExpensesList();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                        getErrorMessage(ex), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Generates appropriate error messages for different exception types
     */
    private String getErrorMessage(Exception ex) {
        if (ex instanceof NumberFormatException) {
            return "Invalid amount. Please enter a valid number.";
        } else if (ex instanceof java.time.format.DateTimeParseException) {
            return "Invalid date format. Please use dd.MM.yyyy.";
        } else {
            return "Error: " + ex.getMessage();
        }
    }

    /**
     * Deletes an expense entry
     */
    private void deleteExpense(String date, String description, double amountInCHF) {
        String month = (String) monthCombo.getSelectedItem();
        String year = (String) yearCombo.getSelectedItem();
        String key = month + " " + year;

        List<String> expenses = monthlyExpenses.get(key);
        if (expenses != null) {
            // Remove matching expense
            expenses.removeIf(expense -> {
                String[] parts = expense.split("\t");
                if (parts.length < 3) return false;
                String d = parts[0];
                String desc = parts[1];
                return d.equals(date) && desc.equals(description);
            });

            // Save changes and refresh UI
            storage.saveExpenses(monthlyExpenses);
            refreshExpensesList();
        }
    }

    /**
     * Refreshes the expenses list display
     */
    private void refreshExpensesList() {
        mainPanel.remove(scrollPane);
        scrollPane = createExpensesList();
        mainPanel.add(scrollPane);
        mainPanel.revalidate();
        mainPanel.repaint();
    }
}