/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.system.packages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.services.http.rpc.PackageServiceJSONRPCWrapper;

/**
 *
 * @author John
 */
public final class FormattedPermissions {
 
    static Logger LOG = LogManager.getLogger(FormattedPermissions.class);
    
    /**
     * Formats a permission set to be used in the web json output.
     * @param permissionSet
     * @return 
     */
    public static Object formattedPermissions(Map<String,Object> permissionSet){
        if(permissionSet.containsKey("permissions")){
            return groupedFormattedPermissions((ArrayList<Map<String,String>>)permissionSet.get("permissions"));
        } else {
            return permissionSet;
        }
    }
    
    /**
     * Formats the given permission set to a grouped permission set to be used in the json output.
     * @param plainSet
     * @return 
     */
    private static Object groupedFormattedPermissions(ArrayList<Map<String,String>> plainSet){
        Map<String,Map<String,Object>> permissionSet = new HashMap<>();
        for(Map<String,String> plain:plainSet){
            Map<String,Object> parentCollection = new HashMap<>();
            parentCollection.put("name", "Other");
            parentCollection.put("description", "Uncategorized permissions");
            parentCollection.put("collection", new ArrayList<>());
            switch(plain.get("type")){
                case "java.awt.AWTPermission":
                    parentCollection.put("name", "Perform graphical operations");
                    parentCollection.put("description", "This is not normal to be used within PiDome as there is no graphical interface, by allowing this package you also allow this package to possibly access your clipboard");
                break;
                case "java.io.FilePermission":
                    parentCollection.put("name", "File access");
                    parentCollection.put("description", "Access to files localy on the server.");
                break;
                case "java.net.SocketPermission":
                    parentCollection.put("name", "Make and/or allow network connection(s)");
                    parentCollection.put("description", "Wants to make a connection to remote computers or allow access to this server. This allows data to be send to/fetch from remote servers, or remote servers read/send data from this server.");
                break;
                case "java.net.URLPermission":
                    parentCollection.put("name", "Makes URL/Website connection(s)");
                    parentCollection.put("description", "Wants to make a connection to remote websites/url's. This allows data to be send to remote servers.");
                break;
            }
            if(!permissionSet.containsKey(plain.get("type"))){
                permissionSet.put(plain.get("type"), parentCollection);
            }
            ((ArrayList)permissionSet.get(plain.get("type")).get("collection")).add(createRuleDescription(plain));
        }
        return permissionSet;
    }
 
    private static Map<String,String> createRuleDescription(Map<String,String> rule){
        StringBuilder descBuilder = new StringBuilder();
        switch(rule.get("type")){
            case "java.io.FilePermission":
                descBuilder = createFilePermissionDescription(descBuilder, rule);
            break;
            case "java.net.SocketPermission":
                descBuilder = createSocketPermissionDescription(descBuilder, rule);
            break;
            case "java.net.URLPermission":
                descBuilder = createHTTPPermissionDescription(descBuilder, rule);
            break;
            default:
                descBuilder.append("No description available yet");
            break;
        }
        rule.put("detailed", descBuilder.toString());
        return rule;
    }
    
    private static StringBuilder createFilePermissionDescription(StringBuilder builder, Map<String,String> rule){
        builder.append("Want's to ");
        if(rule.get("action").contains("read") || rule.get("action").contains("readLink")){
            builder.append("read, ");
        }
        if(rule.get("action").contains("write")){
            builder.append("write, ");
        }
        if(rule.get("action").contains("execute")){
            builder.append("execute, ");
        }
        if(rule.get("action").contains("delete")){
            builder.append("delete, ");
        }
        try {
            builder.setLength(builder.length()-2);
            builder.append(" ");
        } catch (IndexOutOfBoundsException ex){
            /// do nothing.
        }
        if(rule.get("name").endsWith("-")){
            builder.append("recursive from directory ").append(rule.get("name"));
        } else if(rule.get("name").endsWith("*")){
            builder.append("all files in directory ").append(rule.get("name"));
        } else {
            builder.append("the file ").append(rule.get("name"));
        }
        return builder;
    }
    
    
    private static StringBuilder createSocketPermissionDescription(StringBuilder builder, Map<String,String> rule){
        builder.append("Want's to ");
        if(rule.get("action").contains("connect")){
            builder.append("connect, ");
        }
        if(rule.get("action").contains("listen")){
            builder.append("listen, ");
        }
        if(rule.get("action").contains("accept")){
            builder.append("allow connections, ");
        }
        try {
            builder.setLength(builder.length()-2);
            builder.append(" ");
        } catch (IndexOutOfBoundsException ex){
            /// do nothing.
        }
        String[] actionSet = rule.get("name").split(":");
        if(actionSet[0].endsWith("*")){
            builder.append("to any known host ");
        } else if (actionSet[0].startsWith("*")){
            builder.append("to any possible subdomain of ").append(actionSet[0]).append(" ");
        }
        try {
            if(actionSet[1].endsWith("*") || actionSet[1].endsWith("0")){
                builder.append("to all possible ports");
            } else if (actionSet[1].endsWith("-")){
                builder.append("to all ports above ").append(actionSet[1].substring(0, actionSet[1].length()-1));
            } else if (actionSet[1].startsWith("-")){
                builder.append("to all ports below ").append(actionSet[1].substring(1, actionSet[1].length()-1));
            } else {
                builder.append("to port ").append(actionSet[1]);
            }
        } catch (NullPointerException ex){
            builder.append("to all possible ports");
        }
        return builder;
    }
    
    private static StringBuilder createHTTPPermissionDescription(StringBuilder builder, Map<String,String> rule){
        builder.append("Wants to acces the following url: ").append(rule.get("name"));
        return builder;
    }
    
}