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
/*
 * Created on Nov 26, 2004
 *
 * $Id: DFConfiguratorPane.java,v 1.1 2007-09-14 09:37:29 dupierris Exp $
 */
package fr.edyp.epims.transfer.gui;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import fr.edyp.epims.transfer.model.BackupParameters;

/**
 * 
 * 
 * @author CB205360
 */
public class DataFormatConfigurationPane extends JPanel implements PropertyChangeListener {

   private static final ResourceBundle RSCS = ResourceBundle.getBundle("fr.edyp.epims.transfer.gui.Resources", Locale.getDefault());

   private final BackupParameters parameters;

   public DataFormatConfigurationPane(BackupParameters params) {
      setLayout(new BorderLayout());
      parameters = params;
      createPanel();
      parameters.addPropertyChangeListener(BackupParameters.DATA_FORMAT_PARAMETER, this);      
   }

   public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals(BackupParameters.DATA_FORMAT_PARAMETER)) {
         this.removeAll();
         createPanel();            
      }
   }
   
   private void createPanel(){     
     Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
     String title = RSCS.getString("dataformat.panel.title");
     TitledBorder tBorder = BorderFactory.createTitledBorder(loweredetched, title);
     tBorder.setTitleJustification(TitledBorder.RIGHT);

     this.add(parameters.getDataFormat().getConfigurator(), BorderLayout.CENTER);
     this.setBorder(tBorder);
   }
    
}
