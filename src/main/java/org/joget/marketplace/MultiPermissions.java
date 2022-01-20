package org.joget.marketplace;

import java.util.HashMap;
import java.util.Map;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DatalistPermission;
import org.joget.apps.form.model.FormPermission;
import org.joget.apps.userview.model.UserviewPermission;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;

public class MultiPermissions extends UserviewPermission implements FormPermission, DatalistPermission {
    
    private final static String MESSAGE_PATH = "messages/multiPermissions";
    
    public String getName() {
        return "Multi Permissions";
    }

    public String getVersion() {
        return "7.0.0";
    }
    
    public String getClassName() {
        return getClass().getName();
    }

    public String getLabel() {
        //support i18n
        return AppPluginUtil.getMessage("org.joget.marketplace.MultiPermissions.pluginLabel", getClassName(), MESSAGE_PATH);
    }
    
    public String getDescription() {
        //support i18n
        return AppPluginUtil.getMessage("org.joget.marketplace.MultiPermissions.pluginDesc", getClassName(), MESSAGE_PATH);
    }

    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/multiPermissions.json", null, true, MESSAGE_PATH);
    }

    @Override
    public boolean isAuthorize() {
        String match = getPropertyString("match");
        Object[] permissions = (Object[]) getProperty("permissions");
        if (permissions != null && permissions.length > 0) {
            PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");

            for (Object permission : permissions) {
                if (permission != null && permission instanceof Map) {
                    Map binderMap = (Map) permission;
                    if (binderMap != null && binderMap.containsKey("className") && !binderMap.get("className").toString().isEmpty()) {
                        String className = binderMap.get("className").toString();
                        UserviewPermission p = (UserviewPermission) pluginManager.getPlugin(className);
                        if (p != null) {
                            Map properties = new HashMap();
                            properties.putAll((Map) binderMap.get("properties"));    
                            if (p instanceof PropertyEditable) {
                                ((PropertyEditable) p).setProperties(properties);                  
                            }
                            p.setRequestParameters(this.getRequestParameters());
                            p.setCurrentUser(this.getCurrentUser());

                            boolean result = p.isAuthorize();
                            if(match.equalsIgnoreCase("ANY") && result){
                                return true;
                            }else if(match.equalsIgnoreCase("ALL") && !result){
                                return false;
                            }
                        }
                    }
                }
            }
            
            //will hit here if none of plugin in ANY return true = return false
            //will hit here if none of the plugin in ALL return false = return true
            return match.equalsIgnoreCase("ALL");
            
        }
        //secure by default, if there's no plugin configured, return false
        return false;
    }
}
