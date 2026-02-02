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
