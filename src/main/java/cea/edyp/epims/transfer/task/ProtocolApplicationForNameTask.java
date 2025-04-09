package cea.edyp.epims.transfer.task;

import fr.edyp.epims.json.ProtocolApplicationJson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

public class ProtocolApplicationForNameTask extends AbstractAuthenticateDatabaseTask {

    private String URL;

    private String m_acqName;
    private String m_instrumentName;

    private ArrayList<ProtocolApplicationJson> m_protocolApplicationJsonList = null;

    public ProtocolApplicationForNameTask(String acqName, String instrumentName) {

        URL = getServerURL() + "/api/protocolapplicationforname/";

        m_acqName = acqName;
        m_instrumentName = instrumentName;


    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            // Send request with GET method, and Headers.
            ResponseEntity<ProtocolApplicationJson[]> response = restTemplate.exchange(URL + m_acqName+"/"+m_instrumentName, //
                    HttpMethod.GET, entity, ProtocolApplicationJson[].class);

            ProtocolApplicationJson[] list = response.getBody();

            m_protocolApplicationJsonList = new ArrayList<>();

            if ((list != null) && (list.length>0)) {
                for (ProtocolApplicationJson s : list) {
                    m_protocolApplicationJsonList.add(s);
                }
            }

        } catch (Exception e) {
            m_error = e.getMessage();
            return false;
        }

        return true;
    }

    public ArrayList<ProtocolApplicationJson> getResult() {
        return m_protocolApplicationJsonList;
    }

}
