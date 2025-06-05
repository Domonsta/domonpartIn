package com.example.b03.service;

import com.example.b03.dto.InquiryCommentRequestDTO;
import com.example.b03.dto.InquiryCommentResponseDTO;
// import com.example.b03.dto.InquiryCommentPageRequestDTO; // ⭐ 제거: 존재하지 않는 DTO
// import com.example.b03.dto.InquiryCommentPageResponseDTO; // ⭐ 제거: 존재하지 않는 DTO

import java.util.List;

public interface InquiryCommentService {

    /**
     * 관리자 답변을 작성합니다.
     * @param requestDTO 답변 요청 DTO (inquiryId, adminNo, content 포함)
     * @return 작성된 답변의 응답 DTO
     */
    InquiryCommentResponseDTO createComment(InquiryCommentRequestDTO requestDTO);

    /**
     * 관리자 답변을 수정합니다.
     * @param commentId 수정할 답변의 ID
     * @param requestDTO 수정할 답변 내용 DTO (content, adminNo 포함)
     * @return 수정된 답변의 응답 DTO
     */
    InquiryCommentResponseDTO updateComment(Integer commentId, InquiryCommentRequestDTO requestDTO);

    /**
     * 관리자 답변을 논리적으로 삭제합니다.
     * @param commentId 삭제할 답변의 ID
     * @param adminNo 삭제를 요청한 관리자의 ID
     */
    void deleteComment(Integer commentId, Integer adminNo);

    /**
     * 특정 문의에 대한 답변 목록을 조회합니다. (삭제되지 않은 답변만)
     * @param inquiryId 답변을 조회할 문의글의 ID
     * @return 해당 문의글에 대한 답변 응답 DTO 목록
     */
    List<InquiryCommentResponseDTO> getCommentsForInquiry(Integer inquiryId); // ⭐ 수정: 반환 타입, 파라미터 변경

    //댓글 개수 세는 메소드 추가!
    int getCommentCountByInquiryId(Integer inquiryId);
}