/*
 * Copyright 2013 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.errorprone.refaster;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.TreeVisitor;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCForLoop;

import java.util.List;

import javax.annotation.Nullable;

/**
 * {@link UTree} representation of a {@link ForLoopTree}.
 * 
 * @author lowasser@google.com (Louis Wasserman)
 */
@AutoValue
public abstract class UForLoop implements UStatement, ForLoopTree {
  
  public static UForLoop create(
      Iterable<? extends UStatement> initializer, @Nullable UExpression condition,
      Iterable<? extends UExpressionStatement> update, UStatement statement) {
    return new AutoValue_UForLoop(
        ImmutableList.copyOf(initializer), condition, ImmutableList.copyOf(update), statement);
  }

  @Override
  public abstract List<UStatement> getInitializer();

  @Override
  @Nullable
  public abstract UExpression getCondition();

  @Override
  public abstract List<UExpressionStatement> getUpdate();

  @Override
  public abstract UStatement getStatement();

  @Override
  @Nullable
  public Unifier unify(JCTree target, Unifier unifier) {
    if (unifier != null && target instanceof JCForLoop) {
      JCForLoop loop = (JCForLoop) target;
      unifier = Unifier.unifyList(unifier, getInitializer(), loop.getInitializer());
      unifier = Unifier.unifyNullable(unifier, getCondition(), loop.getCondition());
      unifier = Unifier.unifyList(unifier, getUpdate(), loop.getUpdate());
      return getStatement().unify(loop.getStatement(), unifier);
    }
    return null;
  }

  @Override
  public <R, D> R accept(TreeVisitor<R, D> visitor, D data) {
    return visitor.visitForLoop(this, data);
  }

  @Override
  public Kind getKind() {
    return Kind.FOR_LOOP;
  }

  @Override
  public JCForLoop inline(Inliner inliner) throws CouldNotResolveImportException {
    return inliner.maker().ForLoop(
        inliner.inlineList(getInitializer()),
        (getCondition() == null) ? null : getCondition().inline(inliner), 
        com.sun.tools.javac.util.List.convert(
            JCExpressionStatement.class, inliner.inlineList(getUpdate())), 
        getStatement().inline(inliner));
  }
}