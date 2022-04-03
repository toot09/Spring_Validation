package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

@Component
public class ItemValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return Item.class.isAssignableFrom(clazz);
        // item == clazz (파라미터로 넘어오는 클래스가 Item을 지원하냐?)
        // item == subItem (자식 클래스 점검)
    }

    @Override
    public void validate(Object target, Errors errors) {
        // Errors는 BindingResult의 부모 인터페이스이다. -> public interface BindingResult extends Errors
        Item item = (Item) target;

        // 검증 로직
        if(!StringUtils.hasText(item.getItemName())) {
            errors.rejectValue("itemName", "required");
        }
        if(item.getPrice() == null || item.getPrice()<1000 || item.getPrice()>1000000) {
            errors.rejectValue("price", "range", new Object[]{1000, 1000000}, null);
        }
        if(item.getQuantity() == null || item.getQuantity() > 9999) {
            errors.rejectValue("quantity", "max", new Object[]{9999}, null);
        }
        if(item.getPrice()!=null && item.getQuantity()!=null) {
            int resultPrice = item.getPrice()*item.getQuantity();
            if(resultPrice < 10000) {
                // field에 대한 에러가 아니기 떄문에 reject사용
                errors.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

    }
}
