package tripster.tripster.account;


import android.content.Intent;

public interface LoginProvider {
  void silentSignIn();

  void setupLoginButton();

  void handleActivityResult(int requestCode, int resultCode, Intent data);
}
