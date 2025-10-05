package com.demo.microservices.model;


import com.demo.microservices.view.Views;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products")
@Data
@RequiredArgsConstructor
public class Product {

    @Id
    private String id;

    @JsonView({Views.Basic.class,Views.Detailed.class})
    private String sku;

    @JsonView({Views.Basic.class,Views.Detailed.class})
    private String name;

    @JsonView(Views.Detailed.class)
    private String description;

    @JsonView({Views.Basic.class,Views.Detailed.class})
    private long price;

    @JsonView(Views.Detailed.class)
    private String currency;

    @JsonView(Views.Detailed.class)
    private int quantity;
}
