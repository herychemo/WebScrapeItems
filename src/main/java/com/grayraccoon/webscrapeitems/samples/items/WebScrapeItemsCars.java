package com.grayraccoon.webscrapeitems.samples.items;

import com.grayraccoon.webscrapeitems.WebScrapeService;
import com.grayraccoon.webscrapeitems.models.*;
import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class WebScrapeItemsCars {

	public static void main(String[] args) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		new WebScrapeItemsCars().fetchAllItemsFromCecilToyota();
		stopWatch.stop();
		System.out.print("All Items from of two pages took: ");
		System.out.println(stopWatch.toString());
	}

	public void fetchAllItemsFromCecilToyota() {
		WebScrapeService webScrapeService = new WebScrapeService();
		List<String> sources = new ArrayList<>();
		sources.add("https://www.ceciltoyota.com/new-inventory/index.htm");
		sources.add("https://www.ceciltoyota.com/used-inventory/index.htm");

		Set<CarModel> cars = webScrapeService.fetchAllItemsFrom(FetchModel.builder()
						.sources(sources)
						.itemSelector(Selector.builder().selector(".inventoryList .item").build())
						.nextButtonSelector(Selector.builder().selector("a[rel='next']:not(.disabled)").build())
						.field(FieldGetter.builder()
								.fieldName("title")
								//.selector(Selector.builder().selector("a.url").build())
								.selector(Selector.builder().selector("//a[@class=\"url\"]").type(Selector.TYPE_XPATH).build())
								.getterMethod(FieldGetter.GETTER_METHOD_TEXT)
								.postProcessModel(PostProcessModel.builder()
										.type(PostProcessModel.TYPE_TRIM)
										.build())
								.build())
						.field(FieldGetter.builder()
								.fieldName("year")
								.selector(Selector.builder().selector("div.hproduct").build())
								.getterMethod(FieldGetter.GETTER_METHOD_ATTR)
								.getterAttr("data-year")
								.build())
						.field(FieldGetter.builder()
								.fieldName("photo")
								.selector(Selector.builder().selector("div.media img").build())
								.getterMethod(FieldGetter.GETTER_METHOD_ATTR)
								.getterAttr("src")
								.postProcessModel(PostProcessModel.builder()
										.type(PostProcessModel.TYPE_TRIM)
										.build())
								.postProcessModel(PostProcessModel.builder()
										.type(PostProcessModel.TYPE_SPLIT)
										.arguments(Arrays.asList("\\?", "#"))
										.build())
								.cancelIfModel(CancelIfModel.builder()
										.type(CancelIfModel.TYPE_CONTAINS_OR)
										.arguments(Arrays.asList("blank", "unavailable"))
										.build())
								.build())
						.build()
				, CarModel.class);

		for (CarModel car: cars) {
			System.out.println(car.toString());
		}

	}

}
