package org.example.entity.zoho.comon;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the default templates used in Zoho for various documents.
 * This class is used to map the JSON response from Zoho's API for default templates.
 */
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultTemplates {

    @JsonProperty("invoice_template_id")
    private String invoiceTemplateId;

    @JsonProperty("invoice_template_name")
    private String invoiceTemplateName;

    @JsonProperty("estimate_template_id")
    private String estimateTemplateId;

    @JsonProperty("estimate_template_name")
    private String estimateTemplateName;

    @JsonProperty("creditnote_template_id")
    private String creditNoteTemplateId;

    @JsonProperty("creditnote_template_name")
    private String creditNoteTemplateName;

    @JsonProperty("invoice_email_template_id")
    private String invoiceEmailTemplateId;

    @JsonProperty("invoice_email_template_name")
    private String invoiceEmailTemplateName;

    @JsonProperty("estimate_email_template_id")
    private String estimateEmailTemplateId;

    @JsonProperty("estimate_email_template_name")
    private String estimateEmailTemplateName;

    @JsonProperty("creditnote_email_template_id")
    private String creditNoteEmailTemplateId;

    @JsonProperty("creditnote_email_template_name")
    private String creditNoteEmailTemplateName;
}
