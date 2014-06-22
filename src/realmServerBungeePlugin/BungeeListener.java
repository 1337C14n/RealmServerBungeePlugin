package realmServerBungeePlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeListener implements Listener {
  BungeeController plugin;
  
  ConcurrentHashMap<String, JSONArray> playersJSON = new ConcurrentHashMap<>();

  public BungeeListener(BungeeController bungeeController) {
    this.plugin = bungeeController;
  }
  
  @EventHandler
  public void onPostLoginEventEvent(ServerConnectEvent event) {
    /*
     * Player connecting to a server we need to query to see what server
     * that player needs to be connected to
     */
    ProxiedPlayer player = event.getPlayer();
    
    if(player.getServer() == null){
      //get JSON from hashmap
      
      JSONArray json = null;
      
      if(playersJSON.containsKey(player.getName())){
        json = playersJSON.get(player.getName());
      }

      String server = getPlayerServer(json);
      System.out.println("Player connecting to: " + server);
      
      event.setTarget(plugin.getProxy().getServerInfo(server));
      
      //remove player from the map
      if(playersJSON.containsKey(player.getName())){
        json = playersJSON.remove(player.getName());
      }
    }
  }
  
  @EventHandler
  public void onPlayerJoin(PreLoginEvent event){
    String player = event.getConnection().getName();
    
    System.out.println("Player " + player + " is joining");
    JSONArray json = getRequest(player);
    
    if(isPlayerBanned(json)){
      event.setCancelReason("You are Banned. Report at forums.1337clan.com");
      event.setCancelled(true);
    }
    
    //Add json to hashmap so it can be referenced later
    if(player != null && json != null){
      playersJSON.put(player, json);
    }
  }
  
  /**
   * Is the player banned
   * 
   * @param json
   * @return if the player is banned
   */
  private boolean isPlayerBanned(JSONArray json){
    if(json != null){
      return ((String) json.getJSONObject(0).get("banned")).equals("0") ? false : true;
    }
      
    return false;
  }
  
  /**
   * Returns the servers name
   * 
   * @param json
   * @return server name
   */
  private String getPlayerServer(JSONArray json){
    System.out.println("getServer test: " + json.toString());
    return json != null ? (String) json.getJSONObject(1).get("name") : "hub";
  }
  
  /**
   * Request the JSON payload need for a player to log in
   * 
   * @param player players name
   * @return JSON containing name of server and if the player is banned
   */
  private JSONArray getRequest(String player){
    String absoluteURI = "http://localhost/network/player.php?query=server&name=" + player;
    System.out.println("Getting JSON");
    try {
      URLConnection connection = new URL(absoluteURI).openConnection();
      connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
      connection.connect();
  
      String line;
      StringBuilder builder = new StringBuilder();
      BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      while((line = reader.readLine()) != null) {
       builder.append(line);
      }
      System.out.println("Recieved JSON: " + builder.toString());
      return new JSONArray(builder.toString());
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
