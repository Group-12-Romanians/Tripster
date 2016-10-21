package tripster.tripster;

import android.net.Uri;

/**
 * Created by dragos on 10/21/16.
 */

public class UserAccount {

    private String username;
    private String email;
    private Uri avatar;

    public UserAccount(String username, String email, Uri avatar) {
        this.username = username;
        this.email = email;
        this.avatar = avatar;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAvatar(Uri avatar) {
        this.avatar = avatar;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Uri getAvatar() {
        return avatar;
    }
}
