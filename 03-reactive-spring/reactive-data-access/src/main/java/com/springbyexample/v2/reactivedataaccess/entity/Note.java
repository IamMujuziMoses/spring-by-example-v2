package com.springbyexample.v2.reactivedataaccess.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * @author Mujuzi Moses
 */
@Table("notes")
public record Note(@Id Long id, String title, String content) {
}
