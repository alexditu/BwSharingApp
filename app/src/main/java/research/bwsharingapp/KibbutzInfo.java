package research.bwsharingapp;

import java.io.Serializable;

/**
 * Created by alex on 5/27/17.
 */

public class KibbutzInfo implements Serializable {
    private String routerIp;
    private String clientIp;
    private String routerPort;

    public KibbutzInfo(String routerIp, String clientIp, String routerPort) {
        this.routerIp = routerIp;
        this.clientIp = clientIp;
        this.routerPort = routerPort;
    }

    public KibbutzInfo() {

    }

    @Override
    public String toString() {
        return "{" + routerIp + ", " + clientIp + ", " + routerPort + "}";
    }

    public String getRouterIp() {
        return routerIp;
    }

    public void setRouterIp(String routerIp) {
        this.routerIp = routerIp;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getRouterPort() {
        return routerPort;
    }

    public void setRouterPort(String routerPort) {
        this.routerPort = routerPort;
    }
}
