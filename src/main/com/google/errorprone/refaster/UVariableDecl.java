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
import com.google.common.base.Optional;

import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.TreeVisitor;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Name;

import javax.annotation.Nullable;

/**
 * A {@link UTree} representation of a local variable declaration.
 *
 * <p>A {@code UVariableDecl} can be unified with any variable declaration which has a matching 
 * type and initializer. Annotations and modifiers are preserved for the corresponding replacement,
 * as well as the variable name. {@link ULocalVarIdent} instances are used to represent references 
 * to local variables.
 *
 * <p>As a result, we can modify variable declarations and initializations in target code while
 * preserving variable names and other contextual information.
 *
 * @author lowasser@google.com (Louis Wasserman)
 */
@AutoValue
public abstract class UVariableDecl implements UStatement, VariableTree {

  public static UVariableDecl create(
      String identifier, UExpression type, @Nullable UExpression initializer) {
    return new AutoValue_UVariableDecl(identifier, type, initializer);
  }

  public static UVariableDecl create(String identifier, UExpression type) {
    return create(identifier, type, null);
  }
  
  abstract String identifier();
  
  @Override
  public abstract UExpression getType();
  
  @Override
  @Nullable
  public abstract UExpression getInitializer();
  
  ULocalVarIdent.Key key() {
    return new ULocalVarIdent.Key(identifier());
  }

  @Override
  @Nullable
  public Unifier unify(JCTree target, @Nullable Unifier unifier) {
    if (unifier != null && target instanceof JCVariableDecl
        && unifier.getBinding(key()) == null) {
      JCVariableDecl decl = (JCVariableDecl) target;
      unifier = getType().unify(decl.getType(), unifier);
      unifier = Unifier.unifyNullable(unifier, getInitializer(), decl.getInitializer());
      if (unifier != null) {
        unifier.putBinding(key(), LocalVarBinding.create(decl.sym, decl.getModifiers()));
        return unifier;
      }
    }
    return null;
  }

  @Override
  public JCVariableDecl inline(Inliner inliner) throws CouldNotResolveImportException {
    Optional<LocalVarBinding> binding = inliner.getOptionalBinding(key());
    JCModifiers modifiers;
    Name name;
    TreeMaker maker = inliner.maker();
    if (binding.isPresent()) {
      modifiers = binding.get().getModifiers();
      name = binding.get().getName();
    } else {
      modifiers = maker.Modifiers(0L);
      name = inliner.asName(identifier());
    }
    return maker.VarDef(modifiers, name, getType().inline(inliner),
        (getInitializer() == null) ? null : getInitializer().inline(inliner));
  }

  @Override
  public Kind getKind() {
    return Kind.VARIABLE;
  }

  @Override
  public <R, D> R accept(TreeVisitor<R, D> visitor, D data) {
    return visitor.visitVariable(this, data);
  }

  @Override
  public ModifiersTree getModifiers() {
    throw new UnsupportedOperationException();
  }

  @Override
  public javax.lang.model.element.Name getName() {
    return StringName.of(identifier());
  }
}