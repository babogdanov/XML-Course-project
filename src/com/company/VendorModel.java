package com.company;

import java.util.ArrayList;

public class VendorModel {
    int vendorId = 0;
    String vendorName = "";
    String vendorPhone = "";
    ArrayList<ProductModel> productModels = new ArrayList<>();

    public VendorModel(int vendorId, String vendorName, String vendorPhone) {
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.vendorPhone = vendorPhone;
    }

    public void addProductModel(ProductModel productModel){
        this.productModels.add(productModel);
    }

    public int getVendorId() {
        return vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public String getVendorPhone() {
        return vendorPhone;
    }

    public ArrayList<ProductModel> getProductModels() {
        return productModels;
    }

    @Override
    public String toString() {
        return "VendorModel{" +
                "vendorId=" + vendorId +
                ", vendorName='" + vendorName + '\'' +
                ", vendorPhone='" + vendorPhone + '\'' +
                ", productModels=" + productModels +
                '}';
    }
}
