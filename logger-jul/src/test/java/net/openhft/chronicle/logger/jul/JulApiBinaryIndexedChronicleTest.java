/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package net.openhft.chronicle.logger.jul;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.logging.Logger;

@Ignore
public class JulApiBinaryIndexedChronicleTest extends JulApiTestBase {
    @Before
    public void setUp() {
        setupLogger(JulApiBinaryIndexedChronicleTest.class);
    }

    @Test
    public void test() {
        Logger l  = Logger.getLogger("logger");
        //Logger l1 = Logger.getLogger("logger_1");
        //Logger l2 = Logger.getLogger("logger_2");
    }
}
