package com.exadel.frs.core.trainservice.controller;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.exadel.frs.core.trainservice.component.FaceClassifierManager;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.component.migration.MigrationComponent;
import com.exadel.frs.core.trainservice.component.migration.MigrationStatusStorage;
import com.exadel.frs.core.trainservice.config.WebMvcTestContext;
import com.exadel.frs.core.trainservice.filter.SecurityValidationFilter;
import com.exadel.frs.core.trainservice.service.FaceService;
import com.exadel.frs.core.trainservice.service.RetrainService;
import com.exadel.frs.core.trainservice.system.SystemService;
import com.exadel.frs.core.trainservice.system.Token;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityValidationFilter.class}
        ))
@WebMvcTestContext
class TrainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RetrainService retrainService;

    @MockBean
    private SystemService systemService;

    private final static String MODEL_KEY = "model_key";
    private final static String API_KEY = MODEL_KEY;

    @Test
    void train() throws Exception {
        val token = new Token(MODEL_KEY);
        when(systemService.buildToken(API_KEY)).thenReturn(token);

        mockMvc.perform(post(API_V1 + "/retrain").header(X_FRS_API_KEY_HEADER, API_KEY))
               .andExpect(status().isAccepted());

        verify(systemService).buildToken(API_KEY);
        verify(retrainService).startRetrain(token.getModelApiKey());
        verifyNoMoreInteractions(systemService);
    }

    @Test
    void getStatus() throws Exception {
        val token = new Token(MODEL_KEY);
        when(systemService.buildToken(API_KEY)).thenReturn(token);
        when(retrainService.isTrainingRun(MODEL_KEY)).thenReturn(false);

        mockMvc.perform(get(API_V1 + "/retrain").header(X_FRS_API_KEY_HEADER, API_KEY))
               .andExpect(status().isOk());
    }

    @Test
    void abortRetrain() throws Exception {
        val token = new Token(MODEL_KEY);
        when(systemService.buildToken(API_KEY)).thenReturn(token);

        mockMvc.perform(delete(API_V1 + "/retrain").header(X_FRS_API_KEY_HEADER, API_KEY))
               .andExpect(status().isAccepted());
    }
}