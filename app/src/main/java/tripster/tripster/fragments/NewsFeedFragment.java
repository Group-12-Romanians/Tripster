package tripster.tripster.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import tripster.tripster.R;
import tripster.tripster.TripPreviewAdapter;

public class NewsFeedFragment extends Fragment {
    private static final String TAG = NewsFeedFragment.class.getName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        ListView list = (ListView) view.findViewById(R.id.friends_list);
        list.setAdapter(new TripPreviewAdapter(getActivity()));
//        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Log.d(TAG, "Here I am");
//                HomeFragment frag = new HomeFragment();
//                getActivity().getFragmentManager().beginTransaction().replace(R.id.main_content, frag).commit();
//            }
//        });
        return view;
    }
}
