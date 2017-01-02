package tripster.tripster.UILayer.users.profile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tripster.tripster.R;

import static junit.framework.Assert.assertNotNull;
import static tripster.tripster.Constants.LEVEL_PRIVATE;
import static tripster.tripster.R.id.settings;

public class MyProfileFragment extends ProfileFragment {

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = super.onCreateView(inflater, container, savedInstanceState);
    assertNotNull(view);
    view.findViewById(settings).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        tM.accessSettings();
      }
    });
    return view;
  }

  @Override
  protected int getLayoutRes() {
    return R.layout.my_profile;
  }

  @Override
  public int getLevel() {
    return LEVEL_PRIVATE;
  }
}
