package tripster.tripster;

import java.util.HashMap;
import java.util.Map;

public class Constants {
  public static final String APP_NAME = "Tripster";
  public static final String SERVER = "http://146.169.46.142";
  public static final String APP_PORT = "8081";
  public static final String SERVER_URL = SERVER + ":" + APP_PORT;
  public static final String USER_ID = "userId";
  public static final String TRIP_ID = "tripId";
  public static final String  LOGIN_PROVIDER = "loginProvider";

  //------------------------------------------ SERVICE -------------------------------------------//
  public static final String START_SERVICE = "start";
  public static final String PAUSE_SERVICE = "pause";
  public static final String RESUME_SERVICE = "resume";
  public static final String STOP_SERVICE = "stop";

  //------------------------------------------ DATABASE ------------------------------------------//
  public static final String DB_NAME = "tripster";
  public static final String DB_PORT = "5984";
  public static final String DB_SYNC_URL = SERVER + ":" + DB_PORT + "/" + DB_NAME;
  public static final String DB_STORAGE_TYPE = "ForestDB";

  //------------------------------------------- VIEWS --------------------------------------------//
  public static final String TRIPS_BY_OWNER = "trips/byOwner";
  public static final String IMAGES_BY_TRIP_AND_TIME = "images/byTripAndTime";
  public static final String FOLLOWING_BY_USER = "follow/followingByUser";
  public static final String USERS_BY_ID = "users/byId";
  public static final String NOTIFICATIONS_BY_USER = "notifications/byUser";
  public static final String FOLLOWERS_BY_USER = "follow/followersByUser";

  // GENERAL
  public static final String DOC_ID = "_id";

  // TRIP
  public static final String TRIP_OWNER_K = "ownerId";
  public static final String TRIP_NAME_K = "name";
  public static final String TRIP_PREVIEW_K = "preview";
  public static final String TRIP_STOPPED_AT_K = "stoppedAt";
  public static final String TRIP_DESCRIPTION_K = "description";
  public static final String TRIP_VIDEO_K = "video";
  public static final String TRIP_LEVEL_K = "tripLevel";
  // 10 is private trip (this is also the deafult)
  // 5 is friend trip
  // 1 is public trip

  // USER
  public static final String USER_ABOUT_K = "about";
  public static final String USER_NAME_K = "name";
  public static final String USER_EMAIL_K = "email";
  public static final String USER_AVATAR_K = "avatarUrl";

  // PLACE
  public static final String PLACE_LAT_K = "lat";
  public static final String PLACE_LNG_K = "lng";
  public static final String PLACE_TIME_K = "time";
  public static final String PLACE_TRIP_K = "tripId";
  public static final String PLACE_NAME_K = "name";
  public static final String PLACE_DESC_K = "description";

  // PHOTO
  public static final String PHOTO_PLACE_K = "placeId";
  public static final String PHOTO_PATH_K = "path";
  public static final String PHOTO_TRIP_K = "tripId";
  public static final String PHOTO_TIME_K = "time";
  public static final int MAX_SIZE = 600;

  // FOL (FOLLOWERS) The id is: <followerId>:<folowingId> ie. 1234:5678 means (1234 follows 5678)
  public static final String FOL_LEVEL_K = "folLevel";

  public static final int LEVEL_PRIVATE = 4;
  public static final int LEVEL_BRO = 3;
  public static final int LEVEL_CLOSE_FRIEND = 2;
  public static final int LEVEL_FRIEND = 1;
  public static final int LEVEL_PUBLIC = 0;
  public static final int LEVEL_PUBLIC_DEFAULT = -1;


  public static Map<Integer, String> levels = new HashMap<>();
  static {
    levels.put(LEVEL_PRIVATE, "private");
    levels.put(LEVEL_BRO, "bro");
    levels.put(LEVEL_CLOSE_FRIEND, "close friend");
    levels.put(LEVEL_FRIEND, "friend");
    levels.put(LEVEL_PUBLIC, "public");
    levels.put(LEVEL_PUBLIC_DEFAULT, "public");
  }
  // NOTIFICATIONS
  public static final String NOT_TIME_K = "time";
  public static final String NOT_RECEIVER_K = "receiver";
  public static final String NOT_FOLLOWER_K = "other";
  public static final String NOT_TYPE_K = "type";
  public static final String NOT_FOLLOWER = "follower";
  public static final String NOT_SUGGESTION = "suggestion";

  //------------------------------------------- SPREF --------------------------------------------//
  public static final String MY_ID = "myId";
  public static final String CURR_TRIP_ID = "currTripId";
  public static final String CURR_TRIP_ST = "currTripSt";
  public static final String CURR_TRIP_LL = "currTripLL";

  // CURRENT TRIP
  public static final String TRIP_RUNNING = "running";
  public static final String TRIP_PAUSED = "paused";
  public static final String DEFAULT_PREVIEW = SERVER_URL + "/default_preview.jpg";
  public static final String DEFAULT_NAME = "Current Trip";
}
