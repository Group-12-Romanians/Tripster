package tripster.tripster;

public class Constants {
  public static final String APP_NAME = "Tripster";
  public static final String SERVER = "http://146.169.46.220";

  //------------------------------------------ DATABASE ------------------------------------------//
  public static final String DB_NAME = "tripster";
  public static final String DB_PORT = "6984";
  public static final String DB_SYNC_URL = SERVER + ":" + DB_PORT + "/" + DB_NAME;
  public static final String DB_STORAGE_TYPE = "ForestDB";

  //------------------------------------------- VIEWS --------------------------------------------//
  public static final String TRIPS_BY_OWNER = "trips/byOwner";
  // TRIP
  public static final String TRIP_OWNER_K = "ownerId";
  public static final String TRIP_STATUS_K = "status";
  public static final String TRIP_NAME_K = "name";
  public static final String TRIP_PREVIEW_K = "preview";
  public static final String TRIP_STOPPED_AT = "stoppedAt";

  // USER
  public static final String USER_ID_K = "id";
  public static final String USER_NAME_K = "name";
  public static final String USER_EMAIL_K = "email";
  public static final String USER_AVATAR_K = "avatarUrl";

  // SPREF
  public static final String MY_ID = "myId";
  public static final String CURR_TRIP = "currentTrip";

}
