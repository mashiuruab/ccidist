package com.cefalo.cci.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import com.google.common.base.Charsets;

public abstract class PropertyUtils {
    private PropertyUtils() {

    }

    public static Properties readPropertiesFile(final String resourceFileName) {
        Properties properties = new Properties();
        try (Reader reader = new BufferedReader(new InputStreamReader(
                PropertyUtils.class.getResourceAsStream(resourceFileName), Charsets.UTF_8))) {
            properties.load(reader);
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    String.format("Error trying to read properties file: %s", resourceFileName), ex);
        }
        return properties;
    }

    public static Properties processAdvancedHibernateProperties(Properties props) {
        Properties newProps = new Properties();
        String connectionUrl = null;
        if ("mysql".equalsIgnoreCase(props.getProperty("app.database.name"))) {
            connectionUrl = String.format("jdbc:mysql://%s:%s/%s?characterEncoding=utf8", props.get("db.host"),
                    props.get("db.port"), props.get("db.database.instance.name"));

            newProps.put("hibernate.connection.SetBigStringTryClob", "true");
            newProps.put("hibernate.dialect", "org.hibernate.dialect.MySQL5InnoDBDialect");
            newProps.put("hibernate.connection.driver", "com.mysql.jdbc.Driver");

        } else if ("oracle".equalsIgnoreCase(props.getProperty("app.database.name"))) {
            connectionUrl = String.format("jdbc:oracle:thin:@%s:%s:%s", props.get("db.host"), props.get("db.port"),
                    props.get("db.database.instance.name"));

            newProps.put("hibernate.jdbc.use_streams_for_binary", "true");
            newProps.put("hibernate.dialect", "org.hibernate.dialect.Oracle10gDialect");
            newProps.put("hibernate.connection.driver", "oracle.jdbc.driver.OracleDriver");
        }

        newProps.put("hibernate.connection.url", connectionUrl);
        newProps.put("hibernate.c3p0.min_size", props.get("db.connection_pool.min_size"));
        newProps.put("hibernate.c3p0.max_size", props.get("db.connection_pool.max_size"));
        newProps.put("hibernate.connection.username", props.get("db.username"));
        newProps.put("hibernate.connection.password", props.get("db.password"));
        newProps.put("hibernate.hbm2ddl.auto", "update");

        // "db.advanced" will override our default config options. e.g. You can override the connection URL from here.
        for (String propName : props.stringPropertyNames()) {
            if (propName.contains("db.advanced.")) {
                newProps.put(propName.replace("db.advanced.", ""), props.get(propName));
            }
        }

        return newProps;
    }
}
