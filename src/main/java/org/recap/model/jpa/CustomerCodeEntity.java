package org.recap.model.jpa;

import lombok.Data;
import lombok.EqualsAndHashCode;


import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * Created by rajeshbabuk on 18/10/16.
 */
@Entity
@Table(name = "customer_code_t", catalog = "")
@AttributeOverride(name = "id", column = @Column(name = "CUSTOMER_CODE_ID"))
@Data
@EqualsAndHashCode(callSuper=false)
public class CustomerCodeEntity extends CustomerCodeAbstractEntity {
}
