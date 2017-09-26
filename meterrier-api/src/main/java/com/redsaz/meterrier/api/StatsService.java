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
package com.redsaz.meterrier.api;

import com.redsaz.meterrier.api.model.Timeseries;
import java.util.List;

/**
 * Stores and accesses statistics such as timeseries.
 *
 * @author Redsaz <redsaz@gmail.com>
 */
public interface StatsService {

    public void createSampleLabels(long logId, List<String> labels);

    public List<String> getSampleLabels(long logId);

    public Timeseries getTimeseries(long logId, long labelId);

    public void createOrUpdateTimeseries(long logId, long labelId, Timeseries timeseries);

}
