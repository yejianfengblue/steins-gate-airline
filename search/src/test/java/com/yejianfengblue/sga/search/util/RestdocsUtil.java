package com.yejianfengblue.sga.search.util;

import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.hypermedia.HypermediaDocumentation;
import org.springframework.restdocs.hypermedia.LinkDescriptor;
import org.springframework.restdocs.hypermedia.LinksSnippet;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.util.StringUtils;

import java.util.Arrays;

import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.snippet.Attributes.key;

public interface RestdocsUtil {

    final LinkDescriptor[] PAGING_LINKS = new LinkDescriptor[]{
            linkWithRel("first").optional().description("The first page of results"),
            linkWithRel("last").optional().description("The last page of results"),
            linkWithRel("next").optional().description("The next page of results"),
            linkWithRel("prev").optional().description("The previous page of results")
    };

    final FieldDescriptor[] AUDIT_FIELDS = new FieldDescriptor[]{
            fieldWithPath("createdBy").description("The user creating this resource"),
            fieldWithPath("createdDate").description("The creation date time in UTC time"),
            fieldWithPath("lastModifiedBy").description("The user last modifying this resource"),
            fieldWithPath("lastModifiedDate").description("The last modified date time in UTC time")
    };

    public static LinksSnippet links(LinkDescriptor... descriptors) {
        return HypermediaDocumentation.links(
                linkWithRel("self").ignored().optional(),
                linkWithRel("curies").ignored().optional()
        ).and(descriptors);
    }

    public static ResponseFieldsSnippet responseFields(FieldDescriptor... descriptors) {
        return PayloadDocumentation.responseFields(
                subsectionWithPath("_links").ignored().optional()
        ).and(Arrays.asList(descriptors));
    }

    public static class ConstrainedFields {

        private final ConstraintDescriptions constraintDescriptions;

        public ConstrainedFields(Class<?> input) {
            this.constraintDescriptions = new ConstraintDescriptions(input);
        }

        public FieldDescriptor withPath(String path) {
            return fieldWithPath(path).attributes(key("constraints").value(StringUtils
                    .collectionToDelimitedString(this.constraintDescriptions
                            .descriptionsForProperty(path), ". ")));
        }
    }
}
