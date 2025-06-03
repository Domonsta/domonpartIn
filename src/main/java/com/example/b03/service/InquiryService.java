package com.example.b03.service;

import com.example.b03.dto.InquiryListDTO;
import com.example.b03.dto.InquiryRegisterRequestDTO;
import com.example.b03.dto.InquiryResponseDTO;

import java.util.List;

public interface InquiryService {
    // 1:1 문의글 전체보기 (목록 조회)
    List<InquiryListDTO> getAllInquiries();

    // 1:1 문의글 상세보기
    InquiryResponseDTO getInquiryDetail(Integer inquiryId);

    // 1:1 문의글 작성
    InquiryResponseDTO registerInquiry(InquiryRegisterRequestDTO requestDTO);

    // 1:1 문의글 수정
    InquiryResponseDTO updateInquiry(Integer inquiryId, InquiryRegisterRequestDTO requestDTO);

    // 1:1 문의글 삭제 (논리적 삭제)
    // 여기 파라미터를 InquiryServiceImpl과 동일하게 맞춰줘야 해!
    void deleteInquiry(Integer inquiryId, Integer requestingUserMemberNo); // <-- 이 부분이 수정되었어!
}