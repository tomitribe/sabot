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

import java.util.Properties;

/**
 * Provides the configuration observer interface.
 * No observer order is provided. Existing resolved properties can be overwritten.
 *
 * Implementations should register as an observer using ConfigurationResolver.registerConfigurationObserver([impl]);
 */
public interface ConfigurationObserver {

    /**
     * Calls the observer with the current properties.
     * Implementations can add to or override the supplied properties.
     *
     * This ConfigurationObserver is automatically removed
     *
     * @param resolved Currently resolved properties
     */
    void mergeConfiguration(final Properties resolved);
}
