/*
 * Created on Nov 30, 2004
 *
 * $Id: AnalysisOperationAction.java,v 1.1 2007-09-14 09:37:29 dupierris Exp $
 */
package cea.edyp.epims.transfer.gui;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import javax.swing.*;

import cea.edyp.epims.transfer.log.LogTextPanel;
import cea.edyp.epims.transfer.model.BackupException;
import org.slf4j.Logger;

import cea.edyp.epims.transfer.model.Analysis;
import cea.edyp.epims.transfer.model.AnalysisOperationMgr;
import cea.edyp.epims.transfer.model.Backup;
import cea.edyp.epims.transfer.model.BackupParameters;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author CB205360
 */
public class AnalysisOperationAction extends AbstractAction {

   private static final Logger logger = LoggerFactory.getLogger(AnalysisOperationAction.class);
   private static final Logger panelLogger = LoggerFactory.getLogger(LogTextPanel.LOGGER_NAME);
   
   private static final ResourceBundle RSCS = ResourceBundle.getBundle("cea.edyp.epims.transfer.gui.Resources", Locale.getDefault());

   private boolean taskInterrupted;
   private boolean running;
   private final Backup backup;
   private final JProgressBar bar;

   public AnalysisOperationAction(Backup bck) {
      backup = bck;
      running = false;
      setTaskInterrupted(false);
      putValue(Action.NAME, RSCS.getString("action.operation.start.label"));
      bar = new JProgressBar();
      bar.setStringPainted(true);
   }

   public void actionPerformed(ActionEvent event) {
      if (!running) {
         running = true;
         backup.transferBegin();
         putValue(Action.NAME, RSCS.getString("action.operation.stop.label"));
         BackupParameters params = backup.getBackupParameters();
         Analysis[] files = backup.getAnalysisToBackup();
         if(files.length == 0){
            // Aucune analyse n'est selectionnee
           updateProgressBar(0,0,RSCS.getString("process.error.no.selection"));
           resetBackupAction(null);
           return;
         }

         panelLogger.info("Traitement de " + files.length + " analyses depuis " + params.getSourcePath());

         try {
            backupFilesAsync(files, params);
         } catch (Exception e) {
            panelLogger.error(RSCS.getString("process.error"), e);
         }
      } else {
         logger.info(RSCS.getString("process.stop"));
         putValue(Action.NAME, RSCS.getString("action.operation.interrupt.label"));
         setEnabled(false);
         setTaskInterrupted(true);
         backup.transferAbort(null);
      }
   }

   private void resetBackupAction(List<Analysis> analysisList){
     setTaskInterrupted(false);
     running = false;
     putValue(Action.NAME, RSCS.getString("action.operation.start.label"));
     setEnabled(true);
     backup.transferEnd(analysisList);
   }


   private void backupFilesAsync(final Analysis[] analysis, final BackupParameters params) {

      SwingWorker<List<Analysis>, Analysis> sw = new SwingWorker<>() {

        @Override
        protected List<Analysis> doInBackground() {
          try {

            AnalysisOperationMgr operationMgr = new AnalysisOperationMgr(params);
            List<Analysis> processedAnalysis = new ArrayList<>();
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < analysis.length; i++) {
              int finalI = i;
              try {
                operationMgr.transfer(analysis[i]);
                publish(analysis[i]);
              } catch (BackupException be) {
                logger.debug(" Exception occurred {}", be.getMessage());
                SwingUtilities.invokeLater(() -> {
                  buffer.append(analysis[finalI]).append(" : ").append( RSCS.getString("process.stop"));
                  updateProgressBar(finalI, analysis.length, buffer.toString());
                  panelLogger.error(buffer+" : "+ be.getMessage());
                });
                break;
              }

              if (isTaskInterrupted()) {
                 SwingUtilities.invokeLater(() -> {
                    buffer.append(RSCS.getString("process.stop"));
                    updateProgressBar(finalI, analysis.length, RSCS.getString("process.stop"));
                    panelLogger.info(buffer.toString());
                 });
                 break;
              }
              processedAnalysis.add(analysis[i]);
            }
            return processedAnalysis;
          } catch (Exception e) {
            logger.error(" Exception occurred during backup: {} ", e.getMessage());
            return null;
          }
        }


        @Override
        protected void process(List<Analysis> chunks) {
          logger.info(" ... some ({}) analysis processed... ", chunks.size());
          StringBuilder buffer = new StringBuilder();
          for (int i = 0; i < chunks.size(); i++) {
            buffer.setLength(0);
            buffer.append("Traitement de ").append(chunks.get(i).getName()).append("...");
            logger.info("{}", buffer);
            updateProgressBar(i, analysis.length, buffer.toString());
          }
        }

        @Override
        protected void done() {
          logger.info(" Analyses process finished");
          try {
            int c = get().size();
            if (c < analysis.length) {
              if (c > 0)
                panelLogger.info(RSCS.getString("process.error.stop.after")+analysis[c]+" ("+(c+1)+"/"+analysis.length+")");
              else
                panelLogger.info(RSCS.getString("process.error.stop.during")+analysis[0]+" ("+(0)+"/"+analysis.length+")");
            } else {
              panelLogger.info(RSCS.getString("process.end")+" "+analysis.length+" analyses");
              updateProgressBar(0, 0, RSCS.getString("process.end.short"));
            }

            resetBackupAction(get());
          } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
          }
         }
      };
      //End SwingWorker
      sw.execute();

   }//End backupFiles

   private void updateProgressBar(final int index, final int max, final String string) {
      SwingUtilities.invokeLater(() -> {
         bar.setMaximum(max);
         bar.setValue(index);
         bar.setString(string);
      });
   }

   public JProgressBar getProgressBar() {
      return bar;
   }

   public synchronized boolean isTaskInterrupted() {
      return taskInterrupted;
   }

   public synchronized void setTaskInterrupted(boolean value) {
      taskInterrupted = value;
   }
}
