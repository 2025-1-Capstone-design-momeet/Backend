package capstone2.backend.codes.service;

import capstone2.backend.codes.dto.*;
import capstone2.backend.codes.entity.*;
import capstone2.backend.codes.repository.PaymentHistoryRepository;
import capstone2.backend.codes.repository.PersonalNotificationRepository;
import capstone2.backend.codes.repository.UserRepository;
import capstone2.backend.codes.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonalNotificationService {
    private final PersonalNotificationRepository personNotiRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final UserRepository userRepository;

    // 개인 알람 작성
    public boolean writeAlarm(PersonalNotificationDto personNotiDto) throws Exception {
        try {
            String alarmID = UUID.randomUUID().toString().replace("-", "");
            User user = userRepository.findById(personNotiDto.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 userId의 유저를 찾을 수 없습니다."));
            PaymentHistory pay = paymentHistoryRepository.findById(personNotiDto.getPayId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 notificationId의 알림을 찾을 수 없습니다."));

            PersonalNotification personalNotification = new PersonalNotification(
                    alarmID,
                    personNotiDto.getType(),
                    personNotiDto.getContent(),
                    personNotiDto.getTitle(),
                    user,
                    pay
            );
            personNotiRepository.save(personalNotification);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }


    // 개인 알림 삭제
    @Transactional
    public void deleteAlarm(PersonalNotificationDto personNotiDto) throws Exception {
        try {
            User user = userRepository.findById(personNotiDto.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 userId의 유저를 찾을 수 없습니다."));
            PaymentHistory paymentHistory = paymentHistoryRepository.findById(personNotiDto.getPayId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 payId의 정산을 찾을 수 없습니다."));

            personNotiRepository.deleteByUserAndPaymentHistory(user, paymentHistory);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    // 개인 알람 리스트 가져오기
    public List<PersonalNotificationDto> getAlarmList(UserIdRequestDto userIdRequestDto) throws Exception{
        try {
            userRepository.findById(userIdRequestDto.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 userId의 유저를 찾을 수 없습니다."));

            List<PersonalNotification> alarmList = personNotiRepository.findByUserUserId(userIdRequestDto.getUserId());

            return convertToDtoList(alarmList);
        }
        catch (Exception e){
            e.printStackTrace();
            throw new Exception();
        }
    }


    public List<PersonalNotificationDto> convertToDtoList(List<PersonalNotification> entities) {
        return entities.stream()
                .map(p -> new PersonalNotificationDto(
                        p.getNotificationId(),
                        p.getType(),
                        p.getContent(),
                        p.getTitle(),
                        p.getUser().getUserId(),
                        p.getPaymentHistory().getPayId()
                ))
                .collect(Collectors.toList());
    }
}
