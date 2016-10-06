package dk.nodes.netscanner;

/**
 * Created by liamakakpo on 21/09/2016.
 */
public class NetworkDevice {

    public static String address;
    public String hostname;

    public NetworkDevice(final String address, final String hostname) {
        this.address = address;
        this.hostname = hostname;
    }

    public static String getAddress() {
        return address;
    }

    public static void setAddress(String address) {
        NetworkDevice.address = address;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

}
