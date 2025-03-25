package vn.kltn.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.TransferStatus;
import vn.kltn.dto.BaseDto;

@Getter
@Setter
public class OwnerShipTransferResponse extends BaseDto {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long documentId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String documentName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long folderId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String folderName;
    private Long newOwnerId;
    private String newOwnerName;
    private String newOwnerEmail;
    private Long oldOwnerId;
    private TransferStatus status;
}
