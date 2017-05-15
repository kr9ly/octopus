package net.kr9ly.octopus;

import com.squareup.javapoet.*;
import net.kr9ly.octopus.internal.Callbacks;
import net.kr9ly.octopus.internal.CallbacksFinder;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

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
public class CallbacksFinderBuilder {

    private final PackageElement packageElement;

    private final TypeSpec.Builder classBuilder;

    private final MethodSpec.Builder findMethodBuilder;

    private final ProcessingEnvironment processingEnv;

    public CallbacksFinderBuilder(ProcessingEnvironment processingEnv, PackageElement packageElement) {
        this.processingEnv = processingEnv;
        this.packageElement = packageElement;

        classBuilder = TypeSpec.classBuilder("CallbacksFinderImpl")
                .addAnnotation(SpecHelper.getGeneratedAnnotation())
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S", "unchecked").build())
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(CallbacksFinder.class);

        findMethodBuilder = MethodSpec.methodBuilder("findCallbacks")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(TypeVariableName.get("T"))
                .addParameter(ParameterSpec.builder(TypeVariableName.get("T"), "target").build())
                .returns(ParameterizedTypeName.get(ClassName.get(Callbacks.class), TypeVariableName.get("T")));
    }

    public void addCallbacks(TypeElement callbackClass) {
        findMethodBuilder.addCode(
                CodeBlock.builder()
                        .beginControlFlow("if (target instanceof $T)", ClassName.get(callbackClass))
                        .addStatement("return ($T) new $T_Callbacks()", ParameterizedTypeName.get(ClassName.get(Callbacks.class), TypeVariableName.get("T")), ClassName.get(callbackClass))
                        .endControlFlow()
                        .build()
        );
    }

    public TypeSpec build() {
        return classBuilder.addMethod(
                findMethodBuilder.addStatement("return null").build()
        ).build();
    }

    public PackageElement getPackageElement() {
        return packageElement;
    }
}
