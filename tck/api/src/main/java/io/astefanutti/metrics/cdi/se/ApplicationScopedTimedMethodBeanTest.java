/**
 * Copyright © 2013 Antonin Stefanutti (antonin.stefanutti@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.astefanutti.metrics.cdi.se;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Timer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ApplicationScopedTimedMethodBeanTest {

    private final static String TIMER_NAME = MetricRegistry.name(ApplicationScopedTimedMethodBean.class, "applicationScopedTimedMethod");

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(JavaArchive.class)
            // Test bean
            .addClass(ApplicationScopedTimedMethodBean.class)
            // Bean archive deployment descriptor
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private ApplicationScopedTimedMethodBean bean;

    @Before
    public void instantiateApplicationScopedBean() {
        // Let's trigger the instantiation of the application scoped bean explicitly
        // as only a proxy gets injected otherwise
        bean.toString();
    }

    @Test
    @InSequence(1)
    public void timedMethodNotCalledYet() {
        assertThat("Timer is not registered correctly", registry.getTimers(), hasKey(TIMER_NAME));
        Timer timer = registry.getTimers().get(TIMER_NAME);

        // Make sure that the timer hasn't been called yet
        assertThat("Timer count is incorrect", timer.getCount(), is(equalTo(0L)));
    }

    @Test
    @InSequence(2)
    public void callTimedMethodOnce() {
        assertThat("Timer is not registered correctly", registry.getTimers(), hasKey(TIMER_NAME));
        Timer timer = registry.getTimers().get(TIMER_NAME);

        // Call the timed method and assert it's been timed
        bean.applicationScopedTimedMethod();

        // Make sure that the timer has been called
        assertThat("Timer count is incorrect", timer.getCount(), is(equalTo(1L)));
    }
}