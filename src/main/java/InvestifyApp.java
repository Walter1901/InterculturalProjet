import javax.swing.*;
import java.awt.*;

public class InvestifyApp {

    public static JPanel createInvestify() {

        JPanel investifyApp = new JPanel();
        investifyApp.setBackground(phoneUtils.backgroundColor);
        JLabel label = new JLabel("Investify App", SwingConstants.CENTER);
        label.setFont(new Font("Inter", Font.BOLD, 20));
        label.setForeground(phoneUtils.textColor);
        investifyApp.add(label, BorderLayout.CENTER);

        return investifyApp;
    }



}
