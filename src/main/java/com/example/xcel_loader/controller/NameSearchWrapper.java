package com.example.xcel_loader.controller;

import com.example.xcel_loader.model.NameSearch;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;


@Getter
@Setter
@NoArgsConstructor
public class NameSearchWrapper {

    private String name;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date updatedAt;

    private String type;

    private String subType;

    //BRN or RN
    private String no;

    private String status;

    private Object score;

    private float similarity;

    public static NameSearchWrapper nameSearchWrapper(NameSearch nameSearch){
        NameSearchWrapper nameSearchWrapper = new NameSearchWrapper();
        nameSearchWrapper.setName(nameSearch.getName());
        nameSearchWrapper.setNo(nameSearch.getNo());
        var status  = "status";

        if(status == null){
            status = "Status";
        }

        nameSearchWrapper.setStatus(status);
        nameSearchWrapper.setSubType(nameSearch.getSubType());
        nameSearchWrapper.setType(nameSearch.getType());
        nameSearchWrapper.setUpdatedAt(nameSearch.getUpdatedAt());
        nameSearchWrapper.setCreatedAt(nameSearch.getCreatedAt());
        return nameSearchWrapper;
    }

}
