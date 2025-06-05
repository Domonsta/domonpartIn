package com.example.b03.controller;

import com.example.b03.dto.InquiryListDTO;
import com.example.b03.dto.InquiryPageRequestDTO;
import com.example.b03.dto.InquiryPageResponseDTO;
import com.example.b03.dto.InquiryCommentResponseDTO; // 댓글 DTO 임포트
import com.example.b03.service.InquiryService;
import com.example.b03.service.InquiryCommentService; // 댓글 서비스 임포트
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List; // List 임포트

@Controller
@RequestMapping("/inquiry")
@RequiredArgsConstructor
@Log4j2
public class InquiryWebController {

    private final InquiryService inquiryService;
    private final InquiryCommentService inquiryCommentService; // InquiryCommentService 주입

    // 문의 목록 페이지 (GET /inquiry/list)
    @GetMapping("/list")
    public void list(InquiryPageRequestDTO inquiryPageRequestDTO, Model model) {
        log.info("GET /inquiry/list ... pageRequestDTO: " + inquiryPageRequestDTO);
        // ⭐⭐ inquiryService.list()로 수정! ⭐⭐
        InquiryPageResponseDTO<InquiryListDTO> responseDTO = inquiryService.list(inquiryPageRequestDTO);
        model.addAttribute("responseDTO", responseDTO);
        model.addAttribute("pageRequestDTO", inquiryPageRequestDTO);
    }

    // 새 문의 등록 폼 페이지 (GET /inquiry/register)
    @GetMapping("/register")
    public void registerGet(){
        log.info("GET /inquiry/register ... 등록 폼 페이지 요청");
    }

    // 새 문의 등록 처리 및 리다이렉트 (POST /inquiry/register)
    @PostMapping("/register")
    public String registerPost(@Valid InquiryListDTO inquiryListDTO,
                               RedirectAttributes redirectAttributes){
        log.info("POST /inquiry/register ... 등록 처리 요청: " + inquiryListDTO);
        Integer inquiryId = inquiryService.register(inquiryListDTO);
        redirectAttributes.addFlashAttribute("result", inquiryId + "번 문의글이 성공적으로 등록되었습니다! 🎉");
        return "redirect:/inquiry/read?inquiryId=" + inquiryId;
    }

    // 특정 문의 상세 조회 페이지 (GET /inquiry/read)
    @GetMapping("/read")
    public void read(@ModelAttribute("pageRequestDTO") InquiryPageRequestDTO pageRequestDTO,
                     @RequestParam("inquiryId") Integer inquiryId,
                     Model model){
        log.info("GET /inquiry/read ... ID: " + inquiryId + ", PageRequest: " + pageRequestDTO);

        // 1. 문의글 상세 정보 가져오기
        InquiryListDTO inquiryDTO = inquiryService.readOne(inquiryId);
        model.addAttribute("inquiryDTO", inquiryDTO);

        // 2. ⭐️⭐️⭐️ 해당 문의글의 댓글 목록 가져오기 ⭐️⭐️⭐️
        List<InquiryCommentResponseDTO> comments = inquiryCommentService.getCommentsForInquiry(inquiryId);
        model.addAttribute("comments", comments); // 모델에 댓글 목록 추가!
    }

    // 문의 수정 폼 페이지 (GET /inquiry/modify)
    @GetMapping("/modify")
    public void modifyGet(@ModelAttribute("pageRequestDTO") InquiryPageRequestDTO pageRequestDTO,
                          @RequestParam("inquiryId") Integer inquiryId,
                          Model model){
        log.info("GET /inquiry/modify ... ID: " + inquiryId + ", PageRequest: " + pageRequestDTO);
        InquiryListDTO inquiryDTO = inquiryService.readOne(inquiryId);
        model.addAttribute("inquiryDTO", inquiryDTO);
    }

    // 문의 수정 처리 및 리다이렉트 (POST /inquiry/modify)
    @PostMapping("/modify")
    public String modifyPost(@Valid InquiryListDTO inquiryListDTO,
                             RedirectAttributes redirectAttributes,
                             @ModelAttribute("pageRequestDTO") InquiryPageRequestDTO pageRequestDTO){
        log.info("POST /inquiry/modify ... 수정 처리 요청: " + inquiryListDTO + ", PageRequest: " + pageRequestDTO);

        inquiryService.modify(inquiryListDTO);

        redirectAttributes.addFlashAttribute("result", inquiryListDTO.getInquiryId() + "번 문의글이 성공적으로 수정되었습니다! ✨");

        return "redirect:/inquiry/read?inquiryId=" + inquiryListDTO.getInquiryId() +
                "&page=" + pageRequestDTO.getPage() +
                "&size=" + pageRequestDTO.getSize() +
                (pageRequestDTO.getSearchType() != null && !pageRequestDTO.getSearchType().isEmpty() ? "&searchType=" + pageRequestDTO.getSearchType() : "") +
                (pageRequestDTO.getSearchKeyword() != null && !pageRequestDTO.getSearchKeyword().isEmpty() ? "&searchKeyword=" + pageRequestDTO.getSearchKeyword() : "");
    }

    // 문의 삭제 처리 및 리다이렉트 (POST /inquiry/remove)
    @PostMapping("/remove")
    public String removePost(@RequestParam("inquiryId") Integer inquiryId,
                             RedirectAttributes redirectAttributes){
        log.info("POST /inquiry/remove ... 삭제 처리 요청: " + inquiryId);

        inquiryService.remove(inquiryId);

        redirectAttributes.addFlashAttribute("result", inquiryId + "번 문의글이 성공적으로 삭제되었습니다! 🗑️");

        return "redirect:/inquiry/list";
    }
}