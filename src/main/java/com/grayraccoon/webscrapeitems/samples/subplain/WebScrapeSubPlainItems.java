package com.grayraccoon.webscrapeitems.samples.subplain;

import com.grayraccoon.webscrapeitems.WebScrapeServiceImpl;
import com.grayraccoon.webscrapeitems.models.*;
import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

class WebScrapeSubPlainItems {

    public static void main(String[] args) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        new WebScrapeSubPlainItems().fetchSomeCarNames();
        stopWatch.stop();
        System.out.print("All Items from of two pages took: ");
        System.out.println(stopWatch.toString());
    }

    public void fetchSomeCarNames() {
        WebScrapeServiceImpl webScrapeServiceImpl = new WebScrapeServiceImpl();
        List<String> sources = new ArrayList<>();
        sources.add("https://www.ancira.com/searchall.aspx");

        Map<String, Set<String>> allSubPlainItems =
                webScrapeServiceImpl.fetchAllSubPlainItemsFrom(FetchModel.builder()
                        .sources(sources)
                        .plainFetchModel(BasicFetchModel.builder()
                                .plainObjectName("carTitles")
                                .itemSelector(Selector.builder().selector(".vehicleTitleContainer span").build())
                                .singlePlainObject(FieldGetter.builder()
                                        .getterMethod(FieldGetter.GETTER_METHOD_TEXT)
                                        .postProcessModel(PostProcessModel.builder()
                                                .type(PostProcessModel.TYPE_TRIM)
                                                .build())
                                        .build())
                                .build())
                        .plainFetchModel(BasicFetchModel.builder()
                                .plainObjectName("features")
                                .itemSelector(Selector.builder().selector("#collapse-Features input[name='Features']").build())
                                .singlePlainObject(FieldGetter.builder()
                                        .getterMethod(FieldGetter.GETTER_METHOD_ATTR)
                                        .getterAttr("id")
                                        .postProcessModel(PostProcessModel.builder()
                                                .type(PostProcessModel.TYPE_TRIM)
                                                .build())
                                        .postProcessModel(PostProcessModel.builder()
                                                .type(PostProcessModel.TYPE_REMOVE)
                                                .argument("refine-search-Features-")
                                                .build())
                                        .build())
                                .build())
                        .plainFetchModel(BasicFetchModel.builder()
                                .plainObjectName("priceRanges")
                                .itemSelector(Selector.builder().selector("#collapse-PriceRange input[name='Pricerange']").build())
                                .singlePlainObject(FieldGetter.builder()
                                        .getterMethod(FieldGetter.GETTER_METHOD_ATTR)
                                        .getterAttr("value")
                                        .postProcessModel(PostProcessModel.builder()
                                                .type(PostProcessModel.TYPE_TRIM)
                                                .build())
                                        .build())
                                .build())
                        .build());

        System.out.println(allSubPlainItems);
    }

}
