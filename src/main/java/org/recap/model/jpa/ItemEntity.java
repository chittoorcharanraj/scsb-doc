package org.recap.model.jpa;


import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import java.util.List;

/**
 * Created by pvsubrah on 6/11/16.
 */
@Data
@Entity
@Table(name = "item_t", catalog = "")
@AttributeOverride(name = "id", column = @Column(name = "ITEM_ID"))
@DynamicUpdate
public class ItemEntity extends ItemAbstractEntity {

    @Column(name = "IS_CGD_PROTECTION")
    private boolean isCgdProtection;

    @Temporal(TemporalType.DATE)
    @Column(name = "INITIAL_MATCHING_DATE")
    private Date initialMatchingDate;

    @ManyToMany(mappedBy = "itemEntities")
    private List<HoldingsEntity> holdingsEntities;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ITEM_AVAIL_STATUS_ID", insertable = false, updatable = false)
    private ItemStatusEntity itemStatusEntity;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "COLLECTION_GROUP_ID", insertable = false, updatable = false)
    private CollectionGroupEntity collectionGroupEntity;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "OWNING_INST_ID", insertable = false, updatable = false)
    private InstitutionEntity institutionEntity;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "IMS_LOCATION_ID", insertable = false, updatable = false)
    private ImsLocationEntity imsLocationEntity;

    @ManyToMany(mappedBy = "itemEntities")
    private List<BibliographicEntity> bibliographicEntities;

    @Column(name = "CGD_CHANGE_LOG")
    private String cgdChangeLog;

    /**
     * Instantiates a new Item entity.
     */
    public ItemEntity() {
        super();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemEntity that = (ItemEntity) o;

        return getOwningInstitutionItemId() != null ? getOwningInstitutionItemId().equals(that.getOwningInstitutionItemId()) : that.getOwningInstitutionItemId() == null;
    }

    @Override
    public int hashCode() {
        int result =  getOwningInstitutionItemId() != null ? getOwningInstitutionItemId().hashCode() : 0;
        result = 31 * result + (getOwningInstitutionId() != null ? getOwningInstitutionId().hashCode() : 0);
        return result;
    }
}


