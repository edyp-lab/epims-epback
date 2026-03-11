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
package fr.edyp.epims.transfer.task;


import fr.edyp.epims.json.*;
import fr.edyp.epims.json.AcquisitionFileMessageJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;


public class AcquisitionServices {

    private static final ResourceBundle RSCS = ResourceBundle.getBundle("fr.edyp.epims.transfer.gui.Resources", Locale.getDefault());
    private static final Logger logger = LoggerFactory.getLogger(AcquisitionServices.class);


    public static SampleJson getSampleJson(String sampleName) {

        SampleForNameTask task = new SampleForNameTask(sampleName);
        if (!task.fetchData()) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "Server Error", "SampleForNameTask: " + task.getTaskError(), JOptionPane.ERROR_MESSAGE);
                }
            });

            return null;
        }

        return task.getResult();
    }



    public static StudyPathJson getStudyPathJson(String sampleName) {
        StudyForSampleNameTask task = new StudyForSampleNameTask(sampleName);
        if (!task.fetchData()) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "Server Error", "StudyForSampleNameTask: " + task.getTaskError(), JOptionPane.ERROR_MESSAGE);
                }
            });

            return null;
        }

        return task.getResult();
    }

    public static InstrumentJson getInstrumentJson(String instrumentName) {
        InstrumentForNameTask task = new InstrumentForNameTask(instrumentName);
        if (!task.fetchData()) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "Server Error", "InstrumentForNameTask: " + task.getTaskError(), JOptionPane.ERROR_MESSAGE);
                }
            });

            return null;
        }

        return task.getResult();
    }

    public static ProtocolApplicationJson getAcquisitionsDescriptors(String acqName, String instrumentName) throws Exception {
        ProtocolApplicationForNameTask task = new ProtocolApplicationForNameTask(acqName, instrumentName);
        if (!task.fetchData()) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "Server Error", "ProtocolApplicationForNameTask: " + task.getTaskError(), JOptionPane.ERROR_MESSAGE);
                }
            });

            return null;
        }

        List<ProtocolApplicationJson> list = task.getResult();

        // we should have a list with one or no ProtocolApplicationJson
        if (list.size() == 1) {
            return list.get(0);
        } else if (list.size()>1) {
            throw new Exception("Found several ProtocolApplication for "+acqName+" "+instrumentName);
        }

        return null;

    }

    public static String getAcquisitionDestinationPath(AcquisitionFileMessageJson acqContext) {
        AcquisitionDestinationPathTask task = new AcquisitionDestinationPathTask(acqContext);
        if (!task.fetchData()) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    // this problem can happen
                    //JOptionPane.showMessageDialog(null, "Server Error", "AcquisitionDestinationPathTask: " + task.getTaskError(), JOptionPane.ERROR_MESSAGE);
                    logger.warn("AcquisitionDestinationPathTask : path not found");
                }
            });

            return null;
        }

      return task.getResult();

    }

    public static boolean createAcquisition(AcquisitionFileMessageJson acqFileMessage) {
        if(! acqMsgValidation(acqFileMessage)) {
            return false;
        }

        CreateAcquisitionTask task = new CreateAcquisitionTask(acqFileMessage);
        if (!task.fetchData()) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "Server Error", "CreateAcquisitionTask: " + task.getTaskError(), JOptionPane.ERROR_MESSAGE);
                }
            });

            return false;
        }

        return true;

    }
    private static boolean acqMsgValidation(AcquisitionFileMessageJson acqMsg){
        ProtocolApplicationJson protocolApplicationJson = acqMsg.getAcquisitionFileDescriptor().getAcquisition();
        AcquisitionJson acquisition = protocolApplicationJson.getAcquisitionJson();
        boolean isError = false;
        if ((acquisition.getNature() != null) && acquisition.getNatureAsEnum().equals(AcquisitionJson.Nature.RESEARCH)) {
            isError = (acqMsg.getSampleDescriptor() == null);
        }

        return !isError && protocolApplicationJson != null
                && protocolApplicationJson.getName() != null
                && !protocolApplicationJson.getName().equals("")
                && protocolApplicationJson.getDate() != null
                && acquisition.getNature() != null
                && ((acquisition.getInstrumentId() != null) || (acquisition.getInstrumentName() != null));

    }


}
