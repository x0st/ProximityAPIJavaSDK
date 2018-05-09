package proximity.sdk.entity;

public class WiFiNetwork {
    private final String SSID;
    private final String BSSID;
    private final int signal;

    public WiFiNetwork(String ssid, String bssid, int signal) {
        this.SSID = ssid;
        this.BSSID = bssid;
        this.signal = signal;
    }

    public String getSSID() {
        return SSID;
    }

    public String getBSSID() {
        return BSSID;
    }

    public int getSignal() {
        return signal;
    }
}
