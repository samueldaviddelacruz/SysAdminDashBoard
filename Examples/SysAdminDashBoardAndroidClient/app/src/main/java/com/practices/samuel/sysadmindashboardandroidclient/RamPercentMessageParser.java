package com.practices.samuel.sysadmindashboardandroidclient;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by samuel on 7/23/16.
 */
public class RamPercentMessageParser implements  SocketMessageParser{

    @Override
    public Map<String,String> ParseString(String message) {

        HashMap<String,String> result = new HashMap<String,String>();
        if(message.contains("RAMusagePercent")){
            String[] messageArray = message.split(":");
            result.put(messageArray[0] , messageArray[1] );
            return result;
        }
        result.put("RAMusagePercent","0");
        return result;
    }
}
