package capstone2.backend.codes.service;

import capstone2.backend.codes.dto.CalendarDto;
import capstone2.backend.codes.dto.UpcomingScheduleDto;
import capstone2.backend.codes.entity.Calendar;
import capstone2.backend.codes.repository.CalendarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarRepository calendarRepository;

    public List<Calendar> getCalendarByClubId(String clubId) {
        return calendarRepository.findByClubIdOrderByDateAsc(clubId);
    }

    public void addSchedule(CalendarDto dto) {
        Calendar calendar = new Calendar();
        calendar.setScheduleId(UUID.randomUUID().toString().replace("-", ""));
        calendar.setClubId(dto.getClubId());
        calendar.setDate(dto.getDate());
        calendar.setTime(dto.getTime());
        calendar.setTitle(dto.getTitle());
        calendar.setContent(dto.getContent());
        calendarRepository.save(calendar);
    }

    public void deleteSchedule(String scheduleId) {
        calendarRepository.deleteById(scheduleId);
    }

    public Optional<UpcomingScheduleDto> getUpcomingSchedule(String clubId) {
        return calendarRepository.findByClubIdOrderByDateAsc(clubId).stream()
                .filter(c -> !c.getDate().isBefore(LocalDate.now())) // 오늘 이후 포함
                .findFirst()
                .map(c -> new UpcomingScheduleDto(c.getTitle(), c.getDate()));
    }
}

