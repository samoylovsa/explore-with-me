package ewm.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserDto {
    private Long id;

    @NotNull(message = "Имя пользователя должно быть в теле запроса")
    @NotBlank(message = "Имя пользователя не может быть пустым")
    private String name;

    @NotNull(message = "Адрес электронной почты должен быть в теле запроса")
    @NotBlank(message = "Адрес электронной почты не может быть пустым")
    @Email(message = "Некорректный адрес электронной почты")
    private String email;
}
