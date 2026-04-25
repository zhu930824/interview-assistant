package interview.backend.common.handler;

import com.alibaba.fastjson2.JSON;
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

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<String, Integer> parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, JSON.toJSONString(parameter));
    }

    @Override
    public Map<String, Integer> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return parseMap(value);
    }

    @Override
    public Map<String, Integer> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return parseMap(value);
    }

    @Override
    public Map<String, Integer> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return parseMap(value);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Integer> parseMap(String value) {
        if (value == null || value.isBlank()) {
            return Map.of();
        }
        return JSON.parseObject(value, Map.class);
    }
}
