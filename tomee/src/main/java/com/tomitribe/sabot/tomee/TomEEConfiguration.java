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
package com.tomitribe.sabot.tomee;

import org.apache.openejb.loader.SystemInstance;
import org.tomitribe.sabot.ConfigurationObserver;
import org.tomitribe.sabot.ConfigurationResolver;

import javax.ejb.Singleton;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Provides properties defined in the TomEE application server environment
 * Adding @Singleton makes sure this class is loaded and the static block is executed
 */
@Singleton
public class TomEEConfiguration implements ConfigurationObserver {

    static {
        //This must occur before anything is initialized
        ConfigurationResolver.registerConfigurationObserver(new TomEEConfiguration());
    }

    private static final Logger LOGGER = Logger.getLogger(TomEEConfiguration.class.getName());

    /**
     * First loads system properties then appends/overwrites with base.properties if found on the classpath
     */
    private TomEEConfiguration() {

    }

    /**
     * See {@link ConfigurationObserver#mergeConfiguration(Properties)}
     *
     * @param resolved Currently resolved properties
     * @return Combined Properties
     */
    @Override
    public void mergeConfiguration(final Properties resolved) {

        Properties overrides = SystemInstance.get().getProperties();

        if (null == overrides) {
            overrides = new Properties();
        }

        for (final Map.Entry<Object, Object> entry : resolved.entrySet()) {
            if (overrides.containsKey(entry.getKey())) {
                final Object newValue = overrides.get(entry.getKey());

                LOGGER.info(String.format("Override config %s from `%s` to `%s`", entry.getKey(), entry.getValue(), newValue));

                entry.setValue(newValue);
            }
        }

        //This is a one time deal
        ConfigurationResolver.deregisterConfigurationObserver(this);
    }
}
