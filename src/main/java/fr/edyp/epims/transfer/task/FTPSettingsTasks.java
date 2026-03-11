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
