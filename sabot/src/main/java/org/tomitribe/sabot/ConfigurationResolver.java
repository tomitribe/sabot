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

import javax.resource.ResourceException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * Singleton responsible for loading the configuration properties prior to CDI initialization.
 */
public class ConfigurationResolver {

    public static final String ENVIRONMENT = "org.tomitribe.sabot.environment";

    private static final Logger LOGGER = Logger.getLogger(ConfigurationResolver.class.getName());
    private static final ConfigurationResolver instance = new ConfigurationResolver();

    private final Properties properties = new Properties();
    private final String environment;
    private final AtomicBoolean initialized;
    private final ReentrantLock lock;
    private final List<ConfigurationObserver> observers;

    ConfigurationResolver() {
        this.environment = System.getProperty(ConfigurationResolver.ENVIRONMENT);
        this.initialized = new AtomicBoolean(false);
        this.lock = new ReentrantLock();
        this.observers = new ArrayList<ConfigurationObserver>();
    }

    private void initialize() {
        if (!this.initialized.getAndSet(true)) {

            List<ConfigurationObserver> current;

            this.lock.lock();
            try {
                current = new ArrayList<ConfigurationObserver>(this.observers);
            } finally {
                this.lock.unlock();
            }

            //Default
            final String environment = this.environment;
            try {
                this.properties.putAll(Environments.getProperties(environment));
            } catch (final ResourceException e) {
                throw new RuntimeException("Failed to load environment: " + environment, e);
            }

            //Additional
            for (final ConfigurationObserver observer : current) {
                observer.mergeConfiguration(this.properties);
            }

            //Final
            for (final Map.Entry<Object, Object> entry : this.properties.entrySet()) {
                LOGGER.info(String.format("Configuration: %s = `%s`", entry.getKey(), entry.getValue()));
            }
        }
    }

    public static void reset(){
        ConfigurationResolver.get().clear();
    }

    private void clear(){
        this.properties.clear();
        this.initialized.set(false);
    }

    public static void registerConfigurationObserver(final ConfigurationObserver observer) {
        ConfigurationResolver.get().register(observer);
    }

    @SuppressWarnings("unused")
    public static void deregisterConfigurationObserver(final ConfigurationObserver observer) {
        ConfigurationResolver.get().deregister(observer);
    }

    private void register(final ConfigurationObserver observer) {
        lock.lock();
        try {
            if (!observers.contains(observer)) {
                observers.add(observer);
            }
        } finally {
            lock.unlock();
        }
    }

    private void deregister(final ConfigurationObserver observer) {
        lock.lock();
        try {
            observers.remove(observer);
        } finally {
            lock.unlock();
        }
    }

    public boolean isResolvableConfig(final String key, final String defaultValue) {

        this.initialize();

        Objects.requireNonNull(key, "config key can not be null");
        Objects.requireNonNull(defaultValue, "default config value can not be null");

        return this.properties.containsKey(key) || !defaultValue.isEmpty();
    }

    public String resolve(final String key, final String defaultValue) {

        this.initialize();

        Objects.requireNonNull(key, "config key can not be null");
        Objects.requireNonNull(defaultValue, "default config value can not be null");

        return this.properties.getProperty(key, defaultValue);
    }

    public String getEnvironment() {
        return null != environment ? environment : "base";
    }

    /**
     * Get the ConfigurationResolver singleton
     *
     * @return The ConfigurationResolver for the current classloader.
     */
    public static ConfigurationResolver get() {
        return instance;
    }
}
