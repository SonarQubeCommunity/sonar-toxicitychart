/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.toxicity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.issue.Issue;
import org.sonar.plugins.toxicity.debts.cost.DebtProcessor;
import org.sonar.plugins.toxicity.debts.cost.DebtProcessorFactory;
import org.sonar.plugins.toxicity.model.Debt;
import org.sonar.plugins.toxicity.model.Source;
import org.sonar.plugins.toxicity.model.Toxicity;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible to filter violations and to store the founds debts.
 * The Singleton pattern was used in order to support multi-module projects.
 *
 * @author ccoca
 *
 */
final class DebtsFilter {

  /**
   * Eager initialization.
   */
  private static final DebtsFilter INSTANCE = new DebtsFilter();

  private static final Logger LOGGER = LoggerFactory
    .getLogger(DebtsFilter.class);

  private final Map<String, Source> sources;
  private DebtProcessorFactory issuesMapper;

  private DebtsFilter() {
    super();
    this.issuesMapper = new DebtProcessorFactory();
    sources = new HashMap<String, Source>();
  }

  static DebtsFilter getInstance() {
    return INSTANCE;
  }

  void filter(Issue issue) {
    DebtProcessor debtProcessor = issuesMapper.getDebtProcessor(issue);
    if (debtProcessor != null) {

      Debt debt = new Debt(debtProcessor.getType());
      debt.addCost(debtProcessor.getCostProcessor().getCost(issue));

      Source source = getSource(issue);
      source.addDebt(debt);

      LOGGER.debug("Match found. Debt type is: {} - for: {}.",
        debtProcessor.getKey(), source.getName());
    }
  }

  private Source getSource(Issue issue) {

    String name = issue.componentKey();
    Source source = sources.get(name);
    if (source == null) {
      source = new Source(name);
      sources.put(name, source);
    }

    return source;
  }

  Toxicity getToxicity() {

    Toxicity toxicity = new Toxicity();
    toxicity.setSources(sources.values());

    return toxicity;
  }

  void cleanup() {
    sources.clear();
  }

}
