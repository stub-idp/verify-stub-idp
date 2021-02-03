package uk.gov.ida.matchingserviceadapter.domain;

import org.opensaml.saml.saml2.core.Attribute;
import stubidp.saml.domain.assertions.Cycle3Dataset;
import stubidp.saml.domain.assertions.MatchingDataset;

import java.util.Optional;

public interface AttributeExtractor {
    Optional<Attribute> transform(MatchingDataset matchingDataset, Optional<Cycle3Dataset> cycle3Dataset);
}
