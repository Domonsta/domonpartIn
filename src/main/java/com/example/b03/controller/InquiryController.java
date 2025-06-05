package com.example.b03.controller;

import com.example.b03.dto.InquiryListDTO;
import com.example.b03.dto.InquiryPageRequestDTO;
import com.example.b03.dto.InquiryPageResponseDTO;
import com.example.b03.service.InquiryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // ⭐ @RestController 사용을 위해 변경

@RestController // ⭐ @RestController로 변경
@RequestMapping("/api/inquiries") // 🛣️ REST API의 기본 URL 경로
@RequiredArgsConstructor
@Log4j2
public class InquiryController { // ⭐ 이 컨트롤러는 REST API 역할만 해.

    private final InquiryService inquiryService;

    // ➕ 새 문의 등록 API (POST /api/inquiries)
    @PostMapping("")
    public ResponseEntity<Integer> registerInquiry(@Valid @RequestBody InquiryListDTO inquiryListDTO) {
        log.info("새 문의 등록 요청: " + inquiryListDTO);
        Integer inquiryId = inquiryService.register(inquiryListDTO);
        return new ResponseEntity<>(inquiryId, HttpStatus.CREATED);
    }

    // 🔍 특정 문의 조회 API (GET /api/inquiries/{inquiryId})
    @GetMapping("/{inquiryId}")
    public ResponseEntity<InquiryListDTO> getInquiry(@PathVariable("inquiryId") Integer inquiryId) {
        log.info("문의 조회 요청 (ID: " + inquiryId + ")");
        InquiryListDTO inquiryListDTO = inquiryService.readOne(inquiryId);
        return new ResponseEntity<>(inquiryListDTO, HttpStatus.OK);
    }

    // ✏️ 문의 수정 API (PUT /api/inquiries/{inquiryId})
    @PutMapping("/{inquiryId}")
    public ResponseEntity<Void> modifyInquiry(@PathVariable("inquiryId") Integer inquiryId,
                                              @Valid @RequestBody InquiryListDTO inquiryListDTO) {
        log.info("문의 수정 요청 (ID: " + inquiryId + ", DTO: " + inquiryListDTO + ")");
        inquiryListDTO.setInquiryId(inquiryId);
        inquiryService.modify(inquiryListDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 🗑️ 문의 삭제 API (DELETE /api/inquiries/{inquiryId})
    @DeleteMapping("/{inquiryId}")
    public ResponseEntity<Void> removeInquiry(@PathVariable("inquiryId") Integer inquiryId) {
        log.info("문의 삭제 요청 (ID: " + inquiryId + ")");
        inquiryService.remove(inquiryId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // 📄 문의 목록 조회 API (GET /api/inquiries)
    @GetMapping("")
    public ResponseEntity<InquiryPageResponseDTO<InquiryListDTO>> getInquiryList(
            @ModelAttribute InquiryPageRequestDTO inquiryPageRequestDTO) {
        log.info("문의 목록 조회 요청: " + inquiryPageRequestDTO);
        InquiryPageResponseDTO<InquiryListDTO> responseDTO = inquiryService.list(inquiryPageRequestDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
}