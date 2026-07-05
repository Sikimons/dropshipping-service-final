package org.ups.dropshippingservice.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "org.ups.dropshippingservice",
        importOptions = ImportOption.DoNotIncludeTests.class)
public class CleanArchitectureTest {

    @ArchTest
    static final ArchRule domain_should_not_depend_on_outer_layers =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..application.service..", "..adapter..", "..infrastructure..");

    @ArchTest
    static final ArchRule application_should_not_depend_on_adapters_or_infra =
            noClasses().that().resideInAPackage("..application..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..adapter..", "..infrastructure..");

    @ArchTest
    static final ArchRule adapter_should_not_depend_on_infrastructure =
            noClasses().that().resideInAPackage("..adapter..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule domain_should_have_no_spring_annotations =
            noClasses().that().resideInAPackage("..domain..")
                    .should().beAnnotatedWith("org.springframework.stereotype.Component")
                    .orShould().beAnnotatedWith("org.springframework.stereotype.Service")
                    .orShould().beAnnotatedWith("org.springframework.stereotype.Repository")
                    .orShould().beAnnotatedWith("org.springframework.web.bind.annotation.RestController");

    @ArchTest
    static final ArchRule application_services_should_have_no_jpa_annotations =
            noClasses().that().resideInAPackage("..application..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("jakarta.persistence..");
}
