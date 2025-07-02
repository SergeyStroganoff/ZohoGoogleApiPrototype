package org.example.entity.zoho.comon;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    @JsonAlias("invoice_template_id")
    private String invoiceTemplateId;

    @JsonAlias("invoice_template_name")
    private String invoiceTemplateName;

    @JsonAlias("estimate_template_id")
    private String estimateTemplateId;

    @JsonAlias("estimate_template_name")
    private String estimateTemplateName;

    @JsonAlias("creditnote_template_id")
    private String creditNoteTemplateId;

    @JsonAlias("creditnote_template_name")
    private String creditNoteTemplateName;

    @JsonAlias("invoice_email_template_id")
    private String invoiceEmailTemplateId;

    @JsonAlias("invoice_email_template_name")
    private String invoiceEmailTemplateName;

    @JsonAlias("estimate_email_template_id")
    private String estimateEmailTemplateId;

    @JsonAlias("estimate_email_template_name")
    private String estimateEmailTemplateName;

    @JsonAlias("creditnote_email_template_id")
    private String creditNoteEmailTemplateId;

    @JsonAlias("creditnote_email_template_name")
    private String creditNoteEmailTemplateName;
}
