/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tripster.tripster.UILayer.trip.map.model;

import android.clustering.ClusterItem;

import com.google.android.gms.maps.model.LatLng;

public class PhotoAtLocation implements ClusterItem {
    private final String locationName;
    private final String photoPath;
    private final LatLng location;

    public PhotoAtLocation(String locationName, String photoPath, LatLng location) {
        this.locationName = locationName;
        this.photoPath = photoPath;
        this.location = location;
    }

    @Override
    public LatLng getPosition() {
        return location;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public String getLocationName() {
        return locationName;
    }
}
