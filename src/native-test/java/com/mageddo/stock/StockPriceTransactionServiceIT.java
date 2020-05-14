package com.mageddo.stock;

import org.junit.jupiter.api.extension.ExtendWith;

import io.quarkus.test.junit.NativeImageTest;
import testing.SingleInstancePostgresExtension;

@ExtendWith(SingleInstancePostgresExtension.class)
@NativeImageTest
class StockPriceTransactionServiceIT extends StockPriceTransactionServiceTest {

}
