package proximity.sdk.entity;

public class Place {
    private String name;
    private String address;
    private String placeId;
    private Location location;

    public Place(String name, String address, String placeId, Location location) {
        this.name = name;
        this.address = address;
        this.placeId = placeId;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPlaceId() {
        return placeId;
    }

    public Location getLocation() {
        return location;
    }
}
