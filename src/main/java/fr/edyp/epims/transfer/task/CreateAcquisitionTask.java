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

import fr.edyp.epims.json.AcquisitionFileMessageJson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


public class CreateAcquisitionTask extends AbstractAuthenticateDatabaseTask {

    private final String URL;

    private final AcquisitionFileMessageJson m_acqFileMessage;


    public CreateAcquisitionTask(AcquisitionFileMessageJson acqFileMessage) {

        URL = getServerURL() + "/api/createacquisition";

        m_acqFileMessage = acqFileMessage;


    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {


            HttpEntity<AcquisitionFileMessageJson> requestEntity = new HttpEntity<>(m_acqFileMessage, entity.getHeaders());

            // Send request with POST method, and Headers.
            ResponseEntity<Void> response = restTemplate.exchange(URL,
                    HttpMethod.POST, requestEntity, Void.class);

            HttpStatusCode statusCode = response.getStatusCode();

            if (!statusCode.is2xxSuccessful()) {
                m_error = "Failed for unknown reason";
                return false;
            }


        } catch (Exception e) {
            m_error = e.getMessage();
            return false;
        }

        return true;
    }

}
