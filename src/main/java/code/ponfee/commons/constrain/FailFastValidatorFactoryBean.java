package code.ponfee.commons.constrain;

import javax.validation.Validation;
import javax.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.springframework.beans.factory.FactoryBean;

/**
 * For fail fast validator factory bean
 * 
 * @author Ponfee
 */
public class FailFastValidatorFactoryBean implements FactoryBean<Validator> {

    private final Validator validator;
    private final Class<? extends Validator> validatorType;

    public FailFastValidatorFactoryBean() {
        this.validator = Validation
            .byProvider(HibernateValidator.class)
            .configure()
            .failFast(true)
            //.addProperty("hibernate.validator.fail_fast", "true")
            .buildValidatorFactory()
            .getValidator();

        this.validatorType = this.validator.getClass();
    }

    @Override
    public Validator getObject() throws Exception {
        return this.validator;
    }

    @Override
    public Class<?> getObjectType() {
        return this.validatorType;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
