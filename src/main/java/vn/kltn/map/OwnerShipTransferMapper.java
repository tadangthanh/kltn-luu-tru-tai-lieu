package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.response.OwnerShipTransferResponse;
import vn.kltn.entity.OwnerShipTransfer;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface OwnerShipTransferMapper {
    @Mapping(target = "newOwnerId", source = "newOwner.id")
    @Mapping(target = "newOwnerName", source = "newOwner.fullName")
    @Mapping(target = "newOwnerEmail", source = "newOwner.email")
    @Mapping(target = "oldOwnerId", source = "oldOwner.id")
    OwnerShipTransferResponse toOwnerShipTransferResponse(OwnerShipTransfer ownerShipTransfer);
}
