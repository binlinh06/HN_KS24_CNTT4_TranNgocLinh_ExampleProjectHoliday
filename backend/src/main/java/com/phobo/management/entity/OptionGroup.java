package com.phobo.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "option_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionGroup {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;

    @Column(name = "max_selectable", nullable = false)
    private Integer maxSelectable = 1;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<ProductOption> options;
}
