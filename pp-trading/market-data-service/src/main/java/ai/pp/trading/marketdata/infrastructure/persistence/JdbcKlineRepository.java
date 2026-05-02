package ai.pp.trading.marketdata.infrastructure.persistence;

import ai.pp.trading.marketdata.domain.model.Kline;
import ai.pp.trading.marketdata.domain.port.KlineRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * K线数据JDBC仓储实现
 * 使用JdbcTemplate操作TimescaleDB数据库
 */
@Repository
public class JdbcKlineRepository implements KlineRepository {

    /** 允许的表名白名单，防止SQL注入 */
    private static final Set<String> ALLOWED_TABLES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("kline_daily", "kline_weekly", "kline_monthly"))
    );

    private final JdbcTemplate jdbcTemplate;

    /** K线行映射器：将数据库行转换为K线领域模型 */
    private final RowMapper<Kline> rowMapper = (rs, rowNum) -> new Kline(
            rs.getTimestamp("time").toInstant().atZone(ZoneOffset.UTC).toLocalDate(),
            rs.getDouble("open"),
            rs.getDouble("high"),
            rs.getDouble("low"),
            rs.getDouble("close"),
            rs.getLong("volume")
    );

    public JdbcKlineRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Kline> findByRange(String tableName, String symbol, String market, LocalDate startDate, LocalDate endDate) {
        String resolvedTable = validateTableName(tableName);
        String sql = "SELECT time, open, high, low, close, volume"
                + " FROM " + resolvedTable
                + " WHERE symbol = ?"
                + " AND market = ?"
                + " AND time BETWEEN ? AND ?"
                + " ORDER BY time ASC";

        return jdbcTemplate.query(
                sql,
                rowMapper,
                symbol,
                market,
                Timestamp.from(startDate.atStartOfDay().toInstant(ZoneOffset.UTC)),
                Timestamp.from(endDate.plusDays(1).atStartOfDay().minusNanos(1).toInstant(ZoneOffset.UTC))
        );
    }

    @Override
    public void saveAll(String tableName, String symbol, String market, List<Kline> klines) {
        if (klines.isEmpty()) {
            return;
        }

        String resolvedTable = validateTableName(tableName);
        // 使用ON CONFLICT DO NOTHING避免重复插入
        String sql = "INSERT INTO " + resolvedTable + " (time, symbol, market, open, high, low, close, volume)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                + " ON CONFLICT DO NOTHING";

        jdbcTemplate.batchUpdate(sql, klines, klines.size(), (ps, kline) -> {
            ps.setTimestamp(1, Timestamp.from(kline.date().atStartOfDay().toInstant(ZoneOffset.UTC)));
            ps.setString(2, symbol);
            ps.setString(3, market);
            ps.setDouble(4, kline.open());
            ps.setDouble(5, kline.high());
            ps.setDouble(6, kline.low());
            ps.setDouble(7, kline.close());
            ps.setLong(8, kline.volume());
        });
    }

    /**
     * 验证表名是否在白名单中，防止SQL注入
     */
    private String validateTableName(String tableName) {
        if (!ALLOWED_TABLES.contains(tableName)) {
            throw new IllegalArgumentException("不支持的表名: " + tableName);
        }
        return tableName;
    }
}
