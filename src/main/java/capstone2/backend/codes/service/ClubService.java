package capstone2.backend.codes.service;

import capstone2.backend.codes.dto.ClubDto;
import capstone2.backend.codes.entity.Club;
import capstone2.backend.codes.repository.ClubMembersRepository;
import capstone2.backend.codes.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubService {
    private final ClubRepository clubRepository;
    private final ClubMembersRepository clubMembersRepository;

    // 내 클럽 조회
    public List<Club> getClubsByUserId(String userId) throws Exception {
        try {
            return clubMembersRepository.findClubsByUserId(userId);
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
/*

    // 동아리 추가
    public boolean addClub(ClubDto clubDto) throws Exception {
        try {
            // 해당 학교의 동아리명 중복 확인
            if (clubRepository.)
        }
    }
*/
}
