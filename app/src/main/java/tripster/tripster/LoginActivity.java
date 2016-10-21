package tripster.tripster;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getName();

    private List<LoginProvider> loginProviders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginProviders.add(new GoogleProvider(this));
        loginProviders.add(new FacebookProvider(this));

        for (LoginProvider lP : loginProviders) {
            if (lP.isLoggedIn()) return;
        }

        setContentView(R.layout.activity_login);

        for (LoginProvider lP : loginProviders) {
            lP.setupLoginButton();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        for (LoginProvider lP : loginProviders) {
            lP.handleActivityResult(requestCode, resultCode, data);
        }
    }

    protected void enterTripster(String loginProviderClassName) {
        Intent i = new Intent(LoginActivity.this, TripsterActivity.class);
        i.putExtra("loginProvider", loginProviderClassName);
        startActivity(i);
        finish();
    }
}
