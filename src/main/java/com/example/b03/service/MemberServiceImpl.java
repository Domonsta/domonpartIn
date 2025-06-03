package com.example.b03.service;

import com.example.b03.domain.Member;
import com.example.b03.domain.MembershipType;
import com.example.b03.dto.MemberDTO;
import com.example.b03.repository.MemberRepository;
import com.example.b03.repository.MembershipTypeRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MembershipTypeRepository membershipTypeRepository;
    private final ModelMapper modelMapper;

    @Override
    public MemberDTO register(MemberDTO memberDTO) {
        MembershipType membershipType = membershipTypeRepository.findById(memberDTO.getMembershipTypeId())
                .orElseThrow( () ->new IllegalArgumentException("Invalid Membership Type"));

        Member member = modelMapper.map(memberDTO, Member.class);
        member.setMembershipType(membershipType);

        Member saved = memberRepository.save(member);
        return modelMapper.map(saved, MemberDTO.class);
    }

    @Override
    public Optional<MemberDTO> findByLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId)
                .map(member -> modelMapper.map(member, MemberDTO.class));
    }

    @Override
    public Optional<MemberDTO> getByMemberNo(Integer memberNo) {
        return memberRepository.findById(memberNo)
                .map(member -> modelMapper.map(member, MemberDTO.class));
    }

    @Override
    public void delete(Integer memberNo) {
        memberRepository.deleteById(memberNo);
    }
}
