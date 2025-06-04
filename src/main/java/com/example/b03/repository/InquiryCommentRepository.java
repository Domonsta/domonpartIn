package com.example.b03.repository;

import com.example.b03.domain.Inquiry;
import com.example.b03.domain.InquiryComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor; // ⭐ QuerydslPredicateExecutor 임포트! ⭐

import java.util.List;

// ⭐️ JpaRepository와 QuerydslPredicateExecutor를 모두 상속받습니다.
public interface InquiryCommentRepository extends JpaRepository<InquiryComment, Integer>, QuerydslPredicateExecutor<InquiryComment> {

    // 💡 참고: findByInquiry()는 Inquiry 엔티티 객체 자체로 검색하는 메서드
    // List<InquiryComment> findByInquiry(Inquiry inquiry); // 이 메서드는 아래 findAllByInquiry_InquiryId... 메서드와 목적이 겹칠 수 있으니 주석 처리하거나 필요에 따라 선택

    // ⭐⭐ 이전에 에러가 났던 메서드 선언! (삭제되지 않은 답변만 가져오기 위함) ⭐⭐
    // Inquiry 객체로 검색하는 메서드. (InquiryCommentServiceImpl에서 사용)
    List<InquiryComment> findByInquiryAndIsDeletedFalseOrderByCreatedAtAsc(Inquiry inquiry);

    // 💡 참고: findByInquiry_InquiryId...는 InquiryId 값으로 검색하는 메서드
    // List<InquiryComment> findAllByInquiry_InquiryIdAndIsDeletedFalseOrderByCreatedAtAsc(Integer inquiryId); // 이 메서드는 findByInquiryAndIsDeletedFalseOrderByCreatedAtAsc와 역할이 유사하므로 필요에 따라 선택
}