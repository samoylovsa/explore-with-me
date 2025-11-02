package ewm.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserDto {
    private Long id;

    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 2, max = 250)
    private String name;

    @NotBlank(message = "Адрес электронной почты не может быть пустым")
    @Email(message = "Некорректный адрес электронной почты")
    @Size(min = 6, max = 254)
    private String email;
}
