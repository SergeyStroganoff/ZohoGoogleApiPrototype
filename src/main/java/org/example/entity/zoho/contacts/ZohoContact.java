package org.example.entity.zoho.contacts;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    @JsonAlias("contact_id")
    private long contactId;
    @JsonAlias("contact_name")
    private String contactName;
    @JsonAlias("company_name")
    private String companyName;
    @JsonAlias("has_transaction")
    private Boolean hasTransaction;
    @JsonAlias("contact_type")
    private String contactType;
    @JsonAlias("is_taxable")
    private Boolean isTaxable;
    @JsonAlias("tax_id")
    private Long taxId;
    @JsonAlias("tds_tax_id")
    private String tdsTaxId;
    @JsonAlias("tax_name")
    private String taxName;
    @JsonAlias("tax_percentage")
    private Double taxPercentage;
    @JsonAlias("tax_authority_id")
    private Long taxAuthorityId;
    @JsonAlias("tax_exemption_id")
    private Long taxExemptionId;
    @JsonAlias("tax_authority_name")
    private String taxAuthorityName;
    @JsonAlias("tax_exemption_code")
    private String taxExemptionCode;
    @JsonAlias("place_of_contact")
    private String placeOfContact;
    @JsonAlias("gst_no")
    private String gstNo;
    @JsonAlias("vat_treatment")
    private String vatTreatment;
    @JsonAlias("tax_treatment")
    private String taxTreatment;
    @JsonAlias("tax_regime")
    private String taxRegime;
    @JsonAlias("legal_name")
    private String legalName;
    @JsonAlias("is_tds_registered")
    private Boolean isTdsRegistered;
    @JsonAlias("gst_treatment")
    private String gstTreatment;
    @JsonAlias("is_linked_with_zohocrm")
    private Boolean isLinkedWithZohocrm;
    private String website;
    @JsonAlias("primary_contact_id")
    private Long primaryContactId;
    @JsonAlias("payment_terms")
    private Integer paymentTerms;
    @JsonAlias("payment_terms_label")
    private String paymentTermsLabel;
    @JsonAlias("currency_id")
    private Long currencyId;
    @JsonAlias("currency_code")
    private String currencyCode;
    @JsonAlias("currency_symbol")
    private String currencySymbol;
    @JsonAlias("language_code")
    private String languageCode;
    @JsonAlias("outstanding_receivable_amount")
    private Double outstandingReceivableAmount;
    @JsonAlias("outstanding_receivable_amount_bcy")
    private Double outstandingReceivableAmountBcy;
    @JsonAlias("unused_credits_receivable_amount")
    private Double unusedCreditsReceivableAmount;
    @JsonAlias("unused_credits_receivable_amount_bcy")
    private Double unusedCreditsReceivableAmountBcy;
    private String status;
    @JsonAlias("payment_reminder_enabled")
    private Boolean paymentReminderEnabled;
    @JsonAlias("custom_fields")
    private List<CustomField> customFields;
    @JsonAlias("billing_address")
    private Address billingAddress;
    @JsonAlias("shipping_address")
    private Address shippingAddress;
    private String facebook;
    private String twitter;
    @JsonAlias("contact_persons")
    private List<ContactPerson> contactPersons;
    @JsonAlias("default_templates")
    private DefaultTemplates defaultTemplates;
    private String notes;
    @JsonAlias("created_time")
    private String createdTime;
    @JsonAlias("last_modified_time")
    private String lastModifiedTime;
}
