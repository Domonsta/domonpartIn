package com.example.b03.service;

import com.example.b03.domain.Inquiry;
import com.example.b03.domain.InquiryComment;
import com.example.b03.domain.Member;
import com.example.b03.domain.MembershipType;
import com.example.b03.dto.InquiryCommentRequestDTO;
import com.example.b03.dto.InquiryCommentResponseDTO; // InquiryCommentResponseDTO import 확인!
import com.example.b03.repository.InquiryCommentRepository;
import com.example.b03.repository.InquiryRepository;
import com.example.b03.repository.MemberRepository;
import com.example.b03.repository.MembershipTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class InquiryCommentServiceTest {

    @Autowired
    private InquiryCommentService inquiryCommentService;

    @Autowired
    private InquiryCommentRepository inquiryCommentRepository;

    @Autowired
    private InquiryRepository inquiryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MembershipTypeRepository membershipTypeRepository;

    private Member testMember; // 문의글 작성 일반 회원
    private Member testAdmin; // 답변 작성 관리자 회원
    private Inquiry savedInquiry; // 테스트용 문의글
    private InquiryComment savedComment; // 테스트용 답변

    private static final Byte MEMBER_TYPE_ADMIN = 1;
    private static final Byte MEMBER_TYPE_GENERAL = 3;

    @BeforeEach
    void setUp() {
        // 테스트 전에 모든 리포지토리 초기화! ✨
        inquiryCommentRepository.deleteAllInBatch();
        inquiryRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        // 1. 일반 회원 멤버십 타입 불러오기 (없으면 에러)
        MembershipType generalMembershipType = membershipTypeRepository.findById(MEMBER_TYPE_GENERAL)
                .orElseThrow(() -> new RuntimeException("개인회원 멤버십 타입을 찾을 수 없습니다. 테스트를 위해 DB에 3번 '개인회원' 멤버십 타입이 필요합니다."));

        // 2. 관리자 멤버십 타입 불러오기 (없으면 에러)
        MembershipType adminMembershipType = membershipTypeRepository.findById(MEMBER_TYPE_ADMIN)
                .orElseThrow(() -> new RuntimeException("관리자 멤버십 타입을 찾을 수 없습니다. 테스트를 위해 DB에 1번 '관리자' 멤버십 타입이 필요합니다."));

        // 3. 일반 회원 생성 및 저장 🙋‍♂️
        testMember = memberRepository.save(Member.builder()
                .loginId("testuser123")
                .password("testpassword123!")
                .name("테스터회원")
                .birthDate(LocalDate.of(1990, 5, 15))
                .address("서울시 강남구 테헤란로 123")
                .phone("010-1234-5678")
                .membershipType(generalMembershipType)
                .build());

        // 4. 관리자 회원 생성 및 저장 👑
        testAdmin = memberRepository.save(Member.builder()
                .loginId("adminuser")
                .password("adminpassword!")
                .name("관리자")
                .birthDate(LocalDate.of(1985, 1, 1))
                .address("서울시 강남구 역삼동")
                .phone("010-0000-0000")
                .membershipType(adminMembershipType) // 관리자 타입 설정!
                .build());

        // 5. 테스트용 문의글 생성 및 저장 ❓
        savedInquiry = inquiryRepository.save(Inquiry.builder()
                .title("테스트 문의 제목입니다.")
                .content("테스트 문의 내용입니다. 답변이 필요합니다.")
                .member(testMember)
                .build());

        // 6. 테스트용 답변 생성 및 저장 💬
        savedComment = inquiryCommentRepository.save(InquiryComment.builder()
                .content("테스트 답변 내용입니다.")
                .inquiry(savedInquiry)
                .admin(testAdmin)
                .build());
    }

    // --- 관리자 답변 작성 테스트 ✍️ ---

    @Test
    @DisplayName("관리자 답변 작성 성공")
    void testCreateComment_success() {
        // given
        InquiryCommentRequestDTO requestDTO = InquiryCommentRequestDTO.builder()
                .inquiryId(savedInquiry.getInquiryId()) // 기존 문의글에 대한 답변
                .adminNo(testAdmin.getMemberNo()) // 관리자 번호
                .content("새롭게 작성하는 답변 내용입니다.")
                .build();

        // when
        InquiryCommentResponseDTO result = inquiryCommentService.createComment(requestDTO);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCommentId()).isNotNull();
        assertThat(result.getInquiryId()).isEqualTo(requestDTO.getInquiryId());
        assertThat(result.getAdminNo()).isEqualTo(requestDTO.getAdminNo()); // ⭐ adminNo 확인!
        assertThat(result.getContent()).isEqualTo(requestDTO.getContent());
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();

        // DB에 실제로 저장되었는지 확인
        InquiryComment foundComment = inquiryCommentRepository.findById(result.getCommentId()).orElse(null);
        assertThat(foundComment).isNotNull();
        assertThat(foundComment.getContent()).isEqualTo(requestDTO.getContent());
        assertThat(foundComment.getInquiry().getInquiryId()).isEqualTo(requestDTO.getInquiryId());
        assertThat(foundComment.getAdmin().getMemberNo()).isEqualTo(requestDTO.getAdminNo());
        assertThat(foundComment.getIsDeleted()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 문의글에 답변 작성 실패")
    void testCreateComment_inquiryNotFound() {
        // given
        InquiryCommentRequestDTO requestDTO = InquiryCommentRequestDTO.builder()
                .inquiryId(999) // 없는 문의글 ID
                .adminNo(testAdmin.getMemberNo())
                .content("없는 문의글에 대한 답변.")
                .build();

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryCommentService.createComment(requestDTO);
        });
        assertThat(exception.getMessage()).contains("해당 문의를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("삭제된 문의글에 답변 작성 실패")
    void testCreateComment_inquiryIsDeleted() {
        // given
        savedInquiry.setIsDeleted(true); // 문의글 삭제 처리
        inquiryRepository.save(savedInquiry);

        InquiryCommentRequestDTO requestDTO = InquiryCommentRequestDTO.builder()
                .inquiryId(savedInquiry.getInquiryId())
                .adminNo(testAdmin.getMemberNo())
                .content("삭제된 문의글에 대한 답변.")
                .build();

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryCommentService.createComment(requestDTO);
        });
        assertThat(exception.getMessage()).contains("삭제된 문의에는 답변을 작성할 수 없습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 관리자가 답변 작성 실패")
    void testCreateComment_adminNotFound() {
        // given
        InquiryCommentRequestDTO requestDTO = InquiryCommentRequestDTO.builder()
                .inquiryId(savedInquiry.getInquiryId())
                .adminNo(999) // 없는 관리자 ID
                .content("없는 관리자의 답변.")
                .build();

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryCommentService.createComment(requestDTO);
        });
        assertThat(exception.getMessage()).contains("해당 관리자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("관리자 권한이 없는 회원이 답변 작성 실패")
    void testCreateComment_notAdminRole() {
        // given
        InquiryCommentRequestDTO requestDTO = InquiryCommentRequestDTO.builder()
                .inquiryId(savedInquiry.getInquiryId())
                .adminNo(testMember.getMemberNo()) // 일반 회원으로 답변 시도
                .content("일반 회원의 답변.")
                .build();

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryCommentService.createComment(requestDTO);
        });
        assertThat(exception.getMessage()).contains("답변을 작성할 권한이 없습니다.");
    }


    // --- 관리자 답변 수정 테스트 ✏️ ---

    @Test
    @DisplayName("관리자 답변 수정 성공")
    void testUpdateComment_success() {
        // given
        InquiryCommentRequestDTO updateRequestDTO = InquiryCommentRequestDTO.builder()
                .adminNo(testAdmin.getMemberNo()) // 답변 작성 관리자 본인
                .content("수정된 답변 내용입니다. 변경되었습니다.")
                .build();

        // when
        InquiryCommentResponseDTO result = inquiryCommentService.updateComment(savedComment.getCommentId(), updateRequestDTO);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCommentId()).isEqualTo(savedComment.getCommentId());
        assertThat(result.getContent()).isEqualTo(updateRequestDTO.getContent());
        assertThat(result.getUpdatedAt()).isAfterOrEqualTo(result.getCreatedAt()); // 생성 시간보다 나중이거나 같아야 함

        // DB에서 실제로 수정되었는지 확인
        InquiryComment updatedComment = inquiryCommentRepository.findById(savedComment.getCommentId()).orElse(null);
        assertThat(updatedComment).isNotNull();
        assertThat(updatedComment.getContent()).isEqualTo(updateRequestDTO.getContent());
        assertThat(updatedComment.getUpdatedAt()).isAfterOrEqualTo(savedComment.getUpdatedAt()); // 기존 savedComment의 시간과 비교
    }

    @Test
    @DisplayName("존재하지 않는 답변 수정 시도 실패")
    void testUpdateComment_commentNotFound() {
        // given
        InquiryCommentRequestDTO updateRequestDTO = InquiryCommentRequestDTO.builder()
                .adminNo(testAdmin.getMemberNo())
                .content("없는 답변 수정.")
                .build();

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryCommentService.updateComment(999, updateRequestDTO); // 없는 답변 ID
        });
        assertThat(exception.getMessage()).contains("해당 답변을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("삭제된 답변 수정 시도 실패")
    void testUpdateComment_isDeleted() {
        // given
        savedComment.setIsDeleted(true); // 답변 삭제 처리
        inquiryCommentRepository.save(savedComment);

        InquiryCommentRequestDTO updateRequestDTO = InquiryCommentRequestDTO.builder()
                .adminNo(testAdmin.getMemberNo())
                .content("삭제된 답변 수정.")
                .build();

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryCommentService.updateComment(savedComment.getCommentId(), updateRequestDTO);
        });
        assertThat(exception.getMessage()).contains("삭제된 답변은 수정할 수 없습니다.");
    }

    @Test
    @DisplayName("답변 작성 관리자가 아닌 다른 관리자가 수정 시도 실패")
    void testUpdateComment_notAuthorAdmin() {
        // given
        // 새로운 관리자 생성
        MembershipType adminMembershipType = membershipTypeRepository.findById(MEMBER_TYPE_ADMIN)
                .orElseThrow(() -> new RuntimeException("관리자 멤버십 타입을 찾을 수 없습니다."));
        Member anotherAdmin = memberRepository.save(Member.builder()
                .loginId("another_admin")
                .password("another_password!")
                .name("다른관리자")
                .birthDate(LocalDate.of(1990, 1, 1))
                .address("다른 주소")
                .phone("010-1234-1234")
                .membershipType(adminMembershipType)
                .build());

        InquiryCommentRequestDTO updateRequestDTO = InquiryCommentRequestDTO.builder()
                .adminNo(anotherAdmin.getMemberNo()) // 다른 관리자 번호로 수정 시도
                .content("다른 관리자가 수정.")
                .build();

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryCommentService.updateComment(savedComment.getCommentId(), updateRequestDTO);
        });
        assertThat(exception.getMessage()).contains("답변 작성 관리자만 수정할 수 있습니다.");
    }

    // --- 관리자 답변 삭제 (논리적 삭제) 테스트 🗑️ ---

    @Test
    @DisplayName("관리자 답변 삭제 성공")
    void testDeleteComment_success() {
        // given (savedComment가 이미 DB에 있어)
        // when
        inquiryCommentService.deleteComment(savedComment.getCommentId(), testAdmin.getMemberNo());

        // then
        // DB에서 실제로 isDeleted가 true로 변경되었는지 확인
        InquiryComment deletedComment = inquiryCommentRepository.findById(savedComment.getCommentId()).orElse(null);
        assertThat(deletedComment).isNotNull();
        assertThat(deletedComment.getIsDeleted()).isTrue();

        // 삭제 후 업데이트 시간은 이전과 같거나 이후여야 함
        assertThat(deletedComment.getUpdatedAt()).isAfterOrEqualTo(savedComment.getUpdatedAt());
    }

    @Test
    @DisplayName("존재하지 않는 답변 삭제 시도 실패")
    void testDeleteComment_commentNotFound() {
        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryCommentService.deleteComment(999, testAdmin.getMemberNo()); // 없는 답변 ID
        });
        assertThat(exception.getMessage()).contains("해당 답변을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("이미 삭제된 답변 다시 삭제 시도 실패")
    void testDeleteComment_alreadyDeleted() {
        // given
        savedComment.setIsDeleted(true); // 답변 삭제 처리
        inquiryCommentRepository.save(savedComment);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryCommentService.deleteComment(savedComment.getCommentId(), testAdmin.getMemberNo());
        });
        assertThat(exception.getMessage()).contains("이미 삭제된 답변입니다.");
    }

    @Test
    @DisplayName("답변 작성 관리자가 아닌 다른 관리자가 삭제 시도 실패")
    void testDeleteComment_notAuthorAdmin() {
        // given
        // 새로운 관리자 생성
        MembershipType adminMembershipType = membershipTypeRepository.findById(MEMBER_TYPE_ADMIN)
                .orElseThrow(() -> new RuntimeException("관리자 멤버십 타입을 찾을 수 없습니다."));
        Member anotherAdmin = memberRepository.save(Member.builder()
                .loginId("yet_another_admin")
                .password("yet_another_password!")
                .name("또다른관리자")
                .birthDate(LocalDate.of(1992, 1, 1))
                .address("다른 주소")
                .phone("010-5678-5678")
                .membershipType(adminMembershipType)
                .build());

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryCommentService.deleteComment(savedComment.getCommentId(), anotherAdmin.getMemberNo()); // 다른 관리자 번호로 삭제 시도
        });
        assertThat(exception.getMessage()).contains("답변 작성 관리자만 삭제할 수 있습니다.");
    }

    // --- 특정 문의에 대한 답변 목록 조회 테스트 📚 ---

    @Test
    @DisplayName("특정 문의에 대한 답변 목록 조회 성공 (삭제되지 않은 답변만)")
    void testGetCommentsForInquiry_success() {
        // given
        // 새로운 답변 추가
        InquiryComment comment2 = InquiryComment.builder()
                .content("두 번째 답변입니다.")
                .inquiry(savedInquiry)
                .admin(testAdmin)
                .build();
        inquiryCommentRepository.save(comment2);

        // 삭제된 답변 추가 (setter 사용으로 변경!) ⭐⭐⭐
        InquiryComment deletedComment = InquiryComment.builder()
                .content("삭제된 답변입니다.")
                .inquiry(savedInquiry)
                .admin(testAdmin)
                // .isDeleted(true) // ⛔ 이제 이 builder 메서드는 사용하지 않아!
                .build();
        deletedComment.setIsDeleted(true); // ⭐ BaseEntity에 있는 setIsDeleted() 메서드를 직접 호출!
        inquiryCommentRepository.save(deletedComment);

        // when
        List<InquiryCommentResponseDTO> comments = inquiryCommentService.getCommentsForInquiry(savedInquiry.getInquiryId());

        // then
        assertThat(comments).isNotNull();
        assertThat(comments.size()).isEqualTo(2); // savedComment와 comment2만 조회되어야 함

        // 정렬 순서 (createdAt Asc) 및 내용 확인
        assertThat(comments.get(0).getCommentId()).isEqualTo(savedComment.getCommentId());
        assertThat(comments.get(0).getContent()).isEqualTo(savedComment.getContent());
        assertThat(comments.get(1).getCommentId()).isEqualTo(comment2.getCommentId());
        assertThat(comments.get(1).getContent()).isEqualTo(comment2.getContent());
    }

    @Test
    @DisplayName("답변이 없는 문의에 대한 목록 조회 시 빈 리스트 반환")
    void testGetCommentsForInquiry_noComments() {
        // given
        // 새로운 문의글 (답변 없음)
        Inquiry newInquiry = inquiryRepository.save(Inquiry.builder()
                .title("답변 없는 문의")
                .content("이 문의는 아직 답변이 없습니다.")
                .member(testMember)
                .build());

        // when
        List<InquiryCommentResponseDTO> comments = inquiryCommentService.getCommentsForInquiry(newInquiry.getInquiryId());

        // then
        assertThat(comments).isNotNull();
        assertThat(comments).isEmpty(); // 빈 리스트여야 함
    }

    @Test
    @DisplayName("존재하지 않는 문의에 대한 답변 목록 조회 실패")
    void testGetCommentsForInquiry_inquiryNotFound() {
        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inquiryCommentService.getCommentsForInquiry(999); // 없는 문의글 ID
        });
        assertThat(exception.getMessage()).contains("해당 문의를 찾을 수 없습니다.");
    }
}