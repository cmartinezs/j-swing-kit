package cl.cmartinez.jswing.core;

import lombok.Getter;

import javax.swing.*;

@Getter
public enum LookAndFeelType {
  SYSTEM(UIManager.getSystemLookAndFeelClassName()),
  CROSS_PLATFORM(UIManager.getCrossPlatformLookAndFeelClassName()),
  METAL("javax.swing.plaf.metal.MetalLookAndFeel"),
  NIMBUS("javax.swing.plaf.nimbus.NimbusLookAndFeel"),
  WINDOWS("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"),
  WINDOWS_CLASSIC("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");

  private final String className;

  LookAndFeelType(String className) {
    this.className = className;
  }

}
