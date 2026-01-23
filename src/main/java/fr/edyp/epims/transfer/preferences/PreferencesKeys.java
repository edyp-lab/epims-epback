package fr.edyp.epims.transfer.preferences;

public class PreferencesKeys {

    public final static String CONNECT_SERVER_KEY = "webservices.url";
    public final static String SERVER_USER_KEY = "epims.user";
    public final static String SERVER_PSWD_KEY = "epims.pswd";

    public final static String DEFAULT_SERVER_URL = "http://localhost:8080";

    public final static String TRANSFER_MODE = "transfer.mode";

    //FTP Keys
    public final static String FTP_HOST = "ftp.host";
    public final static String FTP_PORT = "ftp.port";
    public final static String FTP_LOGIN = "ftp.login";
    public final static String FTP_AUTHENTICATE_MODE = "ftp.auth.mode";
    public final static String FTP_PSWD = "ftp.password";
    public final static String FTP_KEY_PATH = "ftp.keyPath";

    public final static String DEFAULT_FTP_HOST = "localhost";
    public final static Integer DEFAULT_FTP_PORT = 22;
    public final static String DEFAULT_FTP_KEY_PATH = "./conf/epims-id_rsa";
    public final static String FTP_PASSWORD_AUTH_MODE = "PASSWD_MODE";
    public final static String FTP_KEY_AUTH_MODE = "KEY_MODE";
    public final static String DEFAULT_TRANSFER_MODE = "direct";

}
