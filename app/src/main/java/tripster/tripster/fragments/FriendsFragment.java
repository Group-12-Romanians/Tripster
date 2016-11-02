package tripster.tripster.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import java.util.Arrays;
import java.util.List;

import tripster.tripster.R;
import tripster.tripster.SearchableAdapter;

public class FriendsFragment extends Fragment {
    private static final String TAG = FriendsFragment.class.getName();
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        List<String> data = Arrays.asList("MAMA", "TATA", "BUNI", "BOBO");
        final SearchableAdapter searchableAdapter = new SearchableAdapter(getActivity(), data);

        EditText searchBar = (EditText) view.findViewById(R.id.search);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                System.out.println("Text ["+s+"]");
                searchableAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        ListView friendsList = (ListView) view.findViewById(R.id.friends_list);
        friendsList.setAdapter(searchableAdapter);
        return view;
    }
}
