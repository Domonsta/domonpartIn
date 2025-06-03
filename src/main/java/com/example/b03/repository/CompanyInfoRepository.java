// 3. CompanyInfo
package com.example.b03.repository;

import com.example.b03.domain.CompanyInfo;
import com.example.b03.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyInfoRepository extends JpaRepository<CompanyInfo, Integer> {
    Optional<CompanyInfo> findByMember_MemberNo(Integer memberNo);
}
