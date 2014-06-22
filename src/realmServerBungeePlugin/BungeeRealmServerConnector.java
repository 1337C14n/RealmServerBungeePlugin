package realmServerBungeePlugin;

import java.util.concurrent.ConcurrentHashMap;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import packets.ClientLogin;
import packets.Packet;
import packets.RequestServer;
import realmConnection.RealmServerConnector;


public class BungeeRealmServerConnector extends RealmServerConnector {

  private static ConcurrentHashMap<String, NetworkWaitObject> wait;
  
  public BungeeRealmServerConnector(String address, int port) {
    super(address, port);
    wait = new ConcurrentHashMap<>();
  }

  @Override
  public void run() {
    while (true) {
      Packet packet = connection.read();
      
      if (packet instanceof ClientLogin) {
        /*
         * Client Login is required for handshake with the server it is the
         * servers way of asking who you are.
         */
        write(new ClientLogin("proxy", "BungeeCord"));
      } else if(packet instanceof RequestServer){
        /*
         * We will check to see if this request server packet is a response
         */
        System.out.println("Request Returned");
        System.out.println("Name: " + ((RequestServer) packet).getPlayerName());
        System.out.println("Server: " + ((RequestServer) packet).getServerName());
        
        if(wait.containsKey(((RequestServer) packet).getPlayerName())){
          NetworkWaitObject networkwaitObject = wait.get(((RequestServer) packet).getPlayerName());
          Object waitObject = networkwaitObject.getWaitObject();
          
          networkwaitObject.setPacket(packet);
          wait.put((((RequestServer)networkwaitObject.getPacket())).getPlayerName(), networkwaitObject);
          
          synchronized(waitObject){
            waitObject.notify();
          }
          
        } else {
          //A server is requesting bungee to move the player.
          System.out.println("Request to move " + ((RequestServer) packet).getPlayerName() + " to " + ((RequestServer) packet).getServerName());
          ServerInfo server = ProxyServer.getInstance().getServerInfo(((RequestServer) packet).getServerName());
          
          if(server != null){
            try{
              ProxyServer.getInstance().getPlayer(((RequestServer) packet).getPlayerName()).connect(server);
            } catch (NullPointerException e) {
              System.out.println("Threw A null pointer?");
            }
            
          }
          System.out.println("Server does not exist");
        }
      }
    }
  }
  
  public static Packet requestServerAndWait(Packet packet) {
    Object waitObjectObject = new Object();
    NetworkWaitObject waitObject = new NetworkWaitObject(waitObjectObject, packet);
    wait.put(((RequestServer)packet).getPlayerName(), waitObject);
    write(packet);
    synchronized (waitObject.getWaitObject()) {
      try {
        waitObject.getWaitObject().wait();
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    Packet returnPacket = wait.get(((RequestServer)packet).getPlayerName()).getPacket();

    wait.remove(((RequestServer) packet).getPlayerName());
    return returnPacket;
  }

  @Override
  public void onReconnect() {
    
  }
}
