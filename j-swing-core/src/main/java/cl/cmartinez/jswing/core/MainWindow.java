package cl.cmartinez.jswing.core;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class MainWindow {
  public static final int[] ICON_SIZES = new int[] {16, 32, 48, 64};
  private final JFrame frame;
  private EventLogDialog eventLogDialog;

  private MainWindow(Builder builder) {
    applyLookAndFeel(builder.lafType);
    this.frame = new JFrame();
    initUI(builder);
  }

  private void applyLookAndFeel(LookAndFeelType lafType) {
    log.debug("Setting LookAndFeel: {}", lafType);
    if (lafType == null) {
      return;
    }
    try {
      if (!LookAndFeelType.SYSTEM.equals(lafType)) {
        JFrame.setDefaultLookAndFeelDecorated(true);
      }
      UIManager.setLookAndFeel(lafType.getClassName());
    } catch (Exception e) {
      System.err.println("Could not set LookAndFeel: " + e.getMessage());
    }
  }

  private void initUI(Builder builder) {
    setTitle(builder);
    setIcon(builder);
    setSize(builder);
    centerOnScreen(builder);
    exitOnClose(builder);
    setResizable(builder);
    setContent(builder);
    SwingUtilities.updateComponentTreeUI(frame);
    setEventLog(builder);
  }

  private void setEventLog(Builder builder) {
    if (builder.enableEventLog) {
      frame.setJMenuBar(createMenuBar());
      createLogDialog(builder);
      attachWindowLogging();
      attachComponentLogging(frame.getContentPane());
    }
  }

  private void createLogDialog(Builder b) {
    if (b == null || !b.enableEventLog) {
      return;
    }
    this.eventLogDialog = new EventLogDialog(frame);
  }

  private void setContent(Builder builder) {
    frame.setContentPane(builder.content != null ? builder.content : new JPanel());
  }

  private JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    JMenu menu = new JMenu("About");
    JMenuItem showLog = new JMenuItem("Show Event Log");
    showLog.addActionListener(e -> eventLogDialog.show());
    menu.add(showLog);
    menuBar.add(menu);
    return menuBar;
  }

  private void attachWindowLogging() {
    frame.addWindowListener(
        new WindowAdapter() {
          public void windowOpened(WindowEvent e) {
            eventLogDialog.logEvent("Window opened");
          }

          public void windowClosing(WindowEvent e) {
            eventLogDialog.logEvent("Window closing");
          }

          public void windowClosed(WindowEvent e) {
            eventLogDialog.logEvent("Window closed");
          }

          public void windowIconified(WindowEvent e) {
            eventLogDialog.logEvent("Window minimized");
          }

          public void windowDeiconified(WindowEvent e) {
            eventLogDialog.logEvent("Window restored");
          }

          public void windowActivated(WindowEvent e) {
            eventLogDialog.logEvent("Window activated");
          }

          public void windowDeactivated(WindowEvent e) {
            eventLogDialog.logEvent("Window deactivated");
          }
        });
    frame.addComponentListener(
        new ComponentAdapter() {
          public void componentResized(ComponentEvent e) {
            eventLogDialog.logEvent("Resized to " + frame.getSize());
          }

          public void componentMoved(ComponentEvent e) {
            eventLogDialog.logEvent("Moved to " + frame.getLocation());
          }
        });
  }

  private void attachComponentLogging(Component comp) {
    if (comp instanceof JButton) {
      ((JButton) comp)
          .addActionListener(
              e -> eventLogDialog.logEvent("Button '" + ((JButton) comp).getText() + "' clicked"));
    } else if (comp instanceof JTextField tf) {
      tf.getDocument()
          .addDocumentListener(
              new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                  logFieldChange(tf);
                }

                public void removeUpdate(DocumentEvent e) {
                  logFieldChange(tf);
                }

                public void changedUpdate(DocumentEvent e) {
                  logFieldChange(tf);
                }

                private void logFieldChange(JTextField f) {
                  eventLogDialog.logEvent(
                      "TextField '" + f.getName() + "' changed: " + f.getText());
                }
              });
    } else if (comp instanceof JCheckBox) {
      ((JCheckBox) comp)
          .addItemListener(
              e ->
                  eventLogDialog.logEvent(
                      "CheckBox '"
                          + ((JCheckBox) comp).getText()
                          + "': "
                          + (e.getStateChange() == ItemEvent.SELECTED
                              ? "selected"
                              : "deselected")));
    } else if (comp instanceof JComboBox) {
      ((JComboBox<?>) comp)
          .addActionListener(
              e ->
                  eventLogDialog.logEvent(
                      "ComboBox changed: " + ((JComboBox<?>) comp).getSelectedItem()));
    }
    if (comp instanceof Container) {
      for (Component child : ((Container) comp).getComponents()) {
        attachComponentLogging(child);
      }
    }
  }

  private void setResizable(Builder builder) {
    frame.setResizable(builder.resizable);
  }

  private void exitOnClose(Builder builder) {
    if (builder.exitOnClose) {
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
  }

  private void centerOnScreen(Builder builder) {
    if (builder.centerOnScreen) {
      frame.setLocationRelativeTo(null);
    }
  }

  private void setSize(Builder b) {
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    int w = b.width;
    int h = b.height;
    if (b.minWidthRatio > 0) w = Math.max(w, (int) (screen.width * b.minWidthRatio));
    if (b.minHeightRatio > 0) h = Math.max(h, (int) (screen.height * b.minHeightRatio));
    if (b.maintainAspect && b.width > 0 && b.height > 0) {
      double aspect = (double) b.width / b.height;
      h = (int) (w / aspect);
    }
    frame.setSize(w, h);
  }

  private void setIcon(Builder builder) {
    if (isValidIconPath(builder)) {
      try {
        BufferedImage baseImage = getBufferedImage(builder);
        frame.setIconImages(getImages(baseImage));

        if (Taskbar.isTaskbarSupported()) {
          try {
            Taskbar.getTaskbar().setIconImage(baseImage);
          } catch (UnsupportedOperationException e) {
            System.err.println("Taskbar no soporta icono: " + e.getMessage());
          }
        }
      } catch (IOException e) {
        System.err.println("No se pudo cargar el icono: " + e.getMessage());
      }
    }
  }

  private static List<Image> getImages(BufferedImage baseImage) {
    return Arrays.stream(ICON_SIZES)
        .mapToObj(s -> baseImage.getScaledInstance(s, s, Image.SCALE_SMOOTH))
        .collect(Collectors.toList());
  }

  private static boolean isValidIconPath(Builder builder) {
    return builder.iconPath != null && !builder.iconPath.isBlank();
  }

  private static BufferedImage getBufferedImage(Builder builder) throws IOException {
    URL resourceUrl = getResourceUrl(builder.iconPath);
    BufferedImage baseImage;

    if (resourceUrl != null) {
      baseImage = ImageIO.read(resourceUrl);
    } else {
      // Evitar constructor URL(String) deprecated
      baseImage = ImageIO.read(Path.of(builder.iconPath).toUri().toURL());
    }
    return baseImage;
  }

  private static URL getResourceUrl(String path) {
    return MainWindow.class.getResource(path);
  }

  private void setTitle(Builder builder) {
    frame.setTitle(builder.title);
  }

  public void show() {
    SwingUtilities.invokeLater(() -> frame.setVisible(true));
  }

  public JFrame getFrame() {
    return frame;
  }

  public static class Builder {
    private int width = 800;
    private int height = 600;
    private boolean centerOnScreen = false;
    private boolean exitOnClose = false;
    private boolean resizable = true;
    private String title = "Main Window";
    private String iconPath = "/icons/default.png";
    private LookAndFeelType lafType = LookAndFeelType.SYSTEM;
    private boolean enableEventLog = false;
    private JPanel content = null;
    private double minWidthRatio = 0.0;
    private double minHeightRatio = 0.0;
    private boolean maintainAspect = true;

    public Builder size(int width, int height) {
      this.width = width;
      this.height = height;
      return this;
    }

    public Builder centerOnScreen() {
      this.centerOnScreen = true;
      return this;
    }

    public Builder exitOnClose() {
      this.exitOnClose = true;
      return this;
    }

    public Builder noResizable() {
      this.resizable = false;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder icon(String iconPath) {
      this.iconPath = iconPath;
      return this;
    }

    public Builder lookAndFeel(LookAndFeelType lafType) {
      this.lafType = lafType;
      return this;
    }

    public Builder enableEventLog() {
      this.enableEventLog = true;
      return this;
    }

    public Builder content(JPanel panel) {
      this.content = panel;
      return this;
    }

    public Builder minScreenRatio(double wRatio, double hRatio) {
      this.minWidthRatio = wRatio;
      this.minHeightRatio = hRatio;
      return this;
    }

    public Builder maintainAspect(boolean m) {
      this.maintainAspect = m;
      return this;
    }

    public MainWindow build() {
      return new MainWindow(this);
    }
  }
}
