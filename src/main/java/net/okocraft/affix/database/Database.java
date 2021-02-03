package net.okocraft.affix.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Level;

import lombok.Getter;
import net.okocraft.affix.AffixPlugin;
import net.okocraft.affix.config.Config;

/**
 * データベースの接続を保持するクラス。
 * @author LazyGon
 */
public class Database {

    @Getter
    private final DatabaseType databaseType;
    
    private String mySQLHost;
    private String mySQLUser;
    private String mySQLPass;
    private int mySQLPort;
    private String mySQLDBName;
    
    private Path sqliteDBFile;

    private Connection connection;
    
    /** データベースへの接続。 */
    final AffixPlugin parent;

    @Getter
    final Tables tables;

    /**
     * コンストラクタ。データベースに接続する。
     * 
     * @param parent プラグイン
     * @param type データベースのタイプ。FLAT_FILEを指定するとIllegalArgumentExceptionをスローする。
     * @throws IOException SQLiteデータベースのファイルが作成できなかったとき
     * @throws SQLException JDBCドライバーが読み込めなかったとき
     * @throws SQLException コネクションを作れなかったとき
     */
    public Database(AffixPlugin parent, DatabaseType type) throws IOException, SQLException {
        this.parent = parent;
        this.databaseType = type;

        Config config = parent.getConfigManager().getConfig();

        if (databaseType == DatabaseType.SQLITE) {
            this.sqliteDBFile = parent.getDataFolder().toPath().resolve("data.db");
            if (!Files.exists(sqliteDBFile)) {
                Files.createFile(sqliteDBFile);
            }
            
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException | LinkageError e) {
                throw new SQLException("Error occurred on loading SQLite JDBC driver.", e);
            }
        } else if (databaseType == DatabaseType.MYSQL) {
            this.mySQLHost = config.getMysqlHost();
            this.mySQLUser = config.getMysqlUser();
            this.mySQLDBName = config.getMysqlDBName();
            this.mySQLPass = config.getMysqlPass();
            this.mySQLPort = config.getMysqlPort();
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException | LinkageError e) {
                throw new SQLException("Error occurred on loading MySQL connector.", e);
            }
        } else {
            throw new IllegalStateException("DatabaseType must be sqlite or mysql");
        }

        reloadConnection();
        if (databaseType == DatabaseType.SQLITE) {
            execute("PRAGMA foreign_keys = ON");
        }
        
        this.tables = new Tables(this);
    }

    /**
     * コネクションがクローズしていた場合、再作成する。クローズしていない場合は今あるコネクションを返す。
     * 再作成されたコネクションはconnectionのフィールドに格納される。
     * 
     * @return 今あるコネクションか、それが利用不能のときは新しいコネクション。
     */
    private Connection reloadConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }
        if (databaseType == DatabaseType.MYSQL) {
            Properties prop = new Properties();
            prop.put("user", mySQLUser);
            prop.put("password", mySQLPass);
            connection = DriverManager.getConnection(
                "jdbc:mysql://" + mySQLHost + ":" + mySQLPort + "/" + mySQLDBName + "?autoReconnect=true&useSSL=false",
                prop
            );
        } else if (databaseType == DatabaseType.SQLITE) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteDBFile);
        }

        return connection;
    }

    /**
     * データベース接続を切る。
     */
    public void dispose() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 指定した {@code SQL}を実行する。
     * 
     * @param SQL 実行するSQL文。メソッド内でPreparedStatementに変換される。
     * @return SQL文の実行に成功したかどうか
     */
    boolean execute(String SQL) {
        try (PreparedStatement preparedStatement = reloadConnection().prepareStatement(SQL)) {
            preparedStatement.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Error occurred on executing SQL: " + SQL);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 指定したINSERT文を実行する。AUTOINCREMENTによって新たに生成された数値を返す。
     * 値は挿入されたレコードの数だけ生成される。主キーがAUTOINCREMENTでない場合は値が生成されない。
     * INSERTではないSQLを実行する場合や、生成されたキーが必要ない場合は {@link org.bitbucket.ucchy.undine.database.Database#execute(String)} を使うこと。
     * 
     * @param insert 実行するSQL文。メソッド内でPreparedStatementに変換される。
     * @return AUTOINCREMENTで生成された数値のリスト
     */
    List<Integer> insert(String insert) {
        try (PreparedStatement preparedStatement = reloadConnection().prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            if (preparedStatement.executeUpdate() == 0) {
                return new ArrayList<>();
            }
            ResultSet rs = preparedStatement.getGeneratedKeys();
            List<Integer> newIds = new ArrayList<>();
            while (rs.next()) {
                newIds.add(rs.getInt(1));
            }
            return newIds;
        } catch (SQLException e) {
            System.err.println("Error occurred on executing SQL: " + insert);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 指定した {@code SQL}を実行し、結果を第二引数で処理する。第二引数の処理が終わった後に、ResultSetはクローズされる。
     * 
     * @param SQL 実行するSQL文。メソッド内でPreparedStatementに変換される。
     * @param function 実行結果を処理する関数。
     * @return fuctionの処理結果
     */
    <T> T query(String SQL, Function<ResultSet, T> function) {
        try (PreparedStatement preparedStatement = reloadConnection().prepareStatement(SQL)) {
            return function.apply(preparedStatement.executeQuery());
        } catch (SQLException e) {
            parent.getLogger().log(Level.SEVERE, "Error occurred on executing SQL: " + SQL, e);
            return null;
        }
    }
    
    /**
     * 渡されたコレクションから、WHERE句の中で使えるIN(element, element, ...)という文字列を生成する。
     * @param <T> コレクションの型
     * @param collection コレクション
     * @return IN(element, element, ...) という文字列
     */
    static <T> String createIn(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return "IN()";
        }
        StringBuilder inBuilder = new StringBuilder("IN(");
        for (T element : collection) {
            if (element instanceof Number) {
                inBuilder.append(element.toString()).append(", ");
            } else {
                inBuilder.append("'").append(element.toString()).append("'").append(", ");
            }
        }
        inBuilder.delete(inBuilder.length() - 2, inBuilder.length()).append(")");
        return inBuilder.toString();
    }

    public enum DatabaseType {
        SQLITE("AUTOINCREMENT"),
        MYSQL("AUTO_INCREMENT");

        public final String autoIncrement;

        private DatabaseType(String autoIncrement) {
            this.autoIncrement = autoIncrement;
        }

        /**
         * 名前からデータベースタイプを取得する。
         * @param name 名前
         * @return データベースタイプ
         */
        public static DatabaseType getByName(String name) {
            for (DatabaseType type : values()) {
                if (type.name().toLowerCase(Locale.ROOT).replace("_", "").equals(name)) {
                    return type;
                }
            }

            return SQLITE;
        }
    }
}
