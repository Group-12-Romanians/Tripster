package tripster.tripster.UILayer.users;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tripster.tripster.R;

import static tripster.tripster.Constants.LEVEL_PRIVATE;

public class MyProfileFragment extends ProfileFragment {

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = super.onCreateView(inflater, container, savedInstanceState);
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
