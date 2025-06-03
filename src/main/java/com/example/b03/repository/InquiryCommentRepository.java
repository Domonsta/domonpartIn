// 12. InquiryComment
package com.example.b03.repository;

import com.example.b03.domain.Inquiry;
import com.example.b03.domain.InquiryComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryCommentRepository extends JpaRepository<InquiryComment, Integer> {
    List<InquiryComment> findByInquiry(Inquiry inquiry);

    List<InquiryComment> findAllByInquiry_InquiryIdAndIsDeletedFalseOrderByCreatedAtAsc(Integer inquiryId);
    // 새롭게 추가: 특정 문의에 대한 삭제되지 않은 답변을 생성일자 기준 오름차순으로 가져옴
}