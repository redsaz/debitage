/*
 * Copyright 2017 Redsaz <redsaz@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redsaz.meterrier.convert;

import com.redsaz.meterrier.convert.model.PreSample;
import java.util.List;

/**
 * Essentially a List with some extra methods.
 *
 * @author Redsaz <redsaz@gmail.com>
 */
public interface Samples {

    long getEarliestMillis();

    PreSample getEarliestSample();

    List<String> getLabels();

    long getLatestMillis();

    PreSample getLatestSample();

    List<PreSample> getSamples();

    StatusCodeLookup getStatusCodeLookup();

    List<String> getThreadNames();

}
