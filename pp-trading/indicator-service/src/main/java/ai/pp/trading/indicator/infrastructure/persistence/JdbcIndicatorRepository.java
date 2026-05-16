package ai.pp.trading.indicator.infrastructure.persistence;

import ai.pp.trading.indicator.domain.model.IndicatorValues;
import ai.pp.trading.indicator.domain.port.IndicatorRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * 技术指标JDBC仓储实现
 * 使用JdbcTemplate操作TimescaleDB数据库
 * 四类指标分别存储在ma_daily、macd_daily、rsi_daily、boll_daily四张表中
 */
@Repository
public class JdbcIndicatorRepository implements IndicatorRepository {

    private final JdbcTemplate jdbcTemplate;

    /** 指标行映射器：通过JOIN四张表将数据库行转换为IndicatorValues领域模型 */
    private final RowMapper<IndicatorValues> rowMapper = (rs, rowNum) -> new IndicatorValues(
            rs.getTimestamp("time").toInstant().atZone(ZoneOffset.UTC).toLocalDate(),
            getNullableDouble(rs, "ma5"),
            getNullableDouble(rs, "ma10"),
            getNullableDouble(rs, "ma20"),
            getNullableDouble(rs, "ma30"),
            getNullableDouble(rs, "ma60"),
            getNullableDouble(rs, "dif"),
            getNullableDouble(rs, "dea"),
            getNullableDouble(rs, "macd_hist"),
            getNullableDouble(rs, "rsi6"),
            getNullableDouble(rs, "rsi12"),
            getNullableDouble(rs, "rsi24"),
            getNullableDouble(rs, "upper_band"),
            getNullableDouble(rs, "middle_band"),
            getNullableDouble(rs, "lower_band")
    );

    public JdbcIndicatorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<IndicatorValues> findByRange(String symbol, String market, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT d.time, d.ma5, d.ma10, d.ma20, d.ma30, d.ma60,"
                + " m.dif, m.dea, m.macd_hist,"
                + " r.rsi6, r.rsi12, r.rsi24,"
                + " b.upper_band, b.middle_band, b.lower_band"
                + " FROM ma_daily d"
                + " JOIN macd_daily m ON d.time = m.time AND d.symbol = m.symbol AND d.market = m.market"
                + " JOIN rsi_daily r ON d.time = r.time AND d.symbol = r.symbol AND d.market = r.market"
                + " JOIN boll_daily b ON d.time = b.time AND d.symbol = b.symbol AND d.market = b.market"
                + " WHERE d.symbol = ? AND d.market = ? AND d.time BETWEEN ? AND ?"
                + " ORDER BY d.time ASC";

        Timestamp start = Timestamp.from(startDate.atStartOfDay().toInstant(ZoneOffset.UTC));
        Timestamp end = Timestamp.from(endDate.plusDays(1).atStartOfDay().minusNanos(1).toInstant(ZoneOffset.UTC));

        return jdbcTemplate.query(sql, rowMapper, symbol, market, start, end);
    }

    @Override
    public void saveAll(String symbol, String market, List<IndicatorValues> values) {
        if (values.isEmpty()) {
            return;
        }

        saveMa(symbol, market, values);
        saveMacd(symbol, market, values);
        saveRsi(symbol, market, values);
        saveBoll(symbol, market, values);
    }

    /** 保存MA指标到ma_daily表 */
    private void saveMa(String symbol, String market, List<IndicatorValues> values) {
        String sql = "INSERT INTO ma_daily (time, symbol, market, ma5, ma10, ma20, ma30, ma60)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING";
        jdbcTemplate.batchUpdate(sql, values, values.size(), (ps, v) -> {
            ps.setTimestamp(1, Timestamp.from(v.date().atStartOfDay().toInstant(ZoneOffset.UTC)));
            ps.setString(2, symbol);
            ps.setString(3, market);
            setNullableDouble(ps, 4, v.ma5());
            setNullableDouble(ps, 5, v.ma10());
            setNullableDouble(ps, 6, v.ma20());
            setNullableDouble(ps, 7, v.ma30());
            setNullableDouble(ps, 8, v.ma60());
        });
    }

    /** 保存MACD指标到macd_daily表 */
    private void saveMacd(String symbol, String market, List<IndicatorValues> values) {
        String sql = "INSERT INTO macd_daily (time, symbol, market, dif, dea, macd_hist)"
                + " VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING";
        jdbcTemplate.batchUpdate(sql, values, values.size(), (ps, v) -> {
            ps.setTimestamp(1, Timestamp.from(v.date().atStartOfDay().toInstant(ZoneOffset.UTC)));
            ps.setString(2, symbol);
            ps.setString(3, market);
            setNullableDouble(ps, 4, v.dif());
            setNullableDouble(ps, 5, v.dea());
            setNullableDouble(ps, 6, v.macdHist());
        });
    }

    /** 保存RSI指标到rsi_daily表 */
    private void saveRsi(String symbol, String market, List<IndicatorValues> values) {
        String sql = "INSERT INTO rsi_daily (time, symbol, market, rsi6, rsi12, rsi24)"
                + " VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING";
        jdbcTemplate.batchUpdate(sql, values, values.size(), (ps, v) -> {
            ps.setTimestamp(1, Timestamp.from(v.date().atStartOfDay().toInstant(ZoneOffset.UTC)));
            ps.setString(2, symbol);
            ps.setString(3, market);
            setNullableDouble(ps, 4, v.rsi6());
            setNullableDouble(ps, 5, v.rsi12());
            setNullableDouble(ps, 6, v.rsi24());
        });
    }

    /** 保存布林带指标到boll_daily表 */
    private void saveBoll(String symbol, String market, List<IndicatorValues> values) {
        String sql = "INSERT INTO boll_daily (time, symbol, market, upper_band, middle_band, lower_band)"
                + " VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING";
        jdbcTemplate.batchUpdate(sql, values, values.size(), (ps, v) -> {
            ps.setTimestamp(1, Timestamp.from(v.date().atStartOfDay().toInstant(ZoneOffset.UTC)));
            ps.setString(2, symbol);
            ps.setString(3, market);
            setNullableDouble(ps, 4, v.upperBand());
            setNullableDouble(ps, 5, v.middleBand());
            setNullableDouble(ps, 6, v.lowerBand());
        });
    }

    /** 从ResultSet读取可为null的Double值 */
    private Double getNullableDouble(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        double val = rs.getDouble(column);
        return rs.wasNull() ? null : val;
    }

    /** 向PreparedStatement设置可为null的Double值 */
    private void setNullableDouble(java.sql.PreparedStatement ps, int index, Double value) throws java.sql.SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.DOUBLE);
        } else {
            ps.setDouble(index, value);
        }
    }
}
