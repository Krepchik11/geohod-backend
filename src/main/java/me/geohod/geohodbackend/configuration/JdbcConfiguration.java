package me.geohod.geohodbackend.configuration;

import java.sql.SQLException;
import java.util.Arrays;

import org.postgresql.util.PGobject;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

@Configuration
public class JdbcConfiguration extends AbstractJdbcConfiguration {

    @Override
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(
            Arrays.asList(
                new JsonbStringToPGobjectConverter(),
                new PGobjectToJsonbStringConverter()
            )
        );
    }

    @WritingConverter
    public static class JsonbStringToPGobjectConverter implements Converter<me.geohod.geohodbackend.data.model.eventlog.JsonbString, PGobject> {
        @Override
        public PGobject convert(me.geohod.geohodbackend.data.model.eventlog.JsonbString source) {
            if (source == null) {
                return null;
            }
            PGobject jsonObject = new PGobject();
            jsonObject.setType("jsonb");
            try {
                jsonObject.setValue(source.value());
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to convert JsonbString to PGobject for jsonb", e);
            }
            return jsonObject;
        }
    }

    @ReadingConverter
    public static class PGobjectToJsonbStringConverter implements Converter<PGobject, me.geohod.geohodbackend.data.model.eventlog.JsonbString> {
        @Override
        public me.geohod.geohodbackend.data.model.eventlog.JsonbString convert(PGobject source) {
            if (source == null) {
                return null;
            }
            return new me.geohod.geohodbackend.data.model.eventlog.JsonbString(source.getValue());
        }
    }
}