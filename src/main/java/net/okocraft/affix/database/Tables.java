package net.okocraft.affix.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import net.okocraft.affix.database.Database.DatabaseType;

/**
 * データを主に保存しているテーブル
 * 
 * プレイヤーデータテーブル
 * id | playerId | type (0 for prefix, 1 for suffix) | affix (prefix or suffix string) | expire (unixtime) | isUnique (0 is for everyone affix, 1 is for only first player)
 * 1  | 1        | 0                                 | test                            | 0                 | 0
 * 2  | 2        | 0                                 | &c&lboldshg                     | 0                 | 1
 * 3  | 3        | 1                                 | a                               | 0                 | 0
 * 4  | 4        | 1                                 | i                               | 0                 | 0
 * 5  | 5        | 1                                 | u                               | 0                 | 0
 * ...
 */
public final class Tables {

    private static final String MASTER_TABLE_NAME = "affix_master";
    private static final String PLAYER_TABLE_NAME = "affix_player";

    private final Database database;

    Tables(Database database) {
        this.database = database;

        database.execute(
            "CREATE TABLE IF NOT EXISTS " + PLAYER_TABLE_NAME + " (" +
                "id INTEGER PRIMARY KEY " + database.getDatabaseType().autoIncrement + ", " +
                "uuidMost BIGINT NOT NULL, " +
                "uuidLeast BIGINT NOT NULL, " +
                "name VARCHAR(16) NOT NULL DEFAULT '', " +
                "UNIQUE(uuidMost, uuidLeast)" +
                (database.getDatabaseType() == DatabaseType.SQLITE ? "" : ", INDEX nameindex (name)") +
            ")"
        );

        database.execute("CREATE TABLE IF NOT EXISTS " + MASTER_TABLE_NAME + " (" +
                "id INTEGER PRIMARY KEY " + database.getDatabaseType().autoIncrement + ", " +
                "playerId INTEGER NOT NULL, " +
                "type BYTE NOT NULL, " +
                "affix VARCHAR(255) NOT NULL, " +
                "expire BIGINT, " +
                "isUnique BYTE NOT NULL DEFAULT 0, " +
                "FOREIGN KEY (playerId) REFERENCES " + PLAYER_TABLE_NAME + "(id) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "UNIQUE(playerId, affix), UNIQUE(type, affix, isUnique)" +
                (database.getDatabaseType() == DatabaseType.SQLITE ? "" : ", INDEX playerindex (playerId)") +
        ")");

        if (database.getDatabaseType() == DatabaseType.SQLITE) {
            database.execute("CREATE INDEX IF NOT EXISTS playerindex ON " + MASTER_TABLE_NAME + "(playerId)");
        }

    }

    public void registerPlayer(OfflinePlayer player) {
        UUID uid = player.getUniqueId();
        long most = uid.getMostSignificantBits();
        long least = uid.getLeastSignificantBits();
        String name = player.getName();
        if (name != null) {
            name.toLowerCase(Locale.ROOT);
        }
        String sql = "INSERT INTO " + PLAYER_TABLE_NAME + " (uuidMost, uuidLeast, name) VALUES (" + most + ", " + least + ", '" + name + "') ON ";
        database.execute(sql + (database.getDatabaseType() == DatabaseType.SQLITE ? "CONFLICT(uuidMost, uuidLeast) DO NOTHING" : "DUPLICATE KEY UPDATE uuidMost = uuidMost"));
    }

    public int getPlayerIdByUUID(UUID uid) {
        long most = uid.getMostSignificantBits();
        long least = uid.getLeastSignificantBits();
        return database.query("SELECT id FROM " + PLAYER_TABLE_NAME + " WHERE uuidMost = " + most + " AND uuidLeast = " + least, rs -> {
            try {
                if (rs.next()) return rs.getInt("id");
            } catch (SQLException ignored) {
            }

            return -1;
        });
    }
    
    public int getPlayerIdByName(String name) {
        return database.query("SELECT id FROM " + PLAYER_TABLE_NAME + " WHERE name = '" + name.toLowerCase(Locale.ROOT) + "' LIMIT 1", rs -> {
            try {
                if (rs.next()) return rs.getInt("id");
            } catch (SQLException ignored) {
            }
    
            return -1;
        });
    }
    
    public OfflinePlayer getPlayerById(int id) {
        return database.query("SELECT uuidMost, uuidLeast FROM " + PLAYER_TABLE_NAME + " WHERE id = " + id, rs -> {
            try {
                if (rs.next()) return Bukkit.getOfflinePlayer(new UUID(rs.getLong("uuidMost"), rs.getLong("uuidLeast")));
            } catch (SQLException ignored) {
            }
    
            return null;
        });
    }

    public List<String> getAllPlayerName() {
        List<String> names = new ArrayList<>();
        database.query("SELECT name FROM " + PLAYER_TABLE_NAME, rs -> {
            try {
                while (rs.next()) {
                    names.add(rs.getString("name"));
                }
            } catch (SQLException ignored) {
            }
            return null;
        });
        return names;
    }

    public int getPrefixId(OfflinePlayer player, String prefix) {
        return database.query("SELECT id FROM " + MASTER_TABLE_NAME + " WHERE type = 0 AND affix = '" + prefix + "' " + createPlayerIdCondition(player) + " LIMIT 1", rs -> {
            try {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            } catch (SQLException e) {
            }
            return -1;
        });
    }

    public int getSuffixId(OfflinePlayer player, String prefix) {
        return database.query("SELECT id FROM " + MASTER_TABLE_NAME + " WHERE type = 1 AND affix = '" + prefix + "' " + createPlayerIdCondition(player) + " LIMIT 1", rs -> {
            try {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            } catch (SQLException e) {
            }
            return -1;
        });
    }

    public String getAffixById(int id) {
        return database.query("SELECT affix FROM " + MASTER_TABLE_NAME + " WHERE id = " + id, rs -> {
            try {
                if (rs.next()) {
                    return rs.getString("affix");
                }
            } catch (SQLException e) {
            }
            return null;
        });
    }

    public boolean updatePlayerName(UUID uid, @Nullable String newName) {
        long most = uid.getMostSignificantBits();
        long least = uid.getLeastSignificantBits();
        if (newName == null) {
            newName = "";
        }
        newName.toLowerCase(Locale.ROOT);
        return database.execute("UPDATE " + PLAYER_TABLE_NAME + " SET name = '" + newName + "' WHERE uuidMost = " + most + " AND uuidLeast = " + least);
    }

    public boolean existsPrefix(String prefix) {
        return database.query("SELECT id FROM " + MASTER_TABLE_NAME + " WHERE type = 0 AND affix = '" + prefix + "' LIMIT 1", rs -> {
            try {
                return rs.next();
            } catch (SQLException ignored) {
            }

            return false;
        });
    }

    public boolean existsSuffix(String prefix) {
        return database.query("SELECT id FROM " + MASTER_TABLE_NAME + " WHERE type = 1 AND affix = '" + prefix + "' LIMIT 1", rs -> {
            try {
                return rs.next();
            } catch (SQLException ignored) {
            }

            return false;
        });
    }

    public boolean isUniquePrefix(String prefix) {
        return database.query("SELECT isUnique FROM " + MASTER_TABLE_NAME + " WHERE type = 0 AND affix = '" + prefix + "'", rs -> {
            try {
                int rowCount = 0;
                while (rs.next()) {
                    rowCount++;
                }
                return rowCount == 1 && rs.first() && rs.getByte("isUnique") == (byte) 1;
            } catch (SQLException e) {
            }
            return false;
        });
    }

    public boolean isUniqueSuffix(String suffix) {
        return database.query("SELECT isUnique FROM " + MASTER_TABLE_NAME + " WHERE type = 1 AND affix = '" + suffix + "'", rs -> {
            try {
                int rowCount = 0;
                while (rs.next()) {
                    rowCount++;
                }
                return rowCount == 1 && rs.first() && rs.getByte("isUnique") == (byte) 1;
            } catch (SQLException e) {
            }
            return false;
        });
    }

    public boolean isAvailablePrefix(String prefix, boolean asUnique) {
        String sql = asUnique
            ? "SELECT isUnique FROM " + MASTER_TABLE_NAME + " WHERE type = 0 AND affix = '" + prefix + "' LIMIT 1"
            : "SELECT isUnique FROM " + MASTER_TABLE_NAME + " WHERE isUnique = 1 AND type = 0 AND affix = '" + prefix + "' LIMIT 1";
        return database.query(sql, rs -> {
            try {
                return !rs.next();
            } catch (SQLException ignored) {
            }
            return false;
        });
    }

    public boolean isAvailableSuffix(String prefix, boolean asUnique) {
        String sql = asUnique
            ? "SELECT isUnique FROM " + MASTER_TABLE_NAME + " WHERE type = 1 AND affix = '" + prefix + "' LIMIT 1"
            : "SELECT isUnique FROM " + MASTER_TABLE_NAME + " WHERE isUnique = 1 AND type = 1 AND affix = '" + prefix + "' LIMIT 1";
        return database.query(sql, rs -> {
            try {
                return !rs.next();
            } catch (SQLException ignored) {
            }
            return false;
        });
    }

    public boolean setUniquePrefix(String prefix, boolean isUnique) {


        if (!isUniquePrefix(prefix) == isUnique) {
            return database.execute("UPDATE " + MASTER_TABLE_NAME + " SET isUnique = " + (isUnique ? 0 : 1) + " WHERE type = 0 AND affix = '" + prefix + "'");
        }
        return false;
    }

    public boolean setUniqueSuffix(String suffix, boolean isUnique) {
        if (!isUniqueSuffix(suffix) == isUnique) {
            return database.execute("UPDATE " + MASTER_TABLE_NAME + " SET isUnique = " + (isUnique ? 0 : 1) + " WHERE type = 1 AND affix = '" + suffix + "'");
        }
        return false;
    }

    public boolean removePrefix(OfflinePlayer player, String prefix) {
        return database.execute("DELETE FROM " + MASTER_TABLE_NAME + " WHERE affix = '" + prefix + "' AND " + createPlayerIdCondition(player));
    }

    public boolean removeExpiredPrefix(OfflinePlayer player, String prefix, long expire) {
        return database.execute("DELETE FROM " + MASTER_TABLE_NAME + " WHERE affix = '" + prefix + "' AND expire < " + expire + " AND " + createPlayerIdCondition(player));
    }

    public boolean removeSuffix(OfflinePlayer player, String suffix) {
        return database.execute("DELETE FROM " + MASTER_TABLE_NAME + " WHERE affix = '" + suffix + "' AND " + createPlayerIdCondition(player));
    }

    public boolean removeExpiredSuffix(OfflinePlayer player, String suffix, long expire) {
        return database.execute("DELETE FROM " + MASTER_TABLE_NAME + " WHERE affix = '" + suffix + "' AND expire < " + expire + " AND " + createPlayerIdCondition(player));
    }

    public boolean addPrefix(OfflinePlayer player, String prefix) {
        int playerId = getPlayerIdByUUID(player.getUniqueId());
        if (playerId == -1) {
            return false;
        }
        return database.execute("INSERT INTO " + MASTER_TABLE_NAME + " (playerId, type, affix, isUnique) VALUES (" + playerId + ", 0 , '" + prefix + "', 0)");
    }

    public boolean addPrefix(OfflinePlayer player, String prefix, long expire) {
        int playerId = getPlayerIdByUUID(player.getUniqueId());
        if (playerId == -1) {
            return false;
        }
        return database.execute("INSERT INTO " + MASTER_TABLE_NAME + " (playerId, type, affix, expire, isUnique) VALUES (" + playerId + ", 0 , '" + prefix + "', " + expire + ", 0)");
    }

    public boolean addPrefix(OfflinePlayer player, String prefix, boolean isUnique) {
        int playerId = getPlayerIdByUUID(player.getUniqueId());
        if (playerId == -1) {
            return false;
        }
        return database.execute("INSERT INTO " + MASTER_TABLE_NAME + " (playerId, type, affix, isUnique) VALUES (" + playerId + ", 0 , '" + prefix + "', " + (isUnique ? 1 : 0) + ")");
    }

    public boolean addPrefix(OfflinePlayer player, String prefix, long expire, boolean isUnique) {
        int playerId = getPlayerIdByUUID(player.getUniqueId());
        if (playerId == -1) {
            return false;
        }
        return database.execute("INSERT INTO " + MASTER_TABLE_NAME + " (playerId, type, affix, expire, isUnique) VALUES (" + playerId + ", 0 , '" + prefix + "', " + expire + ", " + (isUnique ? 1 : 0) + ")");
    }

    public boolean addSuffix(OfflinePlayer player, String suffix) {
        int playerId = getPlayerIdByUUID(player.getUniqueId());
        if (playerId == -1) {
            return false;
        }
        return database.execute("INSERT INTO " + MASTER_TABLE_NAME + " (playerId, type, affix, isUnique) VALUES (" + playerId + ", 0 , '" + suffix + "', 0)");
    }

    public boolean addSuffix(OfflinePlayer player, String suffix, long expire) {
        int playerId = getPlayerIdByUUID(player.getUniqueId());
        if (playerId == -1) {
            return false;
        }
        return database.execute("INSERT INTO " + MASTER_TABLE_NAME + " (playerId, type, affix, expire, isUnique) VALUES (" + playerId + ", 0 , '" + suffix + "', " + expire + ", 0)");
    }

    public boolean addSuffix(OfflinePlayer player, String suffix, boolean isUnique) {
        int playerId = getPlayerIdByUUID(player.getUniqueId());
        if (playerId == -1) {
            return false;
        }
        return database.execute("INSERT INTO " + MASTER_TABLE_NAME + " (playerId, type, affix, isUnique) VALUES (" + playerId + ", 1 , '" + suffix + "', " + (isUnique ? 1 : 0) + ")");
    }

    public boolean addSuffix(OfflinePlayer player, String suffix, long expire, boolean isUnique) {
        int playerId = getPlayerIdByUUID(player.getUniqueId());
        if (playerId == -1) {
            return false;
        }
        return database.execute("INSERT INTO " + MASTER_TABLE_NAME + " (playerId, type, affix, expire, isUnique) VALUES (" + playerId + ", 0 , '" + suffix + "', " + expire + ", " + (isUnique ? 1 : 0) + ")");
    }

    private static final String SELECT_AFFIX = "SELECT affix " + " FROM " + MASTER_TABLE_NAME;

    public List<String> getAllPrefixes() {
        return query(SELECT_AFFIX + " WHERE type = 0");
    }

    public List<String> getAllSuffixes() {
        return query(SELECT_AFFIX + " WHERE type = 1");
    }

    public List<String> getAllAffixes() {
        return query(SELECT_AFFIX);
    }

    public List<String> getPrefixes(OfflinePlayer player) {
        return query(SELECT_AFFIX + " WHERE type = 0 AND " + createPlayerIdCondition(player));
    }

    public List<String> getSuffixes(OfflinePlayer player) {
        return query(SELECT_AFFIX + " WHERE type = 1 AND " + createPlayerIdCondition(player));
    }
    
    public List<String> getAffixes(OfflinePlayer player) {
        return query(SELECT_AFFIX + " WHERE " + createPlayerIdCondition(player));
    }

    public List<String> getPrefixes(OfflinePlayer player, long expire) {
        return query(SELECT_AFFIX + " WHERE expire > " + expire + " AND type = 0 AND " + createPlayerIdCondition(player));
    }

    public List<String> getSuffixes(OfflinePlayer player, long expire) {
        return query(SELECT_AFFIX + " WHERE expire > " + expire + " AND type = 1 AND " + createPlayerIdCondition(player));
    }
    
    public List<String> getAffixes(OfflinePlayer player, long expire) {
        return query(SELECT_AFFIX + " WHERE expire > " + expire + " AND " + createPlayerIdCondition(player));
    }
    
    private String createPlayerIdCondition(OfflinePlayer player) {
        UUID uid = player.getUniqueId();
        long most = uid.getMostSignificantBits();
        long least = uid.getLeastSignificantBits();
        return "playerId = (SELECT id FROM " + PLAYER_TABLE_NAME + " WHERE uuidMost = " + most + " AND least = " + least + " LIMIT 1)";
    }
    
    private List<String> query(String sql) {
        List<String> data = new ArrayList<>();
        database.query(sql, rs -> {
            try {
                while (rs.next()) {
                    data.add(rs.getString("affix"));
                }
            } catch (SQLException e) {
            }
            return null;
        });
        return data;
    }

}