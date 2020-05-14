package com.mageddo.stock;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.quarkus.test.junit.QuarkusTest;
import testing.DatabaseConfigurator;
import testing.SingleInstancePostgresExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.wildfly.common.Assert.assertTrue;

@ExtendWith(SingleInstancePostgresExtension.class)
@QuarkusTest
class StockPriceTransactionServiceTest {

  @Inject
  StockPriceTransactionService stockPriceTransactionService;

  @Inject
  DatabaseConfigurator databaseConfigurator;

  @BeforeEach
  void before() {
    this.databaseConfigurator.truncateTables();
  }

  /**
   * Must rollback because it is trying to create PAGS symbol twice
   */
  @Test
  void mustGetErrorOnInsertAndRollbackAllRecords() {

    // arrange
    final List<Stock> stocks = Arrays.asList(
        Stock
            .builder()
            .price(BigDecimal.TEN)
            .symbol("PAGS")
            .build(),
        Stock
            .builder()
            .price(BigDecimal.ONE)
            .symbol("PAGS")
            .build()
    );

    // act
    assertThrows(DuplicatedStockException.class, () -> {
      this.stockPriceTransactionService.createStock(stocks);
    });

    // assert
    assertTrue(this.stockPriceTransactionService.find()
        .isEmpty());

  }

  @Test
  void mustSaveAllRecords() {

    // arrange
    final List<Stock> stocks = Arrays.asList(
        Stock
            .builder()
            .price(BigDecimal.TEN)
            .symbol("PAGS")
            .build(),
        Stock
            .builder()
            .price(BigDecimal.ONE)
            .symbol("GOOGL")
            .build()
    );

    // act
    this.stockPriceTransactionService.createStock(stocks);

    // assert
    assertEquals(2, this.stockPriceTransactionService.find().size());

  }

}
