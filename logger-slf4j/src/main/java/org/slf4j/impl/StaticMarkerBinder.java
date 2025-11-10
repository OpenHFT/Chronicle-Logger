//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 *  Copyright 2014-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.slf4j.impl;

import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.spi.MarkerFactoryBinder;

/**
 * Supplies the singleton {@link IMarkerFactory} used by SLF4J.
 *
 * <p>SLF4J looks up this binder at run time to obtain a
 * {@link BasicMarkerFactory} instance for marker creation.</p>
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class StaticMarkerBinder implements MarkerFactoryBinder {

    /**
     * The unique instance of this class.
     */
    public static final StaticMarkerBinder SINGLETON = new StaticMarkerBinder();

    final IMarkerFactory markerFactory = new BasicMarkerFactory();

    private StaticMarkerBinder() {
    }

    /**
     * Currently this method always returns an instance of
     * {@link BasicMarkerFactory}.
     */
    @Override
    public IMarkerFactory getMarkerFactory() {
        return markerFactory;
    }

    /**
     * Currently, this method returns the class name of
     * {@link BasicMarkerFactory}.
     */
    @Override
    public String getMarkerFactoryClassStr() {
        return BasicMarkerFactory.class.getName();
    }
}
