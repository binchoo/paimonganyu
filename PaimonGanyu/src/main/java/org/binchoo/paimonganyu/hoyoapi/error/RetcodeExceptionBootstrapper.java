package org.binchoo.paimonganyu.hoyoapi.error;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

class RetcodeExceptionBootstrapper {

    protected static String packageName = RetcodeExceptionBootstrapper.class.getPackage().getName();
    protected static String excecptionPackageName = packageName + ".exceptions";

    private static ClassPathScanningCandidateComponentProvider provider
            = new ClassPathScanningCandidateComponentProvider(false);

    private static RetcodeExceptionMappings mappings = RetcodeExceptionMappings.getInstance();

    protected static void start() {
        initComponentProvider();
        initRetcodeExceptionMappings();
    }

    private static void initComponentProvider() {
        provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));
    }

    private static void initRetcodeExceptionMappings() {
        for (Class<RetcodeException> clazz : scanExceptionPackage()) {
            Retcode retcodeAnnot = clazz.getAnnotation(Retcode.class);

            if (retcodeAnnot != null) {
                boolean singleRetcode = Integer.MIN_VALUE != retcodeAnnot.value();

                if (singleRetcode) {
                    int retcode = retcodeAnnot.value();
                    mappings.addMapping(retcode, clazz);
                }
                else {
                    for (int retcode : retcodeAnnot.codes()) {
                        mappings.addMapping(retcode, clazz);
                    }
                }
            }
        }
    }

    private static List<Class<RetcodeException>> scanExceptionPackage() {
        Set<BeanDefinition> beanDefinitions =
                provider.findCandidateComponents(excecptionPackageName);

        List<Class<RetcodeException>> exceptions = new LinkedList<>();
        for (BeanDefinition bdf : beanDefinitions) {
            try {
                Class<RetcodeException> retcodeExceptionClass
                        = (Class<RetcodeException>) Class.forName(bdf.getBeanClassName());
                exceptions.add(retcodeExceptionClass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return exceptions;
    }
}
