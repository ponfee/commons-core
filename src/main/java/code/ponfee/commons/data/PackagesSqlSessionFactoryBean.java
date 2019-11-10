package code.ponfee.commons.data;

import java.util.HashSet;
import java.util.Set;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import code.ponfee.commons.resource.ResourceScanner;

/**
 * Mybatis typeAliasesPackage config
 * 
 * p:typeAliasesPackage="cn.ponfee.data.**.model"
 * 
 * @author Ponfee
 */
public class PackagesSqlSessionFactoryBean extends SqlSessionFactoryBean {

    private static final Logger logger = LoggerFactory.getLogger(PackagesSqlSessionFactoryBean.class);

    @Override
    public void setTypeAliasesPackage(String typeAliasesPackage) {
        Set<String> result = new HashSet<>();
        for (Class<?> type : new ResourceScanner(typeAliasesPackage.replace('.', '/')).scan4class()) {
            result.add(type.getPackage().getName());
        }

        if (result.isEmpty()) {
            logger.warn("TypeAliasesPackage not scanned: " + typeAliasesPackage);
        } else {
            super.setTypeAliasesPackage(String.join(",", result));
        }
    }

}
