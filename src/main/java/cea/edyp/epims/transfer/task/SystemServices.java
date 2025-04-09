package cea.edyp.epims.transfer.task;

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


}
