package net.kr9ly.octopus;

import com.squareup.javapoet.AnnotationSpec;

import javax.annotation.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

/**
 * Copyright 2017 kr9ly
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class SpecHelper {

    public static AnnotationSpec getGeneratedAnnotation() {
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", "net.kr9ly.octopus.OctopusProcessor")
                .build();
    }

    public static String getTypeName(ProcessingEnvironment processingEnv, TypeMirror typeMirror) {
        return processingEnv.getTypeUtils().asElement(typeMirror).getSimpleName().toString();
    }

    public static String getPackageName(ProcessingEnvironment processingEnv, Element element) {
        return processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
    }
}