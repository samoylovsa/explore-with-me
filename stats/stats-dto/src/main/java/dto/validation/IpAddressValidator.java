package dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class IpAddressValidator implements ConstraintValidator<ValidIpAddress, String> {

    private static final String IPV4_PATTERN =
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    private static final Pattern IPv4_PATTERN_REGEX = Pattern.compile(IPV4_PATTERN);

    @Override
    public boolean isValid(String ip, ConstraintValidatorContext context) {
        if (ip == null || ip.isBlank()) {
            return true;
        }

        return IPv4_PATTERN_REGEX.matcher(ip).matches();
    }
}
