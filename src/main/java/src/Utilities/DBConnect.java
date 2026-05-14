package src.Utilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnect {
    public Connection prepareConn() throws FileNotFoundException, IOException {
    Connection con = null;
    try {
        Properties prop = new Properties();
        InputStream input1 = null;
        input1 = new FileInputStream("vcbDormantconfig.properties");
        prop.load(input1);
        String dbuser = prop.getProperty("dbuser");
        String dbpass = prop.getProperty("dbpass");
        String dbport = prop.getProperty("dbport");
        String dbhost = prop.getProperty("dbhost");
        String dbsid = prop.getProperty("dbsid");
        String url = "jdbc:oracle:thin:@" + dbhost + ":" + dbport + ":" + dbsid;
        String username = dbuser;
        String password = dbpass;
        Class.forName("oracle.jdbc.driver.OracleDriver");
        con = DriverManager.getConnection(url, username, password);
        System.out.println("connection 1:" + con);
    } catch (Exception ex) {
        System.out.println("Properties file not loaded:" + ex);
    }
    return con;
}

    public void closeConn(Connection con) throws SQLException, IOException {
        if (con != null)
            con.close();
    }
}
