package com.theboss.kzeaddonfabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

public class ConfigWrapper {
    public static final Logger LOGGER = LogManager.getLogger("ConfigWrapper");

    private final File configFile;
    private Config config;

    public ConfigWrapper(String configFile, Config config) {
        this.configFile = new File(configFile);
        this.config = config;
    }

    public String getConfigDir() {
        return this.configFile.getParent();
    }

    public Config getConfig() {
        return this.config;
    }

    public void loadConfig() {
        try {
            Gson gson = new GsonBuilder().create();
            JsonReader reader = new JsonReader(new FileReader(configFile));
            config = gson.fromJson(reader, Config.class);
        } catch (Exception e) {
            e.printStackTrace();
            resetConfig();
        }
    }

    public void saveConfig() {
        try {
            FileWriter fWriter = new FileWriter(configFile);
            PrintWriter pWriter = new PrintWriter(new BufferedWriter(fWriter));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String content = gson.toJson(config);
            pWriter.print(content);
            pWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetConfig() {
        try {
            if (!configFile.exists())
                if (!configFile.createNewFile()) LOGGER.error("Config file create failed");
        } catch (IOException e) {
            e.printStackTrace();
        }
        config = new Config();
        saveConfig();
    }
}
