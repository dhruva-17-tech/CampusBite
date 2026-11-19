package com.campusbite.suppliers;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.List;
@Document("purchase_orders")
public class PurchaseOrder {
 @Id private String id; private String supplierId; private String supplierName; private List<Line> items; private double totalCost; private String status="ORDERED"; private String createdBy; private Instant createdAt=Instant.now(); private Instant receivedAt;
 public record Line(String itemName,int quantity,double unitCost){public double lineTotal(){return quantity*unitCost;}}
 public PurchaseOrder() {}
 public PurchaseOrder(String sid,String sn,List<Line> lines,String by){supplierId=sid;supplierName=sn;items=lines;createdBy=by;totalCost=lines.stream().mapToDouble(Line::lineTotal).sum();}
 public String getId(){return id;} public String getSupplierId(){return supplierId;} public String getSupplierName(){return supplierName;} public List<Line> getItems(){return items;} public double getTotalCost(){return totalCost;} public String getStatus(){return status;} public String getCreatedBy(){return createdBy;} public Instant getCreatedAt(){return createdAt;} public Instant getReceivedAt(){return receivedAt;} public void setStatus(String s){status=s;if("RECEIVED".equals(s))receivedAt=Instant.now();}
}
