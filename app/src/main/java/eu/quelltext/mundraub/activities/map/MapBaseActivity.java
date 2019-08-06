package eu.quelltext.mundraub.activities.map;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.io.IOException;

import eu.quelltext.mundraub.activities.AddressSearchActivity;
import eu.quelltext.mundraub.activities.WebViewBaseActivity;
import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.map.MapUrl;
import eu.quelltext.mundraub.map.MundraubProxy;
import eu.quelltext.mundraub.plant.Plant;

public class MapBaseActivity extends WebViewBaseActivity {

    protected static String ARG_MAP_URL = "map-url";
    protected static int REQUEST_CODE_ADDRESS_SEARCH = 1;

    private MundraubProxy apiProxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiProxy = Settings.getMundraubMapProxy();
        Settings.onChange(new Settings.ChangeListener() {
            @Override
            public int settingsChanged() {
                apiProxy.stop();
                apiProxy = Settings.getMundraubMapProxy();
                return SETTINGS_CAN_CHANGE;
            }
        });
        getPermissions().INTERNET.askIfNotGranted();
    }

    protected boolean openInAppUrl(String url) {
        // this method handles the other side of appInteraction.js
        if (super.openInAppUrl(url)) {
            return true;
        }
        if (url.equals("app://gps")) {
            openMapAtGPSPosition();
            return true;
        }
        return false;
    }
    @Override
    protected void onResume() {
        super.onResume();
        try {
            apiProxy.start();
        } catch (IOException e) {
            log.printStackTrace(e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        apiProxy.stop();
    }

    protected void openMapAtPosition(Plant.Position position) {
        openMapAtPosition(position.getLongitude(), position.getLatitude());
    }

    protected void openMapAtPosition(double[] position) {
        openMapAtPosition(position[0], position[1]);
    }

    protected void openMapAtPosition(double longitude, double latitude) {
        MapUrl url = createMapUrl(longitude, latitude);
        openMapAt(url);
    }

    protected void openMapAt(MapUrl url) {
        String urlString = url
                .serveTilesFromLocalhost(apiProxy.getPort())
                .setOfflineAreaBoundingBoxes(Settings.getOfflineAreaBoundingBoxes())
                .getUrl();
        log.d("open map at", urlString);
        webView.loadUrl(urlString);
    }

    @NonNull
    protected MapUrl createMapUrl(double longitude, double latitude) {
        return new MapUrl(longitude, latitude);
    }

    public MapUrl getUrl() {
        String url = webView.getUrl();
        if (url == null) {
            return null;
        }
        return new MapUrl(url);
    }

    @SuppressLint("MissingPermission")
    protected void openMapAtGPSPosition() {
        final LocationManager locationManager = createLocationManager();
        if (locationManager == null) {
            return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 50, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        locationManager.removeUpdates(this);
                        openMapAtPosition(location.getLongitude(), location.getLatitude());
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                }
        );
    }

    protected void openMapAtLastPlantOrDefault() {
        Plant.Position position = Plant.getAPositionNearAPlantForTheMap();
        openMapAtPosition(position);
    }

    @Override
    protected void menuOpenMap() {
        reloadMap();
    }

    protected void reloadMap() {
        MapUrl url = getUrl();
        if (url != null) {
            openMapAt(url);
        }
    }

    @Override
    protected void menuOpenAddressSearch() {
        Intent intent = new Intent(this, AddressSearchActivity.class);
        startActivityForResult(intent, REQUEST_CODE_ADDRESS_SEARCH);
    }

    @Override
    protected boolean menuHideAddressSearch() {
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // preserving the current url to reload the map
        MapUrl url = getUrl();
        outState.putString(ARG_MAP_URL, url.getUrl());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String url = savedInstanceState.getString(ARG_MAP_URL);
        if (url != null) {
            openMapAt(new MapUrl(url));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADDRESS_SEARCH && resultCode == RESULT_OK) {
            String newUrl = data.getStringExtra(AddressSearchActivity.ARG_MAP_URL);
            openMapAt(new MapUrl(newUrl));
        }
    }
}
