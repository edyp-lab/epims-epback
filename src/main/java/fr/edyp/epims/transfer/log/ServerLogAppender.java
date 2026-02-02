package fr.edyp.epims.transfer.log;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import fr.edyp.epims.json.InstrumentLogInfoJson;
import fr.edyp.epims.transfer.task.AppendLogTask;

public class ServerLogAppender extends AppenderBase<ILoggingEvent> {

    private String instrumentName;
    private PatternLayoutEncoder encoder;

    public ServerLogAppender() {
    }

    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    public void setEncoder(PatternLayoutEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (!isStarted()) {
            return;
        }

        if (instrumentName == null || instrumentName.isEmpty()) {
            return;
        }

        String logData = encoder.getLayout().doLayout(eventObject);

        InstrumentLogInfoJson logInfo = new InstrumentLogInfoJson();
        logInfo.setInstrumentName(instrumentName);
        logInfo.setLogData(logData);

        AppendLogTask task = new AppendLogTask(logInfo);
        boolean success = task.fetchData();
        if (!success) {
            addError("Failed to send log to server: " + task.getTaskError());
        }
    }
}
