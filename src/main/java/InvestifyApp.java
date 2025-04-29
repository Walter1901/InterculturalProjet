import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.Config;
import com.crazzyghost.alphavantage.parameters.OutputSize;
import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse;

import javax.swing.*;
import java.awt.*;


public class InvestifyApp {




    public static JPanel createInvestify() {

        // =====API config=====
        Config cfg = Config.builder()
                .key("JFFZ9TYUYT8VISEK")
                .timeOut(10)
                .build();

        AlphaVantage.api().init(cfg);
        // =====================

        // =====Main Card Panel=====
        JPanel mainPanel = new JPanel(new CardLayout()); //Creates the card panel
        // =========================

        // =====Home panel=====
        JPanel homeMain = new JPanel(new GridBagLayout()); //Creates the main panel that has the "grid bag layout"
        homeMain.setBackground(phoneUtils.backgroundColor);

        GridBagConstraints gbcHome = new GridBagConstraints();//Creates constraints
        gbcHome.gridx = 0;
        gbcHome.gridwidth = 1;
        gbcHome.fill = GridBagConstraints.NONE;
        gbcHome.anchor = GridBagConstraints.NORTH;
        gbcHome.insets = new Insets(5, 0, 5, 0); //sets a margin between the labels

        JLabel titlePortfolio = new JLabel("Portfolio"); //title at the top of the panel
        titlePortfolio.setForeground(phoneUtils.textColor);
        titlePortfolio.setFont(new Font("Inter", Font.BOLD, 32));
        gbcHome.gridy = 0; // 0 because it will be the very first text at the top
        homeMain.add(titlePortfolio, gbcHome);

        JLabel subtitle1 = new JLabel(portfolioValue); //subtitle below the title holds the value of user's stock portfolio
        subtitle1.setForeground(phoneUtils.textColor);
        subtitle1.setFont(new Font("Inter", Font.PLAIN, 21));
        gbcHome.gridy = 1; // 1 because it one level below the title
        homeMain.add(subtitle1, gbcHome);

        JPanel graphPlaceholder = new JPanel(); //Graph of past value of user's portfolio
        graphPlaceholder.setPreferredSize(new Dimension(150, 200));
        graphPlaceholder.setBackground(Color.DARK_GRAY);
        gbcHome.gridy = 2;
        gbcHome.fill = GridBagConstraints.BOTH;
        gbcHome.weighty = 1;
        homeMain.add(graphPlaceholder, gbcHome);

        JLabel subtitleUnderGraph = new JLabel("Change view:"); //subtitle below the title holds the value of user's portfolio
        subtitleUnderGraph.setForeground(phoneUtils.textColor);
        subtitleUnderGraph.setFont(new Font("Inter", Font.PLAIN, 18));
        gbcHome.gridy = 3; // 1 because it one level below the title
        homeMain.add(subtitleUnderGraph, gbcHome);

        gbcHome.gridy = 4;
        JPanel changeViewPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 7, 0));
        changeViewPanel.setBackground(phoneUtils.backgroundColor);

        String[] buttonLabels = {"1D", "1W", "1M", "YTD", "MAX"};
        for (String label : buttonLabels) { // for each string in the array do the following:
            JButton btn = new JButton(label);
            btn.setBackground(phoneUtils.backgroundColor);
            btn.setFocusPainted(false);
            btn.setFont(new Font("Inter", Font.PLAIN, 14));
            btn.setForeground(phoneUtils.textColor);
            changeViewPanel.add(btn);

            btn.addActionListener(e -> {
                switch (label) {
                    case "1D":
                        System.out.println("Button 1D ");
                        break;
                    case "1W":
                        System.out.println("Button 1W ");
                        break;
                    case "1M":
                        System.out.println("Button 1M ");
                        break;
                    case "YTD":
                        System.out.println("Button YTD ");
                        break;
                    case "MAX":
                        System.out.println("Button MAX ");
                        break;
                }

            });
            homeMain.add(changeViewPanel, gbcHome);
        }

        gbcHome.gridy = 5;
        gbcHome.fill = GridBagConstraints.HORIZONTAL;
        gbcHome.weighty = 0;
        gbcHome.anchor = GridBagConstraints.SOUTH;
        homeMain.add(createNavBar((CardLayout) mainPanel.getLayout(), mainPanel), gbcHome);
        //=======================


        // =====Search panel=====
        JPanel searchMain = new JPanel(new GridBagLayout());
        searchMain.setBackground(phoneUtils.backgroundColor);

        GridBagConstraints gbcSearch = new GridBagConstraints();
        gbcSearch.gridx = 0;
        gbcSearch.gridwidth = 1;
        gbcSearch.insets = new Insets(5, 0, 5, 0);
        gbcSearch.fill = GridBagConstraints.NONE;
        gbcSearch.anchor = GridBagConstraints.NORTH;

        JLabel searchLabel = new JLabel("Page Search", SwingConstants.CENTER);
        searchLabel.setForeground(phoneUtils.textColor);
        searchLabel.setFont(new Font("Inter", Font.BOLD, 24));
        gbcSearch.gridy = 0;
        searchMain.add(searchLabel, gbcSearch);

        JLabel subtitleSearch = new JLabel("Search for symbols", SwingConstants.CENTER);
        subtitleSearch.setForeground(phoneUtils.textColor);
        subtitleSearch.setFont(new Font("Inter", Font.PLAIN, 18));
        gbcSearch.gridy = 1; // Position entre le titre et la barre de recherche
        searchMain.add(subtitleSearch, gbcSearch);

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

        JLabel searchResults = new JLabel("", SwingConstants.CENTER);
        searchResults.setForeground(phoneUtils.textColor);
        searchResults.setFont(new Font("Inter", Font.PLAIN, 18));
        gbcSearch.gridy = 3;
        gbcSearch.weighty = 1;

        searchBar.addActionListener(e -> {
            String symbol = searchBar.getText().trim(); // Récupère le symbole entré par l'utilisateur
            if (!symbol.isEmpty()) {
                try {
                    TimeSeriesResponse response = AlphaVantage.api()
                            .timeSeries()
                            .daily()
                            .forSymbol(symbol)
                            .outputSize(OutputSize.COMPACT)
                            .fetchSync();

                    if (response.getErrorMessage() == null) {
                        response.getStockUnits().stream()
                                .findFirst()
                                .ifPresentOrElse(
                                        unit -> searchResults.setText("Last price: " + unit.getClose() + " USD"),
                                        () -> searchResults.setText("No data available for this symbol.")
                                );
                    } else {
                        searchResults.setText("ERROR : " + response.getErrorMessage());
                    }
                } catch (Exception ex) {
                    searchResults.setText("Error retrieving data.");
                    ex.printStackTrace();
                }
            } else {
                searchResults.setText("Please enter a valid symbol.");
            }
        });

        searchMain.add(searchResults, gbcSearch);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(phoneUtils.backgroundColor);

        JButton buyButton = new JButton("Buy");
        buyButton.setFont(new Font("Inter", Font.BOLD, 16));
        buyButton.setBackground(new Color(50,205,50));
        buyButton.setForeground(phoneUtils.textColor);
        buyButton.setFocusPainted(false);

        JButton sellButton = new JButton("Sell");
        sellButton.setFont(new Font("Inter", Font.BOLD, 16));
        sellButton.setBackground(new Color(255, 62, 65));
        sellButton.setForeground(phoneUtils.textColor);
        sellButton.setFocusPainted(false);

        buttonPanel.add(buyButton);
        buttonPanel.add(sellButton);

        gbcSearch.gridy = 4; // Position sous le label des résultats
        gbcSearch.weighty = 0;
        searchMain.add(buttonPanel, gbcSearch);

        buyButton.addActionListener(e -> openTransactionDialog("Buy", searchMain));
        sellButton.addActionListener(e -> openTransactionDialog("Sell", searchMain));

        gbcSearch.gridy = 5;
        gbcSearch.fill = GridBagConstraints.HORIZONTAL;
        gbcSearch.weighty = 0;
        gbcSearch.anchor = GridBagConstraints.SOUTH;
        searchMain.add(createNavBar((CardLayout) mainPanel.getLayout(), mainPanel), gbcSearch);
        //=======================

        // =====Portfolio Panel=====
        JPanel portfolioMain = new JPanel(new GridBagLayout());
        portfolioMain.setBackground(phoneUtils.backgroundColor);

        GridBagConstraints gbcPortfolio = new GridBagConstraints();
        gbcPortfolio.gridx = 0;
        gbcPortfolio.gridy = 0;
        gbcPortfolio.fill = GridBagConstraints.NONE;
        gbcPortfolio.anchor = GridBagConstraints.NORTH;
        JLabel portfolioLabel = new JLabel("Your Portfolio", SwingConstants.CENTER);
        portfolioLabel.setForeground(phoneUtils.textColor);
        portfolioLabel.setFont(new Font("Inter", Font.BOLD, 24));
        portfolioMain.add(portfolioLabel, gbcPortfolio);

        gbcPortfolio.gridy = 1;
        gbcPortfolio.weighty = 1;
        JLabel portfolioInfo = new JLabel("Portfolio info here...");
        portfolioInfo.setForeground(phoneUtils.textColor);
        portfolioInfo.setFont(new Font("Inter", Font.PLAIN, 18));
        portfolioMain.add(portfolioInfo, gbcPortfolio);

        gbcPortfolio.gridy = 2;
        gbcPortfolio.fill = GridBagConstraints.HORIZONTAL;
        gbcPortfolio.weighty = 0;
        gbcPortfolio.anchor = GridBagConstraints.SOUTH;
        portfolioMain.add(createNavBar((CardLayout) mainPanel.getLayout(), mainPanel), gbcPortfolio);
        //=======================


        // =====Recurrent Panel=====
        JPanel recurrentMain = new JPanel(new GridBagLayout());
        recurrentMain.setBackground(phoneUtils.backgroundColor);

        GridBagConstraints gbcRecurrent = new GridBagConstraints();
        gbcRecurrent.gridx = 0;
        gbcRecurrent.gridy = 0;
        gbcRecurrent.fill = GridBagConstraints.NONE;
        gbcRecurrent.anchor = GridBagConstraints.NORTH;
        JLabel recurrentLabel = new JLabel("Recurrent Investments", SwingConstants.CENTER);
        recurrentLabel.setForeground(phoneUtils.textColor);
        recurrentLabel.setFont(new Font("Inter", Font.BOLD, 24));
        recurrentMain.add(recurrentLabel, gbcRecurrent);

        gbcRecurrent.weighty = 1;
        gbcRecurrent.gridy = 1;
        JLabel recurrentInfo = new JLabel("Recurrent investments info...");
        recurrentInfo.setForeground(phoneUtils.textColor);
        recurrentInfo.setFont(new Font("Inter", Font.PLAIN, 18));
        recurrentMain.add(recurrentInfo, gbcRecurrent);

        gbcRecurrent.gridy = 2;
        gbcRecurrent.fill = GridBagConstraints.HORIZONTAL;
        gbcRecurrent.weighty = 0;
        gbcRecurrent.anchor = GridBagConstraints.SOUTH;
        recurrentMain.add(createNavBar((CardLayout) mainPanel.getLayout(), mainPanel), gbcRecurrent);
        //=======================

        // =====Account Panel=====
        JPanel accountMain = new JPanel(new GridBagLayout());
        accountMain.setBackground(phoneUtils.backgroundColor);

        GridBagConstraints gbcAccount = new GridBagConstraints();
        gbcAccount.gridx = 0;
        gbcAccount.gridy = 0;
        gbcAccount.fill = GridBagConstraints.NONE;
        gbcAccount.anchor = GridBagConstraints.NORTH;
        JLabel accountLabel = new JLabel("Account Settings", SwingConstants.CENTER);
        accountLabel.setForeground(phoneUtils.textColor);
        accountLabel.setFont(new Font("Inter", Font.BOLD, 24));
        accountMain.add(accountLabel, gbcAccount);

        gbcAccount.weighty = 1;
        gbcAccount.gridy = 1;
        JLabel accountInfo = new JLabel("Account settings info...");
        accountInfo.setForeground(phoneUtils.textColor);
        accountInfo.setFont(new Font("Inter", Font.PLAIN, 18));
        accountMain.add(accountInfo, gbcAccount);

        gbcAccount.gridy = 2;
        gbcAccount.fill = GridBagConstraints.HORIZONTAL;
        gbcAccount.weighty = 0;
        gbcAccount.anchor = GridBagConstraints.SOUTH;
        accountMain.add(createNavBar((CardLayout) mainPanel.getLayout(), mainPanel), gbcAccount);
        //=======================



        // Adding the panels to the card layout
        mainPanel.add(homeMain, "home");
        mainPanel.add(searchMain, "search");
        mainPanel.add(portfolioMain, "portfolio");
        mainPanel.add(recurrentMain, "recurrent");
        mainPanel.add(accountMain, "account");

        return mainPanel;
    }

    private static JButton createNavButton(String iconPath) {
        ImageIcon originalIcon = new ImageIcon(iconPath);
        Image scaledImage = originalIcon.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH); // Choisis ta taille ici
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        JButton button = new JButton(scaledIcon);
        button.setBackground(phoneUtils.backgroundColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setMargin(new Insets(5, 7, 5, 7));
        return button;
    }

    private static JPanel createNavBar(CardLayout layout, JPanel parentPanel) {
        JPanel navBar = new JPanel(new GridLayout(1, 5));
        navBar.setBackground(phoneUtils.backgroundColor);

        JButton homeIcon = createNavButton("src/main/resources/investifyIcons/homeIcon.png");
        JButton searchIcon = createNavButton("src/main/resources/investifyIcons/searchIcon.png");
        JButton portfolioIcon = createNavButton("src/main/resources/investifyIcons/portfolioIcon.png");
        JButton recurrentIcon = createNavButton("src/main/resources/investifyIcons/recurrentIcon.png");
        JButton accountIcon = createNavButton("src/main/resources/investifyIcons/accountIcon.png");

        navBar.add(homeIcon);
        navBar.add(searchIcon);
        navBar.add(portfolioIcon);
        navBar.add(recurrentIcon);
        navBar.add(accountIcon);

        homeIcon.addActionListener(e -> layout.show(parentPanel, "home"));
        searchIcon.addActionListener(e -> layout.show(parentPanel, "search"));
        portfolioIcon.addActionListener(e -> layout.show(parentPanel, "portfolio"));
        recurrentIcon.addActionListener(e -> layout.show(parentPanel, "recurrent"));
        accountIcon.addActionListener(e -> layout.show(parentPanel, "account"));

        return navBar;
    }

    private static void openTransactionDialog(String action, JPanel parentPanel) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(parentPanel), action + " Stocks", true);
        dialog.setSize(300, 200);
        dialog.setLayout(new GridBagLayout());
        dialog.setLocationRelativeTo(parentPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel label = new JLabel("Enter quantity to " + action.toLowerCase() + ":");
        dialog.add(label, gbc);

        JTextField quantityField = new JTextField(10);
        gbc.gridy = 1;
        dialog.add(quantityField, gbc);

        JButton confirmButton = new JButton("Confirm");
        gbc.gridy = 2;
        dialog.add(confirmButton, gbc);

        confirmButton.addActionListener(ev -> {
            String quantityText = quantityField.getText().trim();
            try {
                int quantity = Integer.parseInt(quantityText);
                if (quantity > 0) {
                    JOptionPane.showMessageDialog(dialog, "Order to " + action.toLowerCase() + " " + quantity + " stocks confirmed");
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Quantity must be greater than 0.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    public static String portfolioValue;



}
