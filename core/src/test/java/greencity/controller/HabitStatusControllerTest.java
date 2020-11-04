package greencity.controller;

import greencity.dto.habitstatus.UpdateHabitStatusDto;
import greencity.service.HabitStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HabitStatusControllerTest {

    private MockMvc mockMvc;

    @Mock
    HabitStatusService habitStatusService;

    @InjectMocks
    HabitStatusController habitStatusController;

    private static final String habitStatusLink = "/habit";

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(habitStatusController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void getHabitStatusByHabitAssignId() throws Exception {
        mockMvc.perform(get(habitStatusLink + "/assign/{id}/status", 1))
                .andExpect(status().isOk());
        verify(habitStatusService).findStatusByHabitAssignId(1L);
    }

    @Test
    void getHabitStatusByHabitId() throws Exception {
        mockMvc.perform(get(habitStatusLink + "/{id}/status", 1))
            .andExpect(status().isOk());
        verify(habitStatusService).findActiveStatusByHabitIdAndUserId(1L, 1L);
    }

    @Test
    void enrollHabit() throws Exception {
        mockMvc.perform(post(habitStatusLink + "/{id}/status/enroll", 1))
                .andExpect(status().isOk());
        verify(habitStatusService).enrollHabit(1L, 1L);
    }

    @Test
    void update() throws Exception {
        UpdateHabitStatusDto habitStatusForUpdateDto = new UpdateHabitStatusDto();
        habitStatusForUpdateDto.setHabitStreak(1);
        habitStatusForUpdateDto.setWorkingDays(5);
        habitStatusForUpdateDto.setLastEnrollmentDate(LocalDateTime.parse("2020-10-10T16:03:01.652"));
        mockMvc.perform(put(habitStatusLink + "/{id}/status", 1)
                .content("{\n" +
                        "  \"habitStreak\": 1,\n" +
                        "  \"lastEnrollmentDate\": \"2020-10-10T16:03:01.652\",\n" +
                        "  \"workingDays\": 5\n" +
                        "}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(habitStatusService).update(1L, 1L, habitStatusForUpdateDto);
    }
}
