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
