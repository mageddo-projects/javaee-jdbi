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
class StockPriceTransactionV2ServiceTest {

  @Inject
  StockPriceTransactionV2Service stockPriceTransactionV2Service;

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
      this.stockPriceTransactionV2Service.createStock(stocks);
    });

    // assert
    assertTrue(this.stockPriceTransactionV2Service.find()
        .isEmpty());

  }

  @Test
  void mustGetErrorOnInsertAndRollbackJustTheSecondWhenUsingNestedPropagation() {

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
    this.stockPriceTransactionV2Service.createStockNested(stocks);

    // assert
    assertEquals(1, this.stockPriceTransactionV2Service.find()
        .size());

  }

  @Test
  void cantChangeIsolationLevelInAAlreadyInProgressTransaction() {
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
    this.stockPriceTransactionV2Service.createSomethingThenCreateItemsOnNestedTransaction(stocks);

    // assert
    assertEquals(2, this.stockPriceTransactionV2Service.find().size());
  }
}
