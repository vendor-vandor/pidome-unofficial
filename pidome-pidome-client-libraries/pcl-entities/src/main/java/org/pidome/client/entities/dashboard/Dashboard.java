/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.dashboard;

import java.util.List;
import java.util.Map;
import org.pidome.pcl.utilities.properties.ObservableArrayListBean;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;
import org.pidome.pcl.utilities.properties.ReadOnlyStringPropertyBindingBean;
import org.pidome.pcl.utilities.properties.StringPropertyBindingBean;

/**
 * A user/client dashboard
 * @author John
 */
public class Dashboard {
    
    public enum DashBoardClientType {
        /**
         * No dashboard contents available
         */
        NONE,
        /**
         * Dashboard for mobile device.
         */
        MOBILE,
        /**
         * Dashboard for a fixed device.
         */
        FIXED,
        /**
         * Personalized dashboard on a fixed device.
         */
        HYBRID;
    }
    
    private DashBoardClientType type = DashBoardClientType.NONE;
    
    /**
     * The dashboard name.
     */
    private final StringPropertyBindingBean dashboardName = new StringPropertyBindingBean();
    
    /**
     * The dashboard id.
     */
    private final int dashId;
    
    /**
     * The uid this dashboard is for.
     * When this is an user personalized dashboard the uid will be available with
     * the client id.
     */
    private int dashUid;
    
    /**
     * The person id this dashboard is for.
     * Used on mobile dashboards.
     */
    private int persId;
    
    /**
     * The client id this dashboard is for.
     */
    private int clientId;
    
    /**
     * List of dashboard items.
     */
    private final ObservableArrayListBean<DashboardItem> itemList = new ObservableArrayListBean<>();
    
    /**
     * A read only wrapper for the dashboard items list.
     */
    private final ReadOnlyObservableArrayListBean<DashboardItem> readOnlyitemList = new ReadOnlyObservableArrayListBean<>(itemList);
    
    /**
     * Constructor.
     * @param id The dashboard id
     * @param name The dashboard's name
     */
    protected Dashboard(int id, String name){
        this.dashId = id;
        this.dashboardName.setValue(name);
    }
    
    /**
     * Set the person id's personalized dashboard id.
     * @param uId int of personalized uid.
     */
    protected void setUid(int uId){
        dashUid = uId;
    }

    /**
     * Set's the person id's dashboard id.
     * @param persId The person id this dashboard is for.
     */
    protected void setPersonId(int persId){
        this.persId = persId;
    }
    
    /**
     * Set's the client's client dashboard id.
     * @param clientId The client id this dashboard is for.
     */
    protected void setClientId(int clientId){
        this.clientId = clientId;
    }
    
    /**
     * Get the person id's personalized dashboard id.
     * @return uid int.
     */
    protected int getUid(){
        return dashUid;
    }

    /**
     * Set's the dashboard type from the string typed type from RPC.
     * @param type The dashboard type as string.
     */
    protected void setDashboardType(String type){
        for(DashBoardClientType dct:DashBoardClientType.values()){
            if(dct.toString().equals(type)){
                this.type = dct;
                break;
            }
        }
    }
    
    /**
     * Returns the dashboard type.
     * @return DashBoardClientType The dashboard type.
     */
    protected DashBoardClientType getDashboardType(){
        return type;
    }
    
    /**
     * Get the person id's dashboard id.
     * @return Person id int
     */
    protected int getPersonId(){
        return persId;
    }
    
    /**
     * Set the client's client dashboard id.
     * @return int client id.
     */
    protected int getClientId(){
        return clientId;
    }
    
    /**
     * Rebuild this dashboard items.
     * @param components List of components in a map.
     */
    protected final void reBuild(List<Map<String, Object>> components){
        itemList.clear();
        build(components);
    }
    
    /**
     * Build the components.
     * @param components  List of components in a map.
     */
    protected final void build(List<Map<String, Object>> components){
        for(Map<String,Object> data: components){
            DashboardItem item = null;
            switch((String)((Map<String,Object>)data.get("config")).get("data-type")){
                case "time":
                    item = new DashboardTimeItem();
                break;
                case "weather":
                    item = new DashboardWeatherItem();
                break;
                case "device":
                    item = new DashboardDeviceItem();
                break;
                case "macro":
                    item = new DashboardMacroItem();
                break;
                case "scene":
                    item = new DashboardSceneItem();
                break;
                case "spacer":
                    item = new DashboardSpacerItem();
                break;
            }
            if(item!=null){
                item.setDimentions(((Number)data.get("row")).intValue(),
                                   ((Number)data.get("col")).intValue(),
                                   ((Number)data.get("size_x")).intValue(),
                                   ((Number)data.get("size_y")).intValue());
                item.setConfig((Map<String,Object>)data.get("config"));
                itemList.add(item);
            }
        }
    }
    
    /**
     * Returns a list of available dashboard items.
     * @return Returns an observable list of items in a dashboard.
     */
    public final ReadOnlyObservableArrayListBean<DashboardItem> getItems(){
        return this.readOnlyitemList;
    }
    
    /**
     * Returns the dashboard id.
     * @return The id of the dashboard.
     */
    public final int getId(){
        return this.dashId;
    }
    
    /**
     * Returns the dashboard name.
     * @return A read only bind-able string property holding the dashboard name.
     */
    public final ReadOnlyStringPropertyBindingBean getName(){
        return this.dashboardName.getReadOnlyBooleanPropertyBindingBean();
    }
    
}