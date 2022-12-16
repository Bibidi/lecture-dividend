package com.example.lecturediviend.scheduler;

import com.example.lecturediviend.model.Company;
import com.example.lecturediviend.model.ScrapedResult;
import com.example.lecturediviend.model.constant.CacheKey;
import com.example.lecturediviend.persist.CompanyRepository;
import com.example.lecturediviend.persist.DividendRepository;
import com.example.lecturediviend.persist.entity.CompanyEntity;
import com.example.lecturediviend.persist.entity.DividendEntity;
import com.example.lecturediviend.scraper.Scraper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
@EnableCaching
public class ScraperScheduler {

    private final CompanyRepository companyRepository;

    private final DividendRepository dividendRepository;

    private final Scraper financeScraper;

    @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true)
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void financeScheduling() {
        log.info("scraping scheduler is started");
        // 저장된 회사 목록을 조회
        List<CompanyEntity> companyEntities = companyRepository.findAll();

        // 회사마다 배당금 정보를 새로 스크래핑
        companyEntities.forEach(companyEntity -> {
            log.info("scraping scheduler is started -> " + companyEntity.getName());
            ScrapedResult scrapedResult = financeScraper.scrap(new Company(companyEntity.getName(), companyEntity.getTicker()));

            // 스크래핑한 배당금 정보 중 데이터베이스에 없는 값은 저장
            scrapedResult.getDividends().stream()
                    .map(dividend -> new DividendEntity(companyEntity.getId(), dividend))
                    .forEach(dividendEntity -> {
                        boolean exists = dividendRepository.existsByCompanyIdAndDate(dividendEntity.getCompanyId(), dividendEntity.getDate());
                        if (!exists) {
                            dividendRepository.save(dividendEntity);
                        }
                    });

            // 연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시정지
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
}
