package capstone2.backend.codes.repository;

import capstone2.backend.codes.entity.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalendarRepository extends JpaRepository<Calendar, String> {
    List<Calendar> findByClubIdOrderByDateAsc(String clubId);
}

