package cea.edyp.epims.transfer.task;

import fr.edyp.epims.json.AcquisitionFileMessageJson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class AcquisitionDestinationPathTask  extends AbstractAuthenticateDatabaseTask {

    private String URL;

    private AcquisitionFileMessageJson m_acqContext;
    private String m_path = null;

    public AcquisitionDestinationPathTask(AcquisitionFileMessageJson acqContext) {

        URL = getServerURL() + "/api/acquisitiondestpath";

        m_acqContext = acqContext;


    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            HttpEntity<AcquisitionFileMessageJson> requestEntity = new HttpEntity<>(m_acqContext, entity.getHeaders());

            // Send request with GET method, and Headers.
            ResponseEntity<String> response = restTemplate.exchange(URL, //
                    HttpMethod.POST, requestEntity, String.class);

            m_path = response.getBody();


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
