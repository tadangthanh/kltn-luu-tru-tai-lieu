package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.request.ConversationRequest;
import vn.kltn.dto.response.ConversationDto;
import vn.kltn.entity.Conversation;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface ConversationMapper {
    ConversationDto toResponse(Conversation conversation);

    @Mapping(target = "id", ignore = true)
    Conversation toEntity(ConversationRequest conversationRequest);

    @Mapping(target = "id", ignore = true)
    Conversation toEntity(ConversationDto conversationDto);
}
