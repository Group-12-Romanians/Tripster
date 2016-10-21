package tripster.tripster;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;
import static com.facebook.FacebookSdk.getApplicationContext;

public class GpsTrackingFragment extends ListFragment {

    private static final long LOCATION_REFRESH_TIME = 1000;
    private static final float LOCATION_REFRESH_DISTANCE = 0;
    private static List<Place> savedPlaces;
    private Place startingPlace;

    private GoogleApiClient mGoogleApiClient;
    private Place previousPlace;
    private LocationManager mLocationManager;

    private List<String> locations;
    private ArrayAdapter<String> adapter;

    private static final int UPDATE_LOCATION_DISTANCE = 200;
    // Max value for latitude and longitude.
    private static final int INITIAL_COORDINATE_VALUE = 181;

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            // TODO: At the moment we do not display the initial place. Maybe display it sometime?
            if (ActivityCompat
                    .checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            final PendingResult<PlaceLikelihoodBuffer> currentPlace =
                    Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);

            currentPlace.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(@NonNull PlaceLikelihoodBuffer placeLikelihoods) {
                    Place newPlace = placeLikelihoods.get(0).getPlace();
                    // Check that the current place and previous place are not close to each other.
                    // If they are then do not consider the new place.
                    // Otherwise, consider it.
                    if (newPlace != null && checkPlaceValidity(newPlace)) {
                        if (placeNotVisitedRecently(newPlace)) {
                            savedPlaces.add(newPlace);
                            locations.add(newPlace.getName().toString());
                            adapter.notifyDataSetChanged();
                        }
                    }
                    previousPlace = newPlace;
                }
            });
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gps_tracking, container, false);
        locations = new ArrayList<>();
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, locations);
        adapter.setNotifyOnChange(true);
        setListAdapter(adapter);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mGoogleApiClient = ((TripsterActivity) getActivity()).getGoogleApiClient();

        mLocationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        if (ActivityCompat
                .checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat
                        .checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        savedPlaces = new ArrayList<>();

        mGoogleApiClient.connect();
        final PendingResult<PlaceLikelihoodBuffer> previousPlacePending =
                Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
        previousPlacePending.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(@NonNull PlaceLikelihoodBuffer placeLikelihoods) {
                previousPlace = placeLikelihoods.get(0).getPlace();
                startingPlace = previousPlace;
                if (ActivityCompat
                        .checkSelfPermission(
                                getActivity().getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplication(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                        LOCATION_REFRESH_DISTANCE, mLocationListener);
            }
        });
    }

    private boolean checkPlaceValidity(Place place) {
        return SphericalUtil.computeDistanceBetween(
                place.getLatLng(), previousPlace.getLatLng()) > UPDATE_LOCATION_DISTANCE;
    }

    // Checks if the given place is not the last one in the list of savedPlaces.
    private boolean placeNotVisitedRecently(Place place) {
        return savedPlaces.isEmpty() || !savedPlaces.get(savedPlaces.size() - 1).equals(place);
    }

}
