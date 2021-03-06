/*
 * Copyright 2019 Confluent Inc.
 *
 * Licensed under the Confluent Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.confluent.ksql.util;

import io.confluent.ksql.engine.KsqlEngine;
import io.confluent.ksql.internal.QueryStateListener;
import io.confluent.ksql.query.QueryError;
import io.confluent.ksql.query.QueryErrorClassifier;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaStreamsUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(KsqlEngine.class);

  private final QueryErrorClassifier errorClassifier;
  private final Optional<QueryStateListener> stateListener;

  public KafkaStreamsUncaughtExceptionHandler(
      final QueryErrorClassifier errorClassifier,
      final Optional<QueryStateListener> errorStateListener
  ) {
    this.errorClassifier = Objects.requireNonNull(errorClassifier, "errorClassifier");
    this.stateListener = Objects.requireNonNull(errorStateListener, "errorStateListener");
  }

  public void uncaughtException(final Thread t, final Throwable e) {
    log.error("Unhandled exception caught in streams thread {}.", t.getName(), e);
    stateListener.ifPresent(lis -> lis.onError(new QueryError(e, errorClassifier.classify(e))));
  }
}
