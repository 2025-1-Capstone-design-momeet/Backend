package capstone2.backend.codes.controller;

import capstone2.backend.codes.config.Response;
import capstone2.backend.codes.dto.*;
import capstone2.backend.codes.entity.VoteState;
import capstone2.backend.codes.enums.PostType;
import capstone2.backend.codes.service.PostService;
import capstone2.backend.codes.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vote")
public class VoteController {
    private final VoteService voteService;

    @PostMapping("/write")
    public ResponseEntity<Response<?>> writePost(
            @RequestBody VoteWriteDto voteWriteDTO) {

        try {
            if (!voteService.writeVote(voteWriteDTO)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new Response<>("false", "투표 작성에 실패했습니다.", null));
            }
            else {
                return ResponseEntity.ok(
                        new Response<>("true", "투표 작성 성공", null)
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "투표 작성에 실패했습니다.", null));
        }
    }

    @PostMapping("/get")
    public ResponseEntity<Response<?>> getVote(@RequestBody VoteIDRequestDto voteIDRequestDto) {
        try {
            GetVoteDto getVoteDto = voteService.getVote(voteIDRequestDto.getVoteID());
            return ResponseEntity.ok(new Response<>("true", "투표 가져오기 성공", getVoteDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "투표 가져오기에 실패했습니다.", null));
        }
    }

    @PostMapping("/getClubVoteList")
    public ResponseEntity<Response<?>> getClubVoteList(@RequestBody ClubIdRequestDto clubIdRequestDto) {
        try {
            List<GetVoteDto> getVoteDtoList = voteService.getClubVoteList(clubIdRequestDto.getClubId());
            return ResponseEntity.ok(new Response<>("true", "동아리 전체 투표 가져오기 성공", getVoteDtoList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "동아리 전체 투표 가져오기에 실패했습니다.", null));
        }
    }

    @PostMapping("/vote")
    public ResponseEntity<Response<?>> vote(@RequestBody VoteStateDto voteStateDto) {
        try {
            if (!voteService.vote(voteStateDto)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new Response<>("false", "투표에 실패했습니다.", null));
            }
            else {
                return ResponseEntity.ok(
                        new Response<>("true", "투표 성공", null)
                );
            }        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "투표에 실패했습니다.", null));
        }
    }

    @PostMapping("/voteState")
    public ResponseEntity<Response<?>> voteState(@RequestBody VoteStateDto voteStateDto) {
        try {
            VoteStateDto voteState = voteService.voteState(voteStateDto);
            return ResponseEntity.ok(new Response<>("true", "투표 현황 가져오기 성공", voteState));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "투표 현황 가져오기에 실패했습니다.", null));
        }
    }



}
