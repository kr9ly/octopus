package net.kr9ly.octopus;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import net.kr9ly.octopus.internal.Callbacks;
import net.kr9ly.octopus.internal.Caller;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.*;

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
@AutoService(Processor.class)
@SupportedAnnotationTypes({"net.kr9ly.octopus.Callback"})
public class OctopusProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<PackageElement, CallbacksFinderBuilder> finderBuilders = new HashMap<PackageElement, CallbacksFinderBuilder>();
        Set<Object> processedCallbacks = new HashSet<Object>();

        Set<? extends Element> methods = roundEnv.getElementsAnnotatedWith(Callback.class);
        for (Element method : methods) {
            ExecutableElement executable = (ExecutableElement) method;
            TypeElement callbackClass = (TypeElement) executable.getEnclosingElement();
            if (processedCallbacks.contains(callbackClass)) {
                continue;
            }

            PackageElement packageElement = (PackageElement) callbackClass.getEnclosingElement();
            CallbacksFinderBuilder builder = finderBuilders.get(packageElement);
            if (builder == null) {
                builder = new CallbacksFinderBuilder(processingEnv, packageElement);
                finderBuilders.put(packageElement, builder);
            }

            builder.addCallbacks(callbackClass);

            String className = SpecHelper.getTypeName(processingEnv, callbackClass.asType()) + "_Callbacks";

            TypeSpec.Builder callbacksClassBuilder = TypeSpec.classBuilder(className)
                    .addAnnotation(SpecHelper.getGeneratedAnnotation())
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(ParameterizedTypeName.get(ClassName.get(Callbacks.class), TypeName.get(callbackClass.asType())));

            ParameterizedTypeName callerListType = ParameterizedTypeName.get(ClassName.get(List.class),
                    ParameterizedTypeName.get(ClassName.get(Caller.class), WildcardTypeName.subtypeOf(Object.class)));

            ParameterizedTypeName callerArrayListType = ParameterizedTypeName.get(ClassName.get(ArrayList.class),
                    ParameterizedTypeName.get(ClassName.get(Caller.class), WildcardTypeName.subtypeOf(Object.class)));

            MethodSpec.Builder callersMethodBuilder = MethodSpec.methodBuilder("callers")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(callerListType)
                    .addParameter(TypeVariableName.get(callbackClass.asType()), "target", Modifier.FINAL)
                    .addStatement("$T callers = new $T()", callerListType, callerArrayListType);

            for (Element element : callbackClass.getEnclosedElements()) {
                if (element.getAnnotation(Callback.class) == null) {
                    continue;
                }

                ExecutableElement callbackMethod = (ExecutableElement) element;
                List<? extends VariableElement> params = callbackMethod.getParameters();
                if (params.size() != 1) {
                    continue;
                }
                VariableElement eventParam = params.get(0);
                TypeMirror eventType = eventParam.asType();

                ParameterizedTypeName eventTypeClass = ParameterizedTypeName.get(ClassName.get(Class.class), ClassName.get(eventType));
                ParameterizedTypeName callerType = ParameterizedTypeName.get(ClassName.get(Caller.class), ClassName.get(eventType));

                MethodSpec.Builder callMethodBuilder = MethodSpec.methodBuilder("call")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addException(ClassName.get(Throwable.class))
                        .addParameter(ClassName.get(eventType), "event")
                        .returns(boolean.class);

                if (TypeName.get(callbackMethod.getReturnType()).equals(TypeName.BOOLEAN)) {
                    callMethodBuilder.addStatement("return target.$N($N)", callbackMethod.getSimpleName(), "event");
                } else {
                    callMethodBuilder.addStatement("target.$N($N)", callbackMethod.getSimpleName(), "event")
                            .addStatement("return false");
                }

                callersMethodBuilder
                        .addStatement("callers.add($L)",
                                TypeSpec.anonymousClassBuilder("")
                                        .addSuperinterface(callerType)
                                        .addMethod(callMethodBuilder.build())
                                        .addMethod(MethodSpec.methodBuilder("eventClass")
                                                .addAnnotation(Override.class)
                                                .addModifiers(Modifier.PUBLIC)
                                                .returns(eventTypeClass)
                                                .addStatement("return $T.class", ClassName.get(eventType))
                                                .build())
                                        .build());
            }

            TypeSpec callbacksClass = callbacksClassBuilder
                    .addMethod(callersMethodBuilder.addStatement("return callers").build())
                    .build();

            JavaFile javaFile = JavaFile.builder(SpecHelper.getPackageName(processingEnv, callbackClass), callbacksClass).build();
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                //
            }

            processedCallbacks.add(callbackClass);
        }

        for (CallbacksFinderBuilder callbacksFinderBuilder : finderBuilders.values()) {
            TypeSpec finderSpec = callbacksFinderBuilder.build();

            JavaFile javaFile = JavaFile.builder(SpecHelper.getPackageName(processingEnv, callbacksFinderBuilder.getPackageElement()), finderSpec).build();
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                //
            }
        }

        finderBuilders.clear();
        processedCallbacks.clear();

        return false;
    }
}
