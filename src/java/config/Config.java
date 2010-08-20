package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.yaml.snakeyaml.Yaml;

public class Config {

    public Config() {
        config = new Properties();
        InputStream in = null;
        try {
            // load override config
            in = this.getClass().getResourceAsStream("local.config.properties");
            config.load(in);

        } catch (IOException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);

            // load default config
            try {
                in = this.getClass().getResourceAsStream("config.properties");
                config.load(in);
            } catch (IOException _ex) {
                Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, _ex);
            }
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            in = this.getClass().getResourceAsStream("keyword.yaml");
            Yaml yaml = new Yaml();
            topics = (List<Map<String, Object>>) yaml.load(in);

            in.close();

        } catch (IOException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Properties config;
    private List<Map<String, Object>> topics;
    public String getConfig(String k) {
        return config.getProperty(k);
    }
    public List<Map<String, Object>> getTopics() {
        return topics;
    }
}
