package com.navigatorTB_Nymph.tool.sql;

import com.navigatorTB_Nymph.pluginMain.PluginMain;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JDBCUtils {

    private static final Map<String, SQLiteJDBC> jdbcMap = new HashMap<>();

    public static SQLiteJDBC getJDBC(String dbName) {
        if (jdbcMap.containsKey(dbName)) {
            return jdbcMap.get(dbName);
        }
        Path path = PluginMain.INSTANCE.resolveDataPath(dbName);
        SQLiteJDBC jdbc = new SQLiteJDBC(path);
        jdbcMap.put(dbName, jdbc);
        return jdbc;
    }

    public static void insert(SQLiteJDBC jdbc, String table, Map<String, Object> entity) {
        List<String> column = new ArrayList<>();
        List<String> value = new ArrayList<>();
        for (Map.Entry<String, Object> e : entity.entrySet()) {
            column.add(e.getKey());
            value.add(e.getValue().toString());
        }
        jdbc.insert(table, column.toArray(new String[0]), value.toArray(new String[0]), "");
    }

    public static void update(SQLiteJDBC jdbc, String table, Map<String, Object> entity, String condition) {
        String sql = "update %s set ";
        List<String> columns = new ArrayList<>();
        for (Map.Entry<String, Object> e : entity.entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();
            String s = " %s = '%s'";
            columns.add(String.format(s, key, value));
        }
        sql += String.join(",", columns);
        sql += " where " + condition;
        jdbc.executeQuerySQL(sql, "");
    }

    public static void insert(SQLiteJDBC jdbc, String table, List<Map<String, Object>> entities) {
        for (Map<String, Object> entity : entities) {
            insert(jdbc, table, entity);
        }
    }
}
