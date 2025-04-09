package cea.edyp.epims.transfer.log;

import cea.edyp.epims.transfer.model.BackupParameters;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;
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

  private static final ResourceBundle RSCS = ResourceBundle.getBundle("cea.edyp.epims.transfer.gui.Resources", Locale.getDefault());

  public static final String LOGGER_NAME = "eP-BackLogger";
  private static final Logger logger = LoggerFactory.getLogger(LogTextPanel.class);
  private static final Logger fileLogger = LoggerFactory.getLogger(LOGGER_NAME);

  private FileAppender logFileAppender;
  private File logFile;
  private final BackupParameters parameters;

  public LogTextPanel(BackupParameters params) {
    constructComponents();
    parameters = params;
    parameters.addPropertyChangeListener(this);
    if(params.getLogFile()!=null)
      updateLogFile(params.getLogFile());
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

  private void updateLogFile(File newLogFile) {
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

    if (logFileAppender != null) {
      Object[] args = { logFile.getName() };
      String msg = RSCS.getString("log.end");
      msg = MessageFormat.format(msg, args);
      fileLogger.info("----"+msg);
      lc.getLogger(LOGGER_NAME).detachAppender(logFileAppender);
    }

    logFile = newLogFile;
    PatternLayoutEncoder ple = new PatternLayoutEncoder();
    ple.setContext(lc);
    ple.setPattern("%date{dd/MM/yy HH:mm:ss} %msg%n");
    ple.start();

    logFileAppender = new FileAppender<>();
    logFileAppender.setEncoder(ple);
    logFileAppender.setContext(lc);
    logFileAppender.setFile(logFile.getAbsolutePath());
    logFileAppender.setAppend(true);
    logFileAppender.start();

    lc.getLogger(LOGGER_NAME).addAppender(logFileAppender);
    Object[] args = { logFile.getName() };
    String msg = RSCS.getString("log.start");
    msg = MessageFormat.format(msg, args);
    logger.info(msg);
    fileLogger.info("----"+msg);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getPropertyName().equals(BackupParameters.LOG_FILE_PARAMETER)) {
      updateLogFile(parameters.getLogFile());
    } /*else if (evt.getPropertyName() == BackupParameters.INSTRUMENT_CONFIGURATION_PROPERTY) {
      clearLog();
    }*/
  }
}