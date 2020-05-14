package com.mageddo.stock;

public class DuplicatedStockException extends RuntimeException {
  public DuplicatedStockException(Throwable e) {
    super(e);
  }
}
