package com.mageddo.stock;

import java.util.List;

public interface StockPriceDao {

  void updateStockPrice(Stock stock);

  Stock getStock(String symbol);

  void createStock(Stock stock);

  List<Stock> find();
}
