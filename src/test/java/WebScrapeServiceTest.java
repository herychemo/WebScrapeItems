import com.optimizedproductions.webscrapeitems.WebScrapeService;
import com.optimizedproductions.webscrapeitems.models.CancelIfModel;
import com.optimizedproductions.webscrapeitems.models.FetchModel;
import com.optimizedproductions.webscrapeitems.models.FieldGetter;
import com.optimizedproductions.webscrapeitems.models.PostProcessModel;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class WebScrapeServiceTest {


	@Test
	public void fetchAllItemsFromSuccess() {
		WebScrapeService webScrapeService = new WebScrapeService();
		List<String> sources = new ArrayList<>();
		sources.add("https://www.ceciltoyota.com/new-inventory/index.htm");
		sources.add("https://www.ceciltoyota.com/used-inventory/index.htm");

		Set<CarModel> cars = webScrapeService.fetchAllItemsFrom(FetchModel.builder()
						.sources(sources)
						.itemSelector(".inventoryList .item")
						.nextButtonSelector("a[rel='next']:not(.disabled)")
						.field(FieldGetter.builder()
								.fieldName("title")
								.selector("a.url")
								.getterMethod(FieldGetter.GETTER_METHOD_TEXT)
								.build())
						.field(FieldGetter.builder()
								.fieldName("year")
								.selector("div.hproduct")
								.getterMethod(FieldGetter.GETTER_METHOD_ATTR)
								.getterAttr("data-year")
								.build())
						.field(FieldGetter.builder()
								.fieldName("photo")
								.selector("div.media img")
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
