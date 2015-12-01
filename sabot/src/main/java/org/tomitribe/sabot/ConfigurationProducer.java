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

import org.tomitribe.util.editor.Converter;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class ConfigurationProducer {

    private static final Logger LOGGER = Logger.getLogger(ConfigurationProducer.class.getName());
    private final ConfigurationResolver resolver;

    public ConfigurationProducer() {
        this.resolver = ConfigurationResolver.get();
    }

    /**
     * Resolves configuration for the provided InjectionPoint
     * @param injectionPoint InjectionPoint
     * @return Resolved Object or default
     */
    @Produces
    @Config
    public Object resolveAndConvert(final InjectionPoint injectionPoint) {

        final Config annotation = injectionPoint.getAnnotated().getAnnotation(Config.class);
        final String key = annotation.value();
        final String defaultValue = annotation.defaultValue();

        // unless the extension is not installed, this should never happen because the extension
        // enforces the resolvability of the config
        if (!resolver.isResolvableConfig(key, defaultValue)) {
            throw new IllegalStateException(String.format(
                    "Unable to resolve configuration %s for environment '%s'.",
                    key, resolver.getEnvironment()));
        }

        final String value = resolver.resolve(key, defaultValue);

        final Class<?> toType = (Class<?>) injectionPoint.getAnnotated().getBaseType();

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(String.format("Injecting %s for key %s in class %s", key, value, injectionPoint.toString()));
        }

        return Converter.convert(value, toType, key);
    }

}
