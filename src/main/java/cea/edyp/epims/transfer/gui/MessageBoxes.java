package cea.edyp.epims.transfer.gui;

import javax.swing.*;

 
public class MessageBoxes {
  public MessageBoxes() {
  }
 
  public static void showMessage(String text)
  {
    showMessage(text,"eP-Back");
  }
 
  public static void showMessage(String text, String title)
  {
    showMessage(text,title,JOptionPane.NO_OPTION);
  }
 
  public static void showMessage(String text, String title, int messageType)
  {
    JOptionPane.showMessageDialog(null,text,title,messageType);
  }
 
  public static void showError(String text, String title)
  {
    showMessage(text,title,JOptionPane.ERROR_MESSAGE);
  }
 
  public static void showWarning(String text, String title)
  {
    showMessage(text,title,JOptionPane.WARNING_MESSAGE);
  }
 
  public static void showInfo(String text, String title)
  {
    showMessage(text,title,JOptionPane.INFORMATION_MESSAGE);
  }
  
}
