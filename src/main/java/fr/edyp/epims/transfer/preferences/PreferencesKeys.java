/*
 * Copyright (C) 2021
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program;
 * If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.edyp.epims.transfer.preferences;

public class PreferencesKeys {

    public final static String CONNECT_SERVER_KEY = "webservices.url";
    public final static String SERVER_USER_KEY = "epims.user";
    public final static String SERVER_PSWD_KEY = "epims.pswd";
    public final static String SERVER_ROOT_KEY = "epims.root";

    public final static String DEFAULT_SERVER_URL = "http://localhost:8080";

    public final static String TRANSFER_MODE = "transfer.mode";

    //FTP Keys
    public final static String FTP_HOST = "ftp.host";
    public final static String FTP_PORT = "ftp.port";
    public final static String FTP_LOGIN = "ftp.login";
    public final static String FTP_AUTHENTICATE_MODE = "ftp.auth.mode";
    public final static String FTP_PSWD = "ftp.password";
    public final static String FTP_KEY_PATH = "ftp.keyPath";

    public final static String DEFAULT_FTP_KEY_PATH = "./conf/epims-id_rsa";
    public final static String FTP_PASSWORD_AUTH_MODE = "PASSWD_MODE";
    public final static String FTP_KEY_AUTH_MODE = "KEY_MODE";
    public final static String DIRECT_TRANSFER_MODE = "direct";
    public final static String DEFAULT_TRANSFER_MODE = DIRECT_TRANSFER_MODE;

}
