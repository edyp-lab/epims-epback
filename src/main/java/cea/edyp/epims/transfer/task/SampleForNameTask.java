package cea.edyp.epims.transfer.task;

import fr.edyp.epims.json.SampleJson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


public class SampleForNameTask extends AbstractAuthenticateDatabaseTask {

    private String URL;

    private String m_sampleName;

    private SampleJson m_sample = null;

    public SampleForNameTask(String sampleName) {

        URL = getServerURL() + "/api/sampleforname/";

        m_sampleName = sampleName;


    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            // Send request with GET method, and Headers.
            ResponseEntity<SampleJson> response = restTemplate.exchange(URL + m_sampleName, //
                    HttpMethod.GET, entity, SampleJson.class);

            m_sample = response.getBody();

        } catch (Exception e) {
            m_error = e.getMessage();
            return false;
        }

        return true;
    }

    public SampleJson getResult() {
        return m_sample;
    }

}
