package code.ponfee.commons.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Hello world!
 *
 */
public class GenericTest2 {
    public static abstract class QueryParams {
        private String type;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class LimitedQueryParams extends QueryParams {
        private int limit;

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }
    }

    public static abstract class QueryExecutor<Q extends QueryParams> {
        public abstract String query(Q query);
    }

    public static class DatabaseQueryExecutor extends QueryExecutor<LimitedQueryParams> {
        @Override
        public String query(LimitedQueryParams query) {
            return query.toString();
        }
    }

    public static class MysqlQueryExecutor extends DatabaseQueryExecutor {
    }

    public static void main(String[] args) throws Exception {
        // class code.ponfee.commons.reflect.GenericTest2$QueryExecutor
        System.out.println(QueryExecutor.class.getMethod("query", QueryParams.class).getDeclaringClass());

        // class code.ponfee.commons.reflect.GenericTest2$DatabaseQueryExecutor
        System.out.println(DatabaseQueryExecutor.class.getMethod("query", QueryParams.class).getDeclaringClass());

        // class code.ponfee.commons.reflect.GenericTest2$DatabaseQueryExecutor
        System.out.println(DatabaseQueryExecutor.class.getMethod("query", LimitedQueryParams.class).getDeclaringClass());

        // class code.ponfee.commons.reflect.GenericTest2$DatabaseQueryExecutor
        System.out.println(MysqlQueryExecutor.class.getMethod("query", QueryParams.class).getDeclaringClass());

        // class code.ponfee.commons.reflect.GenericTest2$DatabaseQueryExecutor
        System.out.println(MysqlQueryExecutor.class.getMethod("query", LimitedQueryParams.class).getDeclaringClass());

        // ---------------------------------------------------------------------
        System.out.println("\n\n\n");
        // class java.lang.Object
        System.out.println(GenericUtils.getMethodArgActualType(
            MysqlQueryExecutor.class, QueryExecutor.class.getMethod("query", QueryParams.class), 0)
        );
        
        // class code.ponfee.commons.reflect.GenericTest2$LimitedQueryParams
        System.out.println(GenericUtils.getMethodArgActualType(
           DatabaseQueryExecutor.class, QueryExecutor.class.getMethod("query", QueryParams.class), 0)
       );
        
        System.out.println("\n\n\n");
        printMethods(QueryExecutor.class);
        printMethods(DatabaseQueryExecutor.class);
        printMethods(MysqlQueryExecutor.class);
    }

    public static void printMethods(Class<?> clazz) {
        System.out.println("\n\n-------------------------" + clazz);
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (   Modifier.isAbstract(method.getModifiers()) 
                || Modifier.isPrivate(method.getModifiers()) 
                || method.getDeclaringClass() == Object.class
                || !"query".equals(method.getName())
            ) {
                continue;
            }
            Class<?>[] params = method.getParameterTypes();
            if (params.length == 1 && QueryParams.class.isAssignableFrom(params[0])) {
                System.out.println(method.toGenericString());
                System.out.println(Arrays.toString(method.getParameters()));
                System.out.println(Arrays.toString(method.getTypeParameters()));
            }
        }
    }
}
