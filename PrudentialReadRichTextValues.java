package com.ehi.dam.reboot.core.models;

import javax.inject.Inject;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

@Model(adaptables = {
        Resource.class},defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class PrudentialReadRichTextValues {
	@Inject
	 private String pruRichText;

	public String getPruRichText() {
        return pruRichText;
    }
}
