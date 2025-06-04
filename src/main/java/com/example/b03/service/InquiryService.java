// src/main/java/com/example/b03/service/InquiryService.java
package com.example.b03.service;

import com.example.b03.dto.InquiryListDTO;
import com.example.b03.dto.InquiryPageRequestDTO;
import com.example.b03.dto.InquiryPageResponseDTO;

// ⭐⭐ InquiryService 인터페이스 ⭐⭐
public interface InquiryService {

    Integer register(InquiryListDTO inquiryListDTO);

    InquiryListDTO readOne(Integer inquiryId);

    void modify(InquiryListDTO inquiryListDTO);

    void remove(Integer inquiryId);

    // ⭐⭐⭐ 이 메서드가 InquiryServiceImpl의 list 메서드와 정확히 일치해야 합니다! ⭐⭐⭐
    // 만약 에러 메시지처럼 getAllInquiries였다면 이 부분을 그렇게 바꿔주세요.
    // 현재 InquiryServiceImpl의 구현에 맞춰 list로 선언합니다.
    InquiryPageResponseDTO<InquiryListDTO> list(InquiryPageRequestDTO inquiryPageRequestDTO);
}