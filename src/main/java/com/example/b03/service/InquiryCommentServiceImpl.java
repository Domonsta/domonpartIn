package com.example.b03.service;

import com.example.b03.domain.Inquiry;
import com.example.b03.domain.InquiryComment;
import com.example.b03.domain.Member;
import com.example.b03.dto.InquiryCommentRequestDTO;
import com.example.b03.dto.InquiryCommentResponseDTO;
import com.example.b03.repository.InquiryCommentRepository;
import com.example.b03.repository.InquiryRepository;
import com.example.b03.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryCommentServiceImpl implements InquiryCommentService {

    private final InquiryCommentRepository inquiryCommentRepository;
    private final InquiryRepository inquiryRepository;
    private final MemberRepository memberRepository;

    //관리자 답변 작성
    @Override
    public InquiryCommentResponseDTO createComment(InquiryCommentRequestDTO requestDTO) {
        // 1. 문의글 존재 여부 확인
        Inquiry inquiry = inquiryRepository.findById(requestDTO.getInquiryId())
                .orElseThrow(() -> new IllegalArgumentException("해당 문의를 찾을 수 없습니다. inquiryId: " + requestDTO.getInquiryId()));

        if (inquiry.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 문의에는 답변을 작성할 수 없습니다.");
        }

        // 2. 관리자 존재 여부 확인 및 역할 검사
        Member adminMember = memberRepository.findById(requestDTO.getAdminNo())
                .orElseThrow(() -> new IllegalArgumentException("해당 관리자를 찾을 수 없습니다. adminNo: " + requestDTO.getAdminNo()));

        // --- 실제 관리자 역할(Role)을 가지고 있는지 추가적인 권한 검사 로직 ---
        // adminMember의 MembershipType의 typeName이 "ADMIN"인지 확인
        if (adminMember.getMembershipType() == null || !adminMember.getMembershipType().getTypeName().equals("ADMIN")) {
            throw new IllegalArgumentException("답변을 작성할 권한이 없습니다. (관리자 권한 필요)");
        }
        // ------------------------------------------------------------------

        // 3. DTO를 InquiryComment 도메인으로 변환 및 저장
        InquiryComment comment = InquiryComment.builder()
                .inquiry(inquiry)
                .content(requestDTO.getContent())
                .admin(adminMember)
                .build();

        InquiryComment savedComment = inquiryCommentRepository.save(comment);
        return entityToInquiryCommentResponseDTO(savedComment);
    }

    //관리자 답변 수정
    @Override
    public InquiryCommentResponseDTO updateComment(Integer commentId, InquiryCommentRequestDTO requestDTO) {
        InquiryComment comment = inquiryCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 답변을 찾을 수 없습니다. commentId: " + commentId));

        if (comment.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 답변은 수정할 수 없습니다.");
        }

        // --- 수정하려는 관리자와 답변을 작성한 관리자가 동일한지 확인하는 로직 ---
        if (!comment.getAdmin().getMemberNo().equals(requestDTO.getAdminNo())) {
            throw new IllegalArgumentException("답변 작성 관리자만 수정할 수 있습니다.");
        }
        // ------------------------------------------------------------------

        comment.setContent(requestDTO.getContent());

        InquiryComment updatedComment = inquiryCommentRepository.save(comment);
        return entityToInquiryCommentResponseDTO(updatedComment);
    }

    //관리자 답변 삭제(논리적 삭제)
    @Override
    public void deleteComment(Integer commentId, Integer adminNo) {
        InquiryComment comment = inquiryCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 답변을 찾을 수 없습니다. commentId: " + commentId));

        if (comment.getIsDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 답변입니다.");
        }

        // --- 삭제하려는 관리자와 답변을 작성한 관리자가 동일한지 확인하는 로직 ---
        if (!comment.getAdmin().getMemberNo().equals(adminNo)) {
            throw new IllegalArgumentException("답변 작성 관리자만 삭제할 수 있습니다.");
        }
        // ------------------------------------------------------------------

        comment.setIsDeleted(true); // 논리적 삭제
        inquiryCommentRepository.save(comment);
    }

    //특정 문의에 대한 답변 목록 조회(관리자용)
    @Override
    @Transactional(readOnly = true)
    public List<InquiryCommentResponseDTO> getCommentsForInquiry(Integer inquiryId) {
        inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 문의를 찾을 수 없습니다. inquiryId: " + inquiryId));

        List<InquiryComment> comments = inquiryCommentRepository.findAllByInquiry_InquiryIdAndIsDeletedFalseOrderByCreatedAtAsc(inquiryId);
        return comments.stream()
                .map(this::entityToInquiryCommentResponseDTO)
                .collect(Collectors.toList());
    }

    // --- DTO 변환 헬퍼 메서드 ---
    private InquiryCommentResponseDTO entityToInquiryCommentResponseDTO(InquiryComment comment) {
        return InquiryCommentResponseDTO.builder()
                .commentId(comment.getCommentId())
                .inquiryId(comment.getInquiry().getInquiryId())
                .content(comment.getContent())
                .adminId(comment.getAdmin().getMemberNo())
                // .adminName(comment.getAdmin().getName()) // 관리자 이름도 필요하다면 Member 도메인에 name 필드 추가 후 사용
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .isDeleted(comment.getIsDeleted())
                .build();
    }
}