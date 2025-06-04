// InquiryCommentServiceImpl.java (예시)
package com.example.b03.service;

import com.example.b03.domain.Inquiry;
import com.example.b03.domain.InquiryComment;
import com.example.b03.domain.Member;
import com.example.b03.domain.MembershipType;
import com.example.b03.dto.InquiryCommentRequestDTO;
import com.example.b03.dto.InquiryCommentResponseDTO;
import com.example.b03.repository.InquiryCommentRepository;
import com.example.b03.repository.InquiryRepository;
import com.example.b03.repository.MemberRepository;
import com.example.b03.repository.MembershipTypeRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper; // ModelMapper 임포트!
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class InquiryCommentServiceImpl implements InquiryCommentService { // ⭐ InquiryCommentService 인터페이스를 implements 합니다.

    private final InquiryCommentRepository inquiryCommentRepository;
    private final InquiryRepository inquiryRepository;
    private final MemberRepository memberRepository;
    private final MembershipTypeRepository membershipTypeRepository; // 필요하다면 추가
    private final ModelMapper modelMapper; // ⭐ ModelMapper 주입!

    // createComment, updateComment, deleteComment 메서드 구현은 그대로 유지 (세부 로직은 여기에 있겠죠?)

    @Override // 인터페이스 메서드 구현
    public InquiryCommentResponseDTO createComment(InquiryCommentRequestDTO requestDTO) {
        // ... (기존 createComment 로직)
        // 예시:
        Inquiry inquiry = inquiryRepository.findById(requestDTO.getInquiryId())
                .orElseThrow(() -> new IllegalArgumentException("해당 문의를 찾을 수 없습니다."));
        if (inquiry.getIsDeleted()) { // isDeleted 필드 확인
            throw new IllegalArgumentException("삭제된 문의에는 답변을 작성할 수 없습니다.");
        }
        Member admin = memberRepository.findById(requestDTO.getAdminNo())
                .orElseThrow(() -> new IllegalArgumentException("해당 관리자를 찾을 수 없습니다."));
        // TODO: 관리자 권한 확인 로직 추가 (testMember.getMemberNo()가 아니라 admin의 membershipType 확인)
        // if (admin.getMembershipType().getTypeNo() != ADMIN_TYPE_NO) { ... }

        InquiryComment comment = InquiryComment.builder()
                .inquiry(inquiry)
                .admin(admin)
                .content(requestDTO.getContent())
                .build();
        inquiryCommentRepository.save(comment);

        return entityToDto(comment); // ModelMapper를 사용하는 entityToDto 호출
    }

    @Override // 인터페이스 메서드 구현
    public InquiryCommentResponseDTO updateComment(Integer commentId, InquiryCommentRequestDTO requestDTO) {
        // ... (기존 updateComment 로직)
        InquiryComment comment = inquiryCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 답변을 찾을 수 없습니다."));
        if (comment.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 답변은 수정할 수 없습니다.");
        }
        if (!comment.getAdmin().getMemberNo().equals(requestDTO.getAdminNo())) {
            throw new IllegalArgumentException("답변 작성 관리자만 수정할 수 있습니다.");
        }

        comment.setContent(requestDTO.getContent()); // 내용 수정
        // comment.onUpdate()는 BaseEntity의 PreUpdate 덕분에 자동으로 호출될 것입니다.
        inquiryCommentRepository.save(comment); // 변경사항 저장

        return entityToDto(comment); // ModelMapper를 사용하는 entityToDto 호출
    }

    @Override // 인터페이스 메서드 구현
    public void deleteComment(Integer commentId, Integer adminNo) {
        // ... (기존 deleteComment 로직)
        InquiryComment comment = inquiryCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 답변을 찾을 수 없습니다."));
        if (comment.getIsDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 답변입니다.");
        }
        if (!comment.getAdmin().getMemberNo().equals(adminNo)) {
            throw new IllegalArgumentException("답변 작성 관리자만 삭제할 수 있습니다.");
        }

        comment.setIsDeleted(true); // 논리적 삭제
        inquiryCommentRepository.save(comment);
    }


    @Override // 인터페이스 메서드 구현
    @Transactional(readOnly = true) // 조회 메서드는 readOnly = true 로 설정하는게 성능에 좋아
    public List<InquiryCommentResponseDTO> getCommentsForInquiry(Integer inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 문의를 찾을 수 없습니다."));

        // 삭제되지 않은 답변만 가져오기
        List<InquiryComment> comments = inquiryCommentRepository.findByInquiryAndIsDeletedFalseOrderByCreatedAtAsc(inquiry);

        // Stream API와 ModelMapper를 사용하는 entityToDto 메서드를 사용하여 DTO 리스트로 변환
        return comments.stream()
                .map(this::entityToDto) // ⭐ ModelMapper를 사용하는 entityToDto 호출!
                .collect(Collectors.toList());
    }


    // ⭐⭐⭐ 이 메서드를 InquiryCommentService '구현체' 클래스에 넣어주세요! ⭐⭐⭐
    private InquiryCommentResponseDTO entityToDto(InquiryComment inquiryComment) {
        InquiryCommentResponseDTO dto = modelMapper.map(inquiryComment, InquiryCommentResponseDTO.class);

        // 연관관계 필드(inquiry, admin)는 ModelMapper가 직접 매핑하지 못하는 경우가 많으므로 수동으로 처리
        // 엔티티가 null일 경우를 대비해 null 체크도 해주는 것이 안전합니다.
        if (inquiryComment.getInquiry() != null) {
            dto.setInquiryId(inquiryComment.getInquiry().getInquiryId());
        }
        if (inquiryComment.getAdmin() != null) {
            dto.setAdminNo(inquiryComment.getAdmin().getMemberNo());
        }

        // isDeleted 필드는 BaseEntity에 있고 InquiryCommentResponseDTO에도 동일한 이름이 있다면 자동으로 매핑될 가능성이 높지만,
        // 명확하게 하기 위해 필요하다면 수동으로 추가할 수도 있습니다.
        // dto.setIsDeleted(inquiryComment.getIsDeleted()); // 필요하다면 추가

        return dto;
    }
}