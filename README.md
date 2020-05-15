# javaee-jdbi
Transactional support for [JDBI][1] on Java EE applications

# Features
* Transactions
* Transaction Isolations (All of them)
    * READ_UNCOMMITTED
    * READ_COMMITTED
    * REPEATABLE_READ
    * SERIALIZABLE
* Transaction Propagation
    * REQUIRED
    * NEVER
    * NESTED
* Native Image support for JDBI [Fluent API][3] without additional configration
* Native Image support for JDBI [Declarative API][4] with some configration

# Examples
* [Quarkus + Transactional + JDBI + Native Image full working sample][5]

# Requirements
* Java 8+
* Java EE container
  * Tested on [Quarkus][2]

[1]: http://jdbi.org/
[2]: https://quarkus.io/
[3]: http://jdbi.org/#_fluent_api
[4]: http://jdbi.org/#_declarative_api
[5]: https://github.com/mageddo/java-examples/blob/quarkus-gradle-full-example-graalvm-19.3.1/quarkus/quarkus-gradle-full-example/src/main/java/com/mageddo/service/StockPriceService.java#L48
