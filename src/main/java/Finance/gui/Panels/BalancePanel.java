package Finance.gui.Panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class BalancePanel {
    private final Map<String, Map<String, String>> monthlyNetSavings;
    private final Map<String, Map<String, String>> monthlyDebts;
    private JPanel balanceArea;
    private JComboBox<String> balanceMonthCombo;
    private JComboBox<String> balanceYearCombo;
    private static final Font TITLE_FONT = new Font("Inter", Font.BOLD, 14);

    public BalancePanel(Map<String, Map<String, String>> monthlyNetSavings,
                        Map<String, Map<String, String>> monthlyDebts) {
        this.monthlyNetSavings = monthlyNetSavings;
        this.monthlyDebts = monthlyDebts;
    }

    public JPanel createBalancePanel() {
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

        JLabel titleLabel = new JLabel("Balance overview");
        titleLabel.setFont(TITLE_FONT);

        datePanel.add(balanceMonthCombo);
        datePanel.add(balanceYearCombo);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(datePanel, BorderLayout.EAST);

        balancePanel.add(headerPanel, BorderLayout.NORTH);

        balanceArea = new JPanel(new BorderLayout());
        balanceArea.setBackground(new Color(245, 245, 250));

        JScrollPane scrollPane = new JScrollPane(balanceArea);
        scrollPane.setBorder(null);
        balancePanel.add(scrollPane, BorderLayout.CENTER);

        ItemListener dateChangeListener = e -> {
            titleLabel.setText("Balance overview");
            updateBalanceArea();
        };
        balanceMonthCombo.addItemListener(dateChangeListener);
        balanceYearCombo.addItemListener(dateChangeListener);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(245, 245, 250));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
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
        JPanel emptyPanel = new JPanel();
        emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
        emptyPanel.setBackground(new Color(245, 245, 250)); // Gleicher Hintergrund wie InvestmentPanel

        JLabel emptyLabel = new JLabel("No data available. Please enter financial data.");
        emptyLabel.setFont(new Font("Inter", Font.PLAIN, 14)); // DETAIL_FONT
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
}