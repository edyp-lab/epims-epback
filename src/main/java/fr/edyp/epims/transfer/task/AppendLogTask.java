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

import fr.edyp.epims.json.InstrumentLogInfoJson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class AppendLogTask extends AbstractAuthenticateDatabaseTask {

    private final String URL;
    private final InstrumentLogInfoJson m_logInfo;

    public AppendLogTask(InstrumentLogInfoJson logInfo) {
        URL = getServerURL() + "/api/updateServerLogFile";
        m_logInfo = logInfo;
    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {
        try {
            HttpEntity<InstrumentLogInfoJson> requestEntity = new HttpEntity<>(m_logInfo, entity.getHeaders());

            ResponseEntity<String> response = restTemplate.exchange(URL,
                    HttpMethod.POST, requestEntity, String.class);

            HttpStatusCode statusCode = response.getStatusCode();

            if (!statusCode.is2xxSuccessful()) {
                m_error = "Failed to update server log file: " + statusCode;
                return false;
            }
        } catch (Exception e) {
            m_error = e.getMessage();
            return false;
        }

        return true;
    }
}
