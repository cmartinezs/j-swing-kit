package cl.cmartinez.jswing.examples;

import cl.cmartinez.jswing.core.LookAndFeelType;
import cl.cmartinez.jswing.core.MainWindow;
import javax.swing.*;
import java.awt.*;

public class Main {
  public static void main(String[] args) {
    // Create a custom content panel
    JPanel contentPanel = createContentPanel();

    // Build the main window with various configurations
    MainWindow window =
        new MainWindow.Builder()
            // Set the look and feel
            .lookAndFeel(LookAndFeelType.NIMBUS)

            // Configure window size and position
            .size(1024, 768)
            .centerOnScreen()
            .minScreenRatio(0.5, 0.5) // Minimum 50% of screen size
            .maintainAspect(true)

            // Configure window behavior
            .exitOnClose()
            .noResizable()

            // Set window appearance
            .title("My Application")
            .icon("/icons/app-icon.png")

            // Add custom content
            .content(contentPanel)

            // Enable event logging
            .enableEventLog()

            // Build the window
            .build();

    // Show the window
    window.show();
  }

  private static JPanel createContentPanel() {
    JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Add some components to demonstrate event logging
    JButton button = new JButton("Click Me");
    button.setName("mainButton");

    JTextField textField = new JTextField(20);
    textField.setName("mainTextField");

    JCheckBox checkBox = new JCheckBox("Enable Feature");
    checkBox.setName("mainCheckBox");

    String[] items = {"Option 1", "Option 2", "Option 3"};
    JComboBox<String> comboBox = new JComboBox<>(items);
    comboBox.setName("mainComboBox");

    // Layout components
    JPanel topPanel = new JPanel(new FlowLayout());
    topPanel.add(textField);
    topPanel.add(button);

    JPanel centerPanel = new JPanel(new FlowLayout());
    centerPanel.add(checkBox);
    centerPanel.add(comboBox);

    panel.add(topPanel, BorderLayout.NORTH);
    panel.add(centerPanel, BorderLayout.CENTER);

    return panel;
  }
}
