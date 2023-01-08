package test.utils;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;


public class RepeatableAnn {

    public static void main(String[] args) {
        Annotation[] annotations = RepeatAnn.class.getAnnotations();
        System.out.println(annotations.length); //1
        Arrays.stream(annotations).forEach(System.out::println);

        Annotation[] annotations2 = Annotations.class.getAnnotations();
        System.out.println(annotations2.length);//1
        Arrays.stream(annotations2).forEach(System.out::println);
    }

    /**
     * The same annotation can be applied to a declaration or type more than
     * once, given that each annotation is marked as @Repeatable. In the
     * following code, the @Repeatable annotation is used to develop an
     * annotation that can be repeated, rather than grouped together as in
     * previous releases of Java. In this situation, an annotation named Role is
     * being created, and it will be used to signify a role for an annotated
     * class or method.
     */
    @Repeatable(value = Roles.class)
    public static @interface Role {
        String name() default "doctor";
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Roles {
        Role[] value();
    }

    @Role(name = "doctor")
    @Role(name = "who")
    public static class RepeatAnn{

    }

    @Roles({@Role(name="doctor"),
            @Role(name="who")})
    public static class Annotations{

    }
}
