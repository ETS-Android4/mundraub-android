package eu.quelltext.mundraub.search;

public class NullObserver implements IAddressSearch.Observer {
    @Override
    public void onNewSearchResults(IAddressSearch addressSearch) {

    }

    @Override
    public void onSearchError(int errorId) {

    }
}
