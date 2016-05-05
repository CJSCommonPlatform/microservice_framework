package uk.gov.justice.services.adapters.rest.generator;

import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.PUBLIC;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;
import static uk.gov.justice.services.adapters.rest.generator.Names.RESOURCE_PACKAGE_NAME_WITH_DOT;
import static uk.gov.justice.services.adapters.rest.generator.Names.applicationNameFrom;
import static uk.gov.justice.services.adapters.rest.generator.Names.baseUriPathWithoutContext;

import uk.gov.justice.raml.core.GeneratorConfig;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import org.raml.model.Raml;

/**
 * Internal code generation class for generating the JAX-RS {@link Application} that ties the
 * resources to a base URI.
 */
class JaxRsApplicationCodeGenerator {

    private static final String DEFAULT_ANNOTATION_PARAMETER = "value";

    private final GeneratorConfig config;

    /**
     * Constructor.
     *
     * @param config the generator configuration
     */
    JaxRsApplicationCodeGenerator(final GeneratorConfig config) {
        this.config = config;
    }

    /**
     * Create an implementation of the {@link Application}.
     *
     * @param implementationNames a collection of fully qualified class names of the resource
     *                            implementation classes
     * @param raml                the RAML document being generated from
     * @return the fully defined application class
     */
    TypeSpec createApplication(final Collection<String> implementationNames, final Raml raml) {
        return classSpecFrom(raml)
                .addMethod(generateGetClassesMethod(implementationNames))
                .build();
    }

    /**
     * Generate the implementation of {@link Application} and set the {@link ApplicationPath}.
     *
     * @param raml the RAML document being generated from
     * @return the {@link TypeSpec.Builder} that defines the class
     */
    private TypeSpec.Builder classSpecFrom(final Raml raml) {
        return classBuilder(applicationNameFrom(raml))
                .addModifiers(PUBLIC)
                .superclass(Application.class)
                .addAnnotation(AnnotationSpec.builder(ApplicationPath.class)
                        .addMember(DEFAULT_ANNOTATION_PARAMETER, "$S", defaultIfBlank(baseUriPathWithoutContext(raml), "/"))
                        .build());
    }

    /**
     * Generate the getClasses method that returns the set of implemented resource classes.
     *
     * @param implementationNames the collection of implementation class names
     * @return the {@link MethodSpec} that represents the getClasses method
     */
    private MethodSpec generateGetClassesMethod(final Collection<String> implementationNames) {
        final ParameterizedTypeName wildcardClassType = ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class));
        final ParameterizedTypeName classSetType = ParameterizedTypeName.get(ClassName.get(Set.class), wildcardClassType);
        final ParameterizedTypeName classHashSetType = ParameterizedTypeName.get(ClassName.get(HashSet.class), wildcardClassType);

        return MethodSpec.methodBuilder("getClasses")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addCode(CodeBlock.builder()
                        .addStatement("$T classes = new $T()", classSetType, classHashSetType)
                        .add(statementsToAddClassToSetForEach(implementationNames))
                        .addStatement("return classes")
                        .build())
                .returns(classSetType)
                .build();
    }

    /**
     * Generate code to add each resource implementation class to the classes hash set.
     *
     * @param implementationNames the collection of implementation class names
     * @return the {@link CodeBlock} that represents the generated statements
     */
    private CodeBlock statementsToAddClassToSetForEach(final Collection<String> implementationNames) {
        final CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();

        implementationNames.stream().forEach(implementationClassName ->
                codeBlockBuilder.addStatement("classes.add($T.class)", classNameTypeOf(implementationClassName)));

        return codeBlockBuilder.build();
    }

    /**
     * Create {@link ClassName} that fully qualifies the implementation class
     *
     * @param className the class name to define
     * @return the {@link ClassName} that represents the full package and class name of the
     * implementation class
     */
    private ClassName classNameTypeOf(final String className) {
        return ClassName.get(config.getBasePackageName() + RESOURCE_PACKAGE_NAME_WITH_DOT, className);
    }
}
