package com.knockbook.backend.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BookSubcategory {

    public enum Subcategory {
        all,
        koreanFiction, westernFiction, japaneseFiction,
        koreanPoetry, worldPoetry, essay,
        humanities, philosophy, psychology,
        marriage, pregnancy, parenting,
        cooking, baking, internationalCuisine,
        health, diseasePreventionAndTreatment, diet,
        homeDecor, diyAndCrafts, sports,
        management,economics, personalFinance,
        selfImprovement, communicationSkills, businessSkills,
        politics, governance, sociology,
        worldHistory, koreanHistory, culture,
        christianity, catholicism, buddhism,
        art, music, cinema,
        architecture, engineering, medicine,
        english, japanese, chinese,
        fundamentalScience, lifeScience, earthSpaceScience,
        domesticTravel, internationalTravel, themedTravel,
        coreTech, webProgramming, infraSecurity
    }

    private Long id;
    private Subcategory subcategoryCodeName;
    private String subcategoryDisplayName;
}
