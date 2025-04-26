import javax.swing.*;
import java.awt.*;

public class InvestifyApp {

    public static JPanel createInvestify() {

        JPanel mainPanel = new JPanel(new CardLayout()); //Creates the card panel
        JPanel homeMain = new JPanel(new GridBagLayout()); //Creates the main panel that has the "grid bag layout"

        homeMain.setBackground(phoneUtils.backgroundColor);

        GridBagConstraints gbc = new GridBagConstraints();//Creates constraints under the name "gbc"
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(5, 0, 5, 0); // set a margin between the labels

        JLabel titlePortfolio = new JLabel("Portfolio"); //title at the top of the panel
        titlePortfolio.setForeground(phoneUtils.textColor);
        titlePortfolio.setFont(new Font("Inter", Font.BOLD, 32));
        gbc.gridy = 0; // 0 because it will be the very first text at the top
        homeMain.add(titlePortfolio, gbc);

        JLabel subtitle1 = new JLabel(portfolioValue); //subtitle below the title holds the value of user's stock portfolio
        subtitle1.setForeground(phoneUtils.textColor);
        subtitle1.setFont(new Font("Inter", Font.PLAIN, 21));
        gbc.gridy = 1; // 1 because it one level below the title
        homeMain.add(subtitle1, gbc);

        JPanel graphPlaceholder = new JPanel(); //Graph of past value of user's portfolio
        graphPlaceholder.setPreferredSize(new Dimension(150, 200));
        graphPlaceholder.setBackground(Color.LIGHT_GRAY);
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH; // Le graphe prend l'espace
        gbc.weighty = 1; // Important : il prend tout l’espace dispo
        homeMain.add(graphPlaceholder, gbc);

        JLabel subtitleUnderGraph = new JLabel("Assets"); //subtitle below the title holds the value of user's stock portfolio
        subtitleUnderGraph.setForeground(phoneUtils.textColor);
        subtitleUnderGraph.setFont(new Font("Inter", Font.PLAIN, 21));
        gbc.gridy = 3; // 1 because it one level below the title
        homeMain.add(subtitleUnderGraph, gbc);

        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.SOUTH;
        homeMain.add(createNavBar((CardLayout) mainPanel.getLayout(), mainPanel), gbc);

        // Search panel
        JPanel searchMain = new JPanel(new BorderLayout());
        searchMain.setBackground(phoneUtils.backgroundColor);

        JLabel searchLabel = new JLabel("Page Search", SwingConstants.CENTER);
        searchLabel.setFont(new Font("Inter", Font.BOLD, 24));
        searchMain.add(searchLabel, BorderLayout.CENTER);

        searchMain.add(createNavBar((CardLayout) mainPanel.getLayout(), mainPanel), BorderLayout.SOUTH);

        // Adding the panels to the card layout
        mainPanel.add(homeMain, "home"); //Adds the home panel to the main card stack
        mainPanel.add(searchMain, "search"); //Adds the search panel to the card stack


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
        button.setMargin(new Insets(5,7,5,7));
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

        // ActionListener pour changer de page
        homeIcon.addActionListener(e -> layout.show(parentPanel, "home"));
        searchIcon.addActionListener(e -> layout.show(parentPanel, "search"));
        // Tu pourras ajouter d'autres boutons plus tard ici !

        return navBar;
    }

    public static String portfolioValue = "CHF 3'600.-";


}
