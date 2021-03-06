package core;

import java.io.*;
import java.sql.*;

public class SQLCommunication {

    private static String address;
    private static String port;
    private static String connectionUrl;
    private static String user;
    private static String password;

    private static Connection con;
    private static Statement stmt;
    private static ResultSet rs;

    public SQLCommunication() throws ClassNotFoundException, SQLException, IOException {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        readConfigurationFile("SQLConfig.txt");
        con = DriverManager.getConnection(connectionUrl, user, password);
        stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
    }

    public static void main(String[] args) {

        try {
            SQLCommunication serv = new SQLCommunication();
            String[][] result;

//            deleteRowsWhere("users", "login", "JavaLogin"); // unregistering
//
//            //REGISTRATION - login has to be an unique value
//            System.out.println("Registeration and signing in..");
//            addToTable("users", "login", "JavaLogin", "password", Hasher.hash("SIEMKA"));
//            //LOGIN
//            result = getFromTableWhere("users", "login", "JavaLogin", "password", Hasher.hash("SIEMKA"));
//            if( result != null)
//                System.out.println("Signed in!");
//            else
//                System.out.println("Invalid credentials!");
//            System.out.println();
//
//            System.out.println("Various tests:");
//            getFromTable("fitnessTraining", "discipline", "favourite");
//            getFromTableWhere( "fitnessTraining", "favourite", "1", "+", "discipline");
//            customQuery("Select * from users");
//
//            addToTable( "fitnessTraining", "discipline", "squats", "repeats", "800", "time", "400", "calories", "6969","favourite","0", "login","Janus", "date","010218");
//            updateRowsTo("fitnessTraining", "discipline", "squats2", "where", "calories", "6969");
//            System.out.println();
//
//            deleteRowsWhere("users", "login", "JavaLogin");
//            System.out.println("Signing in.. (invalid)");
//            result = getFromTableWhere("users", "login", "JavaLogin", "password", Hasher.hash("SIEMKA"));
//            if( result != null)
//                System.out.println("Signed in!");
//            else
//                System.out.println("Invalid credentials!");

            result = customQuery("Select dt.time from DistanceTraining dt where dt.date like '%'");
//            String [][] a = rsToStringMatrix(result);
            System.out.println(result);

        } catch ( Exception e ){
            System.out.println("SQLCommunication error at main: " + e.getMessage());
        }
    }

    public static String[][] customQuery(String query){
        try{
            rs = stmt.executeQuery( query );
            return rsToStringMatrix( rs );

        } catch ( SQLException e ){
            System.out.println("SQLCommunication error at customQuery: " + e.getMessage());
            return null;
        }
    }

    public static String[][] getFromTable(String... args) {
        /* Use samples:
         * getFromTableWhere( "users", "name");
         * getFromTableWhere("users", "name", "surname");
         */
        try {
            String query = buildSelectQuery(args);
            rs = stmt.executeQuery(query);

            return rsToStringMatrix(rs);

        } catch (SQLException ex) {
            System.out.println("SQLCommunication error at getFromTable: " + ex.getMessage());
            return null;
        } catch (NullPointerException ex) {
            System.out.println("SQLCommunication error at getFromTable: " + "too few arguments!");
            return null;
        }
    }

    public static String[][] getFromTableWhere(String... args) {
        /* Use samples:
         * getFromTableWhere( "users", "name", "Daniel");
         * getFromTableWhere("users", "name", "Daniel", "age", "14");
         * getFromTableWhere("users", "name", "Daniel", "age", "14", '+', "surname");
         */
        try {
            String query = buildSelectWhereQuery(args);
            rs = stmt.executeQuery(query);
            return rsToStringMatrix( rs );
            // return rsToStringMatrix(rs, selectArgsGetFromTableWhere(args));

        } catch (SQLException ex) {
            System.out.println("SQLCommunication error at getFromTableWhere: " + ex.getMessage());
            return null;
        } catch (NullPointerException ex) {
            System.out.println("SQLCommunication error at getFromTableWhere: " + "too few arguments!");
            return null;
        }
    }

    public static void addToTable(String... args) {
        /* Use samples:
         * addToTable( "fitnessTraining", "discipline", "squats", "repeats", "40", "time", "50", "calories", "112", "favourite", "1");
         */
        try {
            String query = buildAddQuery(args);
            stmt.executeUpdate(query);

        } catch (SQLException ex) {
            System.out.println("SQLCommunication error at addToTable: " + ex.getMessage());
        }
    }

    public static void updateRowsTo ( String... args ){
        /* Use samples:
         *   updateRowsTo( "users", "name", "Jan", "where", "login", "Janek1990");
         *       updates rows in the column NAME to 'Jan' in table USERS where LOGIN is 'Janek1990'
         */
        try{
            String query = buildUpdateQuery(args);
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            System.out.println("SQLCommunication error at updateRowsWhere: " + e.getMessage());
        }
    }

    public static void deleteRowsWhere(String... args) {
        /* Use samples:
         *  deleteRowsWhere("users", "login", "Smith1990");
         */
        try {
            String query = buildDeleteQuery(args);
            stmt.executeUpdate(query);

        } catch ( SQLException ex ){
            System.out.println("SQLCommunication error at deleteRowsWhere: " + ex.getMessage());
        }
    }

    public static String buildUpdateQuery(String... args ) {
        // Sample query: update users set age = 40, name = 'Janek' where login = 'janus' and height = 192

        try {
            if (args.length < 6)
                throw new IllegalArgumentException("SQLCommunication at updateRowsTo: recived too few arguments!");

            int whereAt = 0;
            for (int i = 0; i < args.length; i++) {
                if ( args[i].toLowerCase().equals("where"))
                    whereAt = i;
            }
            if (whereAt == 0)
                throw new IllegalArgumentException("SQLcommunication at updateRowsTo: missing condition arguments! Make sure to use 'where' operand");

            StringBuilder sb = new StringBuilder("update ");
            sb.append(args[0]).append(" set ").append(args[1]).append(" = '").append(args[2]).append("'");
            for (int i = 3; i < whereAt; i = i + 2) {
                sb.append(", ").append(args[i]).append(" = '").append(args[i + 1]).append("'");
            }
            sb.append(" where ").append(args[whereAt + 1]).append(" = '").append(args[whereAt + 2]).append("'");
            for (int i = whereAt + 3; i < args.length; i = i + 2) {
                sb.append(" and ").append(args[i]).append(" = '").append(args[i + 1]).append("'");
            }

            return sb.toString();
        } catch ( ArrayIndexOutOfBoundsException e){
            throw new IllegalArgumentException("SQLCommunication at UpdateRowsTo: recived too few arguments! Missing condition arguments!");
        }
    }

    public static String buildSelectQuery(String... args) {
        if (args.length < 2) {
            return null;
        }

        String query = "Select ";
        StringBuilder sb = new StringBuilder(query);

        sb.append(args[1]);
        for (int i = 2; i < args.length; i++) {
            sb.append(", ").append(args[i]);
        }
        sb.append(" from ").append(args[0]);
        query = sb.toString();

        return query;
    }

    public static String buildSelectWhereQuery(String... args) {
        if (args.length < 3) {
            return null;
        }

        int extraAt = 0;
        for (int i = 0; i < args.length; i++) {
            if ((args[i]).equals("+")) {
                extraAt = i;
            }
        }
        if (extraAt == 0) {
            extraAt = args.length;
        }

        String query = "Select ";
        StringBuilder sb = new StringBuilder(query);
        sb.append(args[1]);
        for (int i = 3; i < extraAt; i = i + 2) {
            sb.append(", ").append(args[i]);
        }
        if (extraAt != args.length) {
            for (int i = extraAt + 1; i < args.length; i++) {
                sb.append(", ").append(args[i]);
            }
        }
        sb.append(" from ").append(args[0]);
        sb.append(" where ");
        sb.append(args[1]).append(" = '").append(args[2]).append("'");
        for (int i = 3; i+1 < extraAt; i = i + 2) {
            sb.append(" and ").append(args[i]).append(" = '").append(args[i + 1]).append("'");
        }

        query = sb.toString();
        return query;
    }

    public static String buildAddQuery(String... args) {
        //addToTable( "fitnessTraining", "discipline", "squats", "400", "time","700","calories", "6969","favourite","0", "login","Janus", "date","010218");

        String query = ("insert into ");
        StringBuilder sb = new StringBuilder(query);
        sb.append(args[0]).append(" ( ");
        sb.append(args[1]);

        for (int i = 3; i < args.length; i = i + 2) {
            sb.append(", ").append(args[i]);
        }
        sb.append(" ) ").append("values ( ");

        sb.append("'").append(args[2]).append("'");
        for (int i = 4; i < args.length; i = i + 2) {
            sb.append(", ").append("'").append(args[i]).append("'");
        }
        sb.append(")");

        return sb.toString();
    }

    public static String buildDeleteQuery(String... args) {
        // Sample query: delete from miasta where id_miasta ='4' and xyz = 'ABC'

        StringBuilder sb = new StringBuilder("delete from ");
        sb.append(args[0]).append(" where ");
        sb.append(args[1]).append(" = '").append(args[2]).append("'");
        for( int i = 3; i < args.length; i = i + 2)
            sb.append(" and ").append(args[i]).append(" = '").append(args[i+1]).append("'");

        return sb.toString();
    }

    public static String[][] rsToStringMatrix(ResultSet rs) {
        try {
            if(!rs.next())
                return null;

            rs.last();
            int rowNumber = rs.getRow();
            rs.first();

            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            String[] columns = new String[columnCount];
            for (int i = 1; i <= columnCount; i++ ) {
                columns[i-1] = rsmd.getColumnName(i);
            }

            rs.first();
            String matrix[][] = new String[rowNumber][columnCount];
            for (int i = 0; i < rowNumber; i++) {
                for (int j = 0; j < columnCount; j++) {
                    matrix[i][j] = (rs.getString(columns[j]));
                }
                rs.next();
            }
            return matrix;
        } catch (SQLException ex) {
            System.out.println("SQLCommunication error at rsToStringMatrix: " + ex.getMessage());
            return null;
        }
    }

    public static void readConfigurationFile(String path) throws IOException {
        FileReader fr = new FileReader(new File(path));
        BufferedReader br = new BufferedReader(fr);
        String line;
        int lines = 0, parameters = 4;
        while ((line = br.readLine()) != null) {
            if (lines++ == parameters) {
                break;
            }
            String[] p = line.split("\\s+");

            switch (p[0]) {
                case "user":
                    user = p[1];
                    break;
                case "password":
                    password = p[1];
                    break;
                case "ip":
                    address = p[1];
                    break;
                case "port":
                    port = p[1];
                    break;
                default:
                    throw new IOException("Unrecognizable parameter '" + p[0] + "' in file " + path);
            }
        }
        if (lines == parameters || lines == parameters + 1) {
            connectionUrl = "jdbc:sqlserver://" + address + ":" + port + ";integrateSecurity=true";
        } else {
            throw new IOException("Configuration settings are wrong in file: " + path);
        }
    }

    public static void printStringMatrix(String[][] matrix) {

        try {
            if (matrix == null) {
                System.out.println("SQLCommunication error at printStringMatrix: received pointer to null!");
                return;
            }
            for (String[] m : matrix) {
                for (String s : m) {
                    System.out.print(s + " ");
                }
                System.out.println();
            }
        } catch ( NullPointerException e){
            System.out.println("SQLCommunication error at printStringMatrix: received pointer to null!");
        }
    }
}