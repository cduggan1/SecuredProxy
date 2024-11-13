package org.cduggan;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Policy extends PolicyCache {
    private final DatabaseConnection dbc;

    public Policy(long cacheExpiryMillis, DatabaseConnection dbc) {
        super(cacheExpiryMillis, dbc);
        this.dbc = dbc;
    }

    public boolean checkAndLogAccess(URL url,String requesterIp) {
        String urlString = url.toString();
        return false;
//        boolean isDenied = isUrlDenied(urlString);
//        logConnectionAttempt(urlString, requesterIp, isDenied);
//        return isDenied;
    }

    private void logConnectionAttempt(String url, String requesterIp, boolean isDenied) {
        try (Connection conn = dbc.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO ConnectionLog (url, requester_ip, status, timestamp) VALUES (?, ?, ?, ?)")) {

            stmt.setString(1, url);
            stmt.setString(2, requesterIp);
            stmt.setString(3, isDenied ? "DENIED" : "ALLOWED");
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();

        } catch (SQLException e) {
            Logger.log("Error logging connection attempt: " + e.getMessage(), true);
        }
    }
}
