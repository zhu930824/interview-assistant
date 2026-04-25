package interview.backend.common.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(List.class)
public class JsonListTypeHandler extends BaseTypeHandler<List<String>> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, MAPPER.writeValueAsString(parameter));
        } catch (Exception e) {
            throw new SQLException("Failed to serialize list to JSON", e);
        }
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseList(rs.getString(columnName));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseList(rs.getString(columnIndex));
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseList(cs.getString(columnIndex));
    }

    private List<String> parseList(String value) throws SQLException {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        try {
            return MAPPER.readValue(value, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new SQLException("Failed to parse JSON to list", e);
        }
    }
}
