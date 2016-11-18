package tripster.tripster;

public class User {

  private String id;
  private String name;

  public User(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this)  {
      return true;
    }
    if (!(o instanceof User)) {
      return false;
    }

    User user = (User) o;

    return user.getId().equals(id) && user.getName().equals(name);
  }

  @Override
  public int hashCode() {
    return 17 * id.hashCode() + 31 * name.hashCode();
  }

}
