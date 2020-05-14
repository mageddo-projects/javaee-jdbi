# javaee-jdbi
Transactional support for [JDBI][1] on Java EE applications

# Features
* Transactions
* Transaction Isolations
    * READ_UNCOMMITTED
    * READ_COMMITTED
    * REPEATABLE_READ
    * SERIALIZABLE
* Transaction Propagation
    * REQUIRED
    * NEVER
    * NESTED

# Requirements
* Java 8+
* Java EE container
  * Tested on [Quarkus][2]

[1]: http://jdbi.org/
[2]: https://quarkus.io/
