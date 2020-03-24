/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rogueparkour_placeholder;
import cl.omegacraft.kledioz.rparkour.Main;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.clip.placeholderapi.expansion.Cacheable;
import me.clip.placeholderapi.expansion.Configurable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Taskable;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
/**
 *
 * @author Admin
 */
public class RogueParkour_placeholder  extends PlaceholderExpansion implements Configurable, Cacheable, Taskable, Runnable{ 
    private final Map<String, Integer> update_data = new ConcurrentHashMap<>();
    
    public final FileConfiguration file_data=Main.newConfigz;
    public final FileConfiguration general_config=Main.get().getConfig();
    public final ArrayList<String> uuid_players=new ArrayList<>();
    public final ArrayList<String> uuid_score_ordenado=new ArrayList<>();
    public final HashMap<String,String> uuid_name=new HashMap<>();
    public final HashMap<String,Integer> uuid_score=new HashMap<>();
    public final HashMap<String,Integer> name_score=new HashMap<>();
    public Thread hilo;
    public connection sql;
    private BukkitTask task;
    
    /**
     * This method should always return true unless we
     * have a dependency we need to make sure is on the server
     * for our placeholders to work!
     *
     * @return always true since we do not have any dependencies.
     */
    
    @Override
    public boolean canRegister(){
        return true;
    }

    /**
     * The name of the person who created this expansion should go here.
     * 
     * @return The name of the author as a String.
     */
    @Override
    public String getAuthor(){
        return "matahombress";
    }

    /**
     * The placeholder identifier should go here.
     * <br>This is what tells PlaceholderAPI to call our onRequest 
     * method to obtain a value if a placeholder starts with our 
     * identifier.
     * <br>This must be unique and can not contain % or _
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    public String getName(){
        return "RogueParkour-temporary";
    }
    @Override
    public String getIdentifier(){
        return "RogueParkour-temporary";
    }

    /**
     * This is the version of this expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     *
     * @return The version as a String.
     */
    @Override
    public String getVersion(){
        return "1.4";
    }
    
    @Override
    public String getRequiredPlugin(){
        return "RogueParkour";
    }
    
    public String getPlugin() {
        return null;
    }
    //Configurable
    @Override
    public Map<String, Object> getDefaults() {
        final Map<String, Object> defaults = new HashMap<>();
        defaults.put("check_interval", 30);
        return defaults;
    }
    //Cacheable
    @Override
    public void clear() {
        update_data.clear();
    }
    //Taskable
    @Override
    public void start() {
        hilo=new Thread(this);
        hilo.start();
    }
    @Override
    public void stop() {
        hilo=null;
        if (task != null) {
            if(sql!=null){
                sql.closeConnection();
                sql=null;
            }
            try {
                task.cancel();
            } catch (Exception ex) {
            }
            task = null;
        }
    }
    public void order(){
        for(int i=0;i<uuid_score.size();i++){
            int max=0;
            int orden=0;
            for(int s=0;s<uuid_score.size();s++){
                if(uuid_score.get(uuid_players.get(s))>max){
                    if(!uuid_score_ordenado.contains(uuid_players.get(s))){
                        max=uuid_score.get(uuid_players.get(s));
                        orden=s;
                    }
                }
            }
            uuid_score_ordenado.add(uuid_players.get(orden));
        }
    }
    //Request placeholder
    @Override
    public String onRequest(final OfflinePlayer player, final String identifier){
        //%RogueParkour-temporary_top_<number>;<type>%
        //type==score or name
        //%RogueParkour-temporary_get_<player>%
        if(identifier.startsWith("top")){
            //Aplicar los datos
            String[] string=identifier.split("top_")[1].split(";");
            int number=Integer.valueOf(string[0]);
            int select=number-1;
            String type="null";
            if(string.length>1){
                type=string[1];
            }
            if(uuid_score_ordenado==null){
                return "";
            }
            if(uuid_score_ordenado.size()>=number&&number>0){
                String uuid=uuid_score_ordenado.get(select);
                if(type.equalsIgnoreCase("name")){
                    return uuid_name.get(uuid);
                }else{
                    return String.valueOf(uuid_score.get(uuid));
                }
            }else{
                return "";
            }
        }else if(identifier.startsWith("get_")){
            String plName=identifier.split("get_")[1];
            if(uuid_score_ordenado==null){
                return "";
            }
            if(name_score.containsKey(plName)){
                return String.valueOf(name_score.get(plName));
            }else{
                return "0";
            }
        }else{
            return null;
        }
    }    
    public static OfflinePlayer getOfflinePlayer(final String playerStr, final boolean isUUID) {
        OfflinePlayer[] offlinePlayers;
        for (int length = (offlinePlayers = Bukkit.getOfflinePlayers()).length, i = 0; i < length; ++i) {
            final OfflinePlayer p = offlinePlayers[i];
            if (isUUID && p.getUniqueId().toString().equalsIgnoreCase(playerStr)) {
                return p;
            }
            if (p.getName().equalsIgnoreCase(playerStr)) {
                return p;
            }
        }
        return null;
    }

    @Override
    public void run() {
        task=new BukkitRunnable() {
            @Override
            public void run() {
                //Delete and update all information
                boolean mysql_enable=general_config.getBoolean("MYSQL.enabled");
                uuid_name.clear();
                uuid_score.clear();
                name_score.clear();
                uuid_players.clear();
                uuid_score_ordenado.clear();
                if(mysql_enable){
                    try {
                        //Metodo sql
                        if(sql==null){
                            sql=new connection(general_config.getString("MYSQL.ip"), general_config.getString("MYSQL.port"), general_config.getString("MYSQL.database"), general_config.getString("MYSQL.user"), general_config.getString("MYSQL.password"));
                            sql.openConnection();
                        }
                        Statement st=sql.getConnection().createStatement();
                        ResultSet result=st.executeQuery("SELECT * FROM `RPScore`");
                        while(result.next()){
                            String uuid=result.getString("player");
                            OfflinePlayer pl=getOfflinePlayer(uuid,true);
                            String name;
                            int score=result.getInt("score");
                            if(pl==null){
                                name=uuid;
                            }else{
                                name=pl.getName();;
                            }
                            uuid_name.put(uuid, name);
                            uuid_score.put(uuid, score);
                            name_score.put(name, score);
                            uuid_players.add(uuid);

                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }else{
                    //Metodo fichero
                    if(sql!=null){
                        sql.closeConnection();
                        sql=null;
                    }
                    Set<String> all_data= file_data.getKeys(false);
                    for(String uuid : all_data){
                        uuid_name.put(uuid, file_data.getString(uuid+".name"));
                        uuid_score.put(uuid, file_data.getInt(uuid+".highscore"));
                        name_score.put(file_data.getString(uuid+".name"), file_data.getInt(uuid+".highscore"));
                        uuid_players.add(uuid);
                    }
                }
                //Ordenar los hashmap
                order();
            }
        }.runTaskTimer(getPlaceholderAPI(), 100L, 20*getInt("check_interval",30));
    }
    
}
