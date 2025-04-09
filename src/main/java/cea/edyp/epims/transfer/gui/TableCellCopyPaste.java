/*
 * Created on Dec 8, 2004
 *
 * $Id: TableCellCopyPaste.java,v 1.1 2007-09-14 09:37:29 dupierris Exp $
 */
package cea.edyp.epims.transfer.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author CB205360
 */
public class TableCellCopyPaste implements ActionListener {

   private static final Logger logger = LoggerFactory.getLogger(TableCellCopyPaste.class);
   
   private static final String COPY_ACTION = "Copy";
   private static final String PASTE_ACTION = "Paste";
   
   private Object copiedValue;
   private Class<?> copiedColType;      
   
   private JTable table;
   
   public TableCellCopyPaste(JTable aTable) {
     table = aTable;
     KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.CTRL_MASK,false);
     KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V,ActionEvent.CTRL_MASK,false);
     table.registerKeyboardAction(this,COPY_ACTION,copy,JComponent.WHEN_FOCUSED);
     table.registerKeyboardAction(this,PASTE_ACTION,paste,JComponent.WHEN_FOCUSED);
     copiedValue = null;
   }


   public void actionPerformed(ActionEvent e) {     
      
      if(COPY_ACTION.equals(e.getActionCommand())){        
        int col = table.getSelectedColumn();
        int row = table.getSelectedRow();
        if(col == -1 || row == -1){
          return;
        }
        copiedValue = table.getValueAt(row, col);
        copiedColType = table.getColumnClass(col);
        
        
      }else if(PASTE_ACTION.equals(e.getActionCommand())){        
        int[] cols = table.getSelectedColumns();
        int[] rows = table.getSelectedRows();
        if(copiedValue == null){
          return;
        }
                
        for (int c = 0; c < cols.length; c++) {
          Class<?> columnClass = table.getColumnClass(cols[c]);
          boolean uncompatibleCols = false;
          if(!columnClass.isAssignableFrom(copiedColType)) {
            uncompatibleCols = true;
            logger.debug(" Uncompatible column type "+copiedColType+" / "+columnClass);
            if(!columnClass.isAssignableFrom(String.class))
              continue;
          }
          
          for (int r = 0; r < rows.length; r++) {
            Object toCopy = copiedValue;
            if(uncompatibleCols)
              toCopy = copiedValue.toString(); 

            table.setValueAt(toCopy, rows[r], cols[c]);
          }
        }
        
      }
   }

}
