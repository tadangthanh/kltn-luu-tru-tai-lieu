package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.ConversationDto;
import vn.kltn.entity.Conversation;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface ConversationMapper {
    @Mapping(target = "chatSessionId", source = "chatSession.id")
    ConversationDto toDto(Conversation conversation);

    Conversation toEntity(ConversationDto conversationDto);
}
