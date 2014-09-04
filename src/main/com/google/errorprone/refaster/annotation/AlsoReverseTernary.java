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

package com.google.errorprone.refaster.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that any conditionals in the annotated {@code BeforeTemplate} should also match their
 * reversal.  For example, {@code return (list.size() == 0) ? null : list.get(0)} would also match
 * {@code (list.size() != 0) ? list.get(0) : null}.
 * 
 * @author lowasser@google.com (Louis Wasserman)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
@RequiredAnnotation(BeforeTemplate.class)
public @interface AlsoReverseTernary {}
