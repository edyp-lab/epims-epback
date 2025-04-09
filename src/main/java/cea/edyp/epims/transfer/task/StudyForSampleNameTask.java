package cea.edyp.epims.transfer.task;

import fr.edyp.epims.json.StudyPathJson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class StudyForSampleNameTask extends AbstractAuthenticateDatabaseTask {

    private String URL;

    private String m_sampleName;

    private StudyPathJson m_study = null;

    public StudyForSampleNameTask(String sampleName) {

        URL = getServerURL() + "/api/studypathforsamplename/";

        m_sampleName = sampleName;


    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            // Send request with GET method, and Headers.
            ResponseEntity<StudyPathJson> response = restTemplate.exchange(URL + m_sampleName, //
                    HttpMethod.GET, entity, StudyPathJson.class);

            m_study = response.getBody();

        } catch (Exception e) {
            m_error = e.getMessage();
            return false;
        }

        return true;
    }

    public StudyPathJson getResult() {
        return m_study;
    }

}
