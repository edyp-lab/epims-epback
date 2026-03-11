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
