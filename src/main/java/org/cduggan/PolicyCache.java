package org.cduggan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class PolicyCache {
    private final Set<String> deniedUrlsCache = new HashSet<>();
    private final long cacheExpiryMillis;
    private long lastLoadTime;
    private final DatabaseConnection dbc;

    public PolicyCache(long cacheExpiryMillis, DatabaseConnection dbc) {
        this.cacheExpiryMillis = cacheExpiryMillis;
        this.dbc = dbc;
        this.lastLoadTime = System.currentTimeMillis();
//        populate();
    }

    private void populate() {
        deniedUrlsCache.clear();

        try (Connection conn = dbc.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT url FROM DeniedURLs")) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String url = rs.getString("url");
                deniedUrlsCache.add(url);
            }
            lastLoadTime = System.currentTimeMillis();
            System.out.println("Denied URLs cache populated from database.");

        } catch (SQLException e) {
            System.err.println("Error loading denied URLs from database: " + e.getMessage());
        }
    }

    public boolean isUrlDenied(String url) {
        return false;
//        if (System.currentTimeMillis() - lastLoadTime >= cacheExpiryMillis) {
//            populate();
//        }
//        return deniedUrlsCache.stream().anyMatch(url::contains);
    }

    public void addDeniedUrl(String url) {
        deniedUrlsCache.add(url);

        try (Connection conn = dbc.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO DeniedURLs (url) VALUES (?)")) {

            stmt.setString(1, url);
            stmt.executeUpdate();
            System.out.println("URL added to denied list in database and cache.");

        } catch (SQLException e) {
            System.err.println("Error adding denied URL to database: " + e.getMessage());
        }
    }
}
