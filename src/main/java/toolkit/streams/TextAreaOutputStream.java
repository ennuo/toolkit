package toolkit.streams;

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JTextArea;

public class TextAreaOutputStream extends OutputStream {
  private JTextArea textControl;
  public TextAreaOutputStream(JTextArea control) { this.textControl = control; }
  public void write(int b) throws IOException {
    this.textControl.append(String.valueOf((char)b));
    this.textControl.setCaretPosition(this.textControl.getDocument().getLength());
  }
}
