import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.Config;
import com.crazzyghost.alphavantage.parameters.OutputSize;
import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.general.DefaultPieDataset;

/**
 * Main class for the Investify application.
 * This class manages the stock investment interface with its different screens:
 * home, search, portfolio, recurrent investments, and account settings.
 * It uses AlphaVantage API to fetch real-time stock data.
 */
public class InvestifyApp {

    /**
     * Stores the current total value of the user's portfolio as a formatted string.
     */
    public static String portfolioValue = "0.00 $";

    /**
     * Stores the currently selected currency (USD or CHF).
     */
    public static String currentCurrency = "USD";

    /**
     * Conversion rate from USD to Swiss Franc.
     */
    public static final double USD_TO_CHF = 0.89;


    /**
     * Creates the main Investify interface with all its screens.
     * Handles API key configuration and initializes the different views.
     * @return The main JPanel containing all the Investify screens
     */
    public JPanel createInvestify() {

        // Ask the user to enter their API key through a dialog box
        String apiKey = showApiKeyDialog();
        // Check if the API key is missing or empty
        if (apiKey == null || apiKey.isEmpty()) {
            // Create an error panel with a dark background
            JPanel errorPanel = new JPanel(new BorderLayout());
            errorPanel.setBackground(phoneUtils.backgroundColor);

            // Create an error message label with centered text
            JLabel errorLabel = new JLabel("API key required to use Investify", SwingConstants.CENTER);
            errorLabel.setForeground(phoneUtils.textColor);
            errorLabel.setFont(new Font("Inter", Font.BOLD, 18));

            // Add the error message to the center of the panel
            errorPanel.add(errorLabel, BorderLayout.CENTER);
            // Return the error panel instead of continuing with application initialization
            return errorPanel;
        }

        // =====API config=====
        // Configure the AlphaVantage API with the provided key
        Config cfg = Config.builder()
                .key(apiKey)
                .timeOut(30)
                .build();

        AlphaVantage.api().init(cfg);
        // =====================

        // =====Main Card Panel=====
        // Create the main panel with card layout to switch between different screens
        JPanel mainPanel = new JPanel(new CardLayout()); //Creates the card panel
        // =========================

        // =====Home panel=====
        // Create the main home panel with GridBagLayout for flexible component placement
        JPanel homeMain = new JPanel(new GridBagLayout());
        homeMain.setName("home");
        homeMain.setBackground(phoneUtils.backgroundColor);

        // Set up layout constraints for the home screen components
        GridBagConstraints gbcHome = new GridBagConstraints();
        gbcHome.gridx = 0;
        gbcHome.gridwidth = 1;
        gbcHome.fill = GridBagConstraints.NONE;
        gbcHome.anchor = GridBagConstraints.NORTH;
        gbcHome.insets = new Insets(5, 0, 5, 0);

        // Create and add the main portfolio title
        JLabel titlePortfolio = new JLabel("Portfolio");
        titlePortfolio.setForeground(phoneUtils.textColor);
        titlePortfolio.setFont(new Font("Inter", Font.BOLD, 32));
        gbcHome.gridy = 0;
        homeMain.add(titlePortfolio, gbcHome);

        // Calculate the portfolio value by calling createPieChartPanel()
        // This updates the portfolioValue variable
        createPieChartPanel();

        // Create and add the subtitle showing total portfolio value
        JLabel subtitle1 = new JLabel("Total value: " + portfolioValue);
        subtitle1.setForeground(phoneUtils.textColor);
        subtitle1.setFont(new Font("Inter", Font.BOLD, 24));
        gbcHome.gridy = 1;
        gbcHome.insets = new Insets(5, 0, 15, 0);
        homeMain.add(subtitle1, gbcHome);

        gbcHome.insets = new Insets(5, 0, 5, 0);

        // Create and add the pie chart showing portfolio distribution
        JPanel pieChartPanel = createPieChartPanel();
        gbcHome.gridy = 2;
        gbcHome.fill = GridBagConstraints.BOTH;
        gbcHome.weighty = 1;
        homeMain.add(pieChartPanel, gbcHome);

        // Add the navigation bar at the bottom of the home screen
        gbcHome.gridy = 3;
        gbcHome.fill = GridBagConstraints.HORIZONTAL;
        gbcHome.weighty = 0;
        gbcHome.anchor = GridBagConstraints.SOUTH;
        homeMain.add(createNavBar((CardLayout) mainPanel.getLayout(), mainPanel), gbcHome);
        //=======================


        // =====Search panel=====
        // Create the main search panel with GridBagLayout
        JPanel searchMain = new JPanel(new GridBagLayout());
        searchMain.setBackground(phoneUtils.backgroundColor);

        // Set up layout constraints for the search screen components
        GridBagConstraints gbcSearch = new GridBagConstraints();
        gbcSearch.gridx = 0;
        gbcSearch.gridwidth = 1;
        gbcSearch.insets = new Insets(5, 0, 5, 0);
        gbcSearch.fill = GridBagConstraints.NONE;
        gbcSearch.anchor = GridBagConstraints.NORTH;

        // Create and add the search screen title
        JLabel searchLabel = new JLabel("Page Search", SwingConstants.CENTER);
        searchLabel.setForeground(phoneUtils.textColor);
        searchLabel.setFont(new Font("Inter", Font.BOLD, 24));
        gbcSearch.gridy = 0;
        searchMain.add(searchLabel, gbcSearch);

        // Create and add the search subtitle
        JLabel subtitleSearch = new JLabel("Search for symbols", SwingConstants.CENTER);
        subtitleSearch.setForeground(phoneUtils.textColor);
        subtitleSearch.setFont(new Font("Inter", Font.PLAIN, 18));
        gbcSearch.gridy = 1; // Position entre le titre et la barre de recherche
        searchMain.add(subtitleSearch, gbcSearch);

        // Create and configure the search text field
        JTextField searchBar = new JTextField();
        searchBar.setPreferredSize(new Dimension(250, 30));
        searchBar.setFont(new Font("Inter", Font.PLAIN, 16));
        searchBar.setBackground(Color.WHITE);
        searchBar.setForeground(Color.BLACK);
        searchBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        gbcSearch.gridy = 2;
        gbcSearch.insets = new Insets(10, 0, 10, 0);
        searchMain.add(searchBar, gbcSearch);

        // Create a label to display search results
        JLabel searchResults = new JLabel("", SwingConstants.CENTER);
        searchResults.setForeground(phoneUtils.textColor);
        searchResults.setFont(new Font("Inter", Font.PLAIN, 18));
        gbcSearch.gridy = 3;
        gbcSearch.weighty = 1;

        searchMain.add(searchResults, gbcSearch);

        // Create a panel for buy/sell buttons (initially hidden)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(phoneUtils.backgroundColor);
        buttonPanel.setVisible(false);

        // Set up the search action to fetch stock data when the user presses Enter
        searchBar.addActionListener(e -> {
            String symbol = searchBar.getText().trim();
            buttonPanel.setVisible(false); // Hide buttons by default

            if (!symbol.isEmpty()) {
                try {
                    // Request stock data from AlphaVantage API
                    TimeSeriesResponse response = AlphaVantage.api()
                            .timeSeries()
                            .daily()
                            .forSymbol(symbol)
                            .outputSize(OutputSize.COMPACT)
                            .fetchSync();

                    if (response.getErrorMessage() == null) {
                        // If API response is valid, display the latest stock price
                        response.getStockUnits().stream()
                                .findFirst()
                                .ifPresentOrElse(
                                        unit -> {
                                            searchResults.setText("Last price: " + unit.getClose() + " USD");
                                            buttonPanel.setVisible(true);
                                        },
                                        () -> searchResults.setText("No data available for this symbol.")
                                );
                    } else {
                        // Display error message if symbol not found
                        searchResults.setText("ERROR : Symbol not found");
                    }
                } catch (Exception ex) {
                    // Handle API errors
                    searchResults.setText("Error retrieving data.");
                    ex.printStackTrace();
                }
            } else {
                // Prompt user to enter a valid symbol
                searchResults.setText("Please enter a valid symbol.");
            }
        });

        // Create Buy button with green background
        JButton buyButton = new JButton("Buy");
        buyButton.setFont(new Font("Inter", Font.BOLD, 16));
        buyButton.setBackground(new Color(50,205,50));
        buyButton.setForeground(phoneUtils.textColor);
        buyButton.setFocusPainted(false);

        // Create Sell button with red background
        JButton sellButton = new JButton("Sell");
        sellButton.setFont(new Font("Inter", Font.BOLD, 16));
        sellButton.setBackground(new Color(255, 62, 65));
        sellButton.setForeground(phoneUtils.textColor);
        sellButton.setFocusPainted(false);

        // Add buttons to the button panel
        buttonPanel.add(buyButton);
        buttonPanel.add(sellButton);

        gbcSearch.gridy = 4;
        gbcSearch.weighty = 0;
        searchMain.add(buttonPanel, gbcSearch);

        // Set up button actions to open transaction dialog
        buyButton.addActionListener(e -> openTransactionDialog("Buy", searchMain));
        sellButton.addActionListener(e -> openTransactionDialog("Sell", searchMain));

        // Add navigation bar to the search screen
        gbcSearch.gridy = 5;
        gbcSearch.fill = GridBagConstraints.HORIZONTAL;
        gbcSearch.weighty = 0;
        gbcSearch.anchor = GridBagConstraints.SOUTH;
        searchMain.add(createNavBar((CardLayout) mainPanel.getLayout(), mainPanel), gbcSearch);
        //=======================


        // =====Portfolio Panel=====
        // Create the main portfolio panel with BorderLayout
        JPanel portfolioMain = new JPanel(new BorderLayout());
        portfolioMain.setName("portfolio");
        portfolioMain.setBackground(phoneUtils.backgroundColor);

        // Create and add the portfolio screen title
        JLabel portfolioLabel = new JLabel("Your Portfolio", SwingConstants.CENTER);
        portfolioLabel.setForeground(phoneUtils.textColor);
        portfolioLabel.setFont(new Font("Inter", Font.BOLD, 24));

        // Create a header panel for the portfolio title
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(phoneUtils.backgroundColor);
        headerPanel.add(portfolioLabel);

        // Add components to the portfolio screen
        portfolioMain.add(headerPanel, BorderLayout.NORTH);
        portfolioMain.add(createPortfolioContent(), BorderLayout.CENTER);
        portfolioMain.add(createNavBar((CardLayout) mainPanel.getLayout(), mainPanel), BorderLayout.SOUTH);
        // =========================

        // =====Recurrent Panel (LINKED WITH FINANCE TRACKER)=====
        // Create the main recurrent investments panel with GridBagLayout
        JPanel recurrentMain = new JPanel(new GridBagLayout());
        recurrentMain.setBackground(phoneUtils.backgroundColor);

        // Set up layout constraints for the recurrent investments screen
        GridBagConstraints gbcRecurrent = new GridBagConstraints();
        gbcRecurrent.gridx = 0;
        gbcRecurrent.gridy = 0;
        gbcRecurrent.fill = GridBagConstraints.NONE;
        gbcRecurrent.anchor = GridBagConstraints.NORTH;

        // Create and add the recurrent investments screen title
        JLabel recurrentLabel = new JLabel("Recurrent Investments", SwingConstants.CENTER);
        recurrentLabel.setForeground(phoneUtils.textColor);
        recurrentLabel.setFont(new Font("Inter", Font.BOLD, 24));
        recurrentMain.add(recurrentLabel, gbcRecurrent);

        // Add placeholder information text
        gbcRecurrent.weighty = 1;
        gbcRecurrent.gridy = 1;
        JLabel recurrentInfo = new JLabel("Recurrent investments info...");
        recurrentInfo.setForeground(phoneUtils.textColor);
        recurrentInfo.setFont(new Font("Inter", Font.PLAIN, 18));
        recurrentMain.add(recurrentInfo, gbcRecurrent);

        // Add navigation bar to the recurrent investments screen
        gbcRecurrent.gridy = 2;
        gbcRecurrent.fill = GridBagConstraints.HORIZONTAL;
        gbcRecurrent.weighty = 0;
        gbcRecurrent.anchor = GridBagConstraints.SOUTH;
        recurrentMain.add(createNavBar((CardLayout) mainPanel.getLayout(), mainPanel), gbcRecurrent);
        //=======================

        // =====Account Panel=====
        // Create the main account settings panel with GridBagLayout
        JPanel accountMain = new JPanel(new GridBagLayout());
        accountMain.setBackground(phoneUtils.backgroundColor);

        // Set up layout constraints for the account settings screen
        GridBagConstraints gbcAccount = new GridBagConstraints();
        gbcAccount.gridx = 0;
        gbcAccount.gridy = 0;
        gbcAccount.fill = GridBagConstraints.NONE;
        gbcAccount.anchor = GridBagConstraints.NORTH;

        // Create and add the account settings screen title
        JLabel accountLabel = new JLabel("Account Settings", SwingConstants.CENTER);
        accountLabel.setForeground(phoneUtils.textColor);
        accountLabel.setFont(new Font("Inter", Font.BOLD, 24));
        accountMain.add(accountLabel, gbcAccount);

        // Create a panel for currency selection options
        JPanel currencyPanel = new JPanel();
        currencyPanel.setBackground(phoneUtils.backgroundColor);
        currencyPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(phoneUtils.textColor),
                "Currency",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Inter", Font.BOLD, 16),
                phoneUtils.textColor
        ));

        // Create radio buttons for currency selection
        ButtonGroup currencyGroup = new ButtonGroup();
        JRadioButton usdButton = new JRadioButton("USD ($)");
        usdButton.setFont(new Font("Inter", Font.PLAIN, 16));
        usdButton.setForeground(phoneUtils.textColor);
        usdButton.setBackground(phoneUtils.backgroundColor);
        usdButton.setSelected(currentCurrency.equals("USD"));

        JRadioButton chfButton = new JRadioButton("CHF (Fr.)");
        chfButton.setFont(new Font("Inter", Font.PLAIN, 16));
        chfButton.setForeground(phoneUtils.textColor);
        chfButton.setBackground(phoneUtils.backgroundColor);
        chfButton.setSelected(currentCurrency.equals("CHF"));

        // Group the radio buttons and add them to the currency panel
        currencyGroup.add(usdButton);
        currencyGroup.add(chfButton);
        currencyPanel.add(usdButton);
        currencyPanel.add(chfButton);

        // Add listener to USD button to change currency when selected
        usdButton.addActionListener(e -> {
            if (usdButton.isSelected() && !currentCurrency.equals("USD")) {
                currentCurrency = "USD";
                JOptionPane.showMessageDialog(accountMain,
                        "Currency changed to USD",
                        "Currency Updated",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Add listener to CHF button to change currency when selected
        chfButton.addActionListener(e -> {
            if (chfButton.isSelected() && !currentCurrency.equals("CHF")) {
                currentCurrency = "CHF";
                JOptionPane.showMessageDialog(accountMain,
                        "Currency changed to CHF",
                        "Currency Updated",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Add the currency panel to the account settings screen
        gbcAccount.gridy = 1;
        gbcAccount.weighty = 0;
        gbcAccount.insets = new Insets(20, 10, 20, 10);
        accountMain.add(currencyPanel, gbcAccount);

        // Add instructions about currency changes
        JLabel currencyInfo = new JLabel("<html>The currency change will be applied<br>when navigating between screens.</html>", SwingConstants.CENTER);
        currencyInfo.setForeground(phoneUtils.textColor);
        currencyInfo.setFont(new Font("Inter", Font.ITALIC, 14));
        gbcAccount.gridy = 2;
        gbcAccount.weighty = 1;
        accountMain.add(currencyInfo, gbcAccount);

        // Add navigation bar to the account settings screen
        gbcAccount.gridy = 3;
        gbcAccount.fill = GridBagConstraints.HORIZONTAL;
        gbcAccount.weighty = 0;
        gbcAccount.anchor = GridBagConstraints.SOUTH;
        gbcAccount.insets = new Insets(5, 0, 5, 0);
        accountMain.add(createNavBar((CardLayout) mainPanel.getLayout(), mainPanel), gbcAccount);
        //=======================



        // Add all screens to the main panel with the card layout
        mainPanel.add(homeMain, "home");
        mainPanel.add(searchMain, "search");
        mainPanel.add(portfolioMain, "portfolio");
        mainPanel.add(recurrentMain, "recurrent");
        mainPanel.add(accountMain, "account");

        // Return the complete main panel with all screens
        return mainPanel;
    }

    /**
     * Creates a navigation button with the specified icon.
     * @param resourcePath Path to the icon image resource
     * @return A configured JButton with the specified icon
     */
    private static JButton createNavButton(String resourcePath) {
        // Load the icon from the specified resource path
        java.net.URL iconURL = InvestifyApp.class.getResource(resourcePath);
        if (iconURL == null) {
            // Log error and return empty button if icon not found
            System.err.println("Icon not found: " + resourcePath);
            return new JButton(); // fallback button
        }

        // Create the original icon and resize it to 48x48 pixels
        ImageIcon originalIcon = new ImageIcon(iconURL);
        Image scaledImage = originalIcon.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        // Create and configure the button with the scaled icon
        JButton button = new JButton(scaledIcon);
        button.setBackground(phoneUtils.backgroundColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setMargin(new Insets(5, 7, 5, 7));
        return button;
    }

    /**
     * Creates the navigation bar with icons for all app screens.
     * Sets up action listeners to handle navigation between screens.
     * @param layout The CardLayout used for screen navigation
     * @param parentPanel The parent panel containing all screens
     * @return A JPanel containing the navigation bar
     */
    private static JPanel createNavBar(CardLayout layout, JPanel parentPanel) {
        // Create a navigation bar with 5 equal columns
        JPanel navBar = new JPanel(new GridLayout(1, 5));
        navBar.setBackground(phoneUtils.backgroundColor);

        // Create icon buttons for each section of the app
        JButton homeIcon = createNavButton("/investify/icons/homeIcon.png");
        JButton searchIcon = createNavButton("/investify/icons/searchIcon.png");
        JButton portfolioIcon = createNavButton("/investify/icons/portfolioIcon.png");
        JButton recurrentIcon = createNavButton("/investify/icons/recurrentIcon.png");
        JButton accountIcon = createNavButton("/investify/icons/accountIcon.png");

        // Add all buttons to the navigation bar
        navBar.add(homeIcon);
        navBar.add(searchIcon);
        navBar.add(portfolioIcon);
        navBar.add(recurrentIcon);
        navBar.add(accountIcon);

        // Set up home button action - updates total portfolio value when clicked
        homeIcon.addActionListener(e -> {
            // Update home screen content
            Component[] components = parentPanel.getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    JPanel panel = (JPanel) comp;
                    if (panel.getName() != null && panel.getName().equals("home")) {
                        // Find and update the "Total value" label and pie chart
                        Component[] panelComponents = panel.getComponents();
                        for (int i = 0; i < panelComponents.length; i++) {
                            Component c = panelComponents[i];
                            if (c instanceof JLabel) {
                                JLabel label = (JLabel) c;
                                if (label.getText().startsWith("Total value:")) {
                                    // Recalculate and update the total value
                                    createPieChartPanel(); // Updates portfolioValue with current currency
                                    label.setText("Total value: " + portfolioValue);
                                }
                            } else if (i == 2) {
                                // Replace the existing graph with a new one
                                JPanel newChartPanel = createPieChartPanel();
                                panel.remove(c);
                                GridBagConstraints gbcChart = new GridBagConstraints();
                                gbcChart.gridx = 0;
                                gbcChart.gridy = 2;
                                gbcChart.fill = GridBagConstraints.BOTH;
                                gbcChart.weighty = 1;
                                panel.add(newChartPanel, gbcChart, i);
                            }
                        }
                        panel.revalidate();
                        panel.repaint();
                        break;
                    }
                }
            }
            layout.show(parentPanel, "home");
        });

        // Set up search button action
        searchIcon.addActionListener(e -> layout.show(parentPanel, "search"));

        // Set up portfolio button action - refreshes portfolio content when clicked
        portfolioIcon.addActionListener(e -> {
            // Update existing portfolio content
            Component[] components = parentPanel.getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    JPanel panel = (JPanel) comp;
                    if (panel.getName() != null && panel.getName().equals("portfolio")) {
                        // Replace only the central content
                        panel.removeAll();

                        JLabel portfolioLabel = new JLabel("Your Portfolio", SwingConstants.CENTER);
                        portfolioLabel.setForeground(phoneUtils.textColor);
                        portfolioLabel.setFont(new Font("Inter", Font.BOLD, 24));

                        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                        headerPanel.setBackground(phoneUtils.backgroundColor);
                        headerPanel.add(portfolioLabel);

                        panel.setLayout(new BorderLayout());
                        panel.add(headerPanel, BorderLayout.NORTH);
                        panel.add(createPortfolioContent(), BorderLayout.CENTER);
                        panel.add(createNavBar(layout, parentPanel), BorderLayout.SOUTH);
                        panel.revalidate();
                        panel.repaint();
                    }
                }
            }

            layout.show(parentPanel, "portfolio");
        });

        // Set up recurrent investments button
        recurrentIcon.addActionListener(e -> layout.show(parentPanel, "recurrent"));

        // Set up Account button
        accountIcon.addActionListener(e -> layout.show(parentPanel, "account"));

        return navBar;
    }

    /**
     * Creates the portfolio content panel showing the user's current holdings.
     * Reads transaction data from the investifyData.json file.
     * Displays stocks, quantities, and values for all user holdings.
     * @return A JPanel containing the portfolio information
     */
    private static JPanel createPortfolioContent() {
        // Create the main content panel
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(phoneUtils.backgroundColor);

        // Get path to the transaction data file
        String filePath = System.getProperty("user.home") + "/investifyData.json";
        File file = new File(filePath);

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                // Read transactions from the JSON file
                Gson gson = new Gson();
                Transaction[] transactions = gson.fromJson(reader, Transaction[].class);

                if (transactions != null && transactions.length > 0) {
                    // Group stocks by symbol
                    Map<String, Integer> holdings = new HashMap<>();
                    Map<String, Double> totalValues = new HashMap<>();

                    for (Transaction transaction : transactions) {
                        String symbol = transaction.getSymbol();
                        int quantity = transaction.getQuantity();
                        double price = transaction.getPrice();
                        boolean isBuy = transaction.getAction().equalsIgnoreCase("Buy");

                        // Update quantities based on transaction type
                        holdings.put(symbol, holdings.getOrDefault(symbol, 0) +
                                (isBuy ? quantity : -quantity));

                        // Update total values based on transaction type
                        double transactionValue = quantity * price;
                        totalValues.put(symbol, totalValues.getOrDefault(symbol, 0.0) +
                                (isBuy ? transactionValue : -transactionValue));
                    }

                    // Remove stocks with zero or negative quantities
                    holdings.entrySet().removeIf(entry -> entry.getValue() <= 0);

                    if (!holdings.isEmpty()) {
                        // Create a vertical panel to display stocks
                        JPanel holdingsPanel = new JPanel();
                        holdingsPanel.setLayout(new BoxLayout(holdingsPanel, BoxLayout.Y_AXIS));
                        holdingsPanel.setBackground(phoneUtils.backgroundColor);

                        for (String symbol : holdings.keySet()) {
                            int quantity = holdings.get(symbol);
                            double totalValue = totalValues.get(symbol);

                            // Create a panel for each stock with a border for visual separation
                            JPanel stockPanel = new JPanel(new BorderLayout());
                            stockPanel.setBackground(phoneUtils.backgroundColor);
                            stockPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 60, 60)));
                            stockPanel.setPreferredSize(new Dimension(300, 60));
                            stockPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

                            // Stock symbol (left side)
                            JLabel symbolLabel = new JLabel(symbol);
                            symbolLabel.setForeground(phoneUtils.textColor);
                            symbolLabel.setFont(new Font("Inter", Font.BOLD, 20));
                            symbolLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

                            // Panel for stock information (right side)
                            JPanel infoPanel = new JPanel(new GridLayout(2, 1));
                            infoPanel.setBackground(phoneUtils.backgroundColor);

                            // Quantity of shares
                            JLabel quantityLabel = new JLabel(quantity + " shares");
                            quantityLabel.setForeground(phoneUtils.textColor);
                            quantityLabel.setFont(new Font("Inter", Font.PLAIN, 14));
                            quantityLabel.setHorizontalAlignment(SwingConstants.RIGHT);

                            // Total value
                            JLabel valueLabel = new JLabel(formatCurrency(totalValue));
                            valueLabel.setForeground(phoneUtils.textColor);
                            valueLabel.setFont(new Font("Inter", Font.PLAIN, 14));
                            valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

                            // Add components to panels
                            infoPanel.add(quantityLabel);
                            infoPanel.add(valueLabel);
                            infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

                            stockPanel.add(symbolLabel, BorderLayout.WEST);
                            stockPanel.add(infoPanel, BorderLayout.EAST);

                            holdingsPanel.add(stockPanel);
                            holdingsPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Espacement entre actions
                        }

                        // Add scroll functionality if there are many stocks
                        JScrollPane scrollPane = new JScrollPane(holdingsPanel);
                        scrollPane.setBorder(null);
                        scrollPane.getViewport().setBackground(phoneUtils.backgroundColor);
                        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                        content.add(scrollPane, BorderLayout.CENTER);
                    } else {
                        // Show message if no active holdings
                        JLabel noHoldingsLabel = new JLabel("No shares found", SwingConstants.CENTER);
                        noHoldingsLabel.setForeground(phoneUtils.textColor);
                        noHoldingsLabel.setFont(new Font("Inter", Font.PLAIN, 18));
                        content.add(noHoldingsLabel, BorderLayout.CENTER);
                    }
                } else {
                    // Show message if no transactions exist
                    JLabel noDataLabel = new JLabel("No transactions found.", SwingConstants.CENTER);
                    noDataLabel.setForeground(phoneUtils.textColor);
                    noDataLabel.setFont(new Font("Inter", Font.PLAIN, 18));
                    content.add(noDataLabel, BorderLayout.CENTER);
                }
            } catch (Exception e) {
                // Handle errors when loading data
                e.printStackTrace();
                JLabel errorLabel = new JLabel("Error loading data.", SwingConstants.CENTER);
                errorLabel.setForeground(phoneUtils.textColor);
                errorLabel.setFont(new Font("Inter", Font.PLAIN, 18));
                content.add(errorLabel, BorderLayout.CENTER);
            }
        } else {
            // Show message if data file doesn't exist
            JLabel noFileLabel = new JLabel("Data file not found.", SwingConstants.CENTER);
            noFileLabel.setForeground(phoneUtils.textColor);
            noFileLabel.setFont(new Font("Inter", Font.PLAIN, 18));
            content.add(noFileLabel, BorderLayout.CENTER);
        }

        return content;
    }

    /**
     * Opens a dialog for buying or selling stocks.
     * Records the transaction in the investifyData.json file.
     * @param action "Buy" or "Sell" to indicate transaction type
     * @param parentPanel The parent panel containing the search components
     */
    private static void openTransactionDialog(String action, JPanel parentPanel) {
        // Find the search field and retrieve the current stock symbol
        JTextField searchBar = null;
        double currentPrice = 0.0;
        String symbol = "";

        // Loop through components to find the search field and price label
        for (Component comp : parentPanel.getComponents()) {
            if (comp instanceof JTextField) {
                searchBar = (JTextField) comp;
                symbol = searchBar.getText().trim().toUpperCase();
            }
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                String text = label.getText();
                if (text != null && text.startsWith("Last price:")) {
                    try {
                        // Extract price from text like "Last price: XX.XX USD"
                        String priceText = text.substring(12, text.indexOf(" USD"));
                        currentPrice = Double.parseDouble(priceText);
                    } catch (Exception e) {
                        // Use default value if extraction fails
                        currentPrice = 0.0;
                    }
                }
            }
        }

        // Show error if required information is missing
        if (symbol.isEmpty() || currentPrice == 0.0) {
            JOptionPane.showMessageDialog(parentPanel, "Please search for a valid symbol first.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create and configure the transaction dialog
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(parentPanel), action + " Stocks", true);
        dialog.setSize(350, 280);
        dialog.setLayout(new GridBagLayout());
        dialog.setLocationRelativeTo(parentPanel);
        dialog.setResizable(false);

        // Set up layout constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Display symbol information
        JLabel symbolLabel = new JLabel("Symbol: " + symbol);
        symbolLabel.setFont(new Font("Inter", Font.BOLD, 16));
        dialog.add(symbolLabel, gbc);

        // Display current price
        gbc.gridy = 1;
        JLabel priceLabel = new JLabel(String.format("Price: %.2f $", currentPrice));
        priceLabel.setFont(new Font("Inter", Font.PLAIN, 16));
        dialog.add(priceLabel, gbc);

        // Add quantity label
        gbc.gridy = 2;
        JLabel label = new JLabel("Quantity to " + action.toLowerCase() + ":");
        label.setFont(new Font("Inter", Font.PLAIN, 16));
        dialog.add(label, gbc);

        // Add quantity input field
        JTextField quantityField = new JTextField(20);
        quantityField.setFont(new Font("Inter", Font.PLAIN, 18));
        quantityField.setPreferredSize(new Dimension(250, 40));
        gbc.gridy = 3;
        gbc.ipady = 10;
        dialog.add(quantityField, gbc);

        // Add confirmation button
        JButton confirmButton = new JButton("Confirm");
        confirmButton.setFont(new Font("Inter", Font.BOLD, 16));
        confirmButton.setPreferredSize(new Dimension(150, 40));
        gbc.gridy = 4;
        gbc.ipady = 5;
        gbc.insets = new Insets(20, 10, 10, 10);
        dialog.add(confirmButton, gbc);

        // Store final values for use in action listener
        final String finalSymbol = symbol;
        final double finalPrice = currentPrice;

        // Set up action for confirm button
        confirmButton.addActionListener(ev -> {
            String quantityText = quantityField.getText().trim();
            try {
                int quantity = Integer.parseInt(quantityText);
                if (quantity > 0) {
                    // Create transaction record
                    Transaction transaction = new Transaction(action, finalSymbol, quantity, finalPrice);

                    // Set up JSON handling
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    String filePath = System.getProperty("user.home") + "/investifyData.json";
                    File file = new File(filePath);

                    // Read existing transactions or create new list
                    List<Transaction> transactions = new ArrayList<>();
                    if (file.exists()) {
                        try (FileReader reader = new FileReader(file)) {
                            Transaction[] existing = gson.fromJson(reader, Transaction[].class);
                            if (existing != null) transactions.addAll(Arrays.asList(existing));
                        }
                    } else {
                        file.createNewFile();
                    }

                    // Add new transaction and save to file
                    transactions.add(transaction);
                    try (FileWriter writer = new FileWriter(file)) {
                        gson.toJson(transactions, writer);
                    }

                    // Show success message and close dialog
                    JOptionPane.showMessageDialog(dialog, "Transaction recorded successfully!");
                    dialog.dispose();

                } else {
                    JOptionPane.showMessageDialog(dialog, "Quantity must be greater than 0.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error while recording the transaction.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Display the dialog
        dialog.setVisible(true);
    }

    /**
     * Creates a pie chart showing the portfolio distribution.
     * Calculates the total portfolio value and updates the portfolioValue variable.
     * @return A JPanel containing the portfolio pie chart
     */
    private static JPanel createPieChartPanel() {
        // Create main panel for the chart
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(phoneUtils.backgroundColor);
        chartPanel.setPreferredSize(new Dimension(300, 200));

        // Get path to data file
        String filePath = System.getProperty("user.home") + "/investifyData.json";
        File file = new File(filePath);

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                // Parse JSON transaction data
                Gson gson = new Gson();
                Transaction[] transactions = gson.fromJson(reader, Transaction[].class);

                if (transactions != null && transactions.length > 0) {
                    // Calculate stock distribution
                    Map<String, Double> holdings = new HashMap<>();

                    // Process each transaction
                    for (Transaction transaction : transactions) {
                        String symbol = transaction.getSymbol();
                        int quantity = transaction.getQuantity();
                        double price = transaction.getPrice();
                        boolean isBuy = transaction.getAction().equalsIgnoreCase("Buy");

                        double transactionValue = quantity * price;

                        // Update holdings based on transaction type
                        if (isBuy) {
                            holdings.put(symbol, holdings.getOrDefault(symbol, 0.0) + transactionValue);
                        } else { // Handle sell transactions
                            holdings.put(symbol, holdings.getOrDefault(symbol, 0.0) - transactionValue);
                        }
                    }

                    // Remove stocks with zero or negative value
                    holdings.entrySet().removeIf(entry -> entry.getValue() <= 0);

                    if (!holdings.isEmpty()) {
                        // Create dataset for chart
                        DefaultPieDataset dataset = new DefaultPieDataset();

                        // Add each stock to dataset
                        for (Map.Entry<String, Double> entry : holdings.entrySet()) {
                            dataset.setValue(entry.getKey(), entry.getValue());
                        }

                        // Create pie chart
                        JFreeChart chart = ChartFactory.createPieChart(
                                null, // No title needed
                                dataset,
                                true, // Show legend
                                false, // No tooltips
                                false // No URLs
                        );

                        // Customize chart appearance
                        chart.setBackgroundPaint(phoneUtils.backgroundColor);

                        // Configure pie plot
                        PiePlot plot = (PiePlot) chart.getPlot();
                        plot.setBackgroundPaint(phoneUtils.backgroundColor);
                        plot.setOutlinePaint(null);
                        plot.setLabelOutlinePaint(null);
                        plot.setLabelShadowPaint(null);
                        plot.setLabelBackgroundPaint(null);
                        plot.setLabelFont(new Font("Inter", Font.PLAIN, 12));
                        plot.setLabelPaint(phoneUtils.textColor);

                        // Configure legend
                        LegendTitle legend = chart.getLegend();
                        legend.setBackgroundPaint(phoneUtils.backgroundColor);
                        legend.setItemFont(new Font("Inter", Font.PLAIN, 12));
                        legend.setItemPaint(phoneUtils.textColor);

                        // Add chart to panel
                        ChartPanel chartComponent = new ChartPanel(chart);
                        chartComponent.setPreferredSize(new Dimension(280, 180));
                        chartComponent.setBackground(phoneUtils.backgroundColor);
                        chartPanel.add(chartComponent, BorderLayout.CENTER);

                        // Calculate total portfolio value
                        double totalValue = holdings.values().stream().mapToDouble(Double::doubleValue).sum();
                        portfolioValue = formatCurrency(totalValue);

                        return chartPanel;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Show placeholder when no data is available
        JLabel noDataLabel = new JLabel("No portfolio data available", SwingConstants.CENTER);
        noDataLabel.setForeground(phoneUtils.textColor);
        noDataLabel.setFont(new Font("Inter", Font.PLAIN, 16));
        chartPanel.add(noDataLabel, BorderLayout.CENTER);

        // Reset portfolio value to zero
        portfolioValue = "0.00 $";
        return chartPanel;
    }

    /**
     * Shows a dialog for entering the AlphaVantage API key.
     * @return The entered API key, or null if cancelled
     */
    private String showApiKeyDialog() {
        // Create panel for API key dialog
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(350, 150));

        // Set up layout constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 5, 10);

        // Add instruction label
        JLabel label = new JLabel("Enter your AlphaVantage API key:");
        label.setFont(new Font("Inter", Font.BOLD, 14));
        panel.add(label, gbc);

        // Add text field for API key input
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField apiKeyField = new JTextField(20);
        apiKeyField.setFont(new Font("Inter", Font.PLAIN, 14));
        apiKeyField.setPreferredSize(new Dimension(300, 30));
        panel.add(apiKeyField, gbc);

        // Add info label
        gbc.gridy = 2;
        JLabel infoLabel = new JLabel("Enter the provided API key");
        infoLabel.setFont(new Font("Inter", Font.ITALIC, 12));
        panel.add(infoLabel, gbc);

        // Show dialog and get result
        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Configuration API Investify",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        // Process result based on user action
        if (result == JOptionPane.OK_OPTION) {
            String key = apiKeyField.getText().trim();
            if (!key.isEmpty()) {
                return key;
            }
        }

        // Return null if cancelled or empty key
        return null;
    }

    /**
     * Formats a monetary amount according to the currently selected currency.
     * Converts from USD to CHF if needed.
     * @param amount The monetary amount in USD to format
     * @return A formatted string with the appropriate currency symbol
     */
    public static String formatCurrency(double amount) {
        // Convert to CHF if selected
        if (currentCurrency.equals("CHF")) {
            amount = amount * USD_TO_CHF;
            return String.format("%.2f Fr.", amount);
        } else {
            // Use USD format
            return String.format("%.2f $", amount);
        }
    }

}