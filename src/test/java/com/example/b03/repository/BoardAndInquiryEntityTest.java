package com.example.b03.repository;

import com.example.b03.domain.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Commit
public class BoardAndInquiryEntityTest {

    @Autowired private MemberRepository memberRepository;
    @Autowired private MembershipTypeRepository membershipTypeRepository;
    @Autowired private InquiryRepository inquiryRepository;
    @Autowired private InquiryCommentRepository inquiryCommentRepository;

    private Member user;
    private Member admin;
    private Member user2;

    @BeforeEach
    void setUp() {
        MembershipType userType = membershipTypeRepository.save(
                MembershipType.builder()
                        .typeId((byte) 3) // 개인회원
                        .typeName("개인회원")
                        .build()
        );
        MembershipType user2Type = membershipTypeRepository.save(
                MembershipType.builder()
                        .typeId((byte) 2) // 개인회원
                        .typeName("기업회원")
                        .build()
        );

        MembershipType adminType = membershipTypeRepository.save(
                MembershipType.builder()
                        .typeId((byte) 1) // 관리자
                        .typeName("관리자")
                        .build()
        );

        user = Member.builder()
                .loginId("user" + UUID.randomUUID())
                .password("1234")
                .name("홍길동")
                .address("서울시 강남구")
                .phone("010-1234-5678")
                .birthDate(LocalDate.of(1990, 1, 1))
                .membershipType(userType)
                .build();

        admin = Member.builder()
                .loginId("admin" + UUID.randomUUID())
                .password("admin1234")
                .name("관리자")
                .address("서울시 중구")
                .phone("010-0000-0000")
                .birthDate(LocalDate.of(1985, 5, 10))
                .membershipType(adminType)
                .build();

        memberRepository.save(user);
        memberRepository.save(admin);
    }
    @Test
    @Order(1)
    void 문의글_작성_및_삭제() {
        Inquiry inquiry = inquiryRepository.save(
                Inquiry.builder()
                        .member(user)
                        .title("로그인 오류")
                        .content("비밀번호가 틀린 것 같아요.")
                        .build()
        );

        assertThat(inquiry.getIsDeleted()).isFalse();

        inquiry.setIsDeleted(true);
        inquiryRepository.save(inquiry);

        assertThat(inquiry.getIsDeleted()).isTrue();
    }

    @Test
    @Order(2)
    void 문의글에_관리자_답변_작성() {
        Inquiry inquiry = inquiryRepository.save(
                Inquiry.builder()
                        .member(user)
                        .title("기능 문의")
                        .content("지원서가 안 올라가요.")
                        .build()
        );

        InquiryComment comment = InquiryComment.builder()
                .inquiry(inquiry)
                .admin(admin)
                .content("파일 확장자를 확인해 주세요.")
                .build();

        inquiryCommentRepository.save(comment);

        assertThat(comment.getIsDeleted()).isFalse();
    }
}