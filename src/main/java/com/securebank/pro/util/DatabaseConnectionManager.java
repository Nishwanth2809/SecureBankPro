package com.securebank.pro.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class DatabaseConnectionManager {

    private static final String CONNECTION_URL = "jdbc:h2:mem:securebank;DB_CLOSE_DELAY=-1";
    private static final int POOL_SIZE = 5;
    private static final BlockingQueue<Connection> pool = new LinkedBlockingQueue<>(POOL_SIZE);
    private static final ThreadLocal<Connection> threadConnection = new ThreadLocal<>();
    
    static {
        // Load H2 driver and pre-populate the connection pool
        try {
            Class.forName("org.h2.Driver");
            for (int i = 0; i < POOL_SIZE; i++) {
                Connection conn = DriverManager.getConnection(CONNECTION_URL, "sa", "");
                pool.offer(conn);
            }
            BankLogger.info("Connection pool initialized with size: " + POOL_SIZE);
        } catch (Exception e) {
            BankLogger.severe("Failed to initialize DatabaseConnectionManager pool", e);
            throw new RuntimeException("Database pool initialization failed", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        // If there is an active transaction context for the thread, use the bound connection
        Connection physicalConn = threadConnection.get();
        boolean isTransactional = (physicalConn != null);

        if (!isTransactional) {
            // Retrieve a connection from the pool
            try {
                physicalConn = pool.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SQLException("Leasing database connection was interrupted", e);
            }
        }

        final Connection targetConn = physicalConn;

        // Return a proxy to intercept close() and prevent early closure or pool return
        return (Connection) Proxy.newProxyInstance(
            DatabaseConnectionManager.class.getClassLoader(),
            new Class<?>[]{Connection.class},
            (proxy, method, args) -> {
                if ("close".equals(method.getName())) {
                    if (isTransactional) {
                        // Do not close or return connection during active transaction
                        return null;
                    }
                    pool.offer(targetConn);
                    return null;
                }
                return method.invoke(targetConn, args);
            }
        );
    }

    public static void startTransaction() throws SQLException {
        if (threadConnection.get() != null) {
            throw new IllegalStateException("Transaction already active on this thread.");
        }
        
        Connection physicalConn;
        try {
            physicalConn = pool.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Failed to lease connection for transaction start", e);
        }
        
        physicalConn.setAutoCommit(false);
        threadConnection.set(physicalConn);
    }

    public static void commitTransaction() throws SQLException {
        Connection physicalConn = threadConnection.get();
        if (physicalConn == null) {
            throw new IllegalStateException("No active transaction found to commit.");
        }
        try {
            physicalConn.commit();
        } finally {
            cleanupTransaction(physicalConn);
        }
    }

    public static void rollbackTransaction() {
        Connection physicalConn = threadConnection.get();
        if (physicalConn == null) {
            return;
        }
        try {
            physicalConn.rollback();
        } catch (SQLException e) {
            BankLogger.severe("Failed to rollback database transaction", e);
        } finally {
            cleanupTransaction(physicalConn);
        }
    }

    private static void cleanupTransaction(Connection physicalConn) {
        threadConnection.remove();
        try {
            physicalConn.setAutoCommit(true);
        } catch (SQLException e) {
            BankLogger.severe("Failed to reset auto-commit status on transaction cleanup", e);
        }
        pool.offer(physicalConn);
    }

    public static void initializeDatabase() {
        BankLogger.info("Initializing H2 Database with schema.sql...");
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Read schema.sql from resources
            InputStream in = DatabaseConnectionManager.class.getClassLoader().getResourceAsStream("schema.sql");
            if (in == null) {
                throw new IllegalStateException("schema.sql could not be found in resources folder.");
            }

            String sql = new BufferedReader(new InputStreamReader(in))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // H2 executes multiple statements separated by semicolons if enabled, 
            // but to be safe and robust, we split by semicolon and run them one by one.
            String[] commands = sql.split(";");
            for (String command : commands) {
                String cleanCommand = command.trim();
                if (!cleanCommand.isEmpty()) {
                    stmt.execute(cleanCommand);
                }
            }
            BankLogger.info("H2 Database initialization completed successfully.");
            
        } catch (Exception e) {
            BankLogger.severe("H2 Database initialization failed", e);
            throw new RuntimeException("Database initialization failed: " + e.getMessage(), e);
        }
    }
}
