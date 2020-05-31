package com.ateqi;

import com.ateqi.server.Server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * websocket-server start point
 *
 */
public class App {
    public static Properties configs = new Properties();
    static {
        InputStream in = App.class.getClassLoader().getResourceAsStream("application.properties");
        try {
            configs.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main( String[] args ) {
        int port = Integer.parseInt((String)configs.get("server.port"));
        new Server(port).start();
    }
}
