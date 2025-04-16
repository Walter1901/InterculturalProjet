import javax.swing.*;
import java.awt.*;

public class InvestifyApp {

    public static JPanel createInvestify() {

        JPanel investifyApp = new JPanel(); // main panel with a grid layout
        investifyApp.setBackground(phoneUtils.backgroundColor);
        investifyApp.setLayout(new GridLayout(6, 1));

        // Nav bar
        JPanel investifyNavBar = createNavBarInvestify();

        investifyApp.add(new JLabel());
        investifyApp.add(new JLabel());
        investifyApp.add(new JLabel());
        investifyApp.add(new JLabel());
        investifyApp.add(new JLabel());
        investifyApp.add(investifyNavBar);

        return investifyApp;
    }

    public static JPanel createNavBarInvestify(){

        JPanel investifyNavBar = new JPanel();
        investifyNavBar.setBackground(phoneUtils.backgroundColor);
        investifyNavBar.setLayout(new GridLayout(1, 5));
        investifyNavBar.setForeground(phoneUtils.textColor);

        JLabel homeLabel = new JLabel();
        homeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        homeLabel.setIcon(new ImageIcon("src/main/resources/investifyIcons/homeIcon.png"));
        investifyNavBar.add(homeLabel, BorderLayout.SOUTH);

        JLabel searchLabel = new JLabel();
        searchLabel.setHorizontalAlignment(SwingConstants.CENTER);
        searchLabel.setIcon(new ImageIcon("src/main/resources/investifyIcons/searchIcon.png"));
        investifyNavBar.add(searchLabel, BorderLayout.SOUTH);

        JLabel portfolioLabel = new JLabel();
        portfolioLabel.setHorizontalAlignment(SwingConstants.CENTER);
        portfolioLabel.setIcon(new ImageIcon("src/main/resources/investifyIcons/portfolioIcon.png"));
        investifyNavBar.add(portfolioLabel, BorderLayout.SOUTH);

        JLabel recurrentLabel = new JLabel();
        recurrentLabel.setHorizontalAlignment(SwingConstants.CENTER);
        recurrentLabel.setIcon(new ImageIcon("src/main/resources/investifyIcons/recurrentIcon.png"));
        investifyNavBar.add(recurrentLabel, BorderLayout.SOUTH);

        JLabel accountLabel = new JLabel();
        accountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        accountLabel.setIcon(new ImageIcon("src/main/resources/investifyIcons/accountIcon.png"));
        investifyNavBar.add(accountLabel, BorderLayout.SOUTH);

        return investifyNavBar;
    }

}
