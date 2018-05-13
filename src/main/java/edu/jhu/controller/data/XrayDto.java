package edu.jhu.controller.data;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;


/**
 * Simple DTO/Entity object for carrying data between layers.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class XrayDto {

    @Id
    @SequenceGenerator(name="xray_dto_seq",
                      sequenceName="xray_dto_seq",
                      allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator="xray_dto_seq")
    private long id;

    @NotNull
    @NotEmpty
    private String firstName;

    @NotNull
    @NotEmpty
    private String lastName;
}
