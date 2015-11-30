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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum Environments {
    ;
    private static final Logger LOGGER = Logger.getLogger(Environments.class.getName());

    /**
     * Combine the base properties with the environment properties specified by the parameter if found.
     * If not found then the method will return only the base properties object, but will log errors.
     *
     * @param environments Single or comma separated list of environment properties to load
     * @return Single or combined Properties
     * @throws ResourceException
     */
    public static Properties getProperties(final String environments) throws ResourceException {

        final Properties properties = new Properties();

        { // Always load the base.properties (optional but default)
            final String base = "base";

            final String resourceName = base + ".properties";

            final URL resource = getResource(resourceName);
            if (resource == null) {
                LOGGER.log(Level.INFO, "No " + resourceName + " found.");
            } else {
                try {
                    properties.putAll(load(base));
                } catch (final ResourceException e) {
                    LOGGER.log(Level.INFO, "The properties file 'base.properties' was not found on the classpasth");
                }
            }
        }

        if (null != environments) {
            // Load each environment in the list
            for (final String env : environments.split(" *, *")) {
                properties.putAll(load(env));
            }
        }

        // Process any {} variable references
        return Interpolation.interpolate(properties);
    }

    private static Properties load(final String value) throws ResourceException {
        final Properties properties = new Properties();
        final String resourceName = value + ".properties";
        final URL resource = getResource(resourceName);

        if (resource == null) {
            throw new ResourceException("Unable to find '" + resourceName + "' on the classpath");
        }

        InputStream inputStream = null;
        try {
            inputStream = resource.openStream();
            properties.load(inputStream);

        } catch (final IOException e) {
            throw new ResourceException("Failed to load environment '" + resource.toExternalForm() + "'");
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (final Exception e) {
                    //no-op
                }
            }
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