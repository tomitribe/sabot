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

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tomitribe.sabot.Config;
import org.tomitribe.sabot.ConfigurationExtension;
import org.tomitribe.sabot.ConfigurationProducer;
import org.tomitribe.sabot.ConfigurationResolver;

import javax.inject.Inject;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class ConfigurationOverrideTest {

    @BeforeClass
    public static void beforeClass(){
        ConfigurationResolver.reset();
    }

    @Configuration
    public Properties configuration() {
        return new PropertiesBuilder()
                .p("remote.username", "joecool override")
                .build();
    }

    @Module
    @Classes(cdi = true, value = {
            SimpleType.class,
            ConfigurationExtension.class,
            ConfigurationProducer.class,
            TomEEConfiguration.class
    })
    public EjbJar jar() {
        return new EjbJar("config");
    }

    @Inject
    protected SimpleType allTypes;

    @Test
    public void actualPropertyValues() {
        assertEquals("joecool override", allTypes.getUsername());
    }

    @SuppressWarnings("CdiInjectionPointsInspection")
    static class SimpleType {

        @Inject
        @Config(value = "remote.username", defaultValue = "bob")
        private String username;

        public String getUsername() {
            return username;
        }

        @Override
        public String toString() {
            return "SimpleType{" +
                    "username='" + username + '\'' +
                    '}';
        }
    }

}
