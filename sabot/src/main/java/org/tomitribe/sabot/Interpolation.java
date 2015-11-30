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

import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Interpolation {
    ;
    private static final Pattern PATTERN = Pattern.compile("(\\$\\{)([\\w._-]+)(})");

    public static Properties interpolate(final Properties properties) {
        return _interpolate(copy(properties));
    }

    private static Properties copy(final Properties properties) {
        final Properties copy = new Properties();
        copy.putAll(properties);
        return copy;
    }

    private static Properties _interpolate(final Properties interpolated) {
        boolean interpolating = true;
        while (interpolating) {

            interpolating = false;
            for (final Map.Entry<Object, Object> entry : interpolated.entrySet()) {
                final String value = entry.getValue().toString();

                final String formatted = format(value, interpolated);

                if (!value.equals(formatted)) {
                    interpolating = true;
                    entry.setValue(formatted);
                }
            }
        }

        return interpolated;
    }

    static String format(final String input, final Properties properties) {
        final Matcher matcher = PATTERN.matcher(input);
        final StringBuffer buf = new StringBuffer();
        while (matcher.find()) {
            final String key = matcher.group(2);
            final Object value = properties.get(key);
            if (value != null) {
                try {
                    matcher.appendReplacement(buf, value.toString());
                } catch (final Exception e) {
                    //Ignore
                }
            }
        }
        matcher.appendTail(buf);
        return buf.toString();
    }
}