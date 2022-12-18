package com.example.lecturediviend.service;

import com.example.lecturediviend.exception.impl.NoCompanyException;
import com.example.lecturediviend.model.Company;
import com.example.lecturediviend.model.Dividend;
import com.example.lecturediviend.model.ScrapedResult;
import com.example.lecturediviend.model.constant.CacheKey;
import com.example.lecturediviend.persist.CompanyRepository;
import com.example.lecturediviend.persist.DividendRepository;
import com.example.lecturediviend.persist.entity.CompanyEntity;
import com.example.lecturediviend.persist.entity.DividendEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    @Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)
    public ScrapedResult getDividendByCompanyName(String companyName) {
        log.info("search company -> " + companyName);
        // 회사명을 기준으로 회사 정보 조회
        CompanyEntity companyEntity = companyRepository.findByName(companyName)
                .orElseThrow(() -> new NoCompanyException());

        // 조회된 회사 ID로 배당금 정보 조회
        List<DividendEntity> dividendEntities = dividendRepository.findAllByCompanyId(companyEntity.getId());

        // 결과 조합 후 반환
        List<Dividend> dividends = dividendEntities.stream()
                .map(dividendEntity -> new Dividend(dividendEntity.getDate(), dividendEntity.getDividend()))
                .collect(Collectors.toList());

        return new ScrapedResult(new Company(companyEntity.getTicker(), companyEntity.getName()), dividends);
    }
}
