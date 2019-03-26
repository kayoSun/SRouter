package com.kayo.srouter.processor;

import com.google.auto.service.AutoService;
import com.kayo.srouter.annos.RouterRule;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class RouterProcess extends BaseProcess {

    @Override
    protected Set<String> getAnnotationTypes() {
        Set<String> annotations = new HashSet<>();
        annotations.add(RouterRule.class.getCanonicalName());
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        String  apiPackageName = "com.kayo.srouter.api";
        utils.getMessager().printMessage(Diagnostic.Kind.OTHER, "RouterProcess 抓取到 apt信息");
        //获取RouterRule注解信息
        Set<? extends Element> routerRules = roundEnvironment.getElementsAnnotatedWith(RouterRule.class);
        if (routerRules != null) {
            //获取RouterConfig注解信息
            List<String> appPacks = new ArrayList<>();
            List<String> appPaths = new ArrayList<>();
            matchConfigs(roundEnvironment, appPacks, appPaths);

            Map<String, String> activityRouters = new HashMap<>();
            Map<String, String> actionRouters = new HashMap<>();
            for (Element routerRule : routerRules) {
                TypeElement rule = (TypeElement) routerRule;
                if (utils.checkSuperClass(rule, "android.app.Activity")) {
                    //activity
                    RouterRule annotation = rule.getAnnotation(RouterRule.class);
                    String[] value = annotation.value();
                    for (String s : value) {
                        if (appPaths.isEmpty()){
                            activityRouters.put(s, rule.getQualifiedName().toString());
                        }else {
                            for (String appPath : appPaths) {
                                activityRouters.put(appPath + s, rule.getQualifiedName().toString());
                            }
                        }
                    }
                } else if (utils.checkSuperClass(rule, apiPackageName+".ActionSupport")) {
                    //action
                    RouterRule annotation = rule.getAnnotation(RouterRule.class);
                    String[] value = annotation.value();
                    for (String s : value) {
                        if (appPaths.isEmpty()) {
                            actionRouters.put(s, rule.getQualifiedName().toString());
                        }else {
                            for (String appPath : appPaths) {
                                actionRouters.put(appPath + s, rule.getQualifiedName().toString());
                            }
                        }
                    }
                }
            }


            //生成方法
            ClassName routerCreator = ClassName.bestGuess(apiPackageName+".RouterRuleCreator");
            ClassName actionSupport = ClassName.bestGuess(apiPackageName+".ActionSupport");
            ClassName router = ClassName.bestGuess(apiPackageName+".Router");
            ClassName activity = ClassName.bestGuess("android.app.Activity");
            //生成构造
            MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("init()");
            //生成createActivityRules方法
            MethodSpec.Builder setActivityRouter =
                    MethodSpec.overriding(utils.getOverrideMethod(routerCreator, "createActivityRules"))
                            .addModifiers(Modifier.PUBLIC)
                            .addStatement("$T<$T,$T<? extends $T>> routers = new $T<>()",
                                    Map.class, String.class, Class.class, activity, HashMap.class);
            Set<Map.Entry<String, String>> entries = activityRouters.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                String key = entry.getKey();
                String value = entry.getValue();
                ClassName className = ClassName.get("", value);
                setActivityRouter.addStatement("routers.put($S,$T.class)", key, className);
            }
            setActivityRouter.addStatement("return routers");
            //生成createActionRules方法
            MethodSpec.Builder setActionRouter =
                    MethodSpec.overriding(utils.getOverrideMethod(routerCreator, "createActionRules"))
                            .addModifiers(Modifier.PUBLIC)
                            .addStatement("$T<$T,$T<? extends $T>> routers = new $T<>()",
                                    Map.class, String.class, Class.class, actionSupport, HashMap.class);
            Set<Map.Entry<String, String>> actionEntries = actionRouters.entrySet();
            for (Map.Entry<String, String> entry : actionEntries) {
                String key = entry.getKey();
                String value = entry.getValue();
                ClassName className = ClassName.get("", value);
                setActionRouter.addStatement("routers.put($S,$T.class)", key, className);
            }
            setActionRouter.addStatement("return routers");

            //生成初始化方法
            MethodSpec.Builder initRouter = MethodSpec.methodBuilder("init");
            initRouter.addModifiers(Modifier.PRIVATE).returns(void.class);
            initRouter.addStatement("$T.with(null).addRuleCreator(this)", router);

            //生成类
            TypeSpec typeSpec = TypeSpec.classBuilder("RuleCreatorImpl")
                    .addSuperinterface(routerCreator)
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(constructor.build())
                    .addMethod(initRouter.build())
                    .addMethod(setActivityRouter.build())
                    .addMethod(setActionRouter.build())
                    .build();
            //生成java文件
            try {
                for (String appPath : appPacks) {
                    JavaFile build = JavaFile.builder(appPath, typeSpec).build();
                    build.writeTo(utils.getFiler());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }
}
