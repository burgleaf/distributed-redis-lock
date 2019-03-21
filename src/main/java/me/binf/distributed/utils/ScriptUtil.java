package me.binf.distributed.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ScriptUtil {

    private static Logger logger = LoggerFactory.getLogger(ScriptUtil.class);


    /**
     * load lua script
     * @param path
     * @return
     */
    public static String getScript(String path) {
        StringBuilder sb = new StringBuilder();
        InputStream stream = ScriptUtil.class.getClassLoader().getResourceAsStream(path);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))){
            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str).append(System.lineSeparator());
            }
        } catch (IOException e) {
            logger.error("load script exception",e);
        }
        return sb.toString();
    }



}
