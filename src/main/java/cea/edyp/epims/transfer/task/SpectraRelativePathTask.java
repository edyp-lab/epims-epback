package cea.edyp.epims.transfer.task;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class SpectraRelativePathTask extends AbstractAuthenticateDatabaseTask {

    private String URL;

    private String m_path = null;

    public SpectraRelativePathTask() {

        URL = getServerURL()+"/api/spectraRelativePath";

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
