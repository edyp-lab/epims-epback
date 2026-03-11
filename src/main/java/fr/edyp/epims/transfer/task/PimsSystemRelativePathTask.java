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

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class PimsSystemRelativePathTask extends AbstractAuthenticateDatabaseTask {

    private String URL;

    private String m_path = null;

    public PimsSystemRelativePathTask() {

        URL = getServerURL()+"/api/pimsSystemRelativePath";

    }


    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            // Send request with GET method, and Headers.
            ResponseEntity<String> responseEntity = restTemplate.exchange(URL, //
                    HttpMethod.GET, entity, String.class);

            m_path = responseEntity.getBody();
            HttpHeaders headers = responseEntity.getHeaders();

        } catch (Exception e) {
            m_error = e.getMessage();
            return false;
        }

        return true;
    }

    public String getResult() {
        return m_path;
    }

}
