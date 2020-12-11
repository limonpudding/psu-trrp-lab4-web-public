package trrp.lab4.service;

import java.net.Socket;

public class ConnectionHolder {

    private Socket socket;
    private Boolean busy;

    public ConnectionHolder(Socket socket, Boolean busy) {
        this.socket = socket;
        this.busy = busy;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public Boolean getBusy() {
        return busy;
    }

    public void setBusy(Boolean busy) {
        this.busy = busy;
    }
}
