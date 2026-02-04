package fr.edyp.epims.transfer.task;

import fr.edyp.epims.json.FtpConfigurationJson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class FTPSettingsTasks extends AbstractAuthenticateDatabaseTask {

  private String URL;

  private FtpConfigurationJson m_ftpConfigurationJson;


  public FTPSettingsTasks() {
    super();
    URL = getServerURL() + "/api/ftpsettings";
  }

  @Override
  public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {
    try {

      // Send request with GET method, and Headers.
      ResponseEntity<FtpConfigurationJson> response = restTemplate.exchange(URL, //
              HttpMethod.GET, entity, FtpConfigurationJson.class);

      m_ftpConfigurationJson = response.getBody();

    } catch (Exception e) {
      m_error  = e.getMessage();
      return false;
    }

    return true;
  }

  public FtpConfigurationJson getResult() {
    return m_ftpConfigurationJson;
  }
}
