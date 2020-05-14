package javax.transactionv2;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.sql.DataSource;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.transaction.UnableToManipulateTransactionIsolationLevelException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Interceptor
@Transactional
public class TransactionalInterceptor {

  private final Jdbi jdbi;

  public TransactionalInterceptor() {
    final Instance<Jdbi> jdbi = CDI
        .current()
        .select(Jdbi.class);
    final Instance<DataSource> datasource = CDI
        .current()
        .select(DataSource.class);

    if(jdbi.isResolvable()){
      this.jdbi = jdbi.get();
    } else if(datasource.isResolvable()){
      this.jdbi = Jdbi.create(datasource.get());
    } else {
      this.jdbi = null;
    }
  }

  @AroundInvoke
  public Object intercept(InvocationContext ctx) throws Exception {
    if(this.jdbi == null){
      if(log.isDebugEnabled()){
        log.debug("status=no-jdbi-instance");
      }
    }
    final Transactional transactionDef = ctx
        .getMethod()
        .getAnnotation(Transactional.class);
    if (transactionDef.propagation() == Propagation.NESTED) {
      return jdbi.inTransaction(handle -> {
        final String savePoint = String.format("savepoint-%d", System.nanoTime());
        handle.savepoint(savePoint);
        try {
          this.tryChangeTransactionIsolation(transactionDef, handle);
          return ctx.proceed();
        } catch (Exception e) {
          handle.rollbackToSavepoint(savePoint);
          throw e;
        }
      });
    } else if (transactionDef.propagation() == Propagation.NEVER) {
      jdbi.useHandle(handle -> {
        if (handle.isInTransaction()) {
          throw new IllegalStateException("Can't be in a transaction when using propagation: " + Propagation.NEVER);
        }
      });
    }
    return jdbi.inTransaction(handle -> {
      this.tryChangeTransactionIsolation(transactionDef, handle);
      return ctx.proceed();
    });
  }

  private void tryChangeTransactionIsolation(Transactional transactionDef, Handle handle) {
    if (transactionDef.isolation()
        .value() != TransactionDefinition.ISOLATION_DEFAULT) {
      try {
        handle.setTransactionIsolation(transactionDef.isolation()
            .value());
      } catch (UnableToManipulateTransactionIsolationLevelException e) {
      }
    }
  }
}
