## 1. BindingResult
### Spring

1. Argument BindingResult 추가
```
public String addMoney(@ModelAttribute Item item, BindingResult bindingResult) ...

```
2. BindingResult 에러추가
```
필드에러 처리 예 : bindingResult.addError(new FieldError(ObjectName, field, message));
글로벌 에러 처리 예 : bindingResult.addError(new ObjectError("ObjectName, message));
```
3. bindingResult는 자동으로 model에 적용되기 떄문에 따로 add할 필요 없음.

### Thymeleaf
1. global error
```
<div th:if="${#fields.hasGlobalErrors()}">
	<p class="field-error" th:each="err : ${#fields.globalErrors()}" th:text="${err}">전체 오류 메시지</p>
</div>
```
- ${#fields.hasGlobalErrors()} : 글로벌에러 여부 확인
- 여러 에러 발생 가능의 경우 each로 처리

2. field error
```
<div class="field-error" th:errors="*{itemName}">
	상품명 오류
</div>
```
- 깔끔하게 *{...}로 처리된다. 물론 Spring 내에서 BindingResult 추가 할 때의 이름과 같아야한다.
```
<input type="text" id="itemName"
th:field="*{itemName}" th:errorclass="field-error"
class="form-control" placeholder="이름을 입력하세요">
```
- th:errorclass="..."로 에러가 발생했을때의(조건부) 클래스 추가 해준다.
>타임리프 스프링 검증 오류 통합 기능
타임리프는 스프링의 BindingResult 를 활용해서 편리하게 검증 오류를 표현하는 기능을 제공한다.
#fields : #fields 로 BindingResult 가 제공하는 검증 오류에 접근할 수 있다.
th:errors : 해당 필드에 오류가 있는 경우에 태그를 출력한다. th:if 의 편의 버전이다.
th:errorclass : th:field 에서 지정한 필드에 오류가 있으면 class 정보를 추가한다.

### BindingResult에 검증 오류를 적용하는 3가지 방법
1.@ModelAttribute 의 객체에 타입 오류 등으로 바인딩이 실패하는 경우 스프링이 FieldError 생성해서 BindingResult 에 넣어준다.
2. 개발자가 직접 넣어준다.
3. Validator 사용 이것은 뒤에서 설명

### 주의
**1. BindingResult 는 검증할 대상 바로 다음에 와야한다.** 순서가 중요하다. 예를 들어서 @ModelAttribute Item item , 바로 다음에 BindingResult 가 와야 한다.
2. BindingResult 는 Model에 자동으로 포함된다

### FieldError 생성자
1. public FieldError(String objectName, String field, String defaultMessage);
2. public FieldError(String objectName, String field,
   @Nullable Object rejectedValue, boolean bindingFailure, @Nullable String[] codes, @Nullable Object[] arguments, @Nullable String defaultMessage)
3. 파라미터 목록
>
objectName : 오류가 발생한 객체 이름
field : 오류 필드
rejectedValue : 사용자가 입력한 값(거절된 값)
bindingFailure : 타입 오류 같은 바인딩 실패인지, 검증 실패인지 구분 값 codes : 메시지 코드들 (MessageCodesResolver에서 가져온 메시지들)
arguments : 메시지에서 사용하는 인자
defaultMessage : 기본 오류 메시지


## 2. BindingResult reject(Value)
bindingResult.rejectValue(field, errorCode, arguments, defaultMessage);
- errorCode : "이름" (+.객체명.필드명) -> 여기에서는 앞에 이름만 쓰는걸로 룰 정함

bindingResult.reject(errorCode, Arguments, default);
- 필드 에러가 아닌경우 reject를 사용

## 3. MessageCodeResolver
![](https://media.vlpt.us/images/ujone/post/8a47a262-2f8c-459f-a2db-8c373396e78b/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA%202022-04-03%20%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE%207.21.15.png)
인터페이스이며 여러가지 메시지를 리턴해주는 기능을 한다.
MessageCodesResolver는 인터페이스이며, DefaultMessageCodesResolver는 구현부이다.
```
MessageCodesResolver codesResolver = new DefaultMessageCodesResolver();
```

### DefaultMessageCodesResolver의 기본 메시지 생성 규칙
1. 객체오류
1) code + "." + object name (예 : required.item)
2) code (예 : required)

2. 필드오류
1) code + "." + object name + "." + field (예 : typeMismatch.user.age)
2) code + "." + field (예 : typeMismatch.age)
3) code + "." + field type (예 : typeMismatch.int)
4) code (예 : typeMismatch)

> 가장 자세한 순위 순서로 나오기 때문에 우선순위 설정을 잘하면 된다.

## Validator
![](https://media.vlpt.us/images/ujone/post/c42e2949-ce33-4f1a-ac46-29fa4ca6f82c/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA%202022-04-03%20%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE%208.34.50.png)

### 1. validator 생성
```
@Component
public class ItemValidator implements Validator {
	
    // 비교대상 가능여부 확인 (여러 Validator가 등록되었을때, 비교 대상 클래스가 맞는지 확인하고 맞다하면 검증 들어감)
    @Override
    public boolean supports(Class<?> clazz) {
        return Item.class.isAssignableFrom(clazz);
        // item == clazz (파라미터로 넘어오는 클래스가 Item을 지원하냐?)
        // item == subItem (자식 클래스 점검)
    }
	
    // 검증
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

```

### 2. Controller InitBinder 추가
```
// 컨트롤러가 실행될 때마다 먼저 실행된다.
@InitBinder
public void init(WebDataBinder dataBinder) {
	dataBinder.addValidators(itemValidator);
}
```

### 3. @Validated 검증 대상 세팅
```
public String addItemV6(@Validated/*검증 대상 Object 앞에 붙여준다.*/ @ModelAttribute Item item, BindingResult bindingResult..)
```