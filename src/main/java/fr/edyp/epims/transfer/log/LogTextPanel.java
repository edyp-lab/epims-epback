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
package fr.edyp.epims.transfer.log;

import fr.edyp.epims.transfer.model.BackupParameters;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;


public class LogTextPanel extends JPanel implements PropertyChangeListener {

  private static final ResourceBundle RSCS = ResourceBundle.getBundle("fr.edyp.epims.transfer.gui.Resources", Locale.getDefault());

  public static final String LOGGER_NAME = "eP-BackLogger";
  private static final Logger logger = LoggerFactory.getLogger(LogTextPanel.class);

  private static final Logger fileLogger = LoggerFactory.getLogger(LOGGER_NAME);
  private ServerLogAppender logFileAppender;

  //private File logFile;
  private String currentLoggedInstrumName ;
  private final BackupParameters parameters;

  public LogTextPanel(BackupParameters params) {
    constructComponents();
    parameters = params;
    parameters.addPropertyChangeListener(this);
    if(params.getInstrumentName() !=null)
      updateLogFile(params.getInstrumentName());
  }

  private void constructComponents() {
    // setup the panel's additional components...
    this.setLayout(new BorderLayout());

    JTextPane textPane = new JTextPane();
    textPane.setEditable(false);
    textPane.setText("");
    JScrollPane scrollPane = new JScrollPane(textPane);
    this.add(scrollPane, BorderLayout.CENTER);
    JTextPaneAppender appender = new JTextPaneAppender(textPane);
    appender.start();
  }

  private void updateLogFile(String newInstrumentName) {
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

    if (logFileAppender != null) {
      Object[] args = { currentLoggedInstrumName };
      String msg = RSCS.getString("log.end");
      msg = MessageFormat.format(msg, args);
      fileLogger.info("----"+msg);
      lc.getLogger(LOGGER_NAME).detachAppender(logFileAppender);
    }

    currentLoggedInstrumName = newInstrumentName;
    PatternLayoutEncoder ple = new PatternLayoutEncoder();
    ple.setContext(lc);
    ple.setPattern("%date{dd/MM/yy HH:mm:ss} %msg%n");
    ple.start();

    logFileAppender = new ServerLogAppender();
    logFileAppender.setEncoder(ple);
    logFileAppender.setContext(lc);
    logFileAppender.setInstrumentName(newInstrumentName);
    logFileAppender.start();

    lc.getLogger(LOGGER_NAME).addAppender(logFileAppender);
    Object[] args = { newInstrumentName };
    String msg = RSCS.getString("log.start");
    msg = MessageFormat.format(msg, args);
    logger.info(msg);
    fileLogger.info("----"+msg);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getPropertyName().equals(BackupParameters.LOG_FILE_PARAMETER)) {
      updateLogFile(parameters.getInstrumentName());
    } /*else if (evt.getPropertyName() == BackupParameters.INSTRUMENT_CONFIGURATION_PROPERTY) {
      clearLog();
    }*/
  }
}