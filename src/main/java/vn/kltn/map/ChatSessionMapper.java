package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.kltn.dto.ChatSessionDto;
import vn.kltn.dto.request.ChatSessionInit;
import vn.kltn.entity.ChatSession;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface ChatSessionMapper {
    @Mapping(target = "userId", source = "user.id")
    ChatSessionDto toDto(ChatSession chatSession);

    ChatSession toEntity(ChatSessionDto chatSessionDto);

    ChatSession requestToEntity(ChatSessionInit chatSessionInit);

    void updateEntity(@MappingTarget ChatSession chatSession, ChatSessionDto chatSessionDto);
}
