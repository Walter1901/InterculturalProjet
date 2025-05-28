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

public class ExpensesPanel {
    private final Map<String, List<String>> monthlyExpenses;
    private JPanel mainPanel;
    private JScrollPane scrollPane;
    private JComboBox<String> monthCombo;
    private JComboBox<String> yearCombo;
    private final String selectedCurrency = "CHF";

    private final Color BACKGROUND_COLOR = new Color(245, 245, 250);
    private final Color CARD_COLOR = Color.WHITE;
    private final Color EXPENSE_COLOR = new Color(220, 20, 60);
    private final Font TITLE_FONT = new Font("Inter", Font.BOLD, 14);
    private final Font CARD_TITLE_FONT = new Font("Inter", Font.BOLD, 16);
    private final Font DETAIL_FONT = new Font("Inter", Font.PLAIN, 14);
    private final Dimension HEADER_SIZE = new Dimension(400, 40);
    private final int MIN_CARD_WIDTH = 250;
    private final int CARD_HEIGHT = 100;

    public ExpensesPanel(Map<String, List<String>> monthlyExpenses) {
        this.monthlyExpenses = monthlyExpenses;
        initializeUI();
    }

    public JPanel createExpensesPanel() {
        return mainPanel;
    }

    private void initializeUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        createHeader();
        scrollPane = createExpensesList();
        mainPanel.add(scrollPane);
    }

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

    private JPanel createDateSelectionPanel() {
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        datePanel.setBackground(BACKGROUND_COLOR);

        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        String[] years = {"2024", "2025"};

        // Aktuellen Monat und Jahr holen
        LocalDate now = LocalDate.now();
        int currentMonthIndex = now.getMonthValue() - 1; // 0-basiert
        String currentMonth = months[currentMonthIndex];
        String currentYear = String.valueOf(now.getYear());

        monthCombo = new JComboBox<>(months);
        monthCombo.setSelectedItem(currentMonth);
        monthCombo.setFont(DETAIL_FONT);

        yearCombo = new JComboBox<>(years);
        yearCombo.setSelectedItem(currentYear);
        yearCombo.setFont(DETAIL_FONT);

        monthCombo.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        yearCombo.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        ActionListener dateChangeListener = e -> refreshExpensesList();
        monthCombo.addActionListener(dateChangeListener);
        yearCombo.addActionListener(dateChangeListener);

        datePanel.add(monthCombo);
        datePanel.add(yearCombo);

        return datePanel;
    }

    private JScrollPane createExpensesList() {
        JPanel cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        cardsPanel.setBackground(BACKGROUND_COLOR);

        int CARD_PADDING = 20;
        int cardWidth = Math.max(MIN_CARD_WIDTH, mainPanel.getWidth() - 2 * CARD_PADDING);
        Dimension cardSize = new Dimension(cardWidth, CARD_HEIGHT);

        String month = (String) monthCombo.getSelectedItem();
        String year = (String) yearCombo.getSelectedItem();
        String key = month + " " + year;
        List<String> currentExpenses = monthlyExpenses.getOrDefault(key, new java.util.ArrayList<>());

        if (currentExpenses.isEmpty()) {
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

            if (!"CHF".equals(selectedCurrency)) {
                try {
                    conversionRate = CurrencyConverter.getExchangeRate("CHF", selectedCurrency);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,
                            "Currency conversion failed: " + e.getMessage(),
                            "Conversion Error", JOptionPane.ERROR_MESSAGE);
                }
            }

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
                    // Ignore invalid amounts
                }

                JPanel card = createExpenseCard(dateStr, description, amountInCHF * conversionRate, cardSize);
                cardsPanel.add(card);
                cardsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }

            // Add total panel
            JPanel totalPanel = createTotalPanel(totalAmount * conversionRate, cardSize);
            cardsPanel.add(totalPanel);
        }

        JScrollPane scroll = new JScrollPane(cardsPanel);
        scroll.setBorder(null);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setPreferredSize(new Dimension(cardWidth + 30, 450));

        return scroll;
    }

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

        //Top Panel: Date (left) + Delete (right)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(CARD_COLOR);

        JLabel dateLabel = new JLabel(date);
        dateLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(100, 100, 100));
        topPanel.add(dateLabel, BorderLayout.WEST);

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

        // Beschreibung (ganz links ohne Einrückung)
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

        //Amount (rechts)
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

    private void showAddExpenseDialog() {
        JPanel panel = new JPanel(new GridLayout(4, 2));

        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        JLabel dateLabel = new JLabel("Date:");
        JTextField dateField = new JTextField(currentDate);

        JLabel descLabel = new JLabel("Description:");
        JTextField descField = new JTextField();

        JLabel amountLabel = new JLabel("Amount:");
        JTextField amountField = new JTextField();

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
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                LocalDate expenseDate = LocalDate.parse(dateField.getText(), formatter);

                String month = expenseDate.getMonth().toString();
                month = month.charAt(0) + month.substring(1).toLowerCase();
                String year = String.valueOf(expenseDate.getYear());
                String key = month + " " + year;

                double originalAmount = Double.parseDouble(amountField.getText());
                String selectedInputCurrency = (String) currencyBox.getSelectedItem();
                if (selectedInputCurrency == null) selectedInputCurrency = "CHF";

                double amountInCHF = originalAmount;
                if (!"CHF".equals(selectedInputCurrency)) {
                    amountInCHF = CurrencyConverter.getExchangeRate(selectedInputCurrency, "CHF") * originalAmount;
                }

                String newEntry = String.format("%s\t%s\t%.2f", dateField.getText(), descField.getText(), amountInCHF);

                if (!monthlyExpenses.containsKey(key)) {
                    monthlyExpenses.put(key, new java.util.ArrayList<>());
                }
                monthlyExpenses.get(key).add(newEntry);

                monthCombo.setSelectedItem(month);
                yearCombo.setSelectedItem(year);
                refreshExpensesList();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                        getErrorMessage(ex), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String getErrorMessage(Exception ex) {
        if (ex instanceof NumberFormatException) {
            return "Invalid amount. Please enter a valid number.";
        } else if (ex instanceof java.time.format.DateTimeParseException) {
            return "Invalid date format. Please use dd.MM.yyyy.";
        } else {
            return "Error: " + ex.getMessage();
        }
    }

    private void deleteExpense(String date, String description, double amountInCHF) {
        String month = (String) monthCombo.getSelectedItem();
        String year = (String) yearCombo.getSelectedItem();
        String key = month + " " + year;

        List<String> expenses = monthlyExpenses.get(key);
        if (expenses != null) {
            System.out.println("Before delete: " + expenses.size() + " expenses");
            expenses.removeIf(expense -> {
                String[] parts = expense.split("\t");
                if (parts.length < 3) return false;
                String d = parts[0];
                String desc = parts[1];
                boolean match = d.equals(date) && desc.equals(description);
                if (match) System.out.println("Deleting: " + expense);
                return match;
            });
            System.out.println("After delete: " + expenses.size() + " expenses");
            refreshExpensesList();
        }
    }

    private void refreshExpensesList() {
        mainPanel.remove(scrollPane);
        scrollPane = createExpensesList();
        mainPanel.add(scrollPane);
        mainPanel.revalidate();
        mainPanel.repaint();
    }
}