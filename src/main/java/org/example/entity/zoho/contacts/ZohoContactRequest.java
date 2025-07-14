package org.example.entity.zoho.contacts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("contact_name")
    private String contactName;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("contact_type")
    private String contactType;

    @JsonProperty("payment_terms")
    private Integer paymentTerms;

    @JsonProperty("currency_id")
    private String currencyId;

    private String website;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("custom_fields")
    private List<CustomField> customFields;

    @JsonProperty("billing_address")
    private Address billingAddress;

    @JsonProperty("shipping_address")
    private Address shippingAddress;

    @JsonProperty("contact_persons")
    private List<ContactPerson> contactPersons;

    @JsonProperty("default_templates")
    private DefaultTemplates defaultTemplates;

    @JsonProperty("language_code")
    private LanguageCode languageCode;

    private String notes;

/*    @JsonProperty("vat_reg_no")
    private String vatRegNo;*/

/*    @JsonProperty("tax_reg_no")
    private String taxRegNo;*/

/*    @JsonProperty("country_code")
    private String countryCode; */

/*    @JsonProperty("vat_treatment")
    private String vatTreatment; */

/*    @JsonProperty("tax_treatment")
    private String taxTreatment; */

/*    @JsonProperty("tax_regime")
    private String taxRegime; */

/*    @JsonProperty("legal_name")
    private String legalName;*/

/*    @JsonProperty("is_tds_registered")
    private Boolean isTdsRegistered;*/

/*    @JsonProperty("place_of_contact")
    private String placeOfContact;*/

/*    @JsonProperty("gst_no")
    private String gstNo;*/

/*    @JsonProperty("gst_treatment")
    private String gstTreatment;*/

    @JsonProperty("tax_authority_name")
    private String taxAuthorityName;

    @JsonProperty("tax_exemption_code")
    private String taxExemptionCode;

/*    @JsonProperty("avatax_exempt_no")
    private String avataxExemptNo;*/

/*    @JsonProperty("avatax_use_code")
    private String avataxUseCode;*/

    @JsonProperty("tax_exemption_id")
    private String taxExemptionId;

    @JsonProperty("tax_authority_id")
    private String taxAuthorityId;

    @JsonProperty("tax_id")
    private String taxId;

/*    @JsonProperty("tds_tax_id")
    private String tdsTaxId;*/

    @JsonProperty("is_taxable")
    private Boolean isTaxable;

    private String facebook;
    private String twitter;
}
