package greencity.webcontroller;

import greencity.constant.HttpStatuses;
import greencity.dto.PageableDto;
import greencity.dto.advice.AdviceVO;
import greencity.dto.advice.AdvicePostDto;
import greencity.dto.genericresponse.GenericResponseDto;
import greencity.entity.Advice;
import greencity.service.AdviceService;
import greencity.service.LanguageService;
import static greencity.dto.genericresponse.GenericResponseDto.buildGenericResponseDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/management/advices")
public class ManagementAdvicesController {
    private final AdviceService adviceService;
    private final LanguageService languageService;

    /**
     * Method that returns management page with all {@link Advice}'s.
     * @param model {@link Model} - for passing data between controller and view
     * @param pageable {@link Pageable}
     * @return name of template {@link String}
     * @author Markiyan Derevetskyi
     * */
    @ApiOperation(value = "Get all advices")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpStatuses.OK),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
    })
    @GetMapping
    public String findAllAdvices(Model model, @ApiIgnore Pageable pageable) {
        PageableDto<AdviceVO> allAdvices = adviceService.getAllAdvices(pageable);
        model.addAttribute("pageable", allAdvices);
        model.addAttribute("languages", languageService.getAllLanguages());

        return "core/management_advices";
    }

    /**
     * Method that saves new {@link Advice}.
     * @param advice {@link AdvicePostDto} - advice that will be saved in DB.
     * @return saved advice {@link GenericResponseDto}
     * @author Markiyan Derevetskyi
     * */
    @ApiOperation(value = "Save advice")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpStatuses.OK, response = GenericResponseDto.class),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
    })
    @ResponseBody
    @PostMapping
    public GenericResponseDto saveAdvice(@Valid @RequestBody AdvicePostDto advice,
                             BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            adviceService.save(advice);
        }

        return buildGenericResponseDto(bindingResult);
    }

    /**
     * Method that deletes {@link Advice} by id.
     *
     * @param id {@link Long}
     * @return  {@link org.springframework.http.ResponseEntity}
     * @author Markiyan Derevetskyi
     * */
    @ApiOperation(value = "Delete advice by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpStatuses.OK),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Long> deleteAdviceById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(adviceService.delete(id));
    }

    /**
     * Method that deletes all {@link Advice}'s by given id's.
     *
     * @param ids - list of  {@link Long}
     * @return  {@link org.springframework.http.ResponseEntity}
     * @author Markiyan Derevetskyi
     * */
    @ApiOperation(value = "Delete all advices by given id's")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpStatuses.OK),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
    })
    @DeleteMapping("/deleteAll")
    public ResponseEntity<List<Long>> deleteAllAdvices(@RequestBody List<Long> ids) {
        adviceService.deleteAllByIds(ids);
        return ResponseEntity.status(HttpStatus.OK).body(ids);
    }
}
