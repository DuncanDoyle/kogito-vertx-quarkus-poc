package org.kie.kogito.poc.util;

import java.util.Collection;
import java.util.List;

import org.drools.core.spi.KnowledgeHelper;
import org.drools.modelcompiler.consequence.DroolsImpl;
import org.kie.api.KieBase;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KieUtil
 */
public class KieUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(KieUtil.class);

    public static void logRules(KieContext kcontext) {
        kcontext.getKieRuntime().getKieBase().getKiePackages().stream()
                .flatMap(kiePackage -> kiePackage.getRules().stream())
                .forEach(rule -> LOGGER.info(rule.getName()));
    }

    public static void logMatch(KnowledgeHelper helper) {
        LOGGER.info("Match with the following objects: ");
        List<Object> objects = helper.getMatch().getObjects();
        for (Object object : objects) {
            LOGGER.info("\n- Object: " + object.getClass().getCanonicalName());
        }
    }
}