package com.mageddo.stock;

import java.math.BigDecimal;
import java.util.List;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Singleton;

import com.mageddo.transaction.Isolation;
import com.mageddo.transaction.Propagation;
import com.mageddo.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class StockPriceService {

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

  @Transactional
  public void createStockNested(List<Stock> stocks) {
    stocks.forEach(it -> {
      try {
        self().createStockNested(it);
      } catch (DuplicatedStockException e) {
        log.info("status=already-exists, stock={}", it.getSymbol());
      }
    });
  }

  @Transactional(propagation = Propagation.NESTED)
  public void createStockNested(Stock stock) {
    this.stockPriceDao.createStock(stock);
  }

  StockPriceService self() {
    return CDI.current()
        .select(StockPriceService.class)
        .get();
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void createSomethingThenCreateItemsOnNestedTransaction(List<Stock> stocks) {
    this.stockPriceDao.createStock(Stock
        .builder()
        .symbol(String.valueOf(System.currentTimeMillis()))
        .price(BigDecimal.TEN)
        .build());
    stocks.forEach(stock -> {
      try {
        self().createStockNestedRepeatableRead(stock);
      } catch (DuplicatedStockException e) {
        log.info("status=already-exists, stock={}", stock.getSymbol());
      }
    });
  }

  @Transactional(propagation = Propagation.NESTED, isolation = Isolation.REPEATABLE_READ)
  public void createStockNestedRepeatableRead(Stock stock) {
    this.stockPriceDao.createStock(stock);
  }
}
