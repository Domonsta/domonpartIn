package com.example.b03.service;

import com.example.b03.domain.InquiryComment; // 이 import는 필요 없어져요
import com.example.b03.dto.InquiryCommentRequestDTO;
import com.example.b03.dto.InquiryCommentResponseDTO;

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
     * 특정 문의에 대한 답변 목록을 조회합니다. (관리자용)
     * @param inquiryId 답변을 조회할 문의글의 ID
     * @return 해당 문의글에 대한 답변 응답 DTO 목록
     */
    List<InquiryCommentResponseDTO> getCommentsForInquiry(Integer inquiryId);

    // ⭐⭐⭐ private InquiryCommentResponseDTO entityToDto(...) 이 메서드는 인터페이스에서 제거해야 합니다! ⭐⭐⭐
    // 인터페이스는 메서드의 선언만 가능하며, private 메서드나 구현부를 가질 수 없습니다.
    // 이 메서드는 InquiryCommentService의 '구현체' 클래스에 있어야 합니다.
}