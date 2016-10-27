package tripster.tripster;

import android.widget.ImageView;
import android.widget.TextView;

public interface AccountProvider {
    void logOut();

    void setUserAccountFields(final TextView name, final TextView email, final ImageView avatar);
}
