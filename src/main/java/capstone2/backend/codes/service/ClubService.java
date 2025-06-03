package capstone2.backend.codes.service;

import capstone2.backend.codes.dto.*;
import capstone2.backend.codes.entity.*;
import capstone2.backend.codes.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ClubService {
    private final ClubRepository clubRepository;
    private final ClubMembersRepository clubMembersRepository;
    private final WaitingListRepository waitingListRepository;
    private final ClubPostRepository clubPostRepository;
    private final CalendarService calendarService;
    private final ExecutiveRepository executiveRepository;
    private final PresidentRepository presidentRepository;
    private final UserRepository userRepository;
    private final ClubPromotionRepository clubPromotionRepository;

    // 내 클럽 조회
    public List<ClubSummaryDto> getClubsByUserId(String userId) throws Exception {
        try {
            return clubMembersRepository.findClubSummariesByUserId(userId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    public List<Club> getClubsByUnivName(String univName) throws Exception {
        try {
            return clubRepository.findAllByUnivName(univName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    public Club addClub(ClubDto clubDto) throws Exception {
        try {
            // 1. 중복 체크
            boolean exists = clubRepository.existsByClubNameAndUnivName(
                    clubDto.getClubName(), clubDto.getUnivName()
            );
            if (exists) return null;

            // 2. Club 생성
            String clubId = UUID.randomUUID().toString().replace("-", "");

            Club club = new Club();
            club.setClubId(clubId);
            club.setClubName(clubDto.getClubName());
            club.setProfileImage(null);
            club.setManagerId(clubDto.getManagerId());
            club.setCategory(clubDto.getCategory());
            club.setBannerImage(null);
            club.setUnivName(clubDto.getUnivName());
            club.setOfficial(false);

            clubRepository.save(club);

            // 3. 회장 등록
            registerInitialPresident(clubId, clubDto.getManagerId());

            // ✅ 4. Club 반환
            return club;

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("동아리 추가 중 오류 발생", e);
        }
    }

    public ClubIdRequestDto applyToClub(WaitingListDto waitingListDto) {
        try {
            // 중복 신청 여부 확인
            boolean exists = waitingListRepository.existsByUserIdAndClubId(
                    waitingListDto.getUserId(),
                    waitingListDto.getClubId()
            );
            if (exists) return null;

            // 신청 정보 저장
            WaitingList waiting = new WaitingList();
            waiting.setUserId(waitingListDto.getUserId());
            waiting.setClubId(waitingListDto.getClubId());
            waiting.setWhy(waitingListDto.getWhy());
            waiting.setWhat(waitingListDto.getWhat());
            waitingListRepository.save(waiting);

            ClubIdRequestDto clubIdRequestDto = new ClubIdRequestDto();
            clubIdRequestDto.setClubId(waitingListDto.getClubId());
            return clubIdRequestDto;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ClubIdRequestDto processClubApplication(WaitingListDecisionDto dto) {
        try {
            Optional<WaitingList> optional = waitingListRepository.findByUserIdAndClubId(dto.getUserId(), dto.getClubId());
            if (optional.isEmpty()) {
                return null;
            }

            // 승인 처리
            if ("approve".equalsIgnoreCase(dto.getAction())) {
                ClubMembers member = new ClubMembers();
                member.setUserId(dto.getUserId());
                member.setClubId(dto.getClubId());
                member.setRole(null);
                clubMembersRepository.save(member);
            }

            // 승인이든 거절이든 WaitingList에서 삭제
            waitingListRepository.delete(optional.get());

            // ✅ 승인 또는 거절 후 clubId 반환
            ClubIdRequestDto result = new ClubIdRequestDto();
            result.setClubId(dto.getClubId());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<RecentPostDto> getTop3RecentPosts(String clubId) {
        return clubPostRepository.findTop3ByClub_ClubIdOrderByPost_FixactionDescPost_DateDesc(clubId).stream()
                .map(cp -> {
                    Post p = cp.getPost();
                    return new RecentPostDto(
                            p.getPostNum(),
                            p.getTitle(),
                            p.getLike(),
                            p.getDate()
                    );
                })
                .toList();
    }

    public ClubMainDto getClubMainInfo(String clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("동아리가 존재하지 않습니다."));

        // ✅ 인원 수 계산: 회장 + 임원 + 멤버
        int presidentCount = presidentRepository.findByClubId(clubId).isPresent() ? 1 : 0;
        int executiveCount = executiveRepository.countByClubId(clubId);
        int memberCount = clubMembersRepository.countByClubId(clubId);
        int totalCount = presidentCount + executiveCount + memberCount;

        String welcomeMessage = "안녕하세요, " + club.getClubName() + " 입니다!";
        String bannerImage = club.getBannerImage();

        // ✅ 가장 가까운 일정 가져오기
        UpcomingScheduleDto upcoming = calendarService.getUpcomingSchedule(clubId).orElse(null);

        // ✅ 최근 게시글 3개 가져오기 (fixaction 우선 → 최신순)
        List<RecentPostDto> recentPosts = clubPostRepository
                .findTop3ByClub_ClubIdOrderByPost_FixactionDescPost_DateDesc(clubId)
                .stream()
                .map(cp -> {
                    Post post = cp.getPost();
                    return new RecentPostDto(
                            post.getPostNum(),
                            post.getTitle(),
                            post.getLike(),
                            post.getDate()
                    );
                })
                .toList();

        return new ClubMainDto(
                club.getClubName(),
                club.getUnivName(),
                club.getCategory(),
                club.isOfficial(),
                totalCount,  // ✅ 총 인원 수 반영
                bannerImage,
                welcomeMessage,
                upcoming != null ? List.of(upcoming) : List.of(),
                recentPosts
        );
    }

    public List<User> getAllUsersInClub(String clubId) {
        Set<String> seenUserIds = new HashSet<>();
        List<User> allUsers = new ArrayList<>();

        // 1. 일반 멤버
        List<User> members = clubMembersRepository.findByClubId(clubId)
                .stream()
                .map(ClubMembers::getUser)
                .filter(Objects::nonNull)
                .toList();

        for (User user : members) {
            if (seenUserIds.add(user.getUserId())) {
                allUsers.add(user);
            }
        }

        // 2. 임원
        List<User> executives = executiveRepository.findByClubId(clubId)
                .stream()
                .map(Executive::getUser)
                .filter(Objects::nonNull)
                .toList();

        for (User user : executives) {
            if (seenUserIds.add(user.getUserId())) {
                allUsers.add(user);
            }
        }

        // 3. 회장
        Optional<User> presidentOpt = presidentRepository.findByClubId(clubId)
                .map(President::getUser);

        if (presidentOpt.isPresent()) {
            User president = presidentOpt.get();
            if (seenUserIds.add(president.getUserId())) {
                allUsers.add(president);
            }
        }

        return allUsers;
    }


    /**
     * ✔ 동아리 생성 시 초기 회장 등록 (managerId를 기준으로)
     */
    public void registerInitialPresident(String clubId, String userId) {
        President president = new President();
        president.setClubId(clubId);
        president.setUserId(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 userId의 유저가 존재하지 않습니다."));
        president.setUser(user);

        presidentRepository.save(president);
    }

    /**
     * ✔ 회장 위임 기능 (기존 회장은 덮어씀)
     */
    @Transactional
    public ClubDelegateDto delegatePresident(ClubDelegateDto clubDelegateDto) {
        String clubId = clubDelegateDto.getClubId();
        String newUserId = clubDelegateDto.getNewUserId();

        // 1. 기존 회장이 있으면 → 멤버로 이동
        Optional<President> optional = presidentRepository.findByClubId(clubId);

        optional.ifPresent(oldPresident -> {
            String oldUserId = oldPresident.getUserId();

            // 이전 회장과 newUser가 같으면 아무것도 안 해도 됨
            if (!oldUserId.equals(newUserId)) {
                boolean alreadyMember = clubMembersRepository.existsByUserIdAndClubId(oldUserId, clubId);
                if (!alreadyMember) {
                    ClubMembers downgraded = new ClubMembers();
                    downgraded.setUserId(oldUserId);
                    downgraded.setClubId(clubId);
                    downgraded.setRole(null);
                    clubMembersRepository.save(downgraded);
                }
            }
        });

        // 2. newUserId가 ClubMembers 또는 Executive에 있으면 제거
        ClubMembersId newId = new ClubMembersId(newUserId, clubId);
        clubMembersRepository.findById(newId).ifPresent(clubMembersRepository::delete);
        executiveRepository.findById(newId).ifPresent(executiveRepository::delete);

        // 3. 회장 교체 (기존 없으면 새로 생성)
        President president = optional.orElseGet(() -> {
            President p = new President();
            p.setClubId(clubId);
            return p;
        });

        president.setUserId(newUserId);

        User user = userRepository.findById(newUserId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저 없음"));
        president.setUser(user);

        presidentRepository.save(president);

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("해당 동아리가 존재하지 않습니다."));

        club.setManagerId(newUserId);
        clubRepository.save(club);

        return clubDelegateDto;
    }

    public boolean canManageClub(String userId, String clubId) {
        // 회장인지 확인
        boolean isPresident = presidentRepository.findByClubId(clubId)
                .map(p -> p.getUserId().equals(userId))
                .orElse(false);

        // 임원인지 확인
        boolean isExecutive = executiveRepository.existsByUserIdAndClubId(userId, clubId);
        return isPresident || isExecutive;
    }

    public List<WaitingListListDto> getClubApplicationList(ClubIdRequestDto clubIdRequestDto) {
        try {
            String clubId = clubIdRequestDto.getClubId();
            Club club = clubRepository.findById(clubId)
                    .orElseThrow(() -> new IllegalArgumentException("동아리가 존재하지 않습니다."));
            List<WaitingList> waitingLists = waitingListRepository.findByClubId(club.getClubId());
            List<WaitingListListDto> waitingListDtos = new ArrayList<>();

            for (WaitingList waitingList : waitingLists) {
                User user = userRepository.findById(waitingList.getUserId())
                        .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));
                WaitingListListDto dto = new WaitingListListDto(
                        waitingList.getUserId(),
                        user.getName(),
                        user.getDepartment(),
                        user.getStudentNum(),
                        user.getGrade(),
                        waitingList.getWhy(),
                        waitingList.getWhat()
                );
                waitingListDtos.add(dto);
            }
            return waitingListDtos;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("동아리 신청 리스트 조회 중 오류 발생");
        }
    }

    public ClubPromotionDto writeClubPromotion(ClubPromotionDto clubPromotionDto) {
        try {
            String clubId = clubPromotionDto.getClubId();

            Club club = clubRepository.findById(clubId)
                    .orElseThrow(() -> new IllegalArgumentException("동아리가 존재하지 않습니다."));

            // 기존 ClubPromotion이 있다면 업데이트
            ClubPromotion promotion = clubPromotionRepository.findByClubId(clubId)
                    .orElse(new ClubPromotion()); // 없으면 새로 생성

            // 핵심: @MapsId 사용 시 club만 설정하면 clubId 자동 설정됨
            promotion.setClub(club);
            promotion.setTarget(clubPromotionDto.getTarget());
            promotion.setDues(clubPromotionDto.getDues());
            promotion.setInterview(clubPromotionDto.isInterview());
            promotion.setEndDate(clubPromotionDto.getEndDate());
            promotion.setRecruiting(clubPromotionDto.isRecruiting());

            clubPromotionRepository.save(promotion);

            return new ClubPromotionDto(
                    club.getClubId(),
                    promotion.getTarget(),
                    promotion.getDues(),
                    promotion.isInterview(),
                    promotion.getEndDate(),
                    promotion.isRecruiting()
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("동아리 홍보 작성 중 오류 발생", e);
        }
    }

    public ClubPromotionDetailDto getClubPromotionDetail(ClubIdRequestDto clubIdRequestDto) {
        try {
            String clubId = clubIdRequestDto.getClubId();
            Club club = clubRepository.findById(clubId)
                    .orElseThrow(() -> new IllegalArgumentException("동아리가 존재하지 않습니다."));

            ClubPromotion promotion = clubPromotionRepository.findByClubId(clubId)
                    .orElse(null);

            if (promotion == null) {
                return null;
            }
            return new ClubPromotionDetailDto(
                    club.getClubName(),
                    club.getCategory(),
                    promotion.getTarget(),
                    promotion.getDues(),
                    promotion.isInterview(),
                    promotion.getEndDate(),
                    promotion.isRecruiting()
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("동아리 홍보 상세 조회 중 오류 발생", e);
        }
    }

    @Transactional
    public ExecutiveRequestDto addClubExecutive(ExecutiveRequestDto executiveRequestDto) {
        try {
            String clubId         = executiveRequestDto.getClubId();
            String callerUserId   = executiveRequestDto.getUserId();         // 권한을 확인할 사용자 ID
            String newExecutiveId = executiveRequestDto.getNewExecutiveId();  // 새로 임원으로 추가할 사용자 ID
            String duty           = executiveRequestDto.getDuty();

            // 1) 호출자(callerUserId)가 동아리를 관리할 권한이 있는지 확인
            if (!canManageClub(callerUserId, clubId)) {
                throw new IllegalArgumentException("해당 동아리를 관리할 권한이 없습니다.");
            }

            // 2) 이미 newExecutiveId가 같은 동아리의 임원인지 확인
            boolean alreadyExec = executiveRepository.existsByUserIdAndClubId(newExecutiveId, clubId);
            if (alreadyExec) {
                throw new IllegalArgumentException("이미 해당 사용자는 임원으로 등록되어 있습니다.");
            }

            // 3) Club 존재 여부 확인 (권한 검사에서 이미 존재를 확인한 경우가 많지만, 안전을 위해 다시 확인)
            clubRepository.findById(clubId)
                    .orElseThrow(() -> new IllegalArgumentException("동아리가 존재하지 않습니다."));

            // 4) 새 임원(newExecutiveId)이 유저 테이블에 존재하는지 확인
            User user = userRepository.findById(newExecutiveId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

            // 5) Executive 엔티티 생성 및 필드 세팅
            Executive executive = new Executive();
            executive.setUserId(newExecutiveId);
            executive.setClubId(clubId);
            executive.setDuty(duty);
            executive.setUser(user);

            // 6) 저장
            executiveRepository.save(executive);

            return executiveRequestDto;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("임원 추가 중 오류 발생", e);
        }
    }

    @Transactional
    public boolean deleteExecutive(ExecutiveRequestDto executiveRequestDto) {
        String clubId       = executiveRequestDto.getClubId();
        String callerUserId = executiveRequestDto.getUserId();
        String targetExecId = executiveRequestDto.getNewExecutiveId();

        // 1) 호출자(callerUserId)가 동아리를 관리할 권한이 있는지 확인
        if (!canManageClub(callerUserId, clubId)) {
            throw new IllegalArgumentException("해당 동아리를 관리할 권한이 없습니다.");
        }

        // 2) 삭제할 임원(targetExecId)이 실제로 존재하는지 확인
        ClubMembersId execId = new ClubMembersId(targetExecId, clubId);
        Executive execEntity = executiveRepository.findById(execId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 임원이 존재하지 않습니다."));

        // 3) 해당 임원 레코드 삭제
        executiveRepository.delete(execEntity);

        // 4) 삭제된 사용자를 일반 멤버로 등록 (이미 멤버라면 중복 방지)
        boolean alreadyMember = clubMembersRepository.existsByUserIdAndClubId(targetExecId, clubId);
        if (!alreadyMember) {
            ClubMembers downgraded = new ClubMembers();
            downgraded.setUserId(targetExecId);
            downgraded.setClubId(clubId);
            downgraded.setRole(null); // 역할이 없는 일반 멤버로 처리
            clubMembersRepository.save(downgraded);
        }

        return true;
    }
        public List<ClubMembersDto> getClubMembers(String clubId) {
            try {
                // 1) 일반 멤버(ClubMembers) 조회
                List<ClubMembers> members       = clubMembersRepository.findByClubId(clubId);
                // 2) 임원(Executive) 조회
                List<Executive>   executives    = executiveRepository.findByClubId(clubId);
                // 3) 회장(President) 조회 (Optional)
                Optional<President> presidentOpt = presidentRepository.findByClubId(clubId);

                List<ClubMembersDto> memberDtos = new ArrayList<>();

                // --- 1) 일반 멤버 처리 (role만 세팅) ---
                for (ClubMembers cm : members) {
                    String userId = cm.getUserId();
                    userRepository.findById(userId).ifPresent(user -> {
                        ClubMembersDto dto = new ClubMembersDto();
                        dto.setUserId(user.getUserId());
                        dto.setUserName(user.getName());
                        dto.setRole(cm.getRole());        // 일반 멤버만 role 채워줌
                        dto.setDuty(null);               // duty는 비워둠
                        dto.setDepartment(user.getDepartment());
                        memberDtos.add(dto);
                    });
                }

                // --- 2) 임원 처리 (duty만 세팅) ---
                for (Executive ex : executives) {
                    String userId = ex.getUserId();
                    userRepository.findById(userId).ifPresent(user -> {
                        ClubMembersDto dto = new ClubMembersDto();
                        dto.setUserId(user.getUserId());
                        dto.setUserName(user.getName());
                        dto.setDuty(ex.getDuty());      // 임원만 duty 채워줌 (예: “총무”, “서기” 등)
                        dto.setRole(null);              // role은 비워둠
                        dto.setDepartment(user.getDepartment());
                        memberDtos.add(dto);
                    });
                }

                // --- 3) 회장 처리 (President) ---
                if (presidentOpt.isPresent()) {
                    President pres = presidentOpt.get();
                    String userId = pres.getUserId();
                    userRepository.findById(userId).ifPresent(user -> {
                        ClubMembersDto dto = new ClubMembersDto();
                        dto.setUserId(user.getUserId());
                        dto.setUserName(user.getName());
                        dto.setDuty("회장");  // 회장은 duty에 “동아리장” 고정
                        dto.setRole(null);       // role은 비워둠
                        dto.setDepartment(user.getDepartment());
                        memberDtos.add(dto);
                    });
                }

                return memberDtos;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("동아리 멤버 조회 중 오류 발생", e);
            }
        }
}
