/*
 * Copyright (C) 2021
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program;
 * If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.edyp.epims.transfer.gui;

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
