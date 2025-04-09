package cea.edyp.epims.transfer.task;

import fr.edyp.epims.json.InstrumentJson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class InstrumentForNameTask extends AbstractAuthenticateDatabaseTask {

    private String URL;

    private String m_instrumentName;

    private InstrumentJson m_instrument = null;

    public InstrumentForNameTask(String instrumentName) {

        URL = getServerURL() + "/api/instrumentforname/";

        m_instrumentName = instrumentName;


    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            // Send request with GET method, and Headers.
            ResponseEntity<InstrumentJson> response = restTemplate.exchange(URL + m_instrumentName, //
                    HttpMethod.GET, entity, InstrumentJson.class);

            m_instrument = response.getBody();

        } catch (Exception e) {
            m_error = e.getMessage();
            return false;
        }

        return true;
    }

    public InstrumentJson getResult() {
        return m_instrument;
    }

}
