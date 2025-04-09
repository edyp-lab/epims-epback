package cea.edyp.epims.transfer.log;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;

import static ch.qos.logback.classic.Level.*;

public class JTextPaneAppender extends AppenderBase<ILoggingEvent> {

  private static final Logger mainLogger = LoggerFactory.getLogger(LogTextPanel.LOGGER_NAME);

  private final PatternLayoutEncoder m_encoder;

  private final JTextPane m_textPane;

  SimpleAttributeSet debugStyle;
  SimpleAttributeSet infoStyle;
  SimpleAttributeSet errorStyle;

  public JTextPaneAppender(JTextPane textPane) {

    m_textPane = textPane;
    initializeStyles();

    // set ctx & launch
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    setContext(lc);
    // auto-add
    lc.getLogger(LogTextPanel.LOGGER_NAME).addAppender(this);
    lc.getLogger(LogTextPanel.LOGGER_NAME).setLevel(INFO);

    m_encoder = new PatternLayoutEncoder();
    m_encoder.setContext(lc);
    m_encoder.setPattern("%date{dd/MMM/yyyy HH:mm:ss.SSS} [%thread] %-5level %logger{36} %mdc - %msg%n");
    m_encoder.start();

  }

  private void initializeStyles() {
    debugStyle = new SimpleAttributeSet();
    StyleConstants.setFontFamily(debugStyle, "Arial");
    StyleConstants.setForeground(debugStyle, Color.BLUE);

    infoStyle = new SimpleAttributeSet();
    StyleConstants.setFontFamily(infoStyle, "Arial");
    StyleConstants.setForeground(infoStyle, Color.BLACK);

    errorStyle = new SimpleAttributeSet();
    StyleConstants.setFontFamily(errorStyle, "Arial Black");
    StyleConstants.setForeground(errorStyle, Color.RED);
  }

  @Override
  public void start() {
    super.start();
  }

  @Override
  public void append(ILoggingEvent event) {

    m_encoder.encode(event);

    final String line = event.getMessage();
    SwingUtilities.invokeLater(new Runnable() {
           @Override
           public void run() {

             try {
               if (event.getLevel() == DEBUG) {
                 m_textPane.getDocument().insertString(m_textPane.getDocument().getLength(), line, debugStyle);
                 m_textPane.getDocument().insertString(m_textPane.getDocument().getLength(), "\n", null);
               } else if (event.getLevel() == INFO) {
                 m_textPane.getDocument().insertString(m_textPane.getDocument().getLength(), line, infoStyle);
                 m_textPane.getDocument().insertString(m_textPane.getDocument().getLength(), "\n", null);
               } else if (event.getLevel() == ERROR) {
                 m_textPane.getDocument().insertString(m_textPane.getDocument().getLength(), line, errorStyle);
                 m_textPane.getDocument().insertString(m_textPane.getDocument().getLength(), "\n", null);
               }
             } catch (BadLocationException ex) {
               mainLogger .error("BadLocationException exception!");
             }
           }
         }
    );

  }

}
