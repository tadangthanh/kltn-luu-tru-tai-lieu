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
        return new ResponseData<>(200, "Thành công", ownerShipTransferService.acceptTransferByDocumentId(documentId));
    }

    @GetMapping("/decline-owner/document")
    public ResponseData<OwnerShipTransferResponse> declineTransferByDocumentId(@RequestParam Long documentId) {
        return new ResponseData<>(200, "Thành công", ownerShipTransferService.declineTransferByDocumentId(documentId));
    }

    @GetMapping("/accept-owner/folder")
    public ResponseData<OwnerShipTransferResponse> acceptTransferByFolderId(@RequestParam Long folderId) {
        return new ResponseData<>(200, "Thành công", ownerShipTransferService.acceptTransferByFolderId(folderId));
    }

    @GetMapping("/decline-owner/folder")
    public ResponseData<OwnerShipTransferResponse> declineTransferByFolderId(@RequestParam Long folderId) {
        return new ResponseData<>(200, "Thành công", ownerShipTransferService.declineTransferByFolderId(folderId));
    }
}
