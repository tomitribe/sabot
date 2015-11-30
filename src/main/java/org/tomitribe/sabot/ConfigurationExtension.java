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

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessProducerMethod;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class ConfigurationExtension implements Extension {

    private static class ConverterBean implements Bean<Object> {

        private final Bean<Object> delegate;
        private final Set<Type> types;

        public ConverterBean(final Bean convBean, final Set<Type> types) {

            this.types = types;
            this.delegate = convBean;
        }

        @Override
        public Set<Type> getTypes() {
            return types;
        }

        @Override
        public Class<?> getBeanClass() {
            return delegate.getBeanClass();
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return delegate.getInjectionPoints();
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public Set<Annotation> getQualifiers() {
            return delegate.getQualifiers();
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return delegate.getScope();
        }

        @Override
        public Set<Class<? extends Annotation>> getStereotypes() {
            return delegate.getStereotypes();
        }

        @Override
        public boolean isAlternative() {
            return delegate.isAlternative();
        }

        @Override
        public boolean isNullable() {
            return delegate.isNullable();
        }

        @Override
        public Object create(final CreationalContext<Object> creationalContext) {
            return delegate.create(creationalContext);
        }

        @Override
        public void destroy(final Object instance, final CreationalContext<Object> creationalContext) {
            delegate.destroy(instance, creationalContext);
        }
    }

    private final Set<Type> types = new HashSet<>();
    private Bean<?> convBean;

    public void retrieveTypes(@Observes final ProcessBean<?> pb) {

        final ConfigurationResolver resolver = ConfigurationResolver.get();
        final Set<InjectionPoint> ips = pb.getBean().getInjectionPoints();

        for (final InjectionPoint injectionPoint : ips) {
            if (injectionPoint.getAnnotated().isAnnotationPresent(Config.class)) {
                final Config annotation = injectionPoint.getAnnotated().getAnnotation(Config.class);
                final String key = annotation.value();

                // We don't want to wait until the injection really fails at runtime.
                // If there is a non resolvable configuration, we want to know at startup.
                if (!resolver.isResolvableConfig(key, annotation.defaultValue())) {
                    throw new IllegalStateException(String.format(
                            "Can't resolve config %s for environment `%s`. " +
                                    "Make sure to define the following property `%s = <your config value>` " +
                                    "for the environment `%s`",
                            key, resolver.getEnvironment(), key, resolver.getEnvironment()));
                }

                types.add(injectionPoint.getType());
            }
        }
    }

    public void captureConvertBean(@Observes final ProcessProducerMethod<?, ?> ppm) {
        if (ppm.getAnnotated().isAnnotationPresent(Config.class)) {
            convBean = ppm.getBean();
        }

    }

    public void addConverter(@Observes final AfterBeanDiscovery abd, final BeanManager bm) {
        abd.addBean(new ConverterBean(convBean, types));
    }
}
