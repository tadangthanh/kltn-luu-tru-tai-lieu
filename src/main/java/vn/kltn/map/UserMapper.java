package vn.kltn.map;

import org.mapstruct.Mapper;
import vn.kltn.dto.request.UserRegister;
import vn.kltn.entity.User;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface UserMapper {
    User registerToUser(UserRegister userRegister);
}
