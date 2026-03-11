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
    m_ftpConnection.upload(a.getFileToTransfer(), destPath);
  }

  protected void doAssociatedDataCopy(File src, String destPath) throws IOException {
    m_ftpConnection.upload(src, destPath);
  }

  protected void afterCopy() {
    m_ftpConnection.closeConnection();
  }

}
