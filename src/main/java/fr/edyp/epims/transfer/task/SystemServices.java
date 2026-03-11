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

import fr.edyp.epims.json.FtpConfigurationJson;

import javax.swing.*;

public class SystemServices {


    public static String getPimsSystemRelativePathTask() {

        PimsSystemRelativePathTask task = new PimsSystemRelativePathTask();
        if (!task.fetchData()) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "Server Error", "PimsSystemRelativePathTask: " + task.getTaskError(), JOptionPane.ERROR_MESSAGE);
                }
            });

            return null;
        }

        return task.getResult();
    }

    public static String getSpectraRelativePath() {

        SpectraRelativePathTask task = new SpectraRelativePathTask();
        if (!task.fetchData()) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "Server Error", "SpectraRelativePathTask: " + task.getTaskError(), JOptionPane.ERROR_MESSAGE);
                }
            });

            return null;
        }

        return task.getResult();
    }

    public static FtpConfigurationJson getFTPSettings() {

        FTPSettingsTasks task = new FTPSettingsTasks();
        if (!task.fetchData()) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "Server Error", "FTPSettingsTasks: " + task.getTaskError(), JOptionPane.ERROR_MESSAGE);
                }
            });

            return null;
        }

        return task.getResult();
    }


}
