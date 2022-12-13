package com.example.lecturediviend.service;

import com.example.lecturediviend.model.Company;
import com.example.lecturediviend.model.ScrapedResult;
import com.example.lecturediviend.persist.CompanyRepository;
import com.example.lecturediviend.persist.DividendRepository;
import com.example.lecturediviend.persist.entity.CompanyEntity;
import com.example.lecturediviend.persist.entity.DividendEntity;
import com.example.lecturediviend.scraper.Scraper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final Scraper scraper;

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public Company save(String ticker) {
        boolean exists = companyRepository.existsByTicker(ticker);
        if (exists) {
            throw new RuntimeException("Already exists ticker -> " + ticker);
        }
        return storeCompanyAndDividend(ticker);
    }

    private Company storeCompanyAndDividend(String ticker) {
        // ticker를 기준으로 회사를 스크래핑
        Company company = scraper.scrapCompanyByTicker(ticker);
        if (ObjectUtils.isEmpty(company)) {
            throw new RuntimeException("failed to scrap ticker -> " + ticker);
        }

        // 해당 회사가 존재할 경우, 회사의 배당금 정보를 스크래핑
        ScrapedResult scrapedResult = scraper.scrap(company);

        // 스크래핑 결과
        CompanyEntity companyEntity = companyRepository.save(new CompanyEntity(company));
        List<DividendEntity> dividendEntities = scrapedResult.getDividends().stream()
                .map(dividend -> new DividendEntity(companyEntity.getId(), dividend))
                .collect(Collectors.toList());
        dividendRepository.saveAll(dividendEntities);

        return company;
    }
}
