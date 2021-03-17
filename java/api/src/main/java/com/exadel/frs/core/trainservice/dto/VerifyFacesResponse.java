/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.exadel.frs.core.trainservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.util.StringUtils;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Value
@EqualsAndHashCode(callSuper = true)
@JsonInclude(NON_NULL)
public class VerifyFacesResponse extends FaceProcessResponse {

    VerifyFacesResultDto processFileData;
    VerifyFacesResultDto checkFileData;
    float similarity;

    @Override
    public VerifyFacesResponse prepareResponse(FaceProcessResponse response, ProcessImageParams processImageParams) {
        VerifyFacesResponse result = (VerifyFacesResponse) response;
        String facePlugins = processImageParams.getFacePlugins();
        if (StringUtils.isEmpty(facePlugins) || !facePlugins.contains(CALCULATOR)) {
            result.getProcessFileData().setEmbedding(null);
            result.getCheckFileData().setEmbedding(null);
        }

        if (!processImageParams.getStatus()) {
            result.getProcessFileData().setExecutionTime(null);
            result.getCheckFileData().setExecutionTime(null);
        }

        return result;
    }
}