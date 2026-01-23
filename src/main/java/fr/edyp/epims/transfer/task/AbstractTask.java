package fr.edyp.epims.transfer.task;


import fr.edyp.epims.transfer.preferences.EPBackPreferences;
import fr.edyp.epims.transfer.preferences.PreferencesKeys;

import java.util.prefs.Preferences;

/**
 * Base class for authentified or non-authentified tasks
 *
 * @author JM235353
 */
public abstract class AbstractTask {

    protected String m_error = null;


    public AbstractTask() {

    }

    public abstract boolean fetchData();


    public String getTaskError() {
        return m_error;
    }


    public static String getServerURL() {
      Preferences preferences = EPBackPreferences.root();
      return preferences.get(PreferencesKeys.CONNECT_SERVER_KEY, PreferencesKeys.DEFAULT_SERVER_URL );
    }
}
