package javax.transactionv2;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.transaction.UnableToManipulateTransactionIsolationLevelException;

import lombok.RequiredArgsConstructor;

@Interceptor
@javax.transactionv2.Transactional
@RequiredArgsConstructor
public class TransactionalInterceptor {

  private final Jdbi jdbi;

  @AroundInvoke
  public Object intercept(InvocationContext ctx) throws Exception {
    final javax.transactionv2.Transactional transactionDef = ctx
        .getMethod()
        .getAnnotation(javax.transactionv2.Transactional.class);
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
