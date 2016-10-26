package tripster.tripster;

import com.google.android.gms.common.api.GoogleApiClient;

public interface AccountProvider {
    void logOut();

    UserAccount getUserAccount();

    GoogleApiClient getGoogleApiClient();
}
