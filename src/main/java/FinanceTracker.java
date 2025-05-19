import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.net.URL;
import java.io.FileNotFoundException;

public class FinanceTracker {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private final Map<String, List<String>> monthlyExpenses = new HashMap<>();
    private final Map<String, Double> monthlyBalances = new HashMap<>();
    private final Map<String, Map<String, String>> monthlyNetSavings = new HashMap<>();
    private final Map<String, Map<String, String>> monthlyDebts = new HashMap<>();

    private JLabel viewAllLabel;
    private JComboBox<String> monthCombo;
    private JComboBox<String> yearCombo;
    private JTextArea expensesArea;
    private JPanel balanceArea;
    private JComboBox<String> balanceMonthCombo;
    private JComboBox<String> balanceYearCombo;

    public JPanel createFinanceTracker() {
        JPanel financeApp = new JPanel(new BorderLayout());
        financeApp.setBackground(new Color(245, 245, 250));

        JPanel mainPanel = createMainPanel();
        JPanel expensesPanel = createExpensesPanel();
        JPanel balancePanel = createBalancePanel();

        cardPanel.add(mainPanel, "main");
        cardPanel.add(expensesPanel, "expenses");
        cardPanel.add(balancePanel, "balance");

        JPanel bottomNav = createBottomNavigation();
        financeApp.add(cardPanel, BorderLayout.CENTER);
        financeApp.add(bottomNav, BorderLayout.SOUTH);

        return financeApp;
    }

    private JPanel createExpensesPanel() {
        JPanel expensesPanel = new JPanel(new BorderLayout());
        expensesPanel.setBackground(new Color(245, 245, 250));
        expensesPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 245, 250));

        JLabel titleLabel = new JLabel("My expenses", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 20));

        JPanel datePanel = createDateSelectionPanel();

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(datePanel, BorderLayout.EAST);
        headerPanel.add(titlePanel, BorderLayout.NORTH);

        JButton addButton = createAddExpenseButton();
        headerPanel.add(addButton, BorderLayout.SOUTH);
        expensesPanel.add(headerPanel, BorderLayout.NORTH);

        expensesArea = new JTextArea();
        expensesArea.setFont(new Font("Inter", Font.PLAIN, 14));
        expensesArea.setEditable(false);
        expensesArea.setBackground(Color.WHITE);
        expensesArea.setLineWrap(true);
        expensesArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(expensesArea);
        expensesPanel.add(scrollPane, BorderLayout.CENTER);

        viewAllLabel = createViewAllLabel();
        expensesPanel.add(viewAllLabel, BorderLayout.SOUTH);

        updateExpensesDisplay();

        return expensesPanel;
    }

    private JPanel createDateSelectionPanel() {
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        datePanel.setBackground(new Color(245, 245, 250));

        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        String[] years = {"2024", "2025"};

        monthCombo = new JComboBox<>(months);
        monthCombo.setSelectedItem("March");
        monthCombo.setFont(new Font("Inter", Font.PLAIN, 14));

        yearCombo = new JComboBox<>(years);
        yearCombo.setFont(new Font("Inter", Font.PLAIN, 14));

        monthCombo.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        yearCombo.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        ActionListener dateChangeListener = e -> updateExpensesDisplay();
        monthCombo.addActionListener(dateChangeListener);
        yearCombo.addActionListener(dateChangeListener);

        datePanel.add(monthCombo);
        datePanel.add(yearCombo);

        return datePanel;
    }

    private JButton createAddExpenseButton() {
        JButton addButton = new JButton("Add new Expense");
        addButton.setBackground(new Color(0, 122, 255));
        addButton.setForeground(Color.WHITE);
        addButton.setBorder(new EmptyBorder(5, 10, 5, 10));
        addButton.addActionListener(e -> {
            JPanel panel = new JPanel(new GridLayout(3, 2));

            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

            JLabel dateLabel = new JLabel("Date:");
            JTextField dateField = new JTextField(currentDate);

            JLabel descLabel = new JLabel("Description:");
            JTextField descField = new JTextField();

            JLabel amountLabel = new JLabel("Amount (CHF):");
            JTextField amountField = new JTextField();

            panel.add(dateLabel);
            panel.add(dateField);
            panel.add(descLabel);
            panel.add(descField);
            panel.add(amountLabel);
            panel.add(amountField);

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

                    String newEntry = String.format("%-12s  %-25s CHF%10.2f",
                            dateField.getText(),
                            descField.getText(),
                            Double.parseDouble(amountField.getText()));

                    if (!monthlyExpenses.containsKey(key)) {
                        monthlyExpenses.put(key, new ArrayList<>());
                    }
                    monthlyExpenses.get(key).add(newEntry);

                    monthCombo.setSelectedItem(month);
                    yearCombo.setSelectedItem(year);

                    updateExpensesDisplay();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null,
                            "Invalid input. Please check date format (dd.MM.yyyy) and amount.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        return addButton;
    }

    private JLabel createViewAllLabel() {
        JLabel label = new JLabel("", SwingConstants.CENTER);
        label.setForeground(new Color(0, 122, 255));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String month = (String) monthCombo.getSelectedItem();
                String year = (String) yearCombo.getSelectedItem();
                int count = monthlyExpenses.getOrDefault(month + " " + year, new ArrayList<>()).size();
                JOptionPane.showMessageDialog(null, "Showing all " + count +
                        " expenses for " + month + " " + year);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                label.setForeground(new Color(0, 80, 200));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                label.setForeground(new Color(0, 122, 255));
            }
        });
        return label;
    }

    private JPanel createBalancePanel() {
        JPanel balancePanel = new JPanel(new BorderLayout());
        balancePanel.setBackground(new Color(245, 245, 250));
        balancePanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 245, 250));

        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        datePanel.setBackground(new Color(245, 245, 250));

        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        String[] years = {"2024", "2025"};

        balanceMonthCombo = new JComboBox<>(months);
        balanceYearCombo = new JComboBox<>(years);
        balanceMonthCombo.setFont(new Font("Inter", Font.PLAIN, 14));
        balanceYearCombo.setFont(new Font("Inter", Font.PLAIN, 14));

        LocalDate now = LocalDate.now();
        balanceMonthCombo.setSelectedIndex(now.getMonthValue() - 1);
        balanceYearCombo.setSelectedItem(String.valueOf(now.getYear()));

        JLabel title = new JLabel("Balance overview");

        datePanel.add(balanceMonthCombo);
        datePanel.add(balanceYearCombo);

        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(datePanel, BorderLayout.EAST);

        balancePanel.add(headerPanel, BorderLayout.NORTH);

        balanceArea = new JPanel(new BorderLayout());
        balanceArea.setBackground(new Color(245, 245, 250));

        JScrollPane scrollPane = new JScrollPane(balanceArea);
        scrollPane.setBorder(null);
        balancePanel.add(scrollPane, BorderLayout.CENTER);

        ItemListener dateChangeListener = e -> {
            title.setText("Balance overview");
            updateBalanceArea();
        };
        balanceMonthCombo.addItemListener(dateChangeListener);
        balanceYearCombo.addItemListener(dateChangeListener);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(245, 245, 250));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(245, 245, 250));

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

        updateBalanceArea();
        return balancePanel;
    }

    private void enterFinancialData(String type) {
        String month = (String) balanceMonthCombo.getSelectedItem();
        String year = (String) balanceYearCombo.getSelectedItem();
        String key = month + " " + year;

        JPanel panel = new JPanel(new GridLayout(0, 1));

        if (type.equals("Net Savings")) {
            JLabel currentLabel = new JLabel("Current month (CHF):");
            JTextField currentField = new JTextField();

            JLabel yearLabel = new JLabel("Full year (CHF):");
            JTextField yearField = new JTextField();

            JLabel janLabel = new JLabel("January (CHF):");
            JTextField janField = new JTextField();

            JLabel febLabel = new JLabel("February (CHF):");
            JTextField febField = new JTextField();

            JLabel marLabel = new JLabel("March (CHF):");
            JTextField marField = new JTextField();

            panel.add(currentLabel);
            panel.add(currentField);
            panel.add(yearLabel);
            panel.add(yearField);
            panel.add(janLabel);
            panel.add(janField);
            panel.add(febLabel);
            panel.add(febField);
            panel.add(marLabel);
            panel.add(marField);

            int result = JOptionPane.showConfirmDialog(null, panel,
                    "Enter Net Savings Data", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    Map<String, String> netSavingsData = new HashMap<>();
                    netSavingsData.put("current", currentField.getText());
                    netSavingsData.put("year", yearField.getText());
                    netSavingsData.put("jan", janField.getText());
                    netSavingsData.put("feb", febField.getText());
                    netSavingsData.put("mar", marField.getText());

                    monthlyNetSavings.put(key, netSavingsData);
                    updateBalanceArea();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Invalid input", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (type.equals("Debts")) {
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
                    updateBalanceArea();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Invalid input", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

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
            JPanel savingsCard = createCardPanel("Net Savings");
            Map<String, String> netSavings = monthlyNetSavings.getOrDefault(key, new HashMap<>());

            JPanel currentMonthPanel = createValuePanel("Current Month",
                    "CHF " + netSavings.getOrDefault("current", "0"),
                    new Color(50, 205, 50));

            JPanel fullYearPanel = createValuePanel("Full Year",
                    "CHF " + netSavings.getOrDefault("year", "0"),
                    new Color(50, 205, 50));

            savingsCard.add(currentMonthPanel);
            savingsCard.add(Box.createRigidArea(new Dimension(0, 5)));
            savingsCard.add(fullYearPanel);

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

            String jan = netSavings.getOrDefault("jan", "0");
            String feb = netSavings.getOrDefault("feb", "0");
            String mar = netSavings.getOrDefault("mar", "0");

            double janValue = parseDoubleSafe(jan);
            double febValue = parseDoubleSafe(feb);
            double marValue = parseDoubleSafe(mar);

            trendPanel.add(createTrendItem("Jan", jan, "-", new Color(100, 100, 100)));

            String febTrend = febValue > janValue ? "▲" : (febValue < janValue ? "▼" : "-");
            Color febColor = febValue > janValue ? new Color(50, 205, 50) :
                    (febValue < janValue ? new Color(220, 20, 60) : new Color(100, 100, 100));
            trendPanel.add(createTrendItem("Feb", feb, febTrend, febColor));

            String marTrend = marValue > febValue ? "▲" : (marValue < febValue ? "▼" : "-");
            Color marColor = marValue > febValue ? new Color(50, 205, 50) :
                    (marValue < febValue ? new Color(220, 20, 60) : new Color(100, 100, 100));
            trendPanel.add(createTrendItem("Mar", mar, marTrend, marColor));

            savingsCard.add(Box.createRigidArea(new Dimension(0, 5)));
            savingsCard.add(trendPanel);

            mainPanel.add(savingsCard);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

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
            JLabel emptyLabel = new JLabel("No data available. Please enter financial data.");
            emptyLabel.setFont(new Font("Inter", Font.PLAIN, 12));
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            mainPanel.add(emptyLabel);
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        balanceArea.add(scrollPane, BorderLayout.CENTER);

        balanceArea.revalidate();
        balanceArea.repaint();
    }

    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

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


    private void updateExpensesDisplay() {
        if (viewAllLabel == null || expensesArea == null) {
            return;
        }

        expensesArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

        String month = (String) monthCombo.getSelectedItem();
        String year = (String) yearCombo.getSelectedItem();
        String key = month + " " + year;

        List<String> currentExpenses = monthlyExpenses.getOrDefault(key, new ArrayList<>());

        StringBuilder sb = new StringBuilder();
        for (String expense : currentExpenses) {
            String[] parts = expense.split("CHF");
            String dateAndDesc = parts[0].trim();
            String amount = parts.length > 1 ? "CHF" + parts[1].trim() : "CHF0.00";

            int lastSpace = dateAndDesc.lastIndexOf("  ");
            String date = lastSpace >= 0 ? dateAndDesc.substring(0, lastSpace).trim() : "";
            String description = lastSpace >= 0 ? dateAndDesc.substring(lastSpace).trim() : dateAndDesc;

            sb.append(String.format("%-12s %-25s %10s%n", date, description, amount));
        }

        expensesArea.setText(sb.toString().trim());
        viewAllLabel.setText("View " + currentExpenses.size() + " expenses");
    }

    private JPanel createBottomNavigation() {
        JPanel navPanel = new JPanel(new GridLayout(1, 3));
        navPanel.setBackground(new Color(230, 230, 250));
        navPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        navPanel.add(createNavButton("Home", "🏠", () -> cardLayout.show(cardPanel, "main")));
        navPanel.add(createNavButton("Stock", "📈", () -> {}));
        navPanel.add(createNavButton("Account", "👤", () -> {}));

        return navPanel;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 250));

        JLabel helloLabel = new JLabel("Hello User", SwingConstants.LEFT);
        helloLabel.setFont(new Font("Inter", Font.PLAIN, 18));
        helloLabel.setForeground(new Color(50, 50, 50));
        helloLabel.setBorder(new EmptyBorder(10, 10, 0, 10));
        mainPanel.add(helloLabel, BorderLayout.NORTH);

        try {
            URL imageUrl = getClass().getResource("/finance/logo.png");
            if (imageUrl != null) {
                ImageIcon logoIcon = new ImageIcon(imageUrl);
                Image image = logoIcon.getImage().getScaledInstance(350, 200, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(image));
                logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
                logoLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
                mainPanel.add(logoLabel, BorderLayout.CENTER);
            } else {
                throw new FileNotFoundException("Resource not found: /finance/logo.png");
            }
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Logo not found", SwingConstants.CENTER);
            mainPanel.add(errorLabel, BorderLayout.CENTER);
            e.printStackTrace();
        }

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(245, 245, 250));
        menuPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        menuPanel.add(createMenuItem("My expenses", " $", () -> cardLayout.show(cardPanel, "expenses")));
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(createMenuItem("Balance overview", "📈", () -> cardLayout.show(cardPanel, "balance")));
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(createMenuItem("Saving goals", "🎯", () -> {}));
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(createMenuItem("Investment", "📄", () -> {}));

        mainPanel.add(menuPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private static JPanel createMenuItem(String text, String iconText, Runnable action) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBackground(Color.WHITE);
        itemPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        itemPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(iconText);
        iconLabel.setFont(new Font("Inter", Font.PLAIN, 24));
        iconLabel.setForeground(new Color(0, 122, 255));

        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Inter", Font.PLAIN, 16));
        textLabel.setForeground(new Color(30, 30, 30));

        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
        contentPanel.add(iconLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        contentPanel.add(textLabel);

        itemPanel.add(contentPanel, BorderLayout.WEST);
        itemPanel.add(new JLabel("⋮"), BorderLayout.EAST);

        itemPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                itemPanel.setBackground(new Color(230, 240, 255));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                itemPanel.setBackground(Color.WHITE);
            }
        });

        return itemPanel;
    }

    private static JPanel createNavButton(String text, String icon, Runnable action) {
        JPanel navButton = new JPanel();
        navButton.setLayout(new BoxLayout(navButton, BoxLayout.Y_AXIS));
        navButton.setBackground(new Color(230, 230, 250));
        navButton.setBorder(new EmptyBorder(5, 5, 5, 5));
        navButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Inter", Font.PLAIN, 20));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel textLabel = new JLabel(text, SwingConstants.CENTER);
        textLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        navButton.add(iconLabel);
        navButton.add(Box.createRigidArea(new Dimension(0, 5)));
        navButton.add(textLabel);

        navButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                navButton.setBackground(new Color(210, 220, 240));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                navButton.setBackground(new Color(230, 230, 250));
            }
        });

        return navButton;
    }
}