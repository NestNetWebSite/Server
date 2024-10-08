package NestNet.NestNetWebSite.controller.attendance;

import NestNet.NestNetWebSite.api.ApiResult;
import NestNet.NestNetWebSite.dto.response.attendance.AttendanceStatisticsResponse;
import NestNet.NestNetWebSite.dto.response.attendance.MonthlyAttendanceStatisticsDto;
import NestNet.NestNetWebSite.dto.response.attendance.WeeklyAttendanceStatisticsDto;
import NestNet.NestNetWebSite.service.attendance.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "출석 API")
public class AttendanceController {

    private final AttendanceService attendanceService;

    /*
    출석
     */
    @PostMapping("/attendance")
    @Operation(summary = "출석", description = "로그인한 사용자가 출석 버튼을 눌렀을 때 동작한다.")
    public ApiResult<?> memberAttendance(@AuthenticationPrincipal UserDetails userDetails){

        attendanceService.saveAttendance(userDetails.getUsername(), LocalDateTime.now());

        return ApiResult.success("출석하였습니다.");
    }

    /*
    주간 / 월간 출석 통계 조회
     */
    @GetMapping("/attendance/statistics")
    @Operation(summary = "주간 / 월간 출석 통계 조회", description = "", responses =
            @ApiResponse(responseCode = "200", description = "", content = @Content(schema = @Schema(implementation = AttendanceStatisticsResponse.class)))
    )
    public ApiResult<?> findAttendanceStatistics(){

        LocalDateTime currTime = LocalDateTime.now();

        List<WeeklyAttendanceStatisticsDto> weeklyAttendanceStatistics = attendanceService.findWeeklyAttendanceStatistics(currTime);
        List<MonthlyAttendanceStatisticsDto> monthlyAttendanceStatistics = attendanceService.findMonthlyAttendanceStatistics(currTime);

        return ApiResult.success(new AttendanceStatisticsResponse(weeklyAttendanceStatistics, monthlyAttendanceStatistics));
    }

}
