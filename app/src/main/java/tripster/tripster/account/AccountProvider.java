package tripster.tripster.account;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import tripster.tripster.UILayer.LoginActivity;

abstract class AccountProvider implements LoginProvider, LogoutProvider {

  private static final String TAG = AccountProvider.class.getName();

  AppCompatActivity parentActivity;

  void login(String loginProviderClassName, String userId, String name, String avatarUrl, String email) {
    ((LoginActivity) parentActivity).handleLogin(loginProviderClassName, userId, name, avatarUrl, email);
  }

  void login(String loginProviderClassName, String userId) {
    ((LoginActivity) parentActivity).handleLogin(loginProviderClassName, userId, null, null, null);
  }

  @Override
  public void logOut() {
    Intent i = new Intent(parentActivity, LoginActivity.class);
    parentActivity.startActivity(i);
    parentActivity.finish();
  }
}
