package ijse.cmjd.cw3.dto;

import ijse.cmjd.cw3.entity.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private Role role;
}
