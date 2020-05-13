package com.mageddo.transaction;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jdbi.v3.core.Jdbi;

import lombok.RequiredArgsConstructor;

@Interceptor
@Transactional
@RequiredArgsConstructor
public class TransactionalInterceptor {

  private final Jdbi jdbi;

  @AroundInvoke
  public Object intercept(InvocationContext ctx) throws Exception {
    final Transactional transactionDef = ctx.getMethod().getAnnotation(Transactional.class);
    if(transactionDef.propagation()  == Propagation.NESTED){
      return jdbi.inTransaction(handle -> {
        final String savePoint = String.format("savepoint-%d", System.nanoTime());
        handle.savepoint(savePoint);
        try {
          return ctx.proceed();
        } catch (Exception e){
          handle.rollbackToSavepoint(savePoint);
          throw e;
        } finally {
          handle.commit();
        }
      });
    }
    return jdbi.inTransaction(handle -> {
      return ctx.proceed();
    });
  }
}
