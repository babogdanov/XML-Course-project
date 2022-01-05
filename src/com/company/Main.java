package com.company;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException, JDOMException {
        while (true) {
            System.out.print("Enter 1 to parse the db entries to out.xml, 2 to do the opposite, or 3 to exit: ");
            Scanner sc = new Scanner(System.in);
            int choice = sc.nextInt();

            if (choice == 1) {
                dbToXml();
            } else if (choice == 2) {
                xmlToDb();
            } else if (choice == 3) {
                break;
            } else {
                System.out.println("Enter 1 or 2, please.");
            }
        }
    }

    static void dbToXml() throws IOException {
        Connection con;
        Statement stmt;
        ResultSet queryResult;

        List<VendorModel> data = new ArrayList<>();
        try {
            //Registering the HSQLDB JDBC driver
            String dbURL = "jdbc:sqlserver://localhost\\sqlexpress";

            //Creating the connection with HSQLDB
            con = DriverManager.getConnection("jdbc:jtds:sqlserver://DESKTOP-78T1K6E:1433/master", "guest", "");

            Class.forName("org.hsqldb.jdbc.JDBCDriver");
            stmt = con.createStatement();
            queryResult = stmt.executeQuery(
                    "SELECT v.id as v_id, v.name as v_name, v.phone as v_phone, p.id as p_id, p.name as p_name," +
                            " p.price as p_price, p.vendor_id as p_v_id " +
                            "FROM (vendors v LEFT JOIN products p ON v.id=p.vendor_id)"
            );

            while (queryResult.next()) {
                final int vendorId = queryResult.getInt("v_id");

                if (data.stream().noneMatch(x -> x.vendorId == vendorId)) {
                    data.add(new VendorModel(queryResult.getInt("v_id"),
                            queryResult.getString("v_name"),
                            queryResult.getString("v_phone")));
                }

                int dataIndex = -1;

                for (int i = 0; i < data.size(); i++) {
                    if (data.get(i).vendorId == vendorId) {
                        dataIndex = i;
                        break;
                    }
                }

                data.get(dataIndex).addProductModel(new ProductModel(queryResult.getInt("p_id"),
                        queryResult.getString("p_name"),
                        queryResult.getInt("p_price")));
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

        Element root = new Element("vendors");
        Document doc = new Document();
        for (VendorModel vendorModel : data) {
            Element vendor = new Element("vendor");
            vendor.setAttribute("id", vendorModel.getVendorId() + "");
            vendor.setAttribute("name", vendorModel.getVendorName());
            vendor.setAttribute("phone", vendorModel.getVendorPhone());
            for (ProductModel productModel : vendorModel.productModels) {
                Element product = new Element("product");
                product.setAttribute("id", productModel.getProductId() + "");
                product.setAttribute("name", productModel.getProductName());
                product.setAttribute("price", productModel.getProductPrice() + "");
                vendor.addContent(product);
            }
            root.addContent(vendor);
        }
        doc.setRootElement(root);

        //Create the XML
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(Format.getPrettyFormat());
        outputter.output(doc, new FileWriter("out.xml"));
        System.out.println("Success!");
    }

    static void xmlToDb() throws IOException, JDOMException {
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new FileInputStream("out.xml"));
        Element root = doc.getRootElement();
        List<VendorModel> data = new ArrayList<>();

        for (Element vendor : root.getChildren()) {
            VendorModel vendorModel = new VendorModel(
                    Integer.parseInt(vendor.getAttributeValue("id")),
                    vendor.getAttributeValue("name"),
                    vendor.getAttributeValue("phone"));
            for (Element product : vendor.getChildren()) {
                ProductModel productModel = new ProductModel(
                        Integer.parseInt(product.getAttributeValue("id")),
                        product.getAttributeValue("name"),
                        Integer.parseInt(product.getAttributeValue("price")));
                vendorModel.productModels.add(productModel);
            }
            data.add(vendorModel);
        }

        Connection con;
        PreparedStatement vendorStmt, productStmt;
        ResultSet queryResult;
        String vendorQuery = "INSERT INTO vendors" +
                " (id, name, phone)" +
                "VALUES(?,?,?)";

        String productQuery = "INSERT INTO products" +
                " (id,name,price,vendor_id)" +
                "VALUES(?,?,?,?)";
        try {
            //Registering the HSQLDB JDBC driver
            String dbURL = "jdbc:sqlserver://localhost\\sqlexpress";

            //Creating the connection with HSQLDB
            con = DriverManager.getConnection("jdbc:jtds:sqlserver://DESKTOP-78T1K6E:1433/master", "guest", "");

            Class.forName("org.hsqldb.jdbc.JDBCDriver");
            vendorStmt = con.prepareStatement(vendorQuery);
            productStmt = con.prepareStatement(productQuery);
            for (VendorModel vendorModel : data) {
                vendorStmt.setInt(1, vendorModel.getVendorId());
                vendorStmt.setString(2, vendorModel.getVendorName());
                vendorStmt.setString(3, vendorModel.getVendorPhone());
                vendorStmt.addBatch();
                for (ProductModel productModel : vendorModel.getProductModels()) {
                    productStmt.setInt(1, productModel.getProductId());
                    productStmt.setString(2, productModel.getProductName());
                    productStmt.setInt(3, productModel.getProductPrice());
                    productStmt.setInt(4, vendorModel.getVendorId());
                    productStmt.addBatch();
                }
            }

            vendorStmt.executeBatch();
            productStmt.executeBatch();

            System.out.println("Success!");
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}
