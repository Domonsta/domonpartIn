package com.example.b03.service;

import com.example.b03.dto.CompanyInfoDTO;
import com.example.b03.dto.PageRequestDTO;
import com.example.b03.dto.PageResponseDTO;

public interface CompanyInfoService {
    CompanyInfoDTO register(CompanyInfoDTO dto);
    CompanyInfoDTO getByMemberNo(Integer memberNo);
    CompanyInfoDTO update(CompanyInfoDTO dto);
    void delete(Integer memberNo);

    PageResponseDTO<CompanyInfoDTO> getList(PageRequestDTO requestDTO);
}

