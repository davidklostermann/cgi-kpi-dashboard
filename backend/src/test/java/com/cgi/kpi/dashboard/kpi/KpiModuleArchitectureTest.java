package com.cgi.kpi.dashboard.kpi;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

class KpiModuleArchitectureTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .importPackages("com.cgi.kpi.dashboard");

    @Test
    void kpiModuleMustNotDependOnAiModule() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage("..kpi..")
                .should().dependOnClassesThat().resideInAnyPackage("..ai..");

        rule.check(CLASSES);
    }
}
