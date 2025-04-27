package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.request.UserRegister;
import vn.kltn.dto.response.UserResponse;
import vn.kltn.entity.User;
import vn.kltn.index.UserIndex;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface UserMapper {
    User registerToUser(UserRegister userRegister);

    UserResponse toUserResponse(User user);

    @Mapping(target = "id", source = "id")
    UserIndex toUserIndex(User user);
}
