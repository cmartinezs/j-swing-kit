package cl.cmartinez.jswing.core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class DialogWindow {

  private static final String DEFAULT_TITLE = "Dialog";
  private static final int DEFAULT_WIDTH = 400;
  private static final int DEFAULT_HEIGHT = 300;
  private static final double DEFAULT_RATIO = 0.8;

  private final JDialog dialog;
  private final Frame owner;
  private DialogSize size = new DialogSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
  private double ratio = DEFAULT_RATIO;

  private DialogWindow(Builder builder) {
    if (builder == null) {
      throw new IllegalArgumentException("Builder cannot be null");
    }
    this.owner = builder.owner;
    this.dialog = new JDialog(this.owner, builder.title, builder.modal);

    if (builder.content != null) {
      this.dialog.getContentPane().add(builder.content, BorderLayout.CENTER);
    }

    this.dialog.pack();
    this.size = builder.size != null ? builder.size : this.size;
    this.ratio = builder.ratio > 0 && builder.ratio <= 1 ? builder.ratio : this.ratio;
    Dimension dialogSize = calculateDialogSize();
    this.dialog.setSize(dialogSize);

    if (this.owner == null) {
      this.dialog.setLocationRelativeTo(null);
    } else {
      new DialogPosition(this.owner, this.dialog).centerOnOwner();
    }
  }

  private Dimension calculateDialogSize() {
    if (this.owner != null) {
      Dimension ownerSize = this.owner.getSize();
      int width = (int) (ownerSize.width * ratio);
      int height = (int) (ownerSize.height * ratio);
      return new Dimension(width, height);
    }
    return new Dimension(size.width(), size.height());
  }

  public void show() {
    if (!dialog.isVisible()) {
      dialog.setVisible(true);
    }
  }

  public void hide() {
    if (dialog.isVisible()) {
      dialog.setVisible(false);
    }
  }

  public void dispose() {
    if (dialog != null) {
      dialog.dispose();
    }
  }

  public JDialog getDialog() {
    return dialog;
  }

  record DialogSize(int width, int height) {
    DialogSize {
      if (width <= 0 || height <= 0) {
        throw new IllegalArgumentException("Width and height must be positive");
      }
    }
  }

  static class DialogPosition {
    private final Frame owner;
    private final JDialog dialog;

    DialogPosition(Frame owner, JDialog dialog) {
      this.owner = owner;
      this.dialog = dialog;
    }

    void centerOnOwner() {
      dialog.addWindowListener(
          new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
              Point ownerLoc = owner.getLocationOnScreen();
              Dimension ownerSize = owner.getSize();
              Dimension dialogSize = dialog.getSize();
              int x = ownerLoc.x + (ownerSize.width - dialogSize.width) / 2;
              int y = ownerLoc.y + (ownerSize.height - dialogSize.height) / 2;
              dialog.setLocation(x, y);
            }
          });
    }
  }

  public static class Builder {

    private Frame owner;
    private String title = DEFAULT_TITLE;
    private boolean modal;
    private DialogSize size = new DialogSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    private double ratio = DEFAULT_RATIO;
    private Component content;

    public Builder owner(Frame owner) {
      this.owner = owner;
      return this;
    }

    public Builder title(String title) {
      this.title = title != null ? title : DEFAULT_TITLE;
      return this;
    }

    public Builder isModal() {
      this.modal = true;
      return this;
    }

    public Builder size(int width, int height) {
      this.size = new DialogSize(width, height);
      return this;
    }

    public Builder content(Component content) {
      if (content == null) {
        throw new IllegalArgumentException("Content cannot be null");
      }
      this.content = content;
      return this;
    }

    public Builder ratio(double ratio) {
      if (ratio <= 0 || ratio >= 1) {
        throw new IllegalArgumentException("Ratio must be between 0 and 1");
      }
      this.ratio = ratio;
      return this;
    }

    public DialogWindow build() {
      return new DialogWindow(this);
    }
  }
}
