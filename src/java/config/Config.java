package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    }

    private Properties config;
    public String getConfig(String k) {
        return config.getProperty(k);
    }
}
