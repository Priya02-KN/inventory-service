package com.company.wms.inventory.event;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PutAwayPayload {

    private String skuId;
    private String warehouseId;
    private String binId;
    private Long quantity;
    private String referenceType;
    private String referenceId;
}

