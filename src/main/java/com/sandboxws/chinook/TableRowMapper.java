package com.sandboxws.chinook;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * Maps a HashMap to a BiigQuery TableRow.
 *
 * @author Ahmed El.Hussaini
 */
public class TableRowMapper {
    public static HashMap<String, Object> rowMap;
    public static String rowAsJson;

    public static HashMap<String, Object> asMap(ResultSet rs, String tableName, String pk) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        rowMap = new HashMap<String, Object>();
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        int columns = md.getColumnCount();
        for (int i = 1; i <= columns; i++) {
            if (md.getColumnName(i).equals(pk)) {
                continue;
            }
            String columnName = md.getColumnName(i);
            // Foreign keys
            if (columnName.matches(".*Id$")) {
                columnName = columnName.replace("Id", "_id");
            }
            columnName = columnName.toLowerCase();

            switch (md.getColumnTypeName(i)) {
                case "varchar":
                    rowMap.put(columnName, rs.getString(i));
                    break;

                case "int4":
                    rowMap.put(columnName, rs.getString(i));
                    break;

                case "timestamp":
                    String name = md.getColumnName(i);
                    if (name.equals("created_at") || name.equals("updated_at")) {
                        rowMap.put(columnName, dateFormat.format(rs.getTimestamp(i)));
                    } else {
                        rowMap.put(name, rs.getTimestamp(i));
                    }
                    if (name.equals("created_at")) {
                        rowMap.put("d_date_id", new SimpleDateFormat("yyyyMMdd").format(rs.getTimestamp(i)));
                    }
                    break;

                case "bool":
                    rowMap.put(columnName, rs.getBoolean(i));
                    break;

                default:
                    rowMap.put(columnName, rs.getObject(i));
                    break;
            }
        }

        rowMap.put("id_" + tableName.toLowerCase(), rs.getString(pk));
        rowMap.put("dwh_created_at", dateFormat.format(date));

        return rowMap;
    }
}