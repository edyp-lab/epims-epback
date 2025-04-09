package cea.edyp.epims.transfer.preferences;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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


        /*private static String getUserHome() {

            String userHome = null;

            String OS = System.getProperty("os.name").toLowerCase();
            if (OS.contains("win"))
            {
                //it is simply the location of the "AppData" folder
                userHome = System.getenv("AppData");
            } else { // linux or Mac

                userHome = System.getProperty("user.home");

                //if we are on a Mac, we are not done, we look for "Application Support"
                if (OS.contains("mac")) {
                    userHome += "/Library/Application Support";
                }
            }

            if (userHome != null) {
                userHome = userHome +File.separator+".epims"+File.separator+"dev"+File.separator+"config";
                Path path = Paths.get(userHome);
                try {
                    Files.createDirectories(path);
                } catch (IOException ie) {
                    //logger.warn("Impossible to create user directory: "+userHome);
                }

            }

            if (userHome == null) {
                // should not happen
                userHome = "./";
            }

            return userHome;
        }*/

    }
