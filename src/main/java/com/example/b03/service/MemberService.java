package com.example.b03.service;

import com.example.b03.dto.MemberDTO;

import java.util.Optional;

public interface MemberService {
    MemberDTO register(MemberDTO memberDTO);
    Optional<MemberDTO> findByLoginId(String loginId);
    Optional<MemberDTO> getByMemberNo(Integer memberNo);
    void delete(Integer memberNo);

}
