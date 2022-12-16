package com.example.lecturediviend.scheduler;

import com.example.lecturediviend.model.Company;
import com.example.lecturediviend.model.ScrapedResult;
import com.example.lecturediviend.persist.CompanyRepository;
import com.example.lecturediviend.persist.DividendRepository;
import com.example.lecturediviend.persist.entity.CompanyEntity;
import com.example.lecturediviend.persist.entity.DividendEntity;
import com.example.lecturediviend.scraper.Scraper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class ScraperScheduler {

    private final CompanyRepository companyRepository;

    private final DividendRepository dividendRepository;

    private final Scraper financeScraper;

    @Scheduled(fixedDelay = 1000)
    public void test1() throws InterruptedException {
        Thread.sleep(5000);
        System.out.println(Thread.currentThread().getName() + " -> 테스트 1 : " + LocalDateTime.now());
    }

    @Scheduled(fixedDelay = 1000)
    public void test2() throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + " -> 테스트 2 : " + LocalDateTime.now());
    }


    // 일정 주기마다 수행
//    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void financeScheduling() {
//        log.info("scraping scheduler is started");
        // 저장된 회사 목록을 조회
        List<CompanyEntity> companyEntities = companyRepository.findAll();

        // 회사마다 배당금 정보를 새로 스크래핑
        companyEntities.forEach(companyEntity -> {
            log.info("scraping scheduler is started -> " + companyEntity.getName());
            ScrapedResult scrapedResult = financeScraper.scrap(Company.builder()
                    .name(companyEntity.getName())
                    .ticker(companyEntity.getTicker())
                    .build());

            scrapedResult.getDividends().stream()
                    .map(dividend -> new DividendEntity(companyEntity.getId(), dividend))
                    .forEach(dividendEntity -> {
                        boolean exists = dividendRepository.existsByCompanyIdAndDate(dividendEntity.getCompanyId(), dividendEntity.getDate());
                        if (!exists) {
                            dividendRepository.save(dividendEntity);
                        }
                    });

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 스크래핑한 배당금 정보 중 데이터베이스에 없는 값은 저장
    }
}
