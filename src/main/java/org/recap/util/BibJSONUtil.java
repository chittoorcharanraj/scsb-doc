package org.recap.util;

import lombok.Getter;
import lombok.Setter;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.HoldingsEntity;
import org.recap.model.jpa.ItemEntity;
import org.recap.model.jpa.InstitutionEntity;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;
import org.recap.model.solr.Bib;
import org.recap.model.solr.Holdings;
import org.recap.model.solr.Item;
import org.recap.repository.jpa.BibliographicDetailsRepository;
import org.recap.repository.jpa.HoldingsDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.util.CollectionUtils;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by pvsubrah on 6/15/16.
 */
public class BibJSONUtil extends MarcUtil {

    private static final Logger logger = LoggerFactory.getLogger(BibJSONUtil.class);

    @Getter
    @Setter
    private List<String> nonHoldingInstitutions;

    @Getter
    @Setter
    private ProducerTemplate producerTemplate;

    /**
     * Gets publisher value from the marcRecord.
     *
     * @param record the record
     * @return the publisher value
     */
    public String getPublisherValue(Record record) {
        String publisherValue;
        List<String> publisherDataFields = Arrays.asList("260", "261", "262", "264");
        for (String publisherDataField : publisherDataFields) {
            publisherValue = getDataFieldValue(record, publisherDataField, null, null, "b");
            if (StringUtils.isNotBlank(publisherValue)) {
                return publisherValue;
            }
        }
        return null;
    }

    /**
     * This method is used to get the publication place value from record
     * @param record
     * @return
     */
    private String getPublicationPlaceValue(Record record) {
        String publicationPlaceValue;
        List<String> publicationPlaceDataFields = Arrays.asList("260", "261", "262", "264");
        for (String publicationPlaceDataField : publicationPlaceDataFields) {
            publicationPlaceValue = getDataFieldValue(record, publicationPlaceDataField, null, null, "a");
            if (StringUtils.isNotBlank(publicationPlaceValue)) {
                return publicationPlaceValue;
            }
        }
        return null;
    }

    /**
     * Gets publication date value from the record.
     *
     * @param record the record
     * @return the publication date value
     */
    public String getPublicationDateValue(Record record) {
        String publicationDateValue;
        List<String> publicationDateDataFields = Arrays.asList("260", "261", "262", "264");
        for (String publicationDateDataField : publicationDateDataFields) {
            publicationDateValue = getDataFieldValue(record, publicationDateDataField, null, null, "c");
            if (StringUtils.isNotBlank(publicationDateValue)) {
                return publicationDateValue;
            }
        }
        return null;
    }

    /**
     * Gets lccn value from the record.
     *
     * @param record the record
     * @return the lccn value
     */
    public String getLCCNValue(Record record) {
        String lccnValue = getDataFieldValue(record, "010", null, null, "a");
        if (lccnValue != null) {
            lccnValue = lccnValue.replaceAll(ScsbConstants.LCCN_PATTERN, " ");
            lccnValue = lccnValue.trim();
        }
        return lccnValue;
    }

    /**
     * This method is used to get OCLC numbers from the records.
     * @param record
     * @param institutionCode
     * @return
     */
    private List<String> getOCLCNumbers(Record record, String institutionCode) {
        List<String> oclcNumbers = new ArrayList<>();
        List<String> oclcNumberList = getMultiDataFieldValues(record, "035", null, null, "a");
        for (String oclcNumber : oclcNumberList) {
            if (StringUtils.isNotBlank(oclcNumber) && oclcNumber.contains("OCoLC")) {
                String modifiedOclc = oclcNumber.replaceAll(ScsbConstants.NUMBER_PATTERN, "");
                modifiedOclc = StringUtils.stripStart(modifiedOclc, "0");
                oclcNumbers.add(modifiedOclc);
            }
        }
        if (CollectionUtils.isEmpty(oclcNumbers) && StringUtils.isNotBlank(institutionCode) && nonHoldingInstitutions.contains(institutionCode)) {
            String oclcTag = getControlFieldValue(record, "003");
            if (StringUtils.isNotBlank(oclcTag) && "OCoLC".equalsIgnoreCase(oclcTag)) {
                oclcTag = getControlFieldValue(record, "001");
            }
            oclcTag = StringUtils.stripStart(oclcTag, "0");
            if (StringUtils.isNotBlank(oclcTag)) {
                oclcNumbers.add(oclcTag);
            }
        }
        return oclcNumbers;
    }

    /**
     * Gets isbn number list from the record.
     *
     * @param record the record
     * @return the list
     */
    public List<String> getISBNNumber(Record record){
        List<String> isbnNumbers = new ArrayList<>();
        List<String> isbnNumberList = getMultiDataFieldValues(record,"020", null, null, "a");
        for(String isbnNumber : isbnNumberList){
            isbnNumbers.add(isbnNumber.replaceAll(ScsbConstants.NUMBER_PATTERN, ""));
        }
        return isbnNumbers;
    }

    /**
     * Get issn number list from the record.
     *
     * @param record the record
     * @return the list
     */
    public List<String> getISSNNumber(Record record){
        List<String> issnNumbers = new ArrayList<>();
        List<String> issnNumberList = getMultiDataFieldValues(record,"022", null, null, "a");
        for(String issnNumber : issnNumberList){
            issnNumbers.add(issnNumber.replaceAll(ScsbConstants.NUMBER_PATTERN, ""));
        }
        return issnNumbers;
    }

    /**
     * This method is used to generate bib and items for indexing.
     *
     * @param bibliographicEntity            the bibliographic entity
     * @param solrTemplate                   the solr template
     * @param bibliographicDetailsRepository the bibliographic details repository
     * @param holdingsDetailsRepository      the holdings details repository
     * @return the solr input document
     */
    public SolrInputDocument generateBibAndItemsForIndex(BibliographicEntity bibliographicEntity, SolrTemplate solrTemplate,
                                                         BibliographicDetailsRepository bibliographicDetailsRepository, HoldingsDetailsRepository holdingsDetailsRepository) {

        Bib bib = generateBib(bibliographicEntity);
        if(bib != null) {
        SolrInputDocument bibSolrInputDocument = generateBibSolrInputDocument(bib, solrTemplate);
        List<HoldingsEntity> holdingsEntities = bibliographicEntity.getHoldingsEntities();
        List<SolrInputDocument> holdingsSolrInputDocuments = new ArrayList<>();
        HoldingsJSONUtil holdingsJSONUtil = new HoldingsJSONUtil();
        holdingsJSONUtil.setProducerTemplate(producerTemplate);
        for (HoldingsEntity holdingsEntity : holdingsEntities) {
            Holdings holdings = holdingsJSONUtil.generateHoldingsForIndex(holdingsEntity);
            if (holdings != null) {
                List<ItemEntity> itemEntities = holdingsEntity.getItemEntities();
                List<SolrInputDocument> itemSolrInputDocuments = new ArrayList<>();
                ItemJSONUtil itemJSONUtil = new ItemJSONUtil();
                itemJSONUtil.setProducerTemplate(producerTemplate);
                for (ItemEntity itemEntity : itemEntities) {
                    Item item = itemJSONUtil.generateItemForIndex(itemEntity);
                    if (item != null) {
                        item.setTitleSort(bib.getTitleSort());
                        SolrInputDocument itemSolrInputDocument = generateItemSolrInputDocument(item, solrTemplate);
                        itemSolrInputDocuments.add(itemSolrInputDocument);
                    }
                }
                SolrInputDocument holdingsSolrInputDocument = generateHoldingsSolrInputDocument(holdings, solrTemplate);
                if (!CollectionUtils.isEmpty(itemSolrInputDocuments))
                    holdingsSolrInputDocument.addChildDocuments(itemSolrInputDocuments);
                holdingsSolrInputDocuments.add(holdingsSolrInputDocument);
            }
        }
            if(!CollectionUtils.isEmpty(holdingsSolrInputDocuments))
                bibSolrInputDocument.addChildDocuments(holdingsSolrInputDocuments);
            return bibSolrInputDocument;
        }
        return null;
    }

    private SolrInputDocument generateItemSolrInputDocument(Item item, SolrTemplate solrTemplate) {
        return solrTemplate.convertBeanToSolrInputDocument(item);
    }

    private SolrInputDocument generateHoldingsSolrInputDocument(Holdings holdings, SolrTemplate solrTemplate) {
        return solrTemplate.convertBeanToSolrInputDocument(holdings);
    }

    private SolrInputDocument generateBibSolrInputDocument(Bib bib, SolrTemplate solrTemplate) {
        return solrTemplate.convertBeanToSolrInputDocument(bib);
    }

    /**
     * This method is used to generate bib to index in solr.
     *
     * @param bibliographicEntity            the bibliographic entity
     * @param bibliographicDetailsRepository the bibliographic details repository
     * @param holdingsDetailsRepository      the holdings details repository
     * @return the bib
     */
    public Bib generateBibForIndex(BibliographicEntity bibliographicEntity, BibliographicDetailsRepository bibliographicDetailsRepository,
                                   HoldingsDetailsRepository holdingsDetailsRepository) {
        Bib bib = generateBib(bibliographicEntity);

        if(bib != null) {
            List<Integer> holdingsIds = new ArrayList<>();
            List<Integer> itemIds = new ArrayList<>();

        List<ItemEntity> itemEntities = bibliographicEntity.getItemEntities();
        for (ItemEntity itemEntity : itemEntities) {
            itemIds.add(itemEntity.getId());
        }
        List<HoldingsEntity> holdingsEntities = bibliographicEntity.getHoldingsEntities();
        for (HoldingsEntity holdingsEntity : holdingsEntities) {
            holdingsIds.add(holdingsEntity.getId());
        }

            bib.setHoldingsIdList(holdingsIds);
            bib.setBibItemIdList(itemIds);
        }
        return bib;
    }

    /**
     * This method is used to create bib document in solr.
     * @param bibliographicEntity
     * @return
     */
    private Bib generateBib(BibliographicEntity bibliographicEntity) {
        try {
            Bib bib = new Bib();
            Integer bibliographicId = bibliographicEntity.getId();
            bib.setBibId(bibliographicId);

            bib.setDocType(ScsbCommonConstants.BIB);
            bib.setContentType("parent");
            bib.setId(bibliographicEntity.getOwningInstitutionId()+bibliographicEntity.getOwningInstitutionBibId());
            String bibContent = new String(bibliographicEntity.getContent());
            List<Record> records = convertMarcXmlToRecord(bibContent);
            Record marcRecord = records.get(0);

            InstitutionEntity institutionEntity = bibliographicEntity.getInstitutionEntity();
            String institutionCode = null != institutionEntity ? institutionEntity.getInstitutionCode() : "";

            bib.setOwningInstitution(institutionCode);
            bib.setTitle(getTitle(marcRecord));
            bib.setTitleDisplay(getTitleDisplay(marcRecord));
            bib.setTitleStartsWith(getTitleStartsWith(marcRecord));
            bib.setTitleSort(getTitleSort(marcRecord, bib.getTitleDisplay()));
            bib.setTitleSubFieldA(getDataFieldValue(marcRecord, "245", null, null, "a"));
            bib.setTitleMatch(getTitleToMatch(bib.getTitleSubFieldA()));
            bib.setTitle245(getTitle245(marcRecord));
            bib.setTitle246(getTitle246(marcRecord));
            bib.setTitle130(getTitle130(marcRecord));
            bib.setTitle730(getTitle730(marcRecord));
            bib.setTitle740(getTitle740(marcRecord));
            bib.setTitle830(getTitle830(marcRecord));
            bib.setAuthorDisplay(getAuthorDisplayValue(marcRecord));
            bib.setAuthorSearch(getAuthorSearchValue(marcRecord));
            bib.setPublisher(getPublisherValue(marcRecord));
            bib.setPublicationPlace(getPublicationPlaceValue(marcRecord));
            bib.setPublicationDate(getPublicationDateValue(marcRecord));
            bib.setSubject(getDataFieldValueStartsWith(marcRecord, "6"));
            bib.setIsbn(getISBNNumber(marcRecord));
            bib.setIssn(getISSNNumber(marcRecord));
            bib.setOclcNumber(getOCLCNumbers(marcRecord, institutionCode));
            bib.setMaterialType(getDataFieldValue(marcRecord, "245", null, null, "h"));
            bib.setNotes(getDataFieldValueStartsWith(marcRecord, "5"));
            bib.setLccn(getLCCNValue(marcRecord));
            bib.setOwningInstitutionBibId(bibliographicEntity.getOwningInstitutionBibId());
            bib.setLeaderMaterialType(getLeaderMaterialType(marcRecord.getLeader()));
            bib.setBibCreatedBy(bibliographicEntity.getCreatedBy());
            bib.setBibCreatedDate(bibliographicEntity.getCreatedDate());
            bib.setBibLastUpdatedBy(bibliographicEntity.getLastUpdatedBy());
            bib.setBibLastUpdatedDate(bibliographicEntity.getLastUpdatedDate());
            bib.setBibHoldingLastUpdatedDate(getBitHoldingLastUpdatedDate(bibliographicEntity));
            bib.setBibItemLastUpdatedDate(getBitItemLastUpdatedDate(bibliographicEntity));

            bib.setDeletedBib(bibliographicEntity.isDeleted());
            bib.setBibCatalogingStatus(bibliographicEntity.getCatalogingStatus());
            bib.setMatchingIdentifier(bibliographicEntity.getMatchingIdentity());
            bib.setMatchScore(bibliographicEntity.getMatchScore());
            bib.setAnamolyFlag(bibliographicEntity.isAnamolyFlag());
            bib.setMaQualifier(bibliographicEntity.getMaQualifier());
            return bib;
        } catch (Exception e) {
            saveExceptionReportForBib(bibliographicEntity, e);
        }
        return null;
    }

    /**
     * This method gets bib item's last updated date.
     *
     * @param bibliographicEntity the bibliographic entity
     * @return the date
     */
    public Date getBitItemLastUpdatedDate(BibliographicEntity bibliographicEntity){
        List<ItemEntity> itemEntityList = bibliographicEntity.getItemEntities();
        List<Date> dateList = new ArrayList<>();
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(itemEntityList)) {
            for(ItemEntity itemEntity: itemEntityList){
                dateList.add(itemEntity.getLastUpdatedDate());
            }
            return Collections.max(dateList);
        } else {
            return null;
        }
    }

    /**
     * This method is used to gets bib holding's last updated date.
     *
     * @param bibliographicEntity the bibliographic entity
     * @return the date
     */
    public Date getBitHoldingLastUpdatedDate(BibliographicEntity bibliographicEntity){
        List<HoldingsEntity> holdingsEntityList = bibliographicEntity.getHoldingsEntities();
        List<Date> dateList = new ArrayList<>();
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(holdingsEntityList)) {
            for(HoldingsEntity holdingsEntity: holdingsEntityList){
                dateList.add(holdingsEntity.getLastUpdatedDate());
            }
            return Collections.max(dateList);
        } else {
            return null;
        }
    }

    /**
     * This method is used to save ExceptionReport for bib in database if exception occurs in bib documentation for solr.
     * @param bibliographicEntity
     * @param e
     */
    private void saveExceptionReportForBib(BibliographicEntity bibliographicEntity, Exception e) {
        logger.error("Exception in Bib Id : {} " , bibliographicEntity != null ? bibliographicEntity.getId() : "BibliographicEntity is Null");
        List<ReportDataEntity> reportDataEntities = new ArrayList<>();

        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setCreatedDate(new Date());
        reportEntity.setType(ScsbCommonConstants.SOLR_INDEX_EXCEPTION);
        reportEntity.setFileName(ScsbCommonConstants.SOLR_INDEX_FAILURE_REPORT);
        InstitutionEntity institutionEntity = null != bibliographicEntity ? bibliographicEntity.getInstitutionEntity() : null;
        String institutionCode = null != institutionEntity ? institutionEntity.getInstitutionCode() : ScsbCommonConstants.NA;
        reportEntity.setInstitutionName(institutionCode);

        ReportDataEntity docTypeDataEntity = new ReportDataEntity();
        docTypeDataEntity.setHeaderName(ScsbCommonConstants.DOCTYPE);
        docTypeDataEntity.setHeaderValue(ScsbCommonConstants.BIB);
        reportDataEntities.add(docTypeDataEntity);

        ReportDataEntity owningInstDataEntity = new ReportDataEntity();
        owningInstDataEntity.setHeaderName(ScsbCommonConstants.OWNING_INSTITUTION);
        owningInstDataEntity.setHeaderValue(institutionCode);
        reportDataEntities.add(owningInstDataEntity);

        ReportDataEntity exceptionMsgDataEntity = new ReportDataEntity();
        exceptionMsgDataEntity.setHeaderName(ScsbCommonConstants.EXCEPTION_MSG);
        exceptionMsgDataEntity.setHeaderValue(StringUtils.isNotBlank(e.getMessage()) ? e.getMessage() : e.toString());
        reportDataEntities.add(exceptionMsgDataEntity);

        if(bibliographicEntity != null && bibliographicEntity.getId() != null) {
            ReportDataEntity bibIdDataEntity = new ReportDataEntity();
            bibIdDataEntity.setHeaderName(ScsbCommonConstants.BIB_ID);
            bibIdDataEntity.setHeaderValue(String.valueOf(bibliographicEntity.getId()));
            reportDataEntities.add(bibIdDataEntity);
        }

        if(bibliographicEntity != null && StringUtils.isNotBlank(bibliographicEntity.getOwningInstitutionBibId())) {
            ReportDataEntity owningInstBibIdDataEntity = new ReportDataEntity();
            owningInstBibIdDataEntity.setHeaderName(ScsbCommonConstants.OWNING_INST_BIB_ID);
            owningInstBibIdDataEntity.setHeaderValue(bibliographicEntity.getOwningInstitutionBibId());
            reportDataEntities.add(owningInstBibIdDataEntity);
        }

        reportEntity.addAll(reportDataEntities);
        producerTemplate.sendBody(ScsbCommonConstants.REPORT_Q, reportEntity);
    }

    /**
     * Gets leader material type.
     *
     * @param leader the leader
     * @return the leader material type
     */
    public String getLeaderMaterialType(Leader leader) {
        String leaderMaterialType = null;
        String leaderFieldValue = leader != null ? leader.toString() : null;
        if (StringUtils.isNotBlank(leaderFieldValue) && leaderFieldValue.length() > 7) {
            char materialTypeChar = leaderFieldValue.charAt(7);
            if ('m' == materialTypeChar) {
                leaderMaterialType = ScsbCommonConstants.MONOGRAPH;
            } else if ('s' == materialTypeChar) {
                leaderMaterialType = ScsbCommonConstants.SERIAL;
            } else {
                leaderMaterialType = ScsbCommonConstants.OTHER;
            }
        }
        return leaderMaterialType;
    }

    /**
     * This method gets the title from marc record.
     *
     * @param marcRecord the marc record
     * @return the title
     */
    public String getTitle(Record marcRecord) {
        StringBuilder title=new StringBuilder();
        title.append(getDataFieldValueStartsWith(marcRecord, "245", Arrays.asList('a', 'b','n','p')) + " ");
        title.append(getDataFieldValueStartsWith(marcRecord, "246", Arrays.asList('a', 'b')) + " ");
        title.append(getDataFieldValueStartsWith(marcRecord, "130", Collections.singletonList('a')) + " ");
        title.append(getDataFieldValueStartsWith(marcRecord, "730", Collections.singletonList('a')) + " ");
        title.append(getDataFieldValueStartsWith(marcRecord, "740", Collections.singletonList('a')) + " ");
        title.append(getDataFieldValueStartsWith(marcRecord, "830", Collections.singletonList('a'))+ " ");
        return title.toString();
    }

    /**
     * This method is used to get the title starting with from the marc record.
     * @param marcRecord
     * @return
     */
    private String getTitleStartsWith(Record marcRecord){
        String title = getTitleDisplay(marcRecord);
        String titleStartsWith = null;
        if(title!=null){
            String[] splitedTitle = title.split(" ");
            titleStartsWith = splitedTitle[0];
        }
        return titleStartsWith;
    }

    /**
     * This method gets title display from the marc record.
     *
     * @param marcRecord the marc record
     * @return the title display
     */
    public String getTitleDisplay(Record marcRecord) {
        StringBuilder titleDisplay = new StringBuilder();
        titleDisplay.append(getDataFieldValueStartsWith(marcRecord, "245", Arrays.asList('a', 'b', 'c', 'f', 'g', 'h', 'k', 'n', 'p', 's')));
        return titleDisplay.toString();
    }

    /**
     * This method gets title 245 $a $b from the marc record.
     *
     * @param marcRecord the marc record
     * @return the title 245 $a $b
     */
    public String getTitle245(Record marcRecord) {
        return getDataFieldValueStartsWith(marcRecord, "245", Arrays.asList('a', 'b'));
    }

    /**
     * This method gets title 246 $a $b from the marc record.
     *
     * @param marcRecord the marc record
     * @return the title 246 $a $b
     */
    public String getTitle246(Record marcRecord) {
        return getDataFieldValueStartsWith(marcRecord, "246", Arrays.asList('a', 'b'));
    }

    /**
     * This method gets title 130 $a from the marc record.
     *
     * @param marcRecord the marc record
     * @return the title 130 $a
     */
    public String getTitle130(Record marcRecord) {
        return getDataFieldValueStartsWith(marcRecord, "130", Arrays.asList('a'));
    }

    /**
     * This method gets title 730 $a from the marc record.
     *
     * @param marcRecord the marc record
     * @return the title 730 $a
     */
    public String getTitle730(Record marcRecord) {
        return getDataFieldValueStartsWith(marcRecord, "730", Arrays.asList('a'));
    }

    /**
     * This method gets title 740 $a from the marc record.
     *
     * @param marcRecord the marc record
     * @return the title 740 $a
     */
    public String getTitle740(Record marcRecord) {
        return getDataFieldValueStartsWith(marcRecord, "740", Arrays.asList('a'));
    }

    /**
     * This method gets title 830 $a from the marc record.
     *
     * @param marcRecord the marc record
     * @return the title 830 $a
     */
    public String getTitle830(Record marcRecord) {
        return getDataFieldValueStartsWith(marcRecord, "830", Arrays.asList('a'));
    }

    /**
     * Gets author display value from the marc record.
     *
     * @param marcRecord the marc record
     * @return the author display value
     */
    public String getAuthorDisplayValue(Record marcRecord) {
        StringBuilder author = new StringBuilder();
        author.append(getDataFieldValueStartsWith(marcRecord, "100", Arrays.asList('a','b','c','d','e','f','g','j','k','l','n','p','q','t','u')) + " ");
        author.append(getDataFieldValueStartsWith(marcRecord, "110", Arrays.asList('a','b','c','d','e','f','g','k','l','n','p','t','u')) + " ");
        author.append(getDataFieldValueStartsWith(marcRecord, "111", Arrays.asList('a','c','d','e','f','g','j','k','l','n','p','q','t','u')) + " ");
        author.append(getDataFieldValueStartsWith(marcRecord, "130", Arrays.asList('a','d','f','g','h','k','l','m','n','o','p','r','s','t')));

        return author.toString();
    }

    /**
     * Gets author search value from the marc record.
     *
     * @param marcRecord the marc record
     * @return the author search value
     */
    public List<String> getAuthorSearchValue(Record marcRecord) {
        List<String> authorSearchValues = new ArrayList<>();
        List<String> fieldValues;

        Map<String, List<Character>> authorMap = new HashMap<>();
        authorMap.put("100", Arrays.asList('a','q'));
        authorMap.put("110", Arrays.asList('a','b'));
        authorMap.put("111", Collections.singletonList('a'));
        authorMap.put("700", Collections.singletonList('a'));
        authorMap.put("710", Arrays.asList('a','b'));
        authorMap.put("711", Collections.singletonList('a'));


        for (Map.Entry<String, List<Character>> entry : authorMap.entrySet()) {
            fieldValues = getListOfDataFieldValuesStartsWith(marcRecord, entry.getKey(), entry.getValue());
            if (!CollectionUtils.isEmpty(fieldValues)) {
                authorSearchValues.addAll(fieldValues);
            }
        }
        return authorSearchValues;
    }

    /**
     * Gets leader for the given marc record.
     *
     * @param marcRecord the marc record
     * @return the leader
     */
    public String getLeader(Record marcRecord) {
        return marcRecord.getLeader() != null ? marcRecord.getLeader().toString() : null;
    }

    /**
     * The method gets title sort from the marc record.
     *
     * @param marcRecord   the marc record
     * @param titleDisplay the title display
     * @return the title sort
     */
    public String getTitleSort(Record marcRecord, String titleDisplay) {
        Integer secondIndicatorForDataField = getSecondIndicatorForDataField(marcRecord, "245");
        if (StringUtils.isNotBlank(titleDisplay) && titleDisplay.length() >= secondIndicatorForDataField) {
            return titleDisplay.substring(secondIndicatorForDataField);
        }
        return "";
    }

    /**
     * This method replaces diacritics(~= accents) characters by replacing them to normal characters in title.
     *
     * @param title the title
     * @return the string
     */
    public String normalizeDiacriticsInTitle(String title) {
        String normalizedTitle = Normalizer.normalize(title, Normalizer.Form.NFD);
        normalizedTitle = normalizedTitle.replaceAll("[^\\p{ASCII}]", "");
        normalizedTitle = normalizedTitle.replaceAll("\\p{M}", "");
        return normalizedTitle.toLowerCase();
    }

    /**
     * This method gets matched title for  the given title.
     *
     * @param title the title
     * @return the title to match
     */
    public String getTitleToMatch(String title) {
        title = normalizeDiacriticsInTitle(title.trim());
        title = title.replaceAll("[^\\w\\s]", "").trim();
        title = title.replaceAll("\\s{2,}", " ");
        String titleToMatch = "";
        if(StringUtils.isNotBlank(title)) {
            String[] titleArray = title.split(" ");
            int count = 0;
            for (int j = 0; j < titleArray.length; j++) {
                String tempTitle = titleArray[j];
                if (!("a".equalsIgnoreCase(tempTitle) || "an".equalsIgnoreCase(tempTitle) || "the".equalsIgnoreCase(tempTitle))) {
                    titleToMatch = getTitleToMatch(titleToMatch, count, tempTitle);
                    count = count + 1;
                } else {
                    if(j != 0) {
                        titleToMatch = getTitleToMatch(titleToMatch, count, tempTitle);
                        count = count + 1;
                    }
                }
                if (count == 4) {
                    break;
                }
            }
        }
        return titleToMatch.replaceAll("\\s", "").toLowerCase();
    }

    private String getTitleToMatch(String titleToMatch, int count, String tempTitle) {
        if (count == 0) {
            titleToMatch = tempTitle;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(titleToMatch);
            stringBuilder.append(" ");
            stringBuilder.append(tempTitle);
            titleToMatch = stringBuilder.toString();
        }
        return titleToMatch.toLowerCase();
    }

}
