package cea.edyp.epims.transfer.task;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class ConnectTask extends AbstractTask {

        private String URL;

        private String m_login;
        private String m_password;

        public ConnectTask(String login, String password) {

            URL = getServerURL()+"/login";

            m_login = login;
            m_password = password;

        }

        @Override
        public boolean fetchData() {

            HttpHeaders headers = new HttpHeaders();

            MultiValueMap<String, String> parametersMap = new LinkedMultiValueMap<>();
            parametersMap.add("username", m_login);
            parametersMap.add("password", m_password);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parametersMap, headers);

            RestTemplate restTemplate = new RestTemplate();

            try {

                ResponseEntity<String> response =  restTemplate.exchange(URL, HttpMethod.POST, requestEntity, String.class);

                HttpHeaders responseHeaders = response.getHeaders();
                List<String> list = responseHeaders.get("Authorization");

                TokenManager.setToken(list.get(0));


            } catch (Exception e) {
                m_error = e.getMessage();
                return false;
            }

            return true;
        }

    }
