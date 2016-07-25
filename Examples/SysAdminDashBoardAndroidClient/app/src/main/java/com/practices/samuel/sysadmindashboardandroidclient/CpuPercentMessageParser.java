package com.practices.samuel.sysadmindashboardandroidclient;

import com.practices.samuel.sysadmindashboardandroidclient.SocketMessageParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by samuel on 7/24/16.
 */
public class CpuPercentMessageParser implements SocketMessageParser {
    @Override
    public Map<String, String> ParseString(String message) {
        HashMap<String,String> result = new HashMap<>();
        if(message.contains("CPUDATA")){
            result.put("CPUDATA",message);
            return result;
        }
        result.put("CPUDATA","");
        return result;
    }
}
