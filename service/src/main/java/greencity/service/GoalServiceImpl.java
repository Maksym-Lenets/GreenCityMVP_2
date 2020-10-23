package greencity.service;

import greencity.dto.goal.GoalDto;
import greencity.dto.goal.ShoppingListDtoResponse;
import greencity.dto.user.UserGoalResponseDto;
import greencity.dto.user.UserGoalVO;
import greencity.entity.CustomGoal;
import greencity.entity.Goal;
import greencity.entity.UserGoal;
import greencity.enums.GoalStatus;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.GoalNotFoundException;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.CustomGoalRepo;
import greencity.repository.GoalRepo;
import greencity.repository.GoalTranslationRepo;
import greencity.constant.ErrorMessage;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class GoalServiceImpl implements GoalService {
    private final GoalTranslationRepo goalTranslationRepo;
    private final CustomGoalRepo customGoalRepo;
    private final GoalRepo goalRepo;
    private final ModelMapper modelMapper;
    private final LanguageService languageService;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GoalDto> findAll(String language) {
        return goalTranslationRepo
            .findAllByLanguageCode(language)
            .stream()
            .map(g -> modelMapper.map(g, GoalDto.class))
            .collect(Collectors.toList());
    }

    @Override
    public UserGoalResponseDto getUserGoalResponseDtoFromPredefinedGoal(UserGoalVO userGoalVO) {
        UserGoal userGoal = modelMapper.map(userGoalVO, UserGoal.class);
        UserGoalResponseDto userGoalResponseDto = modelMapper.map(userGoal, UserGoalResponseDto.class);
        String languageCode = languageService.extractLanguageCodeFromRequest();
        if (userGoal.getCustomGoal() == null) {
            Goal goal = goalRepo
                .findById(userGoal
                    .getGoal().getId()).orElseThrow(() -> new GoalNotFoundException(ErrorMessage.GOAL_NOT_FOUND_BY_ID));
            userGoalResponseDto.setText(goalTranslationRepo.findByGoalAndLanguageCode(goal, languageCode)
                .orElseThrow(() ->
                        new GoalNotFoundException(ErrorMessage.GOAL_NOT_FOUND_BY_LANGUAGE_CODE)).getContent());
        }
        return userGoalResponseDto;
    }

    @Override
    public UserGoalResponseDto getUserGoalResponseDtoFromCustomGoal(UserGoalVO userGoalVO) {
        UserGoal userGoal = modelMapper.map(userGoalVO, UserGoal.class);
        UserGoalResponseDto userGoalResponseDto = modelMapper.map(userGoal, UserGoalResponseDto.class);
        if (userGoal.getGoal() == null) {
            CustomGoal customGoal = customGoalRepo.findById(userGoal
                .getCustomGoal().getId()).orElseThrow(() ->
                    new NotFoundException(ErrorMessage.CUSTOM_GOAL_NOT_FOUND_BY_ID));
            userGoalResponseDto.setText(customGoal.getText());
        }
        return userGoalResponseDto;
    }

    /**
     * Method returns shopping list by user id.
     *
     * @return shopping list {@link ShoppingListDtoResponse}.
     * @author Marian Datsko
     */
    @Override
    public List<ShoppingListDtoResponse> getShoppingList(Long userId, String languageCode) {
        return goalRepo.getShoppingList(userId, languageCode);
    }

    /**
     * Method change goal or custom goal status.
     *
     * @author Marian Datsko
     */
    @Override
    @Transactional
    public void changeGoalOrCustomGoalStatus(Long userId, boolean status, Long goalId, Long customGoalId) {
        String goalStatus = status ? GoalStatus.DONE.toString() : GoalStatus.ACTIVE.toString();
        LocalDateTime now = LocalDateTime.now().withNano(0);
        if ((goalId == null && customGoalId == null) || (goalId != null && customGoalId != null)) {
            throw new BadRequestException(ErrorMessage.WRONG_PARAMETER);
        } else if (goalId != null) {
            if (goalRepo.findById(goalId).isEmpty()) {
                throw new NotFoundException(ErrorMessage.GOAL_WRONG_ID + goalId);
            }
            goalRepo.changeGoalStatus(userId, goalId, goalStatus, now);
        } else {
            if (customGoalRepo.findById(customGoalId).isEmpty()) {
                throw new NotFoundException(ErrorMessage.GOAL_WRONG_ID + customGoalId);
            }
            customGoalRepo.changeCustomGoalStatus(userId, customGoalId, goalStatus, now);
        }
    }
}