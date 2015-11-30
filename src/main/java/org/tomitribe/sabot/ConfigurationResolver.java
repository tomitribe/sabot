/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tomitribe.sabot;

import org.apache.openejb.loader.SystemInstance;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;

public class ConfigurationResolver {

    private static final Logger LOGGER = Logger.getLogger(ConfigurationResolver.class.getName());

    private static final ConfigurationResolver INSTANCE = new ConfigurationResolver();

    private final Properties properties = new Properties();
    private final String environment;


    ConfigurationResolver() {

        this.environment = SystemInstance.get().getOptions().get("environment", "test");
        LOGGER.info(String.format("Environment: = %s", environment));

        this.properties.putAll(Environments.getProperties(environment));

        // This will allow developers to use the conf/system.properties file
        // to override properties packed into the application
        // Any Console features we add in the future for setting system properties
        // will nicely also have the ability to override application settings
        final Properties overrides = SystemInstance.get().getProperties();

        for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
            if (overrides.containsKey(entry.getKey())) {
                final Object newValue = overrides.get(entry.getKey());

                LOGGER.info(String.format("Override config %s from `%s` to `%s`", entry.getKey(), entry.getValue(), newValue));

                entry.setValue(newValue);
            }
        }

        for (final Map.Entry<Object, Object> entry : this.properties.entrySet()) {
            LOGGER.info(String.format("Config: %s = `%s`", entry.getKey(), entry.getValue()));
        }
    }

    public boolean isResolvableConfig(final String key, final String defaultValue) {

        Objects.requireNonNull(key, "config key can not be null");
        Objects.requireNonNull(defaultValue, "default config value can not be null");

        return properties.containsKey(key) || !defaultValue.isEmpty();
    }

    public String resolve(final String key, final String defaultValue) {

        Objects.requireNonNull(key, "config key can not be null");
        Objects.requireNonNull(defaultValue, "default config value can not be null");

        return properties.getProperty(key, defaultValue);
    }

    public String getEnvironment() {
        return environment;
    }

    public static final ConfigurationResolver get() {
        return INSTANCE;
    }
}
