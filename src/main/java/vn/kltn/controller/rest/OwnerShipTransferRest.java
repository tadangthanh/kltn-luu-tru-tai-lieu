package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.kltn.dto.response.OwnerShipTransferResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IOwnerShipTransferService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/ownership-transfers")
public class OwnerShipTransferRest {
    private final IOwnerShipTransferService ownerShipTransferService;

    @PostMapping("/document/{documentId}/owner/{newOwnerId}")
    public ResponseData<OwnerShipTransferResponse> transferDocumentOwner(@PathVariable Long documentId, @PathVariable Long newOwnerId) {
        return new ResponseData<>(200, "Thành công", ownerShipTransferService.createTransferDocumentOwner(documentId, newOwnerId));
    }
}
