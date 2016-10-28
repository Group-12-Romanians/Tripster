package tripster.tripster.account;

import android.widget.ImageView;
import android.widget.TextView;

public interface LogoutProvider {
    void logOut();

    void setUserAccountFields(final TextView name, final TextView email, final ImageView avatar);
}
