package com.mageddo.stock;

import java.util.List;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Singleton;
import javax.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class StockPriceTransactionService {

  private final StockPriceDao stockPriceDao;

  @Transactional
  public void createStock(List<Stock> stocks) {
    stocks.forEach(this.stockPriceDao::createStock);
  }

  @Transactional
  public void updateStockPrice(Stock stock) {
    this.stockPriceDao.updateStockPrice(stock);
  }

  public List<Stock> find() {
    return this.stockPriceDao.find();
  }

  StockPriceTransactionService self() {
    return CDI.current()
        .select(StockPriceTransactionService.class)
        .get();
  }
}
