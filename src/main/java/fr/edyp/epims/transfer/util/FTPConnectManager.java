package fr.edyp.epims.transfer.util;

import fr.edyp.epims.transfer.preferences.EPBackPreferences;
import fr.edyp.epims.transfer.preferences.PreferencesKeys;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

public class FTPConnectManager {
  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(FTPConnectManager.class);

  private SFTPClient m_ftpClient = null;
  private SSHClient m_sshClient = null;

  public FTPConnectManager() {

  }


  public void closeConnection() {
    if(m_ftpClient != null){
      try {
        m_ftpClient.close();
        logger.debug("SFTP -- SFTP Client closed");
      } catch (IOException e) {
        logger.error("Unable to close SFTP Client" ,e);
      }
    }
    if(m_sshClient != null){
      try {
        m_sshClient.close();
        logger.debug("SFTP -- SSH Client closed");
      } catch (IOException e) {
        logger.error("Unable to close SSH Client" ,e);
      }
    }
    m_sshClient = null;
    m_ftpClient = null;
  }

  private boolean isConnected() {
    return m_ftpClient != null && m_sshClient.isConnected();
  }

  private boolean connectFTPClient() {
    Preferences preferences = EPBackPreferences.root();
    String host = preferences.get(PreferencesKeys.FTP_HOST,PreferencesKeys.DEFAULT_FTP_HOST);
    int port = preferences.getInt(PreferencesKeys.FTP_PORT,PreferencesKeys.DEFAULT_FTP_PORT);
    logger.info("Connect to FTP server {}:{}",host,port);
    m_sshClient = null;
    m_ftpClient = null;
    try {
      m_sshClient = new SSHClient();
      m_sshClient.addHostKeyVerifier(new PromiscuousVerifier()); // Do not check who is behind the server.
      m_sshClient.connect(host,port);
      String authMode = preferences.get(PreferencesKeys.FTP_AUTHENTICATE_MODE, PreferencesKeys.FTP_PASSWORD_AUTH_MODE);
      String login = preferences.get(PreferencesKeys.FTP_LOGIN,"");
      if(authMode.equals(PreferencesKeys.FTP_PASSWORD_AUTH_MODE)) {
        String psswd = preferences.get(PreferencesKeys.FTP_PSWD,"");
        m_sshClient.authPassword(login, psswd);
      } else {
        String keyPath =  preferences.get(PreferencesKeys.FTP_KEY_PATH,PreferencesKeys.DEFAULT_FTP_KEY_PATH);
        m_sshClient.authPublickey(login, keyPath);
      }
      m_ftpClient = m_sshClient.newSFTPClient();
      logger.debug("SFTP -- Created SFTP Client ");
      return true;
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      closeConnection();
      return false;
    }

  }

  public void download(File sourceFile, String destination) throws IOException {
    try {
      if(!isConnected()) {
        boolean result = connectFTPClient();
        if(!result) {
          throw new IOException("Unable to connect to FTP server");
        }
      }
      m_ftpClient.getFileTransfer().download(sourceFile.getAbsolutePath(), destination);
    } catch (Exception e) {
      closeConnection();
      throw new IOException("Unable to download file "+sourceFile.getAbsolutePath()+" to "+destination,e);
    }
  }

  public boolean fileExist(String destination) throws IOException {
    try {
      if(!isConnected()) {
        boolean result = connectFTPClient();
        if(!result) {
          throw new IOException("Unable to connect to FTP server");
        }
      }
      m_ftpClient.stat(destination);
      return true;
    } catch (net.schmizz.sshj.sftp.SFTPException e) {
      if (e.getStatusCode() == net.schmizz.sshj.sftp.Response.StatusCode.NO_SUCH_FILE) {
        return false; // File doesn't exist
      }
      closeConnection();
      throw new IOException("Unable to test ftp file "+destination, e);
    } catch (Exception e) {
      closeConnection();
      throw new IOException("Unable to test ftp file "+destination, e);
    }
  }

}
