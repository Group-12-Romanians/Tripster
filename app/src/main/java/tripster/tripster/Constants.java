package tripster.tripster;

public class Constants {
  public static final String APP_NAME = "Tripster";
  public static final String SERVER = "http://146.169.46.220";
  public static final String APP_PORT = "8081";
  public static final String SERVER_URL = SERVER + ":" + APP_PORT;

  //------------------------------------------ SERVICE -------------------------------------------//
  public static final String START_SERVICE = "start";
  public static final String PAUSE_SERVICE = "pause";
  public static final String RESUME_SERVICE = "resume";
  public static final String STOP_SERVICE = "stop";

  //------------------------------------------ DATABASE ------------------------------------------//
  public static final String DB_NAME = "tripster";
  public static final String DB_PORT = "6984";
  public static final String DB_SYNC_URL = SERVER + ":" + DB_PORT + "/" + DB_NAME;
  public static final String DB_STORAGE_TYPE = "ForestDB";

  //------------------------------------------- VIEWS --------------------------------------------//
  public static final String TRIPS_BY_OWNER = "trips/byOwner";
  public static final String PLACES_BY_TRIP_AND_TIME = "places/byTripAndTime";
  public static final String FRIENDS_BY_USER = "friends/byUser";
  public static final String NOTIFICATIONS_BY_USER = "notifications/byUser";

  // TRIP
  public static final String TRIP_OWNER_K = "ownerId";
  public static final String TRIP_STATUS_K = "status";
  public static final String TRIP_NAME_K = "name";
  public static final String TRIP_PREVIEW_K = "preview";
  public static final String TRIP_STOPPED_AT_K = "stoppedAt";
  public static final String TRIP_DESCRIPTION_K = "description";
  public static final String TRIP_VIDEO_K = "video";

  // USER
  public static final String USER_ID_K = "id";
  public static final String USER_NAME_K = "name";
  public static final String USER_EMAIL_K = "email";
  public static final String USER_AVATAR_K = "avatarUrl";

  // PLACE
  public static final String PLACE_LAT_K = "lat";
  public static final String PLACE_LNG_K = "lng";
  public static final String PLACE_TIME_K = "time";
  public static final String PLACE_TRIP_K = "tripId";

  //PHOTO
  public static final String PHOTO_PLACE_K = "placeId";
  public static final String PHOTO_PATH_K = "path";
  public static final String PHOTO_TRIP_K = "tripId";
  public static final String PHOTO_TIME_K = "time";
  public static final int SCALED_WIDTH = 640;
  public static final int SCALED_HEIGHT = 440;

  //FS (FRIENDSHIP)
  public static final String FS_SENDER_K = "sender";
  public static final String FS_RECEIVER_K = "receiver";
  public static final String FS_LEVEL_K = "level";
  public static final String FS_TIME_K = "time";

  public static final String FS_LEVEL_CONFIRMED = "confirmed";
  public static final String FS_LEVEL_SENT = "sent";
  public static final String FS_LEVEL_DECLINED = "declined";
  
  //NOTIFICATIONS
  

  // SPREF
  public static final String MY_ID = "myId";
  public static final String CURR_TRIP = "currentTrip";

}
