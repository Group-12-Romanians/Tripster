package tripster.tripster.account;


import android.content.Intent;

public interface LoginProvider {
  boolean isLoggedIn();

  void setupLoginButton();

  void handleActivityResult(int requestCode, int resultCode, Intent data);
}
