package Finance;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import Finance.gui.Panels.MainPanel;
import Finance.gui.Panels.ExpensesPanel;
import Finance.gui.Panels.BalancePanel;
import Finance.gui.Panels.SavingGoalsPanel;
import Finance.gui.Panels.InvestmentPanel;
import Finance.gui.components.UIComponents;

public class FinanceTracker {

    private final Map<String, List<String>> monthlyExpenses = new HashMap<>();  // Store expenses per month
    private final Map<String, Map<String, String>> monthlyNetSavings = new HashMap<>();  // Store net savings info per month
    private final Map<String, Map<String, String>> monthlyDebts = new HashMap<>();  // Store debts info per month
    private final CardLayout cardLayout = new CardLayout();   // CardLayout allows switching between different "screens" in the app
    private final JPanel cardPanel = new JPanel(cardLayout); // This panel holds all other panels and switches between them with cardLayout
    private MainPanel mainPanel; // These are the main panels in the app, saved here so we can update them later
    private final JPanel financeApp;// The main panel for the whole finance app, stored so we only build it once

    public FinanceTracker() {
        financeApp = initializeUI();
    } // Constructor builds the UI only once and stores it

    // Set up all the panels and the layout of the app here
    private JPanel initializeUI() {
        JPanel financeApp = new JPanel(new BorderLayout());
        financeApp.setBackground(new Color(245, 245, 250));

        // Create the main panel (dashboard/home screen)
        mainPanel = new MainPanel(cardLayout, cardPanel);
        // Create other panels with their data
        ExpensesPanel expensesPanel = new ExpensesPanel(monthlyExpenses);
        BalancePanel balancePanel = new BalancePanel(monthlyNetSavings, monthlyDebts);
        SavingGoalsPanel savingGoalsPanel = new SavingGoalsPanel();
        InvestmentPanel investmentPanel = new InvestmentPanel(cardLayout, cardPanel);

        // Add all the panels to the cardPanel with a string key to switch between them
        cardPanel.add(mainPanel.createMainPanel(), "main");
        cardPanel.add(expensesPanel.createExpensesPanel(), "expenses");
        cardPanel.add(balancePanel.createBalancePanel(), "balance");
        cardPanel.add(savingGoalsPanel.createSavingGoalsPanel(), "goals");
        cardPanel.add(investmentPanel.createInvestmentPanel(), "investment");

        // Create bottom navigation bar with Home button
        JPanel bottomNav = UIComponents.createBottomNavigation(cardLayout, cardPanel);

        // Add the main card panel in the center and navigation at bottom
        financeApp.add(cardPanel, BorderLayout.CENTER);
        financeApp.add(bottomNav, BorderLayout.SOUTH);

        cardLayout.show(cardPanel, "main");// Show the main screen by default

        return financeApp;  // return the built UI panel
    }

    // This method returns the full UI panel for embedding in a JFrame or something else
    public JPanel createFinanceTracker() {
        return financeApp;
    }
}