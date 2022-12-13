package com.example.lecturediviend.service;

import com.example.lecturediviend.model.Company;
import com.example.lecturediviend.model.Dividend;
import com.example.lecturediviend.model.ScrapedResult;
import com.example.lecturediviend.persist.CompanyRepository;
import com.example.lecturediviend.persist.DividendRepository;
import com.example.lecturediviend.persist.entity.CompanyEntity;
import com.example.lecturediviend.persist.entity.DividendEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public ScrapedResult getDividendByCompanyName(String companyName) {
        // 회사명을 기준으로 회사 정보 조회
        CompanyEntity companyEntity = companyRepository.findByName(companyName)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회사명입니다"));

        // 조회된 회사 ID로 배당금 정보 조회
        List<DividendEntity> dividendEntities = dividendRepository.findAllByCompanyId(companyEntity.getId());

        // 결과 조합 후 반환
        List<Dividend> dividends = dividendEntities.stream()
                .map(dividendEntity -> Dividend.builder()
                        .date(dividendEntity.getDate())
                        .dividend(dividendEntity.getDividend())
                        .build())
                .collect(Collectors.toList());

        return new ScrapedResult(Company.builder()
                .ticker(companyEntity.getTicker())
                .name(companyEntity.getName())
                .build(),
                dividends);
    }
}
