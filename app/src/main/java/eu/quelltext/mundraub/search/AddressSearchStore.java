package eu.quelltext.mundraub.search;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AddressSearchStore implements IAddressSearch {

    private static final String JSON_ALL = "all";

    private List<AddressSearchResult> all = new ArrayList<>();
    private List<AddressSearchResult> found = new ArrayList<>();
    private Observer observer;

    public static AddressSearchStore fromJSON(JSONObject json) throws JSONException {
        AddressSearchStore result = new AddressSearchStore();
        result.loadFrom(json);
        return result;
    }

    public void add(AddressSearchResult newAddress) {
        all.remove(newAddress);
        all.add(0, newAddress);
    }

    @Override
    public void notifyAboutChanges(Observer observer) {
        this.observer = observer;
    }

    @Override
    public AddressSearchResult get(int position) {
        return found.get(position);
    }

    @Override
    public int size() {
        return found.size();
    }

    @Override
    public void search(String text) {
        List<AddressSearchResult> newFound = new ArrayList<>();
        for (int i = 0; i < all.size(); i++) {
            AddressSearchResult address = all.get(i);
            if (address.matches(text)) {
                newFound.add(address);
            }
        }
        found = newFound;
        if (observer != null) {
            observer.onNewSearchResults(this);
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject result = new JSONObject();
        JSONArray all = new JSONArray();
        for (int i = 0; i < this.all.size(); i++) {
            AddressSearchResult address = this.all.get(i);
            all.put(address.toJSON());
        }
        result.put(JSON_ALL, all);
        return result;
    }

    public void loadFrom(JSONObject json) throws JSONException {
        JSONArray all = json.getJSONArray(JSON_ALL);
        for (int i = all.length() - 1; i >= 0; i--) {
            JSONObject addressJSON = all.getJSONObject(i);
            AddressSearchResult address = AddressSearchResult.fromJSON(addressJSON);
            add(address);
        }
    }
}
