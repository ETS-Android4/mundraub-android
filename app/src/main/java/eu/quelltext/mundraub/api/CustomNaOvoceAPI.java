package eu.quelltext.mundraub.api;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.common.Settings;

public class CustomNaOvoceAPI extends NaOvoceAPI {


    private final String host;
    private final boolean downloadMarkers;

    public CustomNaOvoceAPI(String host, boolean downloadMarkers) {
        this.host = host;
        this.downloadMarkers = downloadMarkers;
    }

    @Override
    protected String host() {
        return host;
    }

    @Override
    public String id() {
        return Settings.API_ID_MY_NA_OVOCE;
    }

    @Override
    public int nameResourceId() {
        return R.string.login_api_name_my_na_ovoce;
    }

    @Override
    public boolean isCustomNaOvoceAPI() {
        return true;
    }

    @Override
    public int radioButtonId() {
        return R.id.radioButton_my_na_ovoce;
    }

    @Override
    public boolean wantsToProvideMarkers() {
        return downloadMarkers;
    }
}
