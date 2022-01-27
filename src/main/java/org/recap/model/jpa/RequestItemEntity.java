package org.recap.model.jpa;

import lombok.Data;
import lombok.EqualsAndHashCode;
;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by pvsubrah on 6/11/16.
 */
@Entity
@Table(name = "request_item_t", catalog = "")
@AttributeOverride(name = "id", column = @Column(name = "REQUEST_ID"))
@Data
@EqualsAndHashCode(callSuper = false)
public class RequestItemEntity extends RequestItemAbstractEntity {

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ITEM_ID", referencedColumnName = "ITEM_ID", insertable = false, updatable = false)
    private ItemEntity itemEntity;

  }
