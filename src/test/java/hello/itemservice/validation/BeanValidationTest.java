package hello.itemservice.validation;

import hello.itemservice.domain.item.Item;
import org.junit.jupiter.api.Test;
import org.springframework.validation.annotation.Validated;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class BeanValidationTest {

    @Test
    void beanValidation() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Item item = new Item();
        item.setItemName("");
        item.setPrice(1);
        item.setQuantity(10000000);


        for (ConstraintViolation cv : validator.validate(item)) {
            System.out.println("cv= " + cv);
            System.out.println("cv.getMessage() = " + cv.getMessage());
        }

    }
}
