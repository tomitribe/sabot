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


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

public enum Environments {
    ;
    private static final Logger LOGGER = Logger.getLogger(Environments.class.getName());

    public static Properties getProperties(final String value) {

        final Properties properties = new Properties();

        { // Load the base.properties  (optional)
            final String base = "base";

            final String resourceName = base + ".properties";

            final URL resource = getResource(resourceName);
            if (resource == null) {
                LOGGER.info("No " + resourceName + " found.");
            } else {
                properties.putAll(load(base));
            }
        }

        // Load each environment in the list
        for (String env : value.split(" *, *")) {
            properties.putAll(load(env));
        }

        // Process any {} variable references
        return Interpolation.interpolate(properties);
    }

    private static Properties load(final String value) {
        final Properties properties = new Properties();
        final String resourceName = value + ".properties";
        final URL resource = getResource(resourceName);

        if (resource == null) {
            throw new IllegalArgumentException("Can not find environment `" + resourceName + "` from the classpath.");
        }

        try (final InputStream inputStream = resource.openStream()) {
            properties.load(inputStream);

        } catch (final IOException e) {
            throw new IllegalStateException("Can not load environment `" + resource.toExternalForm() + "`.");
        }
        return properties;
    }

    private static URL getResource(final String resourceName) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        final URL resource = loader.getResource(resourceName);

        if (resource != null) {
            return resource;
        }

        return loader.getResource("/" + resourceName);
    }
}