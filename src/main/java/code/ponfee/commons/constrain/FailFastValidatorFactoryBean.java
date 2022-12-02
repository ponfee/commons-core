package code.ponfee.commons.constrain;

import org.hibernate.validator.HibernateValidator;
import org.springframework.beans.factory.FactoryBean;

import javax.validation.Validation;
import javax.validation.Validator;

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
    public Validator getObject() {
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
