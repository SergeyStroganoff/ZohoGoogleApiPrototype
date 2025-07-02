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

@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZohoContactRequest {

    @JsonAlias("contact_name")
    private String contactName;

    @JsonAlias("company_name")
    private String companyName;

    @JsonAlias("payment_terms")
    private Integer paymentTerms;

    @JsonAlias("currency_id")
    private String currencyId;

    private String website;

    @JsonAlias("custom_fields")
    private List<CustomField> customFields;

    @JsonAlias("billing_address")
    private Address billingAddress;

    @JsonAlias("shipping_address")
    private Address shippingAddress;

    @JsonAlias("contact_persons")
    private List<ContactPerson> contactPersons;

    @JsonAlias("default_templates")
    private DefaultTemplates defaultTemplates;

    @JsonAlias("language_code")
    private LanguageCode languageCode;

    private String notes;

    @JsonAlias("vat_reg_no")
    private String vatRegNo;

    @JsonAlias("tax_reg_no")
    private String taxRegNo;

    @JsonAlias("country_code")
    private String countryCode;

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

    @JsonAlias("place_of_contact")
    private String placeOfContact;

    @JsonAlias("gst_no")
    private String gstNo;

    @JsonAlias("gst_treatment")
    private String gstTreatment;

    @JsonAlias("tax_authority_name")
    private String taxAuthorityName;

    @JsonAlias("tax_exemption_code")
    private String taxExemptionCode;

    @JsonAlias("avatax_exempt_no")
    private String avataxExemptNo;

    @JsonAlias("avatax_use_code")
    private String avataxUseCode;

    @JsonAlias("tax_exemption_id")
    private String taxExemptionId;

    @JsonAlias("tax_authority_id")
    private String taxAuthorityId;

    @JsonAlias("tax_id")
    private String taxId;

    @JsonAlias("tds_tax_id")
    private String tdsTaxId;

    @JsonAlias("is_taxable")
    private Boolean isTaxable;

    private String facebook;
    private String twitter;
}
