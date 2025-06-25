package cl.cmartinez.jswing.core;

import javax.swing.*;
import java.awt.*;

public class EventLogDialog {
  private final JFrame owner;
  private JTextArea eventLogArea;
  private final DialogWindow eventLogWindow;

  public EventLogDialog(JFrame owner) {
    if (owner == null) {
      throw new IllegalArgumentException("The owner cannot be null");
    }
    this.owner = owner;
    this.eventLogWindow =
        new DialogWindow.Builder()
            .owner(this.owner)
            .title("Event Log")
            .isModal()
            .content(contentForEventLog())
            .build();
  }

  public void show() {
    eventLogWindow.show();
  }

  public void hide() {
    eventLogWindow.hide();
  }

  public void logEvent(String message) {
    this.eventLogArea.append(message + "\n");
  }

  private Component contentForEventLog() {
    this.eventLogArea = new JTextArea(15, 50);
    this.eventLogArea.setEditable(false);
    return new JScrollPane(this.eventLogArea);
  }
}
