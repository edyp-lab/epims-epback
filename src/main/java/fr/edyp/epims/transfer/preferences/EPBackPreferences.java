package fr.edyp.epims.transfer.preferences;

import java.io.File;

public class EPBackPreferences {

        private static FilePreferences m_preferences;

        public static java.util.prefs.Preferences root() {

            if (m_preferences == null) {
                initPreferences(null);
            }

            return m_preferences;
        }

        public static void initPreferences(String path) {

            if (path == null) {
                path = "./conf/eP-Back.properties";
            } else {
                path = path+File.separator+"eP-Back.properties";
            }

            m_preferences = new FilePreferences(new File(path), null, "");
        }

    }
