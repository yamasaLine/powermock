/*
 *
 *   Copyright 2017 the original author or authors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.powermock.core.transformers;

import net.bytebuddy.utility.RandomString;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.powermock.core.test.MockClassLoaderFactory;
import org.powermock.core.transformers.bytebuddy.MethodMockTransformer;
import org.powermock.core.transformers.bytebuddy.advice.MockMethodDispatchers;
import org.powermock.core.transformers.javassist.StaticFinalNativeMethodMockTransformer;
import org.powermock.core.transformers.mock.MockGatewaySpy;
import org.powermock.core.transformers.mock.MockGatewaySpyMethodDispatcher;
import org.powermock.core.transformers.support.DefaultMockTransformerChain;
import org.powermock.reflect.internal.WhiteboxImpl;
import powermock.test.support.MainMockTransformerTestSupport.NativeMethodsTestClass;

import java.util.ArrayList;
import java.util.Collection;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.powermock.core.transformers.mock.MockGatewaySpy.ConditionBuilder.registered;
import static org.powermock.core.transformers.mock.MockGatewaySpy.methodCalls;

public class NativeMethodsMockTransformerTest extends AbstractBaseMockTransformerTest {
    
    @Parameterized.Parameters(name = "strategy: {0}, transformer: {1}")
    public static Iterable<Object[]> data() {
        Collection<Object[]> data = new ArrayList<Object[]>();
        
        data.addAll(MockTransformerTestHelper.createOneTransformerTestData(MockGatewaySpy.class, StaticFinalNativeMethodMockTransformer.class));
        data.addAll(MockTransformerTestHelper.createOneTransformerTestData(MethodMockTransformer.class));
        
        return data;
    }
    
    public NativeMethodsMockTransformerTest(final TransformStrategy strategy, final MockTransformer transformer,
                                            final MockClassLoaderFactory mockClassloaderFactory) {
        super(strategy, DefaultMockTransformerChain.newBuilder().append(transformer).build(), mockClassloaderFactory);
        if (transformer instanceof MethodMockTransformer) {
            MockMethodDispatchers.set(((MethodMockTransformer) transformer).getIdentifier(), new MockGatewaySpyMethodDispatcher());
        }
    }
    
    @Test
    public void should_return_value_from_getaway_for_native_methods_is_it_is_not_PROCEED() throws Exception {
        final String expected = RandomString.make(10);
        MockGatewaySpy.returnOnMethodCall("nativeReturnMethod", expected);
        
        final Class<?> clazz = loadWithMockClassLoader(NativeMethodsTestClass.class.getName());
        
        final Object instance = WhiteboxImpl.newInstance(clazz);
        
        final String name = "name";
        final Object result = WhiteboxImpl.invokeMethod(instance, "nativeReturnMethod", name);
        
        assertThat(result)
            .isEqualTo(expected);
        
        assertThat(methodCalls())
            .is(registered().forMethod("nativeReturnMethod"));
    }
}