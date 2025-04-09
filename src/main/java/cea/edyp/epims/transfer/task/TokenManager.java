package cea.edyp.epims.transfer.task;

/**
 *
 * Authentification Token received from the server when the user logs.
 * This token is used each time an authentification request is done
 *
 * @author JM235353
 *
 */
public class TokenManager {

    private static String m_token = null;

    public static void setToken(String token) {
        m_token = token;
    }

    public static String getToken() {
        return m_token;
    }
}
