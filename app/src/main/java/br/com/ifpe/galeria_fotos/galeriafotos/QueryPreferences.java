package br.com.ifpe.galeria_fotos.galeriafotos;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by Adrielly, Kaio e Marcela on 19/05/2018.
 */

public class QueryPreferences {
    private static final String PREF_PERQUISA = "searchQuery";

    public static String getStoredQuery(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_PERQUISA, null);
    }

    public static void setStoredQuery(Context context, String consulta) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_PERQUISA, consulta).apply();
    }

}
