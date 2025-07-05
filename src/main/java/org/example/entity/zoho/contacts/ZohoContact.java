package org.example.entity.zoho.contacts;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.entity.zoho.comon.Address;
import org.example.entity.zoho.comon.ContactPerson;
import org.example.entity.zoho.comon.CustomField;
import org.example.entity.zoho.comon.DefaultTemplates;

import java.util.List;

/**
 * Represents a contact in Zoho.
 * This class is used to map the JSON response from Zoho's API for contacts.
 */

@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZohoContact {
    @JsonProperty("contact_id")
    private long contactId;
    @JsonProperty("contact_name")
    private String contactName;
    @JsonProperty("company_name")
    private String companyName;
    @JsonProperty("has_transaction")
    private Boolean hasTransaction;
    @JsonProperty("contact_type")
    private String contactType;
    @JsonProperty("is_taxable")
    private Boolean isTaxable;
    @JsonProperty("tax_id")
    private Long taxId;
    @JsonProperty("tds_tax_id")
    private String tdsTaxId;
    @JsonProperty("tax_name")
    private String taxName;
    @JsonProperty("tax_percentage")
    private Double taxPercentage;
    @JsonProperty("tax_authority_id")
    private Long taxAuthorityId;
    @JsonProperty("tax_exemption_id")
    private Long taxExemptionId;
    @JsonProperty("tax_authority_name")
    private String taxAuthorityName;
    @JsonProperty("tax_exemption_code")
    private String taxExemptionCode;
    @JsonProperty("place_of_contact")
    private String placeOfContact;
    @JsonProperty("gst_no")
    private String gstNo;
    @JsonProperty("vat_treatment")
    private String vatTreatment;
    @JsonProperty("tax_treatment")
    private String taxTreatment;
    @JsonProperty("tax_regime")
    private String taxRegime;
    @JsonProperty("legal_name")
    private String legalName;
    @JsonProperty("is_tds_registered")
    private Boolean isTdsRegistered;
    @JsonProperty("gst_treatment")
    private String gstTreatment;
    @JsonProperty("is_linked_with_zohocrm")
    private Boolean isLinkedWithZohocrm;
    private String website;
    @JsonProperty("primary_contact_id")
    private Long primaryContactId;
    @JsonProperty("payment_terms")
    private Integer paymentTerms;
    @JsonProperty("payment_terms_label")
    private String paymentTermsLabel;
    @JsonProperty("currency_id")
    private Long currencyId;
    @JsonProperty("currency_code")
    private String currencyCode;
    @JsonProperty("currency_symbol")
    private String currencySymbol;
    @JsonProperty("language_code")
    private String languageCode;
    @JsonProperty("outstanding_receivable_amount")
    private Double outstandingReceivableAmount;
    @JsonProperty("outstanding_receivable_amount_bcy")
    private Double outstandingReceivableAmountBcy;
    @JsonProperty("unused_credits_receivable_amount")
    private Double unusedCreditsReceivableAmount;
    @JsonProperty("unused_credits_receivable_amount_bcy")
    private Double unusedCreditsReceivableAmountBcy;
    private String status;
    @JsonProperty("payment_reminder_enabled")
    private Boolean paymentReminderEnabled;
    @JsonProperty("custom_fields")
    private List<CustomField> customFields;
    @JsonProperty("billing_address")
    private Address billingAddress;
    @JsonProperty("shipping_address")
    private Address shippingAddress;
    private String facebook;
    private String twitter;
    @JsonProperty("contact_persons")
    private List<ContactPerson> contactPersons;
    @JsonProperty("default_templates")
    private DefaultTemplates defaultTemplates;
    private String notes;
    @JsonProperty("created_time")
    private String createdTime;
    @JsonProperty("last_modified_time")
    private String lastModifiedTime;
}
