package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
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

    @PostMapping("/folder/{folderId}/owner/{newOwnerId}")
    public ResponseData<OwnerShipTransferResponse> transferFolderOwner(@PathVariable Long folderId, @PathVariable Long newOwnerId) {
        return new ResponseData<>(200, "Thành công", ownerShipTransferService.createTransferFolderOwner(folderId, newOwnerId));
    }

    @GetMapping("/accept-owner/document")
    public ResponseData<OwnerShipTransferResponse> acceptTransferByDocumentId(@RequestParam Long documentId) {
        System.out.println("document id = " + documentId);
        return new ResponseData<>(200, "Thành công", ownerShipTransferService.acceptTransferByDocumentId(documentId));
    }
}
