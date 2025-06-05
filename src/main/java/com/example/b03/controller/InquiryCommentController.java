package com.example.b03.controller;

import com.example.b03.dto.InquiryCommentRequestDTO;
import com.example.b03.dto.InquiryCommentResponseDTO;
import com.example.b03.service.InquiryCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // REST API 컨트롤러
@RequestMapping("/api/comments") // 🛣️ 댓글 관련 API의 기본 URL
@RequiredArgsConstructor
@Log4j2
public class InquiryCommentController {

    private final InquiryCommentService inquiryCommentService;

    // ➕ 새 댓글 등록 API (POST /api/comments)
    @PostMapping("")
    public ResponseEntity<InquiryCommentResponseDTO> createComment(@Valid @RequestBody InquiryCommentRequestDTO requestDTO) {
        log.info("새 댓글 등록 요청: " + requestDTO);
        InquiryCommentResponseDTO responseDTO = inquiryCommentService.createComment(requestDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    // 🔍 특정 문의글의 댓글 목록 조회 API (GET /api/comments/inquiry/{inquiryId})
    // NOTE: InquiryWebController의 read() 메서드에서 직접 comments를 모델에 추가했으므로,
    // 이 API는 클라이언트에서 AJAX로 댓글을 동적으로 로드하고 싶을 때 사용하면 돼.
    // 현재 read.html은 서버사이드 렌더링으로 댓글을 가져오므로, 이 API는 당장은 필수는 아님.
    @GetMapping("/inquiry/{inquiryId}")
    public ResponseEntity<List<InquiryCommentResponseDTO>> getCommentsForInquiry(@PathVariable("inquiryId") Integer inquiryId) {
        log.info("문의 댓글 목록 조회 요청 (Inquiry ID: " + inquiryId + ")");
        List<InquiryCommentResponseDTO> comments = inquiryCommentService.getCommentsForInquiry(inquiryId);
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }

    // ✏️ 댓글 수정 API (PUT /api/comments/{commentId})
    @PutMapping("/{commentId}")
    public ResponseEntity<InquiryCommentResponseDTO> updateComment(@PathVariable("commentId") Integer commentId,
                                                                   @Valid @RequestBody InquiryCommentRequestDTO requestDTO) {
        log.info("댓글 수정 요청 (Comment ID: " + commentId + ", DTO: " + requestDTO + ")");
        InquiryCommentResponseDTO responseDTO = inquiryCommentService.updateComment(commentId, requestDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    // 🗑️ 댓글 삭제 API (DELETE /api/comments/{commentId})
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable("commentId") Integer commentId,
                                              @RequestParam("adminNo") Integer adminNo) { // 삭제 요청 관리자 번호도 받음
        log.info("댓글 삭제 요청 (Comment ID: " + commentId + ", Admin No: " + adminNo + ")");
        inquiryCommentService.deleteComment(commentId, adminNo);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // 갯수 조회 API (GET /api/comments/count/{inquiryId})
    @GetMapping("/count/{inquiryId}")
    public ResponseEntity<Integer> getCommentCount(@PathVariable("inquiryId") Integer inquiryId) {
        log.info("댓글 개수 조회 요청 (Inquiry ID: " + inquiryId + ")");
        int count = inquiryCommentService.getCommentCountByInquiryId(inquiryId);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }
}