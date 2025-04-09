package cea.edyp.epims.transfer.task;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

/**
 *
 * Base class for tasks which need to do a authentified server request
 *
 * @author JM235353
 *
 */
public abstract class AbstractAuthenticateDatabaseTask extends AbstractTask {

    public AbstractAuthenticateDatabaseTask() {
    }

    public final boolean fetchData() {

        HttpHeaders headers = new HttpHeaders();

        //
        // Authorization string (JWT)
        //
        headers.set("Authorization", TokenManager.getToken());
        //
        headers.setAccept(Arrays.asList(new MediaType[] { MediaType.APPLICATION_JSON }));

        // Request to return JSON format
        headers.setContentType(MediaType.APPLICATION_JSON);


        // HttpEntity<String>: To get result as String.
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        // RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        return fetchSecuredData(entity, restTemplate);
    }


    public abstract boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate);
}
