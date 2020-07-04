package test.jandexed;

import com.github.t1.annotations.AmbiguousAnnotationResolutionException;
import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.Stereotype;
import com.github.t1.annotations.impl.AnnotationsLoaderImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static test.jandexed.TestTools.buildAnnotationsLoader;

public class StereotypeBehavior {
    AnnotationsLoaderImpl TheAnnotations = buildAnnotationsLoader();

    @Stereotype
    @Retention(RUNTIME)
    @SomeAnnotation("stereotype")
    @RepeatableAnnotation(1)
    @RepeatableAnnotation(2)
    public @interface SomeStereotype {}

    @Stereotype
    @Retention(RUNTIME)
    @SomeAnnotation("another-stereotype")
    @RepeatableAnnotation(3)
    @RepeatableAnnotation(4)
    public @interface AnotherStereotype {}


    @Nested class StereotypedClasses {
        @SomeStereotype
        @RepeatableAnnotation(5)
        class StereotypedClass {}


        Annotations annotations = TheAnnotations.onType(StereotypedClass.class);

        @Test void shouldGetAnnotationFromClassStereotype() {
            Optional<SomeAnnotation> someAnnotation = annotations.get(SomeAnnotation.class);

            assert someAnnotation.isPresent();
            then(someAnnotation.get().value()).isEqualTo("stereotype");
        }

        @Test void shouldGetAllAnnotationsFromClassStereotype() {
            List<Annotation> someAnnotation = annotations.all();

            then(someAnnotation.stream().map(Object::toString)).containsExactly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 5) on " + StereotypedClass.class.getName(),
                "@" + SomeStereotype.class.getName() + " on " + StereotypedClass.class.getName(),
                "@" + Stereotype.class.getName() + " on " + SomeStereotype.class.getName(),
                "@" + SomeAnnotation.class.getName() + "(value = \"stereotype\") on " + SomeStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + SomeStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 2) on " + SomeStereotype.class.getName(),
                "@" + Retention.class.getName() + "(value = RUNTIME) on " + SomeStereotype.class.getName());
        }

        @Test void shouldGetAllNonRepeatableAnnotationsFromClassStereotype() {
            Stream<SomeAnnotation> someAnnotation = annotations.all(SomeAnnotation.class);

            then(someAnnotation.map(Objects::toString)).containsExactly(
                "@" + SomeAnnotation.class.getName() + "(value = \"stereotype\") on " + SomeStereotype.class.getName()
            );
        }

        @Test void shouldGetAllRepeatableAnnotationFromClassStereotype() {
            Stream<RepeatableAnnotation> someAnnotation = annotations.all(RepeatableAnnotation.class);

            then(someAnnotation.map(Objects::toString)).containsExactly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 5) on " + StereotypedClass.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + SomeStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 2) on " + SomeStereotype.class.getName()
            );
        }

        // TODO test indirect stereotypes

        @Test void shouldNotReplaceExistingClassAnnotation() {
            @SomeStereotype
            @SomeAnnotation("on-class")
            class StereotypedClassWithSomeAnnotation {}

            Annotations annotations = TheAnnotations.onType(StereotypedClassWithSomeAnnotation.class);

            Optional<SomeAnnotation> someAnnotation = annotations.get(SomeAnnotation.class);

            assert someAnnotation.isPresent();
            then(someAnnotation.get().value()).isEqualTo("on-class");
        }
    }

    @Nested class DoubleStereotypedClasses {
        @SomeStereotype
        @AnotherStereotype
        @RepeatableAnnotation(6)
        class DoubleStereotypedClass {}

        Annotations annotations = TheAnnotations.onType(DoubleStereotypedClass.class);

        @Test void shouldFailToGetAmbiguousAnnotationFromTwoStereotypes() {
            Throwable throwable = catchThrowable(() -> annotations.get(SomeAnnotation.class));

            then(throwable).isInstanceOf(AmbiguousAnnotationResolutionException.class);
        }

        @Test void shouldGetAllNonRepeatableAnnotationsFromTwoStereotypes() {
            Stream<SomeAnnotation> someAnnotations = annotations.all(SomeAnnotation.class);

            then(someAnnotations.map(Objects::toString)).containsExactly(
                "@" + SomeAnnotation.class.getName() + "(value = \"stereotype\") on " + SomeStereotype.class.getName(),
                "@" + SomeAnnotation.class.getName() + "(value = \"another-stereotype\") on " + AnotherStereotype.class.getName()
            );
        }

        @Test void shouldGetAllRepeatableAnnotationsFromTwoStereotypes() {
            Stream<RepeatableAnnotation> repeatableAnnotations = annotations.all(RepeatableAnnotation.class);

            then(repeatableAnnotations.map(Objects::toString)).containsExactly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 6) on " + DoubleStereotypedClass.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + SomeStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 2) on " + SomeStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 3) on " + AnotherStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 4) on " + AnotherStereotype.class.getName()
            );
        }

        @Test void shouldGetAllAnnotationsFromTwoStereotypes() {
            List<Annotation> all = annotations.all();

            then(all.stream().map(Objects::toString)).containsExactly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 6) on " + DoubleStereotypedClass.class.getName(),
                "@" + SomeStereotype.class.getName() + " on " + DoubleStereotypedClass.class.getName(),
                "@" + AnotherStereotype.class.getName() + " on " + DoubleStereotypedClass.class.getName(),
                "@" + Stereotype.class.getName() + " on " + SomeStereotype.class.getName(),
                "@" + SomeAnnotation.class.getName() + "(value = \"stereotype\") on " + SomeStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + SomeStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 2) on " + SomeStereotype.class.getName(),
                "@" + Retention.class.getName() + "(value = RUNTIME) on " + SomeStereotype.class.getName(),
                "@" + Stereotype.class.getName() + " on " + AnotherStereotype.class.getName(),
                "@" + SomeAnnotation.class.getName() + "(value = \"another-stereotype\") on " + AnotherStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 3) on " + AnotherStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 4) on " + AnotherStereotype.class.getName(),
                "@" + Retention.class.getName() + "(value = RUNTIME) on " + AnotherStereotype.class.getName()
            );
        }
    }

    @Nested class StereotypedFields {
        @SuppressWarnings("unused")
        class ClassWithFields {
            @SomeStereotype
            @RepeatableAnnotation(7)
            String foo;
            boolean bar;
        }


        Annotations annotations = TheAnnotations.onField(ClassWithFields.class, "foo");

        @Test void shouldGetAnnotationFromFieldStereotype() {
            Optional<SomeAnnotation> someAnnotation = annotations.get(SomeAnnotation.class);

            assert someAnnotation.isPresent();
            then(someAnnotation.get().value()).isEqualTo("stereotype");
        }

        @Test void shouldGetAllAnnotationsFromFieldStereotype() {
            List<Annotation> someAnnotation = annotations.all();

            then(someAnnotation.stream().map(Object::toString)).containsExactly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 7) on " + ClassWithFields.class.getName() + ".foo",
                "@" + SomeStereotype.class.getName() + " on " + ClassWithFields.class.getName() + ".foo",
                "@" + Stereotype.class.getName() + " on " + SomeStereotype.class.getName(),
                "@" + SomeAnnotation.class.getName() + "(value = \"stereotype\") on " + SomeStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + SomeStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 2) on " + SomeStereotype.class.getName(),
                "@" + Retention.class.getName() + "(value = RUNTIME) on " + SomeStereotype.class.getName());
        }

        @Test void shouldGetAllAnnotationNonRepeatableTypedFromFieldStereotype() {
            Stream<SomeAnnotation> someAnnotation = annotations.all(SomeAnnotation.class);

            then(someAnnotation.map(Objects::toString)).containsExactly(
                "@" + SomeAnnotation.class.getName() + "(value = \"stereotype\") on " + SomeStereotype.class.getName()
            );
        }
    }

    @Nested class StereotypedMethods {
        @SuppressWarnings("unused")
        class ClassWithMethods {
            @SomeStereotype
            @RepeatableAnnotation(7)
            String foo() { return "foo"; }
        }


        Annotations annotations = TheAnnotations.onMethod(ClassWithMethods.class, "foo");

        @Test void shouldGetAnnotationFromMethodStereotype() {
            Optional<SomeAnnotation> someAnnotation = annotations.get(SomeAnnotation.class);

            assert someAnnotation.isPresent();
            then(someAnnotation.get().value()).isEqualTo("stereotype");
        }

        @Test void shouldGetAllAnnotationsFromMethodStereotype() {
            List<Annotation> someAnnotation = annotations.all();

            then(someAnnotation.stream().map(Object::toString)).containsExactly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 7) on " + ClassWithMethods.class.getName() + ".foo",
                "@" + SomeStereotype.class.getName() + " on " + ClassWithMethods.class.getName() + ".foo",
                "@" + Stereotype.class.getName() + " on " + SomeStereotype.class.getName(),
                "@" + SomeAnnotation.class.getName() + "(value = \"stereotype\") on " + SomeStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + SomeStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 2) on " + SomeStereotype.class.getName(),
                "@" + Retention.class.getName() + "(value = RUNTIME) on " + SomeStereotype.class.getName());
        }

        @Test void shouldGetAllAnnotationNonRepeatableTypedFromMethodStereotype() {
            Stream<SomeAnnotation> someAnnotation = annotations.all(SomeAnnotation.class);

            then(someAnnotation.map(Objects::toString)).containsExactly(
                "@" + SomeAnnotation.class.getName() + "(value = \"stereotype\") on " + SomeStereotype.class.getName()
            );
        }
    }
}
