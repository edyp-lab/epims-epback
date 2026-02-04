package fr.edyp.epims.transfer.util;

import fr.edyp.epims.transfer.model.Analysis;
import fr.edyp.epims.transfer.model.BackupException;


import java.io.File;
import java.io.IOException;

public class FTPFileTransferManager extends DefaultFileTransferManager {

  private FTPConnectManager m_ftpConnection;

  public FTPFileTransferManager()  {
    super();
    m_ftpConnection = new FTPConnectManager();
  }

  @Override
  protected String getDestinationPath(Analysis a, IEPSystemDataProvider ePimsDataProvider) throws IOException {
    String path =  a.getRelativeDestination();
    return path+"/"+a.getFileName();
  }

  @Override
  protected boolean destinationExist(String destPath) {
    try {
      return m_ftpConnection.fileExist(destPath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected String getAssociatedDataDestinationPath(Analysis a, File f, String fileType, IEPSystemDataProvider ePimsDataProvider) throws IOException {
    try {
      String path = ePimsDataProvider.getRelativeAssociatedFileDestinationDir(a, f, fileType);
      return path+"/"+f.getName();
    } catch (BackupException e) {
      throw new RuntimeException(e);
    }

  }

  protected void doAnalysisCopy(Analysis a, String destPath) throws IOException {
    m_ftpConnection.upload(a.getFile(), destPath);
  }

  protected void doAssociatedDataCopy(File src, String destPath) throws IOException {
    m_ftpConnection.upload(src, destPath);
  }

  protected void afterCopy() {
    m_ftpConnection.closeConnection();
  }

}
