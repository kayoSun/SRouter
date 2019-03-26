package com.kayo.srouter.processor;

import com.kayo.srouter.annos.RouterConfig;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

public abstract class BaseProcess extends AbstractProcessor {

    protected Utils utils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        utils = Utils.from(processingEnvironment);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new HashSet<>();
//        annotations.add(RouterConfig.class.getCanonicalName());
        Set<String> annotationTypes = getAnnotationTypes();
        if (annotationTypes != null) {
            annotations.addAll(getAnnotationTypes());
        }
        return annotations;
    }

    protected abstract Set<String> getAnnotationTypes();

    @Override
    public Set<String> getSupportedOptions() {
        return super.getSupportedOptions();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    protected void matchConfigs(RoundEnvironment roundEnvironment,
                                List<String> appPacks, List<String> appPaths) {
        Set<? extends Element> routerConfigs = roundEnvironment.getElementsAnnotatedWith(RouterConfig.class);
        if (routerConfigs != null) {
            for (Element routerConfig : routerConfigs) {
                TypeElement config = (TypeElement) routerConfig;
                RouterConfig annotation = config.getAnnotation(RouterConfig.class);
                String[] pack = annotation.pack();
                if (pack.length == 0){
                    //获取当前类的包路径
                    Name qualifiedName = config.getQualifiedName();
                    String className = qualifiedName.toString();
                    String packName = "";
                    if (className.contains(".")){
                        packName = className.substring(0,className.lastIndexOf("."));
                    }
                    pack = new String[]{packName};
                }
                appPacks.addAll(Arrays.asList(pack));
                String[] scheme = annotation.scheme();
//                if (scheme.length == 0) {
//                    utils.printMessage("未设置路由scheme~");
//                }
                appPaths.addAll(Arrays.asList(scheme));
            }
        }

    }
}
