package com.example.b03.service;

import com.example.b03.dto.InquiryCommentRequestDTO;
import com.example.b03.dto.InquiryCommentResponseDTO;

import java.util.List;

public interface InquiryCommentService {
    // 관리자 답변 작성
    InquiryCommentResponseDTO createComment(InquiryCommentRequestDTO requestDTO);

    // 관리자 답변 수정
    InquiryCommentResponseDTO updateComment(Integer commentId, InquiryCommentRequestDTO requestDTO);

    // 관리자 답변 삭제 (논리적 삭제)
    void deleteComment(Integer commentId, Integer adminNo);

    // 특정 문의에 대한 답변 목록 조회 (관리자용)
    List<InquiryCommentResponseDTO> getCommentsForInquiry(Integer inquiryId);
}