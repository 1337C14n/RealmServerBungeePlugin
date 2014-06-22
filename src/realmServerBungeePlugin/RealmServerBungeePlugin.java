package realmServerBungeePlugin;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class RealmServerBungeePlugin extends Plugin{

  @Override
  public void onEnable() {
    
    ProxyServer.getInstance().getPluginManager().registerListener(this, new BungeeListener(this));
    BungeeRealmServerConnector connectionHandler = new BungeeRealmServerConnector("localhost", 2000);
    Thread connectionThread = new Thread(connectionHandler);
    connectionThread.start();
    
  }
}
