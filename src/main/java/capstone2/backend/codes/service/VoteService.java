package capstone2.backend.codes.service;

import capstone2.backend.codes.dto.*;

import capstone2.backend.codes.entity.*;
import capstone2.backend.codes.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoteService {
    private final VoteRepository voteRepository;
    private final VoteContentRepository voteContentRepository;
    private final VoteStateRepository voteStateRepository;
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;

    // 투표 작성하기
    public Boolean writeVote(VoteWriteDto voteWriteDto) throws Exception {
        try {
            String voteID = UUID.randomUUID().toString().replace("-", "");
            Club club = new Club(
                    voteWriteDto.getClubId(),"clubName",
                    null, null,"category",
                    null, null,false,null, null,null

            );

            Vote vote = new Vote(
                    voteID,
                    voteWriteDto.getClubId(),
                    voteWriteDto.getEndDate(),
                    voteWriteDto.getTitle(),
                    voteWriteDto.getContent(),
                    voteWriteDto.isAnonymous(),
                    false,
                    club,
                    new ArrayList<>()
            );

            for (VoteContentWriteDto voteContentWriteDto : voteWriteDto.getVoteContets()){
                String voteContentID = UUID.randomUUID().toString().replace("-", "");
                VoteContent voteContent = new VoteContent(
                        voteContentID,
                        voteID,
                        voteContentWriteDto.getField(),
                        voteContentWriteDto.getVoteNum(),
                        vote
                );
                vote.getVoteContents().add(voteContent);

            }
            voteRepository.save(vote);
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            throw new Exception();
        }
    }

    // 투표 가져오기
    public GetVoteDto getVote(String voteID) throws Exception {
        try {
            Vote vote = voteRepository.findById(voteID)
                    .orElseThrow(() -> new IllegalArgumentException("해당 voteID의 투표를 찾을 수 없습니다."));

            // 투표 종료 현황
            boolean isEnd = vote.getEndDate() != null && LocalDateTime.now().isAfter(vote.getEndDate());

            // 투표 내용들..
            List<Object[]> results = voteContentRepository.findVoteContentWithCount(voteID);

            List<VoteContentDto> voteContentDtos = results.stream().map(record -> new VoteContentDto(
                    (String) record[0],
                    (String) record[1],
                    (String) record[2],
                    ((Number) record[3]).intValue(),
                    ((Number) record[4]).intValue()
            )).collect(Collectors.toList());

            return new GetVoteDto(
                    vote.getVoteID(),
                    vote.getClubId(),
                    vote.getEndDate(),
                    isEnd,
                    vote.getTitle(),
                    vote.getContent(),
                    vote.isAnonymous(),
                    vote.isPayed(),
                    voteContentDtos
            );
        }
        catch (Exception e){
            e.printStackTrace();
            throw new Exception();
        }
    }

    // 동아리 전체 투표 리스트 가져오기
    @Transactional
    public List<GetVoteDto> getClubVoteList(String clubId) throws Exception {
        try{
            Club club = clubRepository.findById(clubId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 clubId의 동아리를 찾을 수 없습니다."));

            List<Vote> clubVoteList = club.getClubVotes(); // 연결된 모든 게시글
            List<GetVoteDto> getVoteDtoList = new ArrayList<>();

            for (Vote vote : clubVoteList) {
                getVoteDtoList.add(getVote(vote.getVoteID()));
            }
            return getVoteDtoList;
        }
        catch (Exception e){
            e.printStackTrace();
            throw new Exception();
        }
    }


    // 투표 선택
    public boolean vote (VoteStateDto voteStateDto) throws Exception {
        try {
            Vote vote = voteRepository.findById(voteStateDto.getVoteID())
                    .orElseThrow(() -> new IllegalArgumentException("해당 voteID의 투표를 찾을 수 없습니다."));
            User user = userRepository.findById(voteStateDto.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 userId의 유저를 찾을 수 없습니다."));
            VoteContent voteContent = voteContentRepository.findById(voteStateDto.getVoteContentId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 voteContentID의 투표번호를 찾을 수 없습니다."));
            VoteState voteState = new VoteState(
                    voteStateDto.getUserId(),
                    voteStateDto.getVoteID(),
                    voteContent.getVoteContentId(),
                    voteStateDto.getVoteNum(),
                    user,
                    vote,
                    voteContent
            );
            voteStateRepository.save(voteState);
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            throw new Exception();
        }
    }

    // 투표 현황
    public VoteStateDto voteState (VoteStateDto voteStateDto) throws Exception {
        try {
            voteRepository.findById(voteStateDto.getVoteID())
                    .orElseThrow(() -> new IllegalArgumentException("해당 voteID의 투표를 찾을 수 없습니다."));
            userRepository.findById(voteStateDto.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 userId의 유저를 찾을 수 없습니다."));

            // 이미 투표했는지 확인
            VoteState voteState = voteStateRepository.findByUserIdAndVoteID(
                    voteStateDto.getUserId(), voteStateDto.getVoteID());

            // 투표 이력이 없으면 null 반환
            if (voteState == null) {
                return null;
            }

            // 투표 이력이 있으면 해당 정보를 DTO로 변환하여 반환
            return new VoteStateDto(
                    voteState.getUserId(),
                    voteState.getVoteID(),
                    voteState.getVoteContentId(),
                    voteState.getVoteNum()
            );
        }
        catch (Exception e){
            e.printStackTrace();
            throw new Exception();
        }
    }

    // 투표 멤버 현황
    public List<UserDto> voteStateMembers(VoteStateDto voteStateDto) throws Exception {
        try {
            voteRepository.findById(voteStateDto.getVoteID())
                    .orElseThrow(() -> new IllegalArgumentException("해당 voteID의 투표를 찾을 수 없습니다."));

            List<User> users = voteContentRepository.findUsersByVoteContentAndContentId(voteStateDto.getVoteID(),voteStateDto.getVoteContentId());

            // User → UserDto 변환
            return users.stream()
                    .map(u -> new UserDto(
                            u.getUserId(),
                            u.getPw(),
                            u.getPhoneNum(),
                            u.getName(),
                            u.getEmail(),
                            u.getUnivName(),
                            u.isSchoolCertification(),
                            u.getDepartment(),
                            u.getStudentNum(),
                            u.getGrade(),
                            u.isGender()
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

}
