/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package testing;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import lombok.SneakyThrows;

public class SingleInstancePostgresExtension implements AfterTestExecutionCallback, BeforeTestExecutionCallback,
    BeforeAllCallback, AfterAllCallback {

  private final EmbeddedPostgres epg;
  private final List<Consumer<EmbeddedPostgres.Builder>> builderCustomizers = new CopyOnWriteArrayList<>();

  @SneakyThrows
  public SingleInstancePostgresExtension() {
    this.customize(customizer -> {
      customizer.setPort(5531);
    });
    this.epg = this.pg();
    Runtime.getRuntime().addShutdownHook(new Thread(){
      @SneakyThrows
      public void run() {
        epg.close();
      }
    });
  }

  public static SingleInstancePostgresExtension singleton(){
    if(System.getProperty("postgres") == null){
      final SingleInstancePostgresExtension instance = new SingleInstancePostgresExtension();
      System.getProperties().put("postgres", instance);
    }
    return (SingleInstancePostgresExtension) System.getProperties().get("postgres");
  }

  @Override
  public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {

  }

  @Override
  public void afterTestExecution(ExtensionContext extensionContext) {

  }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception {

  }

  private EmbeddedPostgres pg() throws IOException {
    final EmbeddedPostgres.Builder builder = EmbeddedPostgres.builder();
    this.builderCustomizers.forEach(c -> c.accept(builder));
    return builder.start();
  }

  public SingleInstancePostgresExtension customize(Consumer<EmbeddedPostgres.Builder> customizer) {
    if (this.epg != null) {
      throw new AssertionError("already started");
    }
    this.builderCustomizers.add(customizer);
    return this;
  }

  public EmbeddedPostgres getEmbeddedPostgres() {
    EmbeddedPostgres epg = this.epg;
    if (epg == null) {
      throw new AssertionError("JUnit test not started yet!");
    }
    return epg;
  }
}
