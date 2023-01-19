package com.example.xcel_loader.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import java.util.Date;
import java.util.UUID;

import static com.example.xcel_loader.model.Constants.DATE_FORMAT;

@Setter
@Getter
@NoArgsConstructor
@Entity(name = "names")
public class NameSearch {

    @Id
    @Type(type = "uuid-char")
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(length = 240, nullable = false)
    private String name;

    @Column(name = "created_at")
    @JsonFormat(pattern = DATE_FORMAT)
    private Date createdAt;

    @Column(name = "updated_at")
    @JsonFormat(pattern = DATE_FORMAT)
    @UpdateTimestamp
    private Date updatedAt;

    @Column(length = 42, nullable = false)
    private String type;

    @Column(length = 120, nullable = false)
    private String subType;

    @Column(length = 16, nullable = false)
    private String status;

    //BRN or RN
    @Column(length = 64, nullable = false, unique = true)
    private String no;

    @PrePersist
    public void prePersist(){
        if(no == null){
            no = "RN"+new Date().getTime();
        }
    }
}
