package realmServerBungeePlugin;

import packets.Packet;

public class NetworkWaitObject {

  Object waitObject;
  Packet packet;
  
  public NetworkWaitObject(Object waitObject, Packet packet) {
    this.waitObject = waitObject;
    this.packet = packet;
  }

  public Object getWaitObject() {
    return waitObject;
  }

  public void setWaitObject(Object waitObject) {
    this.waitObject = waitObject;
  }

  public Packet getPacket() {
    return packet;
  }

  public void setPacket(Packet packet) {
    this.packet = packet;
  }
  
}
