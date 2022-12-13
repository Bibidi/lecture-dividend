package com.example.lecturediviend.scraper;

import com.example.lecturediviend.model.Company;
import com.example.lecturediviend.model.ScrapedResult;

public interface Scraper {
    Company scrapCompanyByTicker(String ticker);
    ScrapedResult scrap(Company company);
}
