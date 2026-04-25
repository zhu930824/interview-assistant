package interview.backend.common.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(Map.class)
public class JsonMapTypeHandler extends BaseTypeHandler<Map<String, Integer>> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<String, Integer> parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, MAPPER.writeValueAsString(parameter));
        } catch (Exception e) {
            throw new SQLException("Failed to serialize map to JSON", e);
        }
    }

    @Override
    public Map<String, Integer> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseMap(rs.getString(columnName));
    }

    @Override
    public Map<String, Integer> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseMap(rs.getString(columnIndex));
    }

    @Override
    public Map<String, Integer> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseMap(cs.getString(columnIndex));
    }

    private Map<String, Integer> parseMap(String value) throws SQLException {
        if (value == null || value.isBlank()) {
            return Map.of();
        }
        try {
            return MAPPER.readValue(value, new TypeReference<Map<String, Integer>>() {});
        } catch (Exception e) {
            throw new SQLException("Failed to parse JSON to map", e);
        }
    }
}
