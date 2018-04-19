package proximity.sdk.entity;

public class Place {
    /**
     * Name of the place.
     */
    private String name;

    /**
     * Address of the place.
     */
    private String address;

    /**
     * Google place id.
     */
    private String placeId;

    /**
     * Indicates if the user is currently at this place.
     */
    private Boolean isHere;

    /**
     * Coordinates of the place.
     */
    private Location location;

    /**
     * @param name name of the place
     * @param address address of the place
     * @param placeId google place id
     * @param location coordinates
     */
    public Place(String name, String address, String placeId, Boolean isHere, Location location) {
        this.name = name;
        this.address = address;
        this.placeId = placeId;
        this.isHere = isHere;
        this.location = location;
    }

    /**
     * Name of the place.
     */
    public String getName() {
        return name;
    }

    /**
     * Address of the place.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Google place id.
     */
    public String getPlaceId() {
        return placeId;
    }

    /**
     * Coordinates of the place.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Update the isHere field.
     */
    public void setIsHere(Boolean here) {
        isHere = here;
    }

    /**
     * Indicates if the user is currently at this place.
     */
    public Boolean getIsHere() {
        return isHere;
    }

    @Override
    public String toString() {
        return getName();
    }
}
