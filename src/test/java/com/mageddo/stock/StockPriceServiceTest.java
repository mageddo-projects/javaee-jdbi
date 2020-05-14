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
class StockPriceServiceTest {

//  @RegisterExtension
//  public static final SingleInstancePostgresExtension postgres = SingleInstancePostgresExtension.singleton();

  @Inject
  StockPriceService stockPriceService;

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
      this.stockPriceService.createStock(stocks);
    });

    // assert
    assertTrue(this.stockPriceService.find()
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
    assertThrows(DuplicatedStockException.class, () -> {
      this.stockPriceService.createStockNested(stocks);
    });

    // assert
    assertEquals(1, this.stockPriceService.find()
        .size());

  }
}
