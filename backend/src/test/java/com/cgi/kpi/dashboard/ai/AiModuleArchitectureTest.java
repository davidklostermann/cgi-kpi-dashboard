package com.cgi.kpi.dashboard.ai;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

class AiModuleArchitectureTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .importPackages("com.cgi.kpi.dashboard");

    @Test
    void aiModuleMustNotDependOnPersistenceRepositories() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..dashboard.ai..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure.persistence..");

        rule.check(CLASSES);
    }
}
