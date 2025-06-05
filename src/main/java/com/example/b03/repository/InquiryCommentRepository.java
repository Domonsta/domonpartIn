package com.example.b03.repository;

import com.example.b03.domain.Inquiry;
import com.example.b03.domain.InquiryComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface InquiryCommentRepository extends JpaRepository<InquiryComment, Integer>, QuerydslPredicateExecutor<InquiryComment> {

    // 특정 Inquiry 객체에 연결된 댓글 목록을 조회합니다. (isDeleted가 false이고 createdAt 오름차순)
    // InquiryCommentServiceImpl의 getCommentsForInquiry() 메서드에서 사용됩니다.
    List<InquiryComment> findByInquiryAndIsDeletedFalseOrderByCreatedAtAsc(Inquiry inquiry);

    // ⭐️⭐️⭐️ 추가된 메서드: 특정 Inquiry에 대한 삭제되지 않은 댓글의 개수를 세는 메서드 ⭐️⭐️⭐️
    // InquiryCommentServiceImpl의 getCommentCountByInquiryId() 메서드에서 사용됩니다.
    int countByInquiryAndIsDeletedFalse(Inquiry inquiry);


    // ⭐ 참고: InquiryId로 직접 조회하고 페이징을 원한다면 다음 메서드를 추가하는 것이 좋음 (현재는 사용하지 않음)
    // Page<InquiryComment> findByInquiry_InquiryIdAndIsDeletedFalseOrderByCreatedAtAsc(Integer inquiryId, Pageable pageable);
}