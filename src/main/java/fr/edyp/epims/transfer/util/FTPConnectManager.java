package fr.edyp.epims.transfer.util;

import fr.edyp.epims.json.FtpConfigurationJson;
import fr.edyp.epims.transfer.preferences.EPBackPreferences;
import fr.edyp.epims.transfer.preferences.PreferencesKeys;
import fr.edyp.epims.transfer.task.SystemServices;
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

  private String host;
  private int port;
  private String login;
  private String passwd;
  private String ftpRootPath;

  private Preferences preferences;
  FtpConfigurationJson ftpConfigurationJson;


  public FTPConnectManager() {
    initFTPSettings();
  }

  private void initFTPSettings() {

    ftpConfigurationJson  = SystemServices.getFTPSettings();
    preferences = EPBackPreferences.root();

    if(ftpConfigurationJson == null) {
      logger.warn("Unable to load FTP settings from Server. Use preference specified values");
      ftpConfigurationJson = new FtpConfigurationJson();
      ftpConfigurationJson.setPort(22); //set as default value
    }

    host = preferences.get(PreferencesKeys.FTP_HOST,ftpConfigurationJson.getHost());
    port = preferences.getInt(PreferencesKeys.FTP_PORT,ftpConfigurationJson.getPort());
    login = preferences.get(PreferencesKeys.FTP_LOGIN,ftpConfigurationJson.getLogin());
    passwd = preferences.get(PreferencesKeys.FTP_PSWD,ftpConfigurationJson.getPassword());
    ftpRootPath = ftpConfigurationJson.getStartPath();
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

  private boolean isNotConnected() {
    return m_ftpClient == null || !m_sshClient.isConnected();
  }

  private boolean connectFTPClient() {

    logger.info("Connect to FTP server {}:{}",host,port);
    m_sshClient = null;
    m_ftpClient = null;
    try {
      m_sshClient = new SSHClient();
      m_sshClient.addHostKeyVerifier(new PromiscuousVerifier()); // Do not check who is behind the server.
      m_sshClient.connect(host,port);
      String authMode = preferences.get(PreferencesKeys.FTP_AUTHENTICATE_MODE, PreferencesKeys.FTP_PASSWORD_AUTH_MODE);
      if(authMode.equals(PreferencesKeys.FTP_PASSWORD_AUTH_MODE)) {
        m_sshClient.authPassword(login, passwd);
      } else {
        String keyPath = preferences.get(PreferencesKeys.FTP_KEY_PATH,PreferencesKeys.DEFAULT_FTP_KEY_PATH);
        m_sshClient.authPublickey(login, keyPath);
      }
      m_ftpClient = m_sshClient.newSFTPClient();
      logger.debug("SFTP -- Created SFTP Client ");
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      closeConnection();
      return false;
    }

  }

  public void upload(File sourceFile, String destination) throws IOException {
    try {
      if(isNotConnected()) {
        boolean result = connectFTPClient();
        if(!result) {
          throw new IOException("Unable to connect to FTP server");
        }
      }
      String finalDestPath = ftpRootPath +"/"+ destination;
      m_ftpClient.getFileTransfer().upload(sourceFile.getAbsolutePath(), finalDestPath);
    } catch (Exception e) {
      closeConnection();
      throw new IOException("Unable to upload file "+sourceFile.getAbsolutePath()+" to "+destination,e);
    }
  }

  public boolean fileExist(String destination) throws IOException {
    try {
      if(isNotConnected()) {
        boolean result = connectFTPClient();
        if(!result) {
          throw new IOException("Unable to connect to FTP server");
        }
      }
      String finalDestPath = ftpRootPath +"/"+ destination;
      m_ftpClient.stat(finalDestPath);
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
