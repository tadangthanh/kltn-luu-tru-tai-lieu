package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.ChatSessionDto;
import vn.kltn.entity.ChatSession;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface ChatSessionMapper {
    @Mapping(target = "userId", source = "user.id")
    ChatSessionDto toDto(ChatSession chatSession);

    ChatSession toEntity(ChatSessionDto chatSessionDto);
}
