package cea.edyp.epims.transfer.task;

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
